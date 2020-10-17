package com.example.javaapi;

import android.util.Log;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void getAuthToken_isCorrect() throws IOException, JSONException {
        /*
           In order to test locally with JSONObject:
           `testOptions`, `testCompile 'org.json:json:20200518' is added to build.gradle(:app)`
         */
        flowgateClient fc = new flowgateClient("10.11.16.36", "admin", "Ar_InDataCenter_450");
        String s = fc.getAuthToken();
        Log.d("flowgateClient", s);
        System.out.println("TOKEN: " + s);
        // don't know where to find the output.. could only test by setting break points
    }
}