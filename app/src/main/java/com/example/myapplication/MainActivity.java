package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the app that parses JSON Data from a URL containing item names
 * to be displayed in a list.
 *
 * @author Wen Jie Lee
 */
public class MainActivity extends AppCompatActivity {

    ListView listView; // instance that contains the array of items to be displayed
    String serverResponse; // JSON Data fetched from the URL in string format

    /**
     * Main method that runs all the process(es) required to display the list of items
     * @param savedInstanceState the current instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Fetch and store the data into the string server_response.
        // If needed, replace the url with a string of user input containing the url that the user
        //wants to data fetch from.
        fetchJSONData("https://fetch-hiring.s3.amazonaws.com/hiring.json");
        // Parse and store the data fetched from the URL into a hashmap, to be used by other implementations
        HashMap<String, List<String>> JSONData = storeJSONData(serverResponse);
        // ListView requires a list type to display the items, so we have to sort the items in the hashmap
        // into a long list
        List<String> displayList = sortJSONData(JSONData);
        //Display the list.
        listView = findViewById(R.id.list);
        ArrayAdapter<String> arr =
                new ArrayAdapter<String>(
                        this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        displayList);

        listView.setAdapter(arr);
    }

    /**
     * This method parses the response obtained from the HTTP call made by fetchJSONData()
     * and stores them in a Hashmap containing a list of items. For more advanced implementations, we
     * could easily access the items grouped by the listID that is stored as keys. Uses linkedlists due to
     * frequent insertions.
     *
     * Alternatively, could use List<Hashmap> for frequent duplicate/colliding insertions.
     * Could change to store JSON Objects instead of item names for future use cases.
     *
     * @param JSONData a string of response obtained from the Json URL
     * @return a HashMap that groups items by listID.
     */
    public HashMap<String, List<String>> storeJSONData(String JSONData) {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        //Parse the data while filtering out invalid entries
        try {
            JSONArray arr = new JSONArray(JSONData);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject element = arr.getJSONObject(i);
                // Item cannot be empty or null
                if (!element.getString("name").equals("null") && !element.getString("name").trim().isEmpty()) {
                    List<String> jsonNameList = result.get(element.getString("listId"));
                    if (jsonNameList == null) {
                        //List does not exist for the particular key. Create it.
                        jsonNameList = new LinkedList<String>();
                        result.put(element.getString("listId"), jsonNameList);
                    }
                    // There are no duplicates in the file provided, so just add the item name
                    // directly without checking
                    jsonNameList.add(element.getString("name"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * This method sorts the linked lists of strings by their item names, then by item number.
     * Each list is grouped by their ID.
     * Uses Collections.sort() that is merge sort based. O(n*log n).
     *
     * @param JSONData the Hashmap containing the items to be sorted
     * @return the list of item names in sorted order, ascending.
     */
    public List<String> sortJSONData(HashMap<String, List<String>> JSONData) {
        List<String> displayList = new LinkedList<>();
        for (int i = 1; i <= JSONData.size(); i++) {
            if (JSONData.get(String.valueOf(i)) != null) {
                JSONData.get(String.valueOf(i)).sort((s1, s2) -> {
                    // Specialized comparator that compares the item strings and numbers.
                    // Default comparator used by Collections.sort() does not account for digits
                    // after the first digit i.e. item 1XX
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
                                // continue;
                            } else {
                                return diff;
                            }
                        } catch (NumberFormatException e) {
                            // The items are strings, compare them.
                            return s1Parts[i1].compareTo(s2Parts[i1]);
                        }
                    }
                    return -1;
                });
                displayList.addAll(JSONData.get(String.valueOf(i)));
            }
        }
        return displayList;
    }

    /**
     * This method can be used to load a local asset JSON file instead if the user decides to,
     * or if connection fails
     *
     * @return JSON data parsed in a string.
     */
    private String loadJSONAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("hiring.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * This is a helper method to read and save the server response from the HTTP call made by
     * fetchJSONData().
     *
     * @param in the stream instance used to read the data
     * @return a string containing the server response.
     */
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            //Keep going until reaching the end of line
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

    /**
     * This method fetches JSON data from a specified URL. Runs on a separate thread because
     * network operations cannot be done on the main thread.
     * @param param URL to make a HTTP request to.
     */
    private void fetchJSONData(String param){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(param);
                    HttpURLConnection urlConnection = null;
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    serverResponse = readStream(urlConnection.getInputStream());

                } catch (IOException e) {
                    // No internet connection, fallback to loading from asset folder
                    Log.d("HTTP Request failed", "could not connect, loading from assets");
                    serverResponse = loadJSONAsset();
                }
            }
        });
        // Start the HTTP request
        t.start();
        // The main thread has to wait until the request is completed and server_response is stored.
        // Or it will cause NPE's.
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}







