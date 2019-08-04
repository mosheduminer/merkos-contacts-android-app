package com.example.merkoscontacts;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class Success {
    String success;
}

public class Search extends AppCompatActivity {

    boolean exact, email;

    TextView tvName, tvCountry, tvCity, tvAddress, tvAge, tvLanguages, tvSpouse, tvMarital, tvChildren, tvKnowledge, tvAffiliation, tvEmail, tvNotes, tvNumber;
    String token, token_type, db, collection, city, query;
    CheckBox exactBox;
    Button searchBtn;

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        token = intent.getStringExtra("ACCESS_TOKEN");
        token_type = intent.getStringExtra("TOKEN_TYPE");
        db = intent.getStringExtra("DB");
        collection = intent.getStringExtra("COLLECTION");
        city = intent.getStringExtra("CITY");
        email = intent.getBooleanExtra("EMAIL", false);

        tvCity = findViewById(R.id.city);
        searchBtn = findViewById(R.id.search_btn);

        if (city != null) {
            tvCity.setText(city);
        }

        if (email) {
            CharSequence text = "Send Email";
            searchBtn.setText(text);
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    query = getInfo();
                    if (query.equals("")) {
                        query = "{}";
                    }
                    String ex;
                    if (exact) {
                        ex = "true";
                    } else {
                        ex = "false";
                    }
                    sendEmail(query, ex);
                }
            });
        } else {
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    query = getInfo();
                    if (query.equals("")) {
                        query = "{}";
                    }
                    changeActivity(query);
                }
            });
        }
    }

    public void changeActivity(String query) {
        Intent intent = new Intent(this, CityView.class);
        intent.putExtra("ACCESS_TOKEN", token);
        intent.putExtra("TOKEN_TYPE", token_type);
        intent.putExtra("DB", db);
        intent.putExtra("COLLECTION", collection);
        intent.putExtra("QUERY", query);
        if (exact) {
            intent.putExtra("EXACT", "true");
        } else {
            intent.putExtra("EXACT", "false");
        }
        startActivity(intent);
    }
    OkHttpClient client = new OkHttpClient.Builder().readTimeout(3000, TimeUnit.MILLISECONDS).build();

    public void sendEmail(String query, String exact) {
        final MediaType MEDIA_TYPE_JSON
                = MediaType.parse("application/json");

        Request requestContacts = new Request.Builder()
                .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/send-csv?db=" + db + "&collection=" + collection + "&exact=" + exact)
                .header("Authorization", token_type + " " + token)
                .post(RequestBody.create(query, MEDIA_TYPE_JSON))
                .build();

        client.newCall(requestContacts).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try (final ResponseBody responseBody = response.body()) {
                    final Map obj = gson.fromJson(responseBody.string(), Map.class);
                    if (!response.isSuccessful())
                        throw new IOException(String.valueOf(response));
                    else {
                        Search.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (obj.get("success").equals("no email provided")) {
                                    Toast.makeText(getApplicationContext(), "No email Registered with this account", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Email Sent Successfully", Toast.LENGTH_LONG).show();
                                    onBackPressed();
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    Search.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("traceback", e.getMessage());
                        }
                    });
                }
            }
        });
    }

    public String getInfo() {
        tvName = findViewById(R.id.name);
        tvCountry = findViewById(R.id.country);
        tvAddress = findViewById(R.id.address);
        tvAge = findViewById(R.id.age);
        tvLanguages = findViewById(R.id.languages);
        tvSpouse = findViewById(R.id.spouse);
        tvMarital = findViewById(R.id.marital_status);
        tvChildren = findViewById(R.id.children);
        tvKnowledge = findViewById(R.id.jewish_knowledge);
        tvAffiliation = findViewById(R.id.affiliation);
        tvEmail = findViewById(R.id.email);
        tvNotes = findViewById(R.id.notes);
        tvNumber = findViewById(R.id.number);
        exactBox = findViewById(R.id.exact);

        JsonObject obj = new JsonObject();
        if (tvAge.length() != 0)
            obj.addProperty("age", tvAge.getText().toString());
        if (tvAffiliation.length() != 0) {
            obj.addProperty("affiliation", String.valueOf(tvAffiliation.getText()));
        }
        if (tvChildren.length() != 0) {
            obj.addProperty("children", String.valueOf(tvChildren.getText()));
        }
        if (tvCountry.length() != 0) {
            obj.addProperty("country", String.valueOf(tvCountry.getText()));
        }
        if (tvCity.length() != 0) {
            obj.addProperty("city", String.valueOf(tvCity.getText()));
        }
        if (tvEmail.length() != 0) {
            obj.addProperty("email", String.valueOf(tvEmail.getText()));
        }
        if (tvKnowledge.length() != 0) {
            obj.addProperty("jewish_knowledge", String.valueOf(tvKnowledge.getText()));
        }
        if (tvLanguages.length() != 0) {
            obj.addProperty("languages", String.valueOf(tvLanguages.getText()));
        }
        if (tvMarital.length() != 0) {
            obj.addProperty("marital_status", String.valueOf(tvMarital.getText()));
        }
        if (tvName.length() != 0) {
            obj.addProperty("name", String.valueOf(tvName.getText()));
        }
        if (tvSpouse.length() != 0) {
            obj.addProperty("spouse", String.valueOf(tvSpouse.getText()));
        }
        if (tvAddress.length() != 0) {
            obj.addProperty("street_address", String.valueOf(tvAddress.getText()));
        }
        if (tvNumber.length() != 0) {
            obj.addProperty("phone", String.valueOf(tvNumber.getText()));
        }
        if (tvNotes.length() != 0) {
            obj.addProperty("notes", String.valueOf(tvNotes.getText()));
        }
        exact = exactBox.isChecked();
        return obj.toString();
    }
}
