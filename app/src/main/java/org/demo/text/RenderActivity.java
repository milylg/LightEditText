package org.demo.text;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.lib.text.TextBox;

import static org.demo.text.MainActivity.INTENT_HTML_RENDER_KEY;

public class RenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);
        TextBox textBox = findViewById(R.id.text_box);
        Intent htmlCodeIntent = getIntent();
        if (htmlCodeIntent != null) {
            String code = htmlCodeIntent.getStringExtra(INTENT_HTML_RENDER_KEY);
            textBox.input(code);
        }
    }
}