package org.demo.text;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import static org.demo.text.MainActivity.INTENT_HTML_CODE_KEY;

public class CodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        TextView codeArea = findViewById(R.id.code_area);
        Intent htmlCodeIntent = getIntent();
        if (htmlCodeIntent != null) {
            String code = htmlCodeIntent.getStringExtra(INTENT_HTML_CODE_KEY);
            codeArea.setText(code);
        }
    }
}