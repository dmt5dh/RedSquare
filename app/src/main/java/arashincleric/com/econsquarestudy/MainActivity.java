package arashincleric.com.econsquarestudy;


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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
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
    public static long MANDATORY_WORK_TIME = 10000;
    public static long MAXIMUM_WORK_TIME = 85000;
    public static int MAXIMUM_ACTIVE_ON_TIME = 8000;
    public static int MAXIMUM_REST_TIME = 5000;
    public static double GOLD_SCORE_WEIGHT = 1.25;
    public static double GOLD_PROBABILITY = 0.9;

    public final static String USER_DATA_COLUMNS =
            "UserID\tTimeStart\tTotalTime\tTimeStartSurvey\tNumberRedOffered\tNumberGoldOffered" +
                    "\tNumberRedClicked\tNumberGoldClicked\n";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public double points;


    private LinearLayout taskInfo;
    private TextView remainingTimerView;
    private TextView pointsView;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment mContent;
    private String currentUser = null;
    private Chronometer totalTimer;
    private boolean passedMandatoryTime; //flag to fire the mandatory time code once
    private Button questionnaireBtn;
    private long timerPausedTime;
    private ImageView pauseBtn;

    private boolean isPaused;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createLogFiles();

        points = 0;
        passedMandatoryTime = false;
        timerPausedTime = 0;
        totalRed = 0;
        totalRedClicked = 0;
        totalGold = 0;
        totalGoldClicked = 0;

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
                    resumeTimer();
                    ((TaskScreenFragment) mContent).startTimer();
                    pauseBtn.setImageResource(R.drawable.pause_btn);
                    isPaused = false;
                }
                else{
                    //TODO: pause event here
                    pauseTimer();
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
                //TODO: tinker with this for better accuracy
                //TODO: record goto surveyBtn event here
                pauseTimer();
                taskScreenFragment.pauseTimer();
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.questionnaire_confirm)
                        .setPositiveButton(R.string.questionnaire_continue, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: go to questionnair event here
                                goToQuestionnaire();
                            }
                        })
                        .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resumeTimer();
                                taskScreenFragment.startTimer();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = EnterEmailFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();

    }

    public void pauseTimer(){
        if(totalTimer != null){ //If we pause on the first rest screen when nothing is instantiated
            timerPausedTime = totalTimer.getBase() - SystemClock.elapsedRealtime();
            totalTimer.stop();
        }
    }

    public void resumeTimer(){
        if(totalTimer != null){ //If we pause on the first rest screen when nothing is instantiated
            totalTimer.setBase(SystemClock.elapsedRealtime() + timerPausedTime);
            totalTimer.start();
        }
        //TODO: resume event here
    }

    @Override
    public void onResume(){
        super.onResume();
        turnFullScreen();
    }

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

    @Override
    public void sendUsername(String username){
        currentUser = username;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mContent = ParametersFragment.newInstance();
        transaction.replace(R.id.fragmentContainer, mContent).commit();
    }

    @Override
    public void startTask(){
        switchTaskScreen(true);
        taskInfo.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);

        //TODO: log time user started here
        timeStarted = Calendar.getInstance();
    }

    @Override
    public void countDown(String time){
        remainingTimerView.setText(time);
    }

    @Override
    public void switchTaskScreen(boolean isActiveScreen){
        //TODO: Implement this
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if(isActiveScreen){ //switch to Rest Screen
            mContent = RestScreenFragment.newInstance(MAXIMUM_REST_TIME);
        }
        else if(!isActiveScreen){ //switch to Active Screen
            mContent = ActiveScreenFragment.newInstance(MAXIMUM_ACTIVE_ON_TIME, GOLD_PROBABILITY);

            if(totalTimer == null){ //if this is the first time we are in the Active screen begin to track time
                totalTimer = (Chronometer) findViewById(R.id.totalTimer);
                totalTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        long startTime = chronometer.getBase();
                        long currentTime = SystemClock.elapsedRealtime();
                        long timePassed = currentTime - startTime;
                        if (timePassed >= MAXIMUM_WORK_TIME) {
                            goToQuestionnaire();
                        } else if (!passedMandatoryTime && timePassed >= MANDATORY_WORK_TIME) {
                            passedMandatoryTime = true;
                            questionnaireBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
                resumeTimer();
            }
        }

        transaction.replace(R.id.fragmentContainer, mContent).commit();
    }

    public void goToQuestionnaire() {

        saveUserData();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(mContent).commit();
        fragmentManager.executePendingTransactions();

        Intent intent = new Intent(MainActivity.this, QuestionnaireActivity.class);
        startActivity(intent);
        finish();
    }

    public void saveUserData(){
        //TODO: save user surveystart, totaltime
        Calendar timeFinished = Calendar.getInstance();
        long timeTaken = timeFinished.getTimeInMillis() - timeStarted.getTimeInMillis();
        long timeTakenSeconds = timeTaken / 1000 % 60;
        long timeTakenMinutes = timeTaken / (1000 * 60);
        long timeTakenHours = timeTaken/ (1000 * 60 * 60);
        String timeTakenFormatted = String.format("%02d:%02d:%02d", timeTakenHours, timeTakenMinutes, timeTakenSeconds);

        String dataToSave = currentUser + "\t"
                + dateFormat.format(timeStarted.getTime()) + "\t"
                + timeTakenFormatted + "\t"
                + dateFormat.format(timeFinished.getTime()) + "\t"
                + totalRed + "\t"
                + totalGold + "\t"
                + totalRedClicked + "\t"
                + totalGoldClicked + "\t" + "\n";

        try{
            FileOutputStream userLogFileStream = new FileOutputStream(userLogFile, true);
            userLogFileStream.write(dataToSave.getBytes());
            userLogFileStream.close();
        } catch(Exception e){
            Toast.makeText(this, "Could not save user data. Exiting...", Toast.LENGTH_LONG).show();
        }

        //Maybe this will work on other devices to test
//        MediaScannerConnection.scanFile(this, new String[]{userLogFile.getAbsolutePath()}, null, null);

    }

    public void goldClicked(){
        //TODO: record gold click event here
        points += GOLD_SCORE_WEIGHT;
        totalGoldClicked++;
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
    }
    public void redClicked(){
        //TODO: record red click event here
        points ++;
        totalRedClicked++;
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
    }

    @Override
    public void sendData(boolean goldPresent){
        totalRed++;
        totalGold += goldPresent ? 1 : 0;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void createLogFiles(){

        //Check external storage available
        if(!isExternalStorageWritable()){
            Toast.makeText(this, "External Storage not writable. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }
        //Check if file directory exists. If not, create it and check if it was created.
        filePath = new File(Environment.getExternalStorageDirectory() + "/LogData");
        boolean makeDir = filePath.mkdir(); //Can't use this to check because it is false for error and dir exists
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
}
