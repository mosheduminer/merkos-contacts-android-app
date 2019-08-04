package com.example.merkoscontacts;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonArray;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AddComment extends AppCompatActivity {

    String token, token_type, db, city;
    Button button;
    EditText info;

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        Intent intent = getIntent();

        token = intent.getStringExtra("ACCESS_TOKEN");
        token_type = intent.getStringExtra("TOKEN_TYPE");
        db = intent.getStringExtra("DB");
        city = intent.getStringExtra("CITY");

        button = findViewById(R.id.saveBtn);
        info = findViewById(R.id.editText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveComment(info.getText().toString());
            }
        });
    }

    public void saveComment(String comment) {
        final MediaType MEDIA_TYPE_JSON
                = MediaType.parse("application/json");

        JsonArray temp = new JsonArray();
        temp.add(comment);

        Request requestContacts = new Request.Builder()
                .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/new-city-info?db=" + db + "&city=" + city)
                .header("Authorization", token_type + " " + token)
                .post(RequestBody.create(temp.toString(), MEDIA_TYPE_JSON))
                .build();

        client.newCall(requestContacts).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try (final ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException(String.valueOf(response));
                    else {
                        AddComment.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onBackPressed();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.d("Traceback", e.getMessage());
                }
            }
        });
    }
}
