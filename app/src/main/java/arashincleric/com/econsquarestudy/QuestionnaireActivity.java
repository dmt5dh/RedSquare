package arashincleric.com.econsquarestudy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class QuestionnaireActivity extends Activity {

    ArrayList<Question> questionList;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);

        context = this;

        questionList = new ArrayList<Question>();

        /** ADD QUESTIONS HERE */
        questionList.add(new Question("Describe the difficulty of this test. "));
        questionList.add(new QuestionScaleBar("On a scale of 0 - 10, how much did you like this?", 10));
        questionList.add(new QuestionRadioButtons("What is your year?", 4, new String[]{"1", "2", "3", "4"}));

        /** ADD QUESTIONS ABOVE */

        for(int i = 0; i < questionList.size(); i++){ //Go through each question and generate the view
            Question q = questionList.get(i);
            View child;
            if(q instanceof QuestionRadioButtons){ //Radio buttons questions
                child = getLayoutInflater().inflate(R.layout.question_radio, null);
                TextView questionView = (TextView)child.findViewById(R.id.question);
                questionView.setText(q.getQuestion());
                RadioGroup radioGroup = (RadioGroup)child.findViewById(R.id.radioAnswers);
                ArrayList<String> selections = ((QuestionRadioButtons)q).getSelections();
                for(int j = 0; j < ((QuestionRadioButtons)q).getNumSelections(); j++){ //Set all radio buttons
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(selections.get(j));
                    radioGroup.addView(radioButton);
                }
            }
            else if(q instanceof QuestionScaleBar){ //Scale questions
                child = getLayoutInflater().inflate(R.layout.question_scale, null);
                TextView questionView = (TextView)child.findViewById(R.id.question);
                questionView.setText(q.getQuestion());
                SeekBar seekBar = (SeekBar)child.findViewById(R.id.seekBar);
                seekBar.setMax(((QuestionScaleBar) q).getMaxScale());

                TextView maxScaleText = (TextView)child.findViewById(R.id.maxLimit);
                maxScaleText.setText(Integer.toString(seekBar.getMax()));

                String text = String.format(getResources().getString(R.string.seek_bar_text),
                        0, ((QuestionScaleBar)q).getMaxScale());
                TextView seekBarProgress = (TextView)child.findViewById(R.id.seekBarStatus);
                seekBarProgress.setText(text);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    //Track seek bar change here and display to user
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        View v = (View) seekBar.getParent().getParent();
                        if (v != null) {
                            String s = context.getResources().getString(R.string.seek_bar_text);
                            QuestionScaleBar parentView =(QuestionScaleBar)((View) seekBar.getParent().getParent()).getTag();
                            String text = String.format(s, progress, parentView.getMaxScale());
                            TextView seekBarProgress = (TextView) v.findViewById(R.id.seekBarStatus);
                            seekBarProgress.setText(text);
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }
            else{
                child = getLayoutInflater().inflate(R.layout.question_free_response, null);
                TextView questionView = (TextView)child.findViewById(R.id.question);
                questionView.setText(q.getQuestion());

                EditText answerText = (EditText)child.findViewById(R.id.answer);
                answerText.setHint(R.string.answer_hint);
            }
            child.setTag(q); //Save the object to this view to retrieve data later
            child.setPadding(0,0,0,50);
            layout.addView(child);
        }

        Button submitButton = new Button(this); //Add submit button at the end
        submitButton.setText(R.string.submit_btn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success = createQuestionnaireFile();

                if (success) {
                    Intent intent = new Intent(QuestionnaireActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        layout.addView(submitButton);

        setupUI(findViewById(R.id.scrollView));

        ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.requestFocus(); //Start at the top of the scrollView
    }

    @Override
    public void onResume(){
        super.onResume();
        turnFullScreen();
    }

    /**
     * Save the questionnaire answers to file
     * @return true if successful
     */
    private boolean createQuestionnaireFile(){
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
        File filePath = new File(root + "/QuestionnaireAnswers");
        if(!filePath.exists()){
            boolean makeDir = filePath.mkdirs(); //Can't use this to check because it is false for error and dir exists
        }
        if(!filePath.exists() || !filePath.isDirectory()){
            Toast.makeText(this, "Error with creating directory. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        String answersFileName = "Answers.txt";
        File answersFile = new File(filePath, answersFileName);
        if(!answersFile.exists()){ //Make the file if it doesnt exist
            try{
                FileOutputStream answerFileStream = new FileOutputStream(answersFile);
                String columns = "";
                String userName = "Username" + "\t";
                answerFileStream.write(userName.getBytes());
                for(int i = 0; i < questionList.size(); i++){ //Go through each view and retrieve question
                    columns = columns + questionList.get(i).getQuestion() + "\t";
                }
                columns = columns + "\n";
                answerFileStream.write(columns.getBytes());
                answerFileStream.close();
            } catch (Exception e){
                Toast.makeText(this, "Error creating event file. Exiting...", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
        Intent intent = getIntent();
        String userName = intent.getStringExtra("USERNAME") + "\t"; //Save username
        String data = userName;
        for(int i = 0; i < layout.getChildCount() - 1; i++){ //Go to 2nd to last because last child is the button
            RadioGroup radioGroup = (RadioGroup)layout.getChildAt(i).findViewById(R.id.radioAnswers);
            SeekBar seekBar = (SeekBar)layout.getChildAt(i).findViewById(R.id.seekBar);
            if(radioGroup != null){
                int radioId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton)radioGroup.findViewById(radioId);
                if(radioButton == null){ //Force user to choose something
                    alertFillFields();
                    return false;
                }
                data = data + radioButton.getText() + "\t";
            }
            else if(seekBar != null){
                data = data + Integer.toString(seekBar.getProgress()) + "\t";
            }
            else{
                EditText editText = (EditText)layout.getChildAt(i).findViewById(R.id.answer);
                if(editText.getText().toString().isEmpty()){ //For user to type something
                    alertFillFields();
                    return false;
                }
                data = data + editText.getText().toString() + "\t";
            }
        }

        data = data + "\n";

        try{
            FileOutputStream answerFileStream = new FileOutputStream(answersFile, true);
            answerFileStream.write(data.getBytes());
            answerFileStream.close();
        }catch (Exception e){
            Toast.makeText(this, "Error saving answers. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        return true;
    }

    /**Checks if external storage is available for read and write **/
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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

    /**
     * Put a listener on every view to hide softkeyboard if edittext not chosen
     * @param view The view set up
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

    /**
     * Generate alert to tell the user to fill out all fields
     */
    public void alertFillFields(){
        new AlertDialog.Builder(this)
                .setMessage(R.string.questionnaire_alert)
                .setNegativeButton(R.string.cancel_btn, null)
                .show();
    }

}
