package arashincleric.com.econsquarestudy;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


public class TaskCompleteScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_complete_screen);

        turnFullScreen();

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        String message = String.format(getResources().getString(R.string.complete_screen_msg),
                intent.getDoubleExtra("POINTS", 0),
                username);

        TextView completeView = (TextView)findViewById(R.id.completeMsg);
        completeView.setText(message);

        handler = new Handler();
        handler.postDelayed(finishTask, 30000);
    }

    Handler handler;
    UsernameDbHelper mDbHelper;
    String username;
    private Runnable finishTask = new Runnable() {
        @Override
        public void run() {
            // Gets the data repository in write mode
            mDbHelper = new UsernameDbHelper(getBaseContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(UsernameContract.Usernames.COLUMN_NAME_ENTRY_ID, username);

            long newRowId = db.insert(UsernameContract.Usernames.TABLE_NAME, null, values);
            db.close();
            finish();
        }
    };

    /**
     * Hide nav/status bars
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

}
