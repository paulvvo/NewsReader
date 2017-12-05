package com.example.sleepy.newsreader;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    ListView newsListView;
    ArrayList<String> newsList,urlList;
    ArrayAdapter arrayAdapter;
    Intent intent;

    //BackgroundTask Class
    public class BackgroundTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            String idResult = "";

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                //Get IDs from hacker rank
                int data = reader.read();
                while(data!=-1){
                    char current = (char) data;
                    idResult += current;
                    data = reader.read();
                }

                //Take the IDS and put it into a JSONArray
                JSONArray idJsonArray = new JSONArray(idResult);
                //Log.i("test",idJsonArray.toString());

                //Go Through idJsonArray
                for(int j=0; j<idJsonArray.length(); j++){
                    String jsonWebResult = "";

                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+idJsonArray.get(j).toString()+".json?print=pretty");
                    //Log.i("json site info","https://hacker-news.firebaseio.com/v0/item/"+idJsonArray.get(j).toString()+".json?print=pretty");
                    connection = (HttpURLConnection) url.openConnection();
                    inputStream = connection.getInputStream();
                    reader = new InputStreamReader(inputStream);

                    data = reader.read();
                    while(data!=-1){
                        char current = (char) data;
                        jsonWebResult += current;
                        data = reader.read();
                    }
                    //Create a JSONObject from Hacker-Rank API
                    //If the JSON data has a title and url, they are added to the list
                    //Log.i(Integer.toString(j), jsonWebResult);
                    JSONObject jsonObject = new JSONObject(jsonWebResult);
                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")){
                        Log.i("url",jsonObject.getString("url"));
                        Log.i("title",jsonObject.getString("title"));
                        newsList.add(jsonObject.getString("title"));
                        urlList.add(jsonObject.getString("url"));
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Update the ArrayAdapter
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this,Main2Activity.class);

        //Initialization of ArrayList, ListView,  ArrayAdapter
        urlList = new ArrayList<String>();
        newsList = new ArrayList<String>();
        newsListView = (ListView) findViewById(R.id.newsListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, newsList);
        //Set the adapter to to the ListView
        newsListView.setAdapter(arrayAdapter);

        //Set the Item Click Listener for the ListView
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, newsList.get(i), Toast.LENGTH_SHORT).show();
                //Send URL to be accessed in other activity
                intent.putExtra("url", urlList.get(i));
                //Switch to second activity
                startActivity(intent);
            }
        });

        newsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("Long Press");
                return true;
            }
        });


        //Initialize and Execute Task
        BackgroundTask task = new BackgroundTask();
        try{
            //Using Hacker-News API to get a list of IDs for their sites
            task.execute("https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


