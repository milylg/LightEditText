package org.demo.text;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.lib.text.effect.InputText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ## WARN PROBLEM LOG
 * W/Glide: Failed to find GeneratedAppGlideModule.
 * You should include an annotationProcessor compile dependency on
 * com.github.bumptech.glide:compiler in your application
 * and a @GlideModule annotated AppGlideModule implementation
 * or LibraryGlideModules will be silently ignored
 */
public class MainActivity extends AppCompatActivity {

    private static final String SHARE_PREFERENCES_NOTE_DIRECTORY = "article";
    private static final String NOTE_KEY = "example";
    private static final String DEFAULT_NOTE_VALUE = "You write something at here!";
    private static final int IMAGE_REQUEST_CODE = 0;

    public static final String INTENT_HTML_CODE_KEY = "html_code";
    public static final String INTENT_HTML_RENDER_KEY = "html_render";

    private InputText inputText;
    private InputText.GetImagePostProcessor imagePostProcessor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupInputText();
    }

    private void setupInputText() {
        inputText = findViewById(R.id.input_area);
        SharedPreferences preferences = getSharedPreferences(SHARE_PREFERENCES_NOTE_DIRECTORY, MODE_PRIVATE);
        String htmlCode = preferences.getString(NOTE_KEY, DEFAULT_NOTE_VALUE);
        inputText.input(htmlCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_text_obsever, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.html_code) {
            Intent toSeeCode = new Intent(this, CodeActivity.class);
            toSeeCode.putExtra(INTENT_HTML_CODE_KEY, inputText.output());
            startActivity(toSeeCode);
        } else if (item.getItemId() == R.id.html_render) {
            Intent toRenderCode = new Intent(this, RenderActivity.class);
            toRenderCode.putExtra(INTENT_HTML_RENDER_KEY, inputText.output());
            startActivity(toRenderCode);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        automaticCommit();
    }

    private void automaticCommit() {
        SharedPreferences userInfo = getSharedPreferences(SHARE_PREFERENCES_NOTE_DIRECTORY, MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString(NOTE_KEY, inputText.output());
        editor.apply();
    }
}
