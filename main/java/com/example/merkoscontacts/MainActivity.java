package com.example.merkoscontacts;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class Token {
    String access_token;
    String token_type;
}

public class MainActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

    private EditText usernameInput, passwordInput;
    private CheckBox keepLoggedIn;
    private Button loginBtn;
    private TextView errorDisplay;
    FileOutputStream outputStream;
    FileInputStream inputStream;
    String filename = "login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        errorDisplay = findViewById(R.id.errorDisplay);

        keepLoggedIn = findViewById(R.id.checkBox);
        keepLoggedIn.setChecked(true);

        usernameInput.requestFocus();

        try {
            File file = new File(getFilesDir(), filename);
            byte[] b;
            b = new byte[(int) file.length()];
            inputStream = openFileInput(filename);
            inputStream.read(b);
            inputStream.close();
            String s = new String(b);
            String[] sa = s.split("\n");
            usernameInput.setText(sa[0]);
            passwordInput.setText(sa[1]);
        } catch (Exception e) {
            Log.d("issues", e.getMessage());
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateUser();
            }
        });
    }

    public void changeActivity(Token token) {
        Intent intent = new Intent(this, CollectionActivity.class);
        intent.putExtra("ACCESS_TOKEN", token.access_token);
        intent.putExtra("TOKEN_TYPE", token.token_type);
        startActivity(intent);
    }

    private void validateUser() {
        final String username = usernameInput.getText().toString();
        final String password = passwordInput.getText().toString();

        String postBody = "username=" + username + "&password=" + password;
        final MediaType MEDIA_TYPE_FORM
                = MediaType.parse("application/x-www-form-urlencoded");

        Request request = new Request.Builder()
                .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/token")
                .post(RequestBody.create(postBody, MEDIA_TYPE_FORM))
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
                        Gson gson = new Gson();
                        final Token token = gson.fromJson(responseString, Token.class);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (keepLoggedIn.isChecked()) {
                                    String fileContents = username + "\n" + password;
                                    try {
                                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                        outputStream.write(fileContents.getBytes());
                                        outputStream.close();
                                    } catch (Exception e) {
                                        Log.d("Issues", e.getMessage());
                                    }
                                }
                                changeActivity(token);
                            }
                        });
                    }
                } catch (final Exception e) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorDisplay.setText(e.getMessage());
                        }
                    });
                }
            }
        });
    }
}