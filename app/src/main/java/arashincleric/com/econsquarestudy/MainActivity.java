package arashincleric.com.econsquarestudy;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity
        implements EnterEmailFragment.OnEnterEmailFragmentInteractionListener,
        ParametersFragment.OnParametersFragmentInteractionListener,
        ActiveScreenFragment.OnActiveScreenFragmentInteractionListener,
        TaskScreenFragment.OnTaskScreenFragmentInteractionListener{

    //IN MILLISECONDS
    public static long MANDATORY_WORK_TIME = 10000;
    public static long MAXIMUM_WORK_TIME = 60000;
    public static int MAXIMUM_ACTIVE_ON_TIME = 8000;
    public static int MAXIMUM_REST_TIME = 5000;
    public static double GOLD_SCORE_WEIGHT = 1.25;
    public static double GOLD_PROBABILITY = 0.9;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        points = 0;
        passedMandatoryTime = false;
        timerPausedTime = 0;

        //Upper information bar
        taskInfo = (LinearLayout) findViewById(R.id.taskInfoContainer);
        pointsView = (TextView) findViewById(R.id.pointsView);
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
        remainingTimerView = (TextView) findViewById(R.id.remainingTimer);
        taskInfo.setVisibility(View.GONE);

        questionnaireBtn = (Button)findViewById(R.id.questionnaireBtn);
        questionnaireBtn.setVisibility(View.GONE);
        questionnaireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TaskScreenFragment taskScreenFragment = (TaskScreenFragment)mContent;
                //TODO: tinker with this for better accuracy
                pauseTimer();
                taskScreenFragment.pauseTimer();
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.questionnaire_confirm)
                        .setPositiveButton(R.string.questionnaire_continue, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
        timerPausedTime = totalTimer.getBase() - SystemClock.elapsedRealtime();
        totalTimer.stop();
    }

    public void resumeTimer(){
        totalTimer.setBase(SystemClock.elapsedRealtime() + timerPausedTime);
        totalTimer.start();
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
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(mContent).commit();
        fragmentManager.executePendingTransactions();

        Intent intent = new Intent(MainActivity.this, QuestionnaireActivity.class);
        startActivity(intent);
        finish();
    }

    public void goldClicked(){
        points += GOLD_SCORE_WEIGHT;
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
    }
    public void redClicked(){
        points ++;
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
    }
}
