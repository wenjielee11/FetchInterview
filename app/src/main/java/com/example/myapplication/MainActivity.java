package com.example.myapplication;


import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String fetchData;
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new JsonTask().execute("https://fetch-hiring.s3.amazonaws.com/hiring.json");
        HashMap<String, List<String>> JSONData = storeJSONData();
        List<String> displayList = sortJSONData(JSONData);

        listView = findViewById(R.id.list);
        ArrayAdapter<String>  arr =
                new ArrayAdapter<String>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                displayList);

        listView.setAdapter(arr);
    }


    public HashMap<String, List<String>> storeJSONData() {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        String JSONData = fetchData;
        if(JSONData == null) {
            Log.d("storeJson", "null");
        }
        try {
            JSONArray arr = new JSONArray(JSONData);
            for(int i=0; i<arr.length();i++){
                    JSONObject element = arr.getJSONObject(i);
                    if(!element.getString("name").equals("null") && !element.getString("name").trim().isEmpty()) {
                        List<String> jsonNameList = result.get(element.getString("listId"));
                        if(jsonNameList == null){
                            //List does not exist for the particular key. Create it.
                            jsonNameList = new ArrayList<String>();
                            result.put(element.getString("listId"), jsonNameList);
                        }
                        if(!jsonNameList.contains(element.getString("name"))) {
                            jsonNameList.add(element.getString("name"));
                        }

                    }
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    public List<String> sortJSONData(HashMap<String, List<String>> JSONData){
        ArrayList<String> displayList = new ArrayList<>();
        for(int i =1;i<=JSONData.size();i++){
            if(JSONData.get(""+i)!=null) {
                Collections.sort(JSONData.get("" + i), (s1, s2) -> {

                    String[] s1Parts = s1.split(" +");
                    String[] s2Parts = s2.split(" +");

                    for (int i1 = 0; i1 < s1Parts.length && i1 < s2Parts.length; ++i1) {
                        //if parts are the same
                        if (s1Parts[i1].compareTo(s2Parts[i1]) == 0) {
                            continue;
                        }
                        try {
                            int intS1 = Integer.parseInt(s1Parts[i1]);
                            int intS2 = Integer.parseInt(s2Parts[i1]);

                            //if the parse works
                            int diff = intS1 - intS2;
                            if (diff == 0) {
                                // continue;    // Actually, this is a no-op
                            } else {
                                return diff;
                            }
                        } catch (NumberFormatException ex) {
                            // The items are strings, compare them.
                            return s1Parts[i1].compareTo(s2Parts[i1]);
                        }
                    }
                    return -1;
                });
                displayList.addAll(JSONData.get("" + i));
            }
        }

        return displayList;
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                Log.d("doinbg: ", "1 ");
                connection = (HttpURLConnection) url.openConnection();
                Log.d("doinbg: ", "2 ");
                connection.connect();
                Log.d("doinbg: ", "3 ");

                InputStream stream = connection.getInputStream();
                Log.d("doinbg: ", "4 ");
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                Log.d("doinbg: ", "5 ");
                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }
                Log.d("doinbg: ", "6 ");

                return buffer.toString();


            } catch (MalformedURLException e) {
                Log.d("doInBackground Malformed", "yes");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("doInBackground IO", "yes");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            fetchData = result;

        }
    }
}






