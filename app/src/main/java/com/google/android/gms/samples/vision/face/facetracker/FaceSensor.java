package com.google.android.gms.samples.vision.face.facetracker;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import com.google.android.gms.vision.face.Face;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by MIYE on 3/15/2017.
 */

public class FaceSensor {

    private static final String TAG = "FaceSensor";
    final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    int i;
    int target = 0;
    int prevTarget = 0;
    float prevId = 0;

    void handleFaceUpdate(Face face) {
        i++;
        if(i < 10) // Wait for the 30th frame
            return;
        i=0;
        float id = face.getId();
        if(prevId != id) { // New face, wait for next update for same id
            id = prevId;
            return;
        }
        int target = 2;
        float eulerY = face.getEulerY();
        if(eulerY >20)
            target =1;
        if(eulerY< -20)
            target =3;
        if(prevTarget ==target)
            return;
        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        Log.d(TAG,"id="+String.valueOf(id)+" target="+String.valueOf(target)+" eulerY="+String.valueOf(eulerY));
        prevTarget =target;
        try {
            postGazeEvent(target);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    void postGazeEvent(int target) throws Exception {
        String data = "{target : " + String.valueOf(target) + "}";
        String urlString = "http://192.168.1.81:8080/GazeEvent?payload=" + URLEncoder.encode(data, "UTF-8");
        Log.d(TAG,"postGazeEvent:" + urlString);
        String response = doHttpUrlConnectionAction(urlString);
        Log.d(TAG,"postGazeEvent response:" + response);

    }
    private String doHttpUrlConnectionAction(String desiredUrl)
            throws Exception
    {
        URL url = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        try
        {
            // create the HttpURLConnection
            url = new URL(desiredUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // just want to do an HTTP GET here
            connection.setRequestMethod("GET");

            // uncomment this if you want to write output to this url
            //connection.setDoOutput(true);

            // give it 15 seconds to respond
            connection.setReadTimeout(15*1000);
            connection.connect();

            // read the output from the server
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            // close the reader; this can throw an exception too, so
            // wrap it in another try/catch block.
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
    }
}
