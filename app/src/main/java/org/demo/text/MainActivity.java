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
        inputText.setImageDrawableGetter(postProcessor -> {
            this.imagePostProcessor = postProcessor;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_REQUEST_CODE);
        });

        SharedPreferences preferences = getSharedPreferences(SHARE_PREFERENCES_NOTE_DIRECTORY, MODE_PRIVATE);
        String htmlCode = preferences.getString(NOTE_KEY, DEFAULT_NOTE_VALUE);
        inputText.input(htmlCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE) {
            Uri uri;
            if (data != null && (uri = data.getData()) != null) {
                String filePath = buildNewFileForImage();
                Bitmap bitmap = copyImage(uri, filePath);
                if (bitmap != null) {
                    imagePostProcessor.handleImage(bitmap, filePath);
                }
            }
        }
    }

    private String buildNewFileForImage() {
        File directory = getExternalFilesDir("images");

        // NOTE: folder name must be not include '/' symbol.
        // Cause: open failed: ENOENT (No such file or directory)
        return directory.getAbsolutePath() + File.separator + "example.jpg";
    }

    private Bitmap copyImage(Uri image, String copyTo) {

        File copiedImageFile = new File(copyTo);

        try (FileOutputStream fos = new FileOutputStream(copiedImageFile);
             InputStream inputStream = getContentResolver().openInputStream(image)) {

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // save picture bitmap.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);

            // ?????????InputText?????????????????????????????????
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float scale = (float) inputText.getWidth() / width;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

            // ???copy?????????Bitmap????????????
            fos.flush();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
