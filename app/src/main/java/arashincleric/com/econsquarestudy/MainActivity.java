package arashincleric.com.econsquarestudy;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends FragmentActivity
        implements EnterEmailFragment.OnEnterEmailFragmentInteractionListener,
        ParametersFragment.OnParametersFragmentInteractionListener,
        ActiveScreenFragment.OnActiveScreenFragmentInteractionListener,
        TaskScreenFragment.OnTaskScreenFragmentInteractionListener{

    //IN MILLISECONDS
    public final static long MANDATORY_WORK_TIME = 10000;
    public final static long MAXIMUM_WORK_TIME = 60000;
    public final static int MAXIMUM_ACTIVE_ON_TIME = 8000;
    public final static int MAXIMUM_REST_TIME = 3000;
    public final static double GOLD_SCORE_WEIGHT = 1.25;
    public final static double GOLD_PROBABILITY = 0.9;
    public final static boolean HIDE_REST_TIMER = false;



    public final static String USER_DATA_COLUMNS =
            "UserID\tTimeStart\tTotalTime\tTimeStartSurvey\tNumberRedOffered\tNumberGoldOffered" +
                    "\tNumberRedClicked\tNumberGoldClicked\n";
    public final static String EVENT_DATA_COLUMNS =
            "Date\tTime\tUserID\tScreenType\tSequence\tGold" +
                    "\tAction\n";
    private final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    public double points;


    private LinearLayout taskInfo;
    private TextView remainingTimerView;
    private TextView pointsView;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment mContent;
    private String currentUser = null;
    private Chronometer totalTimer;
    private boolean passedMandatoryTime; //flag to fire the mandatory time code once hit
    private Button questionnaireBtn;
    private long timerPausedTime;
    private ImageView pauseBtn;

    private boolean isPaused;
    private boolean goldOnCurActive;

    private File filePath;
    private File eventLogFile;
//    private FileOutputStream eventLogFileStream;
    private File userLogFile;
//    private FileOutputStream userLogFileStream;

    private int totalRed;
    private int totalRedClicked;
    private int totalGold;
    private int totalGoldClicked;
    private Calendar timeStarted;
    private Calendar timeFinished;

    private Calendar recordedPausedTime;
    private int sequence;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createLogFiles();

        //Initialize all fields
        points = 0;
        passedMandatoryTime = false;
        timerPausedTime = 0;
        totalRed = 0;
        totalRedClicked = 0;
        totalGold = 0;
        totalGoldClicked = 0;
        sequence = 0;

        goldOnCurActive = false;

        //Upper information bar
        taskInfo = (LinearLayout) findViewById(R.id.taskInfoContainer);
        pointsView = (TextView) findViewById(R.id.pointsView);
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
        remainingTimerView = (TextView) findViewById(R.id.remainingTimer);
        taskInfo.setVisibility(View.GONE);

        pauseBtn = (ImageView)findViewById(R.id.pauseBtn);
        pauseBtn.setVisibility(View.GONE);
        isPaused = false;
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPaused){
                    resumeTimer(true);
                    ((TaskScreenFragment) mContent).startTimer();
                    pauseBtn.setImageResource(R.drawable.pause_btn);
                    isPaused = false;
                }
                else{
                    pauseTimer();

                    logPause(false);

                    ((TaskScreenFragment) mContent).pauseTimer();
                    pauseBtn.setImageResource(R.drawable.resume_btn);
                    isPaused = true;
                }
            }
        });

        questionnaireBtn = (Button)findViewById(R.id.questionnaireBtn);
        questionnaireBtn.setVisibility(View.GONE);
        questionnaireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TaskScreenFragment taskScreenFragment = (TaskScreenFragment) mContent;
                //TODO: can we tinker with this for better accuracy??
                pauseTimer();
                logPause(true);

                taskScreenFragment.pauseTimer();
                new AlertDialog.Builder(MainActivity.this) //Open alert for questionnaire
                        .setMessage(R.string.questionnaire_confirm)
                        .setPositiveButton(R.string.questionnaire_continue, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goToQuestionnaire();

                                String screenType = "Questionnaire";
                                String goldInfo = "-";
                                String action = "Clicked confirm go to survey";

                                try{
                                    logEvent(timeFinished, screenType, sequence, goldInfo, action);
                                } catch(Exception e){
                                    Toast.makeText(getBaseContext(),
                                            "Could not save confirm questionnaire event. Exiting...",
                                            Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resumeTimer(true);
                                taskScreenFragment.startTimer();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });

        //Go to email screen
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = EnterEmailFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();

        setupUI(findViewById(android.R.id.content));

    }

    /**
     * Method to pause the chronometer
     */
    public void pauseTimer(){
        recordedPausedTime = Calendar.getInstance(); //Save this to write to file later
        if(totalTimer != null){ //If we pause on the first rest screen when nothing is instantiated
            timerPausedTime = totalTimer.getBase() - SystemClock.elapsedRealtime();
            totalTimer.stop();
        }
    }

    /**
     * Method to resume chronometer
     * @param validTime boolean flag if we can pause. we can only pause after first active screen
     */
    //TODO: should we allow pause on first rest?
    public void resumeTimer(boolean validTime){
        //Only record resumes after first active screen.
        if(validTime){
            //Save resume event.
            // NOTE: do this before restarting the time because it will take some time to write to file.
            String screenType = "Resume";
            String goldInfo = "-";
            String action = "Clicked resume";

            try{
                logEvent(Calendar.getInstance(), screenType, sequence, goldInfo, action);
            } catch(Exception e){
                Toast.makeText(this, "Could not save pause event. Exiting...", Toast.LENGTH_LONG).show();
            }
        }

        if(totalTimer != null){ //If we pause on the first rest screen when nothing is instantiated
            totalTimer.setBase(SystemClock.elapsedRealtime() + timerPausedTime);
            totalTimer.start();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        turnFullScreen(); //Hide status and nav bars
    }

    /**
     * Method to hid status/nav bars
     */
    public void turnFullScreen(){
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else{
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * Retrieve the username from input from email fragment
     * @param username the username that was inputted
     */
    @Override
    public void sendUsername(String username){
        currentUser = username;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mContent = ParametersFragment.newInstance();
        transaction.replace(R.id.fragmentContainer, mContent).commit();
    }

    /**
     * Starts the rest/active screen routine. Make views visible to show information
     */
    @Override
    public void startTask(){
        switchTaskScreen(true);
        taskInfo.setVisibility(View.VISIBLE);
        if(HIDE_REST_TIMER){
            remainingTimerView.setVisibility(View.GONE);
        }
        pauseBtn.setVisibility(View.VISIBLE);

        //Record time started
        timeStarted = Calendar.getInstance();
    }

    /**
     * From task fragment. Changes text of timer
     * @param time The time, sent as a String, to show on the screen
     */
    @Override
    public void countDown(String time){
        remainingTimerView.setText(time);
    }

    /**
     * Changes the screen accordingly
     * @param isActiveScreen true if currently on active screen, false otherwise
     */
    @Override
    public void switchTaskScreen(boolean isActiveScreen){
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if(isActiveScreen){ //switch to Rest Screen
            mContent = RestScreenFragment.newInstance(MAXIMUM_REST_TIME);
            if(HIDE_REST_TIMER){
                remainingTimerView.setVisibility(View.GONE);
            }
        }
        else if(!isActiveScreen){ //switch to Active Screen
            sequence++; //Increment to next sequence

            mContent = ActiveScreenFragment.newInstance(MAXIMUM_ACTIVE_ON_TIME, GOLD_PROBABILITY);

            if(totalTimer == null){ //if this is the first time we are in the Active screen begin to track time
                totalTimer = (Chronometer) findViewById(R.id.totalTimer);
                totalTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        long startTime = chronometer.getBase();
                        long currentTime = SystemClock.elapsedRealtime();
                        long timePassed = currentTime - startTime;
                        if (timePassed >= MAXIMUM_WORK_TIME) { //If pass the max time, just go to questionnaire
                            goToQuestionnaire();
                        } else if (!passedMandatoryTime && timePassed >= MANDATORY_WORK_TIME) { //Show goto questionnaire button after min time
                            passedMandatoryTime = true;
                            questionnaireBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
                resumeTimer(false);
            }
            remainingTimerView.setVisibility(View.VISIBLE);
        }

        transaction.replace(R.id.fragmentContainer, mContent).commit();
    }

    /**
     * Method to save user data and start Questionnaire activity.
     */
    public void goToQuestionnaire() {

        saveUserData();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(mContent).commit();
        fragmentManager.executePendingTransactions(); //Wait for this to close to ensure all fragments removed

        Intent intent = new Intent(MainActivity.this, QuestionnaireActivity.class);
        intent.putExtra("USERNAME", currentUser);
        startActivity(intent);
        finish();
    }

    /**
     * Saved the start, finish, and elapsed time for the current user.
     */
    public void saveUserData(){
        timeFinished = Calendar.getInstance();
        //Have to hard code formatting here because no appropriate formatting available
        long timeTaken = timeFinished.getTimeInMillis() - timeStarted.getTimeInMillis();
        long timeTakenSeconds = timeTaken / 1000 % 60;
        long timeTakenMinutes = timeTaken / (1000 * 60);
        long timeTakenHours = timeTaken/ (1000 * 60 * 60);
        String timeTakenFormatted = String.format("%02d:%02d:%02d", timeTakenHours, timeTakenMinutes, timeTakenSeconds);

        String dataToSaveUser = currentUser + "\t"
                + dateTimeFormat.format(timeStarted.getTime()) + "\t"
                + timeTakenFormatted + "\t"
                + dateTimeFormat.format(timeFinished.getTime()) + "\t"
                + totalRed + "\t"
                + totalGold + "\t"
                + totalRedClicked + "\t"
                + totalGoldClicked + "\t" + "\n";

        try{
            writeToFile(userLogFile, dataToSaveUser);
        } catch(Exception e){
            Toast.makeText(this, "Could not save user data. Exiting...", Toast.LENGTH_LONG).show();
        }




        //Maybe this will work on other devices to test
//        MediaScannerConnection.scanFile(this, new String[]{userLogFile.getAbsolutePath()}, null, null);

    }

    /**
     * Method to write to a file
     * @param f file to write to
     * @param dataToSave String data to write
     * @throws Exception
     */
    public void writeToFile(File f, String dataToSave) throws Exception{
        FileOutputStream eventLogFileStream = new FileOutputStream(f, true);
        eventLogFileStream.write(dataToSave.getBytes());
        eventLogFileStream.close();
    }

    /**
     * Mthose to save a pause event
     * @param isSurveySelected true if goto questionnaire button pressed, false if pause button pressed
     */
    public void logPause(boolean isSurveySelected){
        String screenType = "";
        String action = "";
        String goldInfo = "-";
        if(isSurveySelected){ //Set the screen type and action accordingly
            screenType = "Survey Confirmation";
            action = "Clicked to survey";
        }
        else {
            screenType = "Pause";
            action = "Clicked pause";
        }

        try{
            logEvent(recordedPausedTime, screenType, sequence, goldInfo, action);
        } catch(Exception e){
            Toast.makeText(this, "Could not save pause event. Exiting...", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method to save event for gold square click
     */
    public void goldClicked(){
        String screenType = "Active";
        String goldInfo = goldOnCurActive ? "1" : "0";
        String action = "Clicked gold square";

        try{
            logEvent(Calendar.getInstance(), screenType, sequence, goldInfo, action);
        }
        catch(Exception e){
            Toast.makeText(this, "Could not save gold clicked event. Exiting...", Toast.LENGTH_LONG).show();
        }

        points += GOLD_SCORE_WEIGHT;
        totalGoldClicked++;
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
    }

    /**
     * Method to save event for red square click
     */
    public void redClicked(){
        String screenType = "Active";
        String goldInfo = goldOnCurActive ? "1" : "0";
        String action = "Clicked red square";

        try{
            logEvent(Calendar.getInstance(), screenType, sequence, goldInfo, action);
        }
        catch(Exception e){
            Toast.makeText(this, "Could not save red clicked event. Exiting...", Toast.LENGTH_LONG).show();
        }

        points ++;
        totalRedClicked++;
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
    }

    /**
     * Method to record present active screen items
     * @param goldPresent true if gold square generated
     */
    @Override
    public void sendData(boolean goldPresent){
        totalRed++;
        totalGold += goldPresent ? 1 : 0;
        goldOnCurActive = goldPresent; //save this for logging later
    }

    /** Checks if external storage is available for read and write **/
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if log files present, if not create them.
     */
    private void createLogFiles(){

        //Check external storage available
        if(!isExternalStorageWritable()){
            Toast.makeText(this, "External Storage not writable. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        int currentAPIVersion = Build.VERSION.SDK_INT;
        File root;
        if(currentAPIVersion >= Build.VERSION_CODES.KITKAT){ //Get API version because external storage is different depending
            File[] dirs = ContextCompat.getExternalFilesDirs(this, null);
            root = dirs[0];
        }
        else{
            root = Environment.getExternalStorageDirectory();
        }

        //Check if file directory exists. If not, create it and check if it was created.
        filePath = new File(root + "/LogData");
        if(!filePath.exists()){
            boolean makeDir = filePath.mkdirs(); //Can't use this to check because it is false for both error and dir exists
        }
        if(!filePath.exists() || !filePath.isDirectory()){
            Toast.makeText(this, "Error with creating directory. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }
        String logFileName = "MIN_" + MANDATORY_WORK_TIME/1000
                + "MAX_" + MAXIMUM_ACTIVE_ON_TIME / 1000
                + "ACTIVE_" + MAXIMUM_ACTIVE_ON_TIME / 1000
                + "REST_" + MAXIMUM_REST_TIME
                + "GOLDWEIGHT_" + GOLD_SCORE_WEIGHT
                + "GOLDPROB_" + GOLD_PROBABILITY;

        String eventLogFileName = logFileName + "_DATA.txt";
        String userLogFileName = logFileName + "_USERS.txt";

        //Create event logs
        eventLogFile = new File(filePath, eventLogFileName);
        if(!eventLogFile.exists()){
            try{
                FileOutputStream eventLogFileStream = new FileOutputStream(eventLogFile);
                eventLogFileStream.write(EVENT_DATA_COLUMNS.getBytes());
                eventLogFileStream.close();
            } catch (Exception e){
                Toast.makeText(this, "Error creating event file. Exiting...", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        //Create user logs
        userLogFile = new File(filePath, userLogFileName);
        if(!userLogFile.exists()){
            try{
                FileOutputStream userLogFileStream = new FileOutputStream(userLogFile);
                userLogFileStream.write(USER_DATA_COLUMNS.getBytes());
                userLogFileStream.close();
            } catch(Exception e){
                Toast.makeText(this, "Error creating user file. Exiting...", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Save the every event that occurred
     * @param now time event happened
     * @param screenType which screen event occurred on
     * @param sequence which active screen occurred on
     * @param goldInfo if gold was present
     * @param action what action occurred
     * @throws Exception
     */
    public void logEvent(Calendar now, String screenType, int sequence, String goldInfo, String action) throws Exception{

        String dataToSave = dateFormat.format(now.getTime()) + "\t"
                + timeFormat.format(now.getTime()) + "\t"
                + currentUser + "\t"
                + screenType + "\t"
                + sequence + "\t"
                + goldInfo + "\t"
                + action + "\n";

        writeToFile(eventLogFile, dataToSave);
    }

    /**
     * Save rest button press event
     */
    @Override
    public void logRestButton(){
        String screenType = "Rest";
        String goldInfo = "-";
        String action = "Clicked go to rest screen";
        try{
            logEvent(Calendar.getInstance(), screenType, sequence, goldInfo, action);
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "\"Could not save goto rest screen clicked event. Exiting...\"",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tell active screen to pause
     * @return true if paused
     */
    @Override
    public boolean isScreenPaused(){
        return isPaused;
    }

    /**
     * Put a listener on every view to hide softkeyboard if edittext not chosen
     * @param view The view to set up
     */
    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    turnFullScreen();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    /**
     * Hide soft keyboard
     */
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(this.getCurrentFocus().getWindowToken() != null){
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
