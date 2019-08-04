package com.example.merkoscontacts;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UpdateContact extends AppCompatActivity {

    Intent intent;

    String token, token_type, db, collection, _id, city;

    TextView tvID, tvName, tvCountry, tvCity, tvAddress, tvAge, tvLanguages, tvSpouse, tvMarital, tvChildren, tvKnowledge, tvAffiliation, tvEmail;
    LinearLayout nsvPhones, nsvNotes;
    ImageButton phoneBtn, noteBtn;
    Button saveBtn, transferBtn, deleteBtn;


    boolean exists;

    ArrayList<View> notes = new ArrayList<>();
    ArrayList<View> phones = new ArrayList<>();

    Handler handler = new Handler();

    private final OkHttpClient client = new OkHttpClient();

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

        intent = getIntent();

        token = intent.getStringExtra("ACCESS_TOKEN");
        token_type = intent.getStringExtra("TOKEN_TYPE");
        db = intent.getStringExtra("DB");
        collection = intent.getStringExtra("COLLECTION");
        _id = intent.getStringExtra("CONTACT_ID");
        city = intent.getStringExtra("CITY");
        exists = intent.getBooleanExtra("EXISTS", false);

        tvID = findViewById(R.id.ID);
        tvName = findViewById(R.id.name);
        tvCountry = findViewById(R.id.country);
        tvCity = findViewById(R.id.city);
        tvAddress = findViewById(R.id.address);
        tvAge = findViewById(R.id.age);
        tvLanguages = findViewById(R.id.languages);
        tvSpouse = findViewById(R.id.spouse);
        tvMarital = findViewById(R.id.marital_status);
        tvChildren = findViewById(R.id.children);
        tvKnowledge = findViewById(R.id.jewish_knowledge);
        tvAffiliation = findViewById(R.id.affiliation);
        tvEmail = findViewById(R.id.email);
        nsvPhones = findViewById(R.id.phone);
        nsvNotes = findViewById(R.id.notes);
        phoneBtn = findViewById(R.id.phoneButton);
        noteBtn = findViewById(R.id.noteButton);
        saveBtn = findViewById(R.id.search_btn);
        transferBtn = findViewById(R.id.transferBtn);
        deleteBtn = findViewById(R.id.delete_button);

        final LayoutInflater inflater = LayoutInflater.from(this);

        String postBody = "";

        final MediaType MEDIA_TYPE_JSON
                = MediaType.parse("application/json");

        if (exists) {
            Request requestContacts = new Request.Builder()
                    .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/get-contact-by-id?db=" + db + "&collection=" + collection + "&_id=" + _id)
                    .header("Authorization", token_type + " " + token)
                    .post(RequestBody.create(postBody, MEDIA_TYPE_JSON))
                    .build();

            client.newCall(requestContacts).enqueue(new Callback() {
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
                            UpdateContact.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JsonArray collectionArray = gson.fromJson(responseString, JsonArray.class);
                                    ContactInfo contact = gson.fromJson(collectionArray.get(0), ContactInfo.class);
                                    String text;
                                    tvID.setText(contact._id);
                                    tvName.setText(contact.name);
                                    if (contact.country != null)
                                        tvCountry.setText(contact.country);
                                    if (contact.city != null)
                                        tvCity.setText(contact.city);
                                    if (contact.street_address != null)
                                        tvAddress.setText(contact.street_address);
                                    if (contact.age != null)
                                        tvAge.setText(contact.age.toString());
                                    if (contact.languages != null) {
                                        tvLanguages.setText(contact.languages);
                                    }
                                    if (contact.spouse != null) {
                                        tvSpouse.setText(contact.spouse);
                                    }
                                    if (contact.marital_status != null) {
                                        tvMarital.setText(contact.marital_status);
                                    }
                                    if (contact.children != null) {
                                        tvChildren.setText(contact.children);
                                    }
                                    if (contact.affiliation != null) {
                                        tvAffiliation.setText(contact.affiliation);
                                    }
                                    if (contact.jewish_knowledge != null) {
                                        tvKnowledge.setText(contact.jewish_knowledge);
                                    }
                                    if (contact.email != null) {
                                        tvEmail.setText(contact.email);
                                    }
                                    if (contact.phone != null && contact.phone.length != 0) {
                                        for (int i = 0; i < contact.phone.length; i++) {
                                            View v = inflater.inflate(R.layout.phones, nsvPhones, false);
                                            text = contact.phone[i].toString();
                                            ((EditText) v).setText(text);
                                            nsvPhones.addView(v);
                                            phones.add(v);
                                        }
                                    } else {
                                        View n = inflater.inflate(R.layout.phones, nsvPhones, false);
                                        nsvPhones.addView(n);
                                        phones.add(n);
                                    }
                                    if (contact.notes != null && contact.notes.length != 0) {
                                        for (int i = 0; i < contact.notes.length; i++) {
                                            View v = inflater.inflate(R.layout.notes, nsvNotes, false);
                                            text = contact.notes[i];
                                            ((EditText) v).setText(text);
                                            nsvNotes.addView(v);
                                            notes.add(v);
                                        }
                                    } else {
                                        View v = inflater.inflate(R.layout.notes, nsvNotes, false);
                                        nsvNotes.addView(v);
                                        notes.add(v);
                                    }
                                }
                            });
                        }
                    } catch (final Exception e) {
                        UpdateContact.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = new Toast(getApplicationContext());
                                toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                }
            });
        } else {
            deleteBtn.setVisibility(View.INVISIBLE);
            transferBtn.setVisibility(View.INVISIBLE);
            View p = inflater.inflate(R.layout.phones, nsvPhones, false);
            nsvPhones.addView(p);
            phones.add(p);
            View n = inflater.inflate(R.layout.notes, nsvNotes, false);
            nsvNotes.addView(n);
            notes.add(n);
        }

        noteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View n = inflater.inflate(R.layout.notes, nsvNotes, false);
                nsvNotes.addView(n);
                notes.add(n);
            }
        });
        phoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View n = inflater.inflate(R.layout.phones, nsvPhones, false);
                nsvPhones.addView(n);
                phones.add(n);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                UpdateContact.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                onBackPressed();
                                            }
                                        }, 500);
                                    }
                                });
                            }
                        } catch (final Exception e) { }
                    }
                });
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String postBody = gson.toJson(getInfo());

                final MediaType MEDIA_TYPE_JSON
                        = MediaType.parse("application/json");
                final Request requestContacts;

                if (exists) {
                    requestContacts = new Request.Builder()
                            .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/update-contact?db=" + db + "&collection=" + collection + "&_id=" + _id)
                            .header("Authorization", token_type + " " + token)
                            .put(RequestBody.create(postBody, MEDIA_TYPE_JSON))
                            .build();

                } else {
                    requestContacts = new Request.Builder()
                            .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/new-contact?db=" + db + "&collection=" + collection)
                            .header("Authorization", token_type + " " + token)
                            .post(RequestBody.create(postBody, MEDIA_TYPE_JSON))
                            .build();

                }
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
                                UpdateContact.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (exists)
                                            Snackbar.make(v, "Contact Updated Successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        else
                                            Snackbar.make(v, "Contact Created Successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
                            UpdateContact.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                }
                            });
                        }
                    }
                });
            }
        });
        transferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateContact.this, CollectionActivity.class);
                intent.putExtra("ACCESS_TOKEN", token);
                intent.putExtra("TOKEN_TYPE", token_type);
                intent.putExtra("DB", db);
                intent.putExtra("COLLECTION", collection);
                intent.putExtra("CONTACT", gson.toJson(getInfo()));
                intent.putExtra("ID", _id);
                startActivity(intent);
                onBackPressed();
            }
        });
    }

    public ContactInfo getInfo() {
        ContactInfo obj = new ContactInfo();
        if (tvAge.length() != 0)
            obj.age = Integer.parseInt(tvAge.getText().toString());
        else
            obj.age = null;
        obj.affiliation = String.valueOf(tvAffiliation.getText());
        obj.city = String.valueOf(tvCity.getText());
        obj.children = String.valueOf(tvChildren.getText());
        obj.country = String.valueOf(tvCountry.getText());
        obj.email = String.valueOf(tvEmail.getText());
        obj.jewish_knowledge = String.valueOf(tvKnowledge.getText());
        obj.languages = String.valueOf(tvLanguages.getText());
        obj.marital_status = String.valueOf(tvMarital.getText());
        obj.name = String.valueOf((tvName).getText());
        obj.spouse = String.valueOf(tvSpouse.getText());
        obj.street_address = String.valueOf(tvAddress.getText());
        if (((EditText) phones.get(0)).getText().length() > 0) {
            obj.phone = new Number[phones.size()];
            for (int i = 0; i < phones.size(); i++) {
                String num = ((EditText) phones.get(i)).getText().toString();
                if (num.length() > 0)
                    obj.phone[i] = Long.parseLong(num);
            }
        } else {
            obj.phone = new Number[0];
        }
        if (((EditText) notes.get(0)).getText().length() > 0) {
            obj.notes = new String[notes.size()];
            for (int i = 0; i < notes.size(); i++) {
                String note = ((EditText) notes.get(i)).getText().toString();
                if (note.length() > 0)
                    obj.notes[i] = note;
            }
        } else {
            obj.notes = new String[0];
        }
        return obj;
    }
}
