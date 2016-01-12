package arashincleric.com.econsquarestudy;


import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity
        implements EnterEmailFragment.OnEnterEmailFragmentInteractionListener,
        ParametersFragment.OnParametersFragmentInteractionListener,
        RestScreenFragment.OnRestScreenFragmentInteractionListener{

    //In milliseconds
    public static int MANDATORY_WORK_TIME = 0;
    public static int MAXIMUM_WORK_TIME = 0;
    public static int MAXIMUM_ACTIVE_ON_TIME = 0;
    public static int MAXIMUM_REST_TIME = 5000;
    public static int GOLD_SCORE_WEIGHT = 1;
    public static double GOLD_PROBABILITY = 0.5;

    public int points = 0;


    private LinearLayout taskInfo;
    private TextView remainingTimerView;
    private TextView pointsView;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment mContent;
    private String currentUser = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Upper information bar
        taskInfo = (LinearLayout) findViewById(R.id.taskInfo);
        pointsView = (TextView) findViewById(R.id.pointsView);
        String pointsViewText = String.format(getResources().getString(R.string.points_text), points);
        pointsView.setText(pointsViewText);
        remainingTimerView = (TextView) findViewById(R.id.remainingTimer);
        taskInfo.setVisibility(View.GONE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mContent = EnterEmailFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();

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
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mContent = RestScreenFragment.newInstance(MAXIMUM_REST_TIME);
        transaction.replace(R.id.fragmentContainer, mContent).commit();
        taskInfo.setVisibility(View.VISIBLE);
//        //Listener does not hit until first tick "skipping" a second
//        performTick(MAXIMUM_REST_TIME);
//
//        new CountDownTimer(MAXIMUM_REST_TIME, 100){
//            public void onTick(long millisUntilFinished){
//                performTick(Math.round(millisUntilFinished));
//            }
//
//            public void onFinish(){
//                String time = String.format(getResources().getString(R.string.remaining_time), 0);
//                remainingTimerView.setText(time);
//                Toast.makeText(getBaseContext(), "hello", Toast.LENGTH_SHORT).show();
//            }
//        }.start();
    }
//
//    public void performTick(long millisUntilFinished){
//        String time = String.format(getResources().getString(R.string.remaining_time),
//                (millisUntilFinished / 1000) + 1);
//        remainingTimerView.setText(time);
//    }
    @Override
    public void countDown(String time){
        remainingTimerView.setText(time);
    }

    @Override
    public void goToActive(){

    }
}
