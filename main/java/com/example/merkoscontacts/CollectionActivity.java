package com.example.merkoscontacts;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CollectionActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

    private final Gson gson = new Gson();

    String token;
    String token_type;
    String contact;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        Intent intent = getIntent();
        token = intent.getStringExtra("ACCESS_TOKEN");
        token_type = intent.getStringExtra("TOKEN_TYPE");
        contact = intent.getStringExtra("CONTACT");

        listView = findViewById(R.id.listView);

        String postBody = "";

        final MediaType MEDIA_TYPE_JSON
                = MediaType.parse("application/json");

        Request request = new Request.Builder()
                .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/get-collections")
                .header("Authorization", token_type + " " + token)
                .post(RequestBody.create(postBody, MEDIA_TYPE_JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try (final ResponseBody responseBody = response.body()) {
                    final String responseString = responseBody.string();
                    if (!response.isSuccessful())
                        throw new IOException(String.valueOf(response));
                    else {
                        CollectionActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<String> collectionArray = new ArrayList<String>(){};
                                Map collectionDict = gson.fromJson(responseString, Map.class);
                                Object[] valueArray = collectionDict.keySet().toArray();
                                for (int i = 0; i < valueArray.length; i++) {
                                    ArrayList values = (ArrayList) collectionDict.get(valueArray[i]);
                                    Object[] individualValues = values.toArray();
                                    for (int j = 0; j < individualValues.length; j++)
                                        if (!individualValues[j].toString().equals("city_info"))
                                            collectionArray.add(valueArray[i].toString().toUpperCase() + "\n" + individualValues[j]);
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                        android.R.layout.simple_list_item_1, collectionArray);
                                AdapterView.OnItemClickListener messageClickedHandler = new AdapterView.OnItemClickListener() {
                                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                                        changeActivity(token, token_type, parent.getItemAtPosition(position).toString(), v);
                                    }
                                };
                                listView.setAdapter(adapter);
                                listView.setOnItemClickListener(messageClickedHandler);
                            }
                        });
                    }
                } catch (final Exception e) {
                    CollectionActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String[] collectionArray;
                            collectionArray = new String[]{e.getMessage()};
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, collectionArray);
                            listView.setAdapter(adapter);
                        }
                    });
                }
            }
        });

    }
    public void changeActivity(final String token, final String token_type, String selection, final View v) {
        if (contact != null) {

            final String _id = getIntent().getStringExtra("ID");
            final String db = getIntent().getStringExtra("DB");
            final String collection = getIntent().getStringExtra("COLLECTION");

            final String[] db_and_collection = selection.split("\n");

            final MediaType MEDIA_TYPE_JSON
                    = MediaType.parse("application/json");
            Request requestContacts;

            requestContacts = new Request.Builder()
                    .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/new-contact?db=" + db_and_collection[0].toLowerCase() + "&collection=" + db_and_collection[1])
                    .header("Authorization", token_type + " " + token)
                    .post(RequestBody.create(contact, MEDIA_TYPE_JSON))
                    .build();
            Log.d("contact", contact);

            client.newCall(requestContacts).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) {
                    try {
                        if (!response.isSuccessful())
                            throw new IOException(String.valueOf(response));
                        else {
                            Request requestContacts = new Request.Builder()
                                    .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/delete-contact?db=" + db +
                                            "&collection=" + collection + "&_id=" + _id)
                                    .header("Authorization", token_type + " " + token)
                                    .delete()
                                    .build();

                            client.newCall(requestContacts).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(Call call, final Response response) {
                                    try {
                                        if (!response.isSuccessful())
                                            throw new IOException(String.valueOf(response));
                                        else {
                                            CollectionActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Handler handler = new Handler();
                                                    Snackbar.make(v, "Contact Transferred Successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            onBackPressed();
                                                        }
                                                    }, 1000);
                                                }
                                            });
                                        }
                                    } catch (final Exception e) {
                                        CollectionActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        onBackPressed();
                                                    }
                                                }, 1000);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    } catch (final Exception e) {
                        CollectionActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        });
                    }
                }
            });
        } else {
            Intent intent = new Intent(this, ViewingCollectionActivity.class);
            intent.putExtra("ACCESS_TOKEN", token);
            intent.putExtra("TOKEN_TYPE", token_type);
            String[] split = selection.split("\n");
            String db = split[0].toLowerCase();
            String collection = split[1];
            intent.putExtra("DB", db);
            intent.putExtra("COLLECTION", collection);
            startActivity(intent);
        }
    }
}
