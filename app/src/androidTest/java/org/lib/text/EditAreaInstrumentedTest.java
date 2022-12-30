package org.lib.text;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EditAreaInstrumentedTest {

    private Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private String readHtmlText() {
        StringBuilder textBuilder = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(
                appContext.getAssets().open("text_protocal.html"));
             BufferedReader bufReader = new BufferedReader(isr);
        ){
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                textBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return textBuilder.toString();
    }
}