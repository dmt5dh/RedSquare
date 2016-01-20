package arashincleric.com.econsquarestudy;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

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

        //TODO: add questions here
        questionList.add(new Question("test 1"));
        questionList.add(new QuestionRadioButtons("test 2", 2, new String[]{"Male", "Female"}));
        questionList.add(new QuestionScaleBar("test 3", 10));

        for(int i = 0; i < questionList.size(); i++){
            Question q = questionList.get(i);
            View child;
            if(q instanceof QuestionRadioButtons){
                child = getLayoutInflater().inflate(R.layout.question_radio, null);
                TextView questionView = (TextView)child.findViewById(R.id.question);
                questionView.setText(q.getQuestion());
                RadioGroup radioGroup = (RadioGroup)child.findViewById(R.id.radioAnswers);
                ArrayList<String> selections = ((QuestionRadioButtons)q).getSelections();
                for(int j = 0; j < ((QuestionRadioButtons)q).getNumSelections(); j++){
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(selections.get(j));
                    radioGroup.addView(radioButton);
                }
            }
            else if(q instanceof QuestionScaleBar){
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
            }
            child.setTag(q);
            child.setPadding(0,0,0,10);
            layout.addView(child);
        }

        Button submitButton = new Button(this);
        submitButton.setText(R.string.submit_btn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQuestionnaireFile();

                Intent intent = new Intent(QuestionnaireActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        layout.addView(submitButton);

        setupUI(findViewById(R.id.scrollView));

        layout.requestFocus();
    }

    private void createQuestionnaireFile(){
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
        if(!answersFile.exists()){
            try{
                FileOutputStream answerFileStream = new FileOutputStream(answersFile);
                String columns = "";
                for(int i = 0; i < questionList.size(); i++){
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
        String data = "";
        for(int i = 0; i < layout.getChildCount() - 1; i++){
            RadioGroup radioGroup = (RadioGroup)layout.getChildAt(i).findViewById(R.id.radioAnswers);
            SeekBar seekBar = (SeekBar)layout.getChildAt(i).findViewById(R.id.seekBar);
            if(radioGroup != null){
                int radioId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton)radioGroup.findViewById(radioId);
                data = data + radioButton.getText() + "\t";
            }
            else if(seekBar != null){
                data = data + Integer.toString(seekBar.getProgress()) + "\t";
            }
            else{
                EditText editText = (EditText)layout.findViewById(R.id.answer);
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
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
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

}
