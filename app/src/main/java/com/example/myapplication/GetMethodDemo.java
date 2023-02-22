package com.example.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetMethodDemo extends AsyncTask<String , Void ,String> {
    String server_response;

    @Override
    protected String doInBackground(String... strings) {

        URL url;
        HttpURLConnection urlConnection = null;

        try {

            url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            Log.d("connection URL", "connection established");
            //Does not go beyond this line, crashes without any exception


                server_response = readStream(urlConnection.getInputStream());

                Log.d("CatalogClient", ""+server_response);



        } catch (MalformedURLException e) {
            Log.d("MalformedURL", "yes", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("IOExceptionURL", "yes", e);
            e.printStackTrace();
        }catch (Exception e){
            Log.d("ExceptionRandom", "yes", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.e("Response", "" + server_response);


    }


// Converting InputStream to String

private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}