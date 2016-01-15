package arashincleric.com.econsquarestudy;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class QuestionnaireActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuestionnaireActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
