package com.example.merkoscontacts;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.merkoscontacts.model.Parent;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


class ContactInfo {
    String _id;
    String name;
    Number age;
    String jewish_knowledge;
    String country;
    String city;
    String street_address;
    Number[] phone;
    String email;
    String affiliation;
    String children;
    String marital_status;
    String spouse;
    String languages;
    String[] notes;
}

public class ViewingCollectionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String token;
    String token_type;
    String db;
    String collection;

    String query = "{}";
    String exact = "false";
    int skip = 0;

    public ArrayList<ContactParentObject> contacts = new ArrayList<>();

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;

    private final Gson gson = new Gson();

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing_collection);

        Intent intent = getIntent();

        token = intent.getStringExtra("ACCESS_TOKEN");
        token_type = intent.getStringExtra("TOKEN_TYPE");
        db = intent.getStringExtra("DB");
        collection = intent.getStringExtra("COLLECTION");
        this.setTitle(collection.toUpperCase());

        recyclerView = findViewById(R.id.contacts_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(this, contacts);
        recyclerView.setAdapter(mAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                skip = totalItemsCount;
                getContacts();
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        final NavigationView nav_view = findViewById(R.id.nav_view);
        View hView = nav_view.getHeaderView(0);
        TextView nav_name = hView.findViewById(R.id.nav_header);
        nav_name.setText(collection.toUpperCase());


        String postBody = "";

        final MediaType MEDIA_TYPE_JSON
                = MediaType.parse("application/json");

        Request requestCities = new Request.Builder()
                .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/get-cities?db=" + db + "&collection=" + collection)
                .header("Authorization", token_type + " " + token)
                .post(RequestBody.create(postBody, MEDIA_TYPE_JSON))
                .build();

        client.newCall(requestCities).enqueue(new Callback() {
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
                        ViewingCollectionActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Menu menu = nav_view.getMenu();
                                JsonArray collectionArray = gson.fromJson(responseString, JsonArray.class);
                                for (Iterator i = collectionArray.iterator(); i.hasNext(); ) {
                                    String item = i.next().toString();
                                    menu.add(item.substring(1, item.length()-1));
                                }
                                NavigationView.OnNavigationItemSelectedListener messageClickedHandler = new NavigationView.OnNavigationItemSelectedListener() {
                                    @Override
                                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                                        if (menuItem.toString().equals("Advanced Search")) {
                                            advancedSearch();
                                        } else if (menuItem.toString().equals("Send Email")) {
                                            sendEmail();
                                            onBackPressed();
                                        } else{
                                            changeActivity(menuItem.toString());
                                        }
                                        return false;
                                    }
                                };
                                nav_view.setNavigationItemSelectedListener(messageClickedHandler);
                            }
                        });
                    }
                } catch (final Exception e) {
                    ViewingCollectionActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String[] collectionArray;
                            collectionArray = new String[]{e.getMessage()};
                            Menu menu = nav_view.getMenu();
                            menu.add(R.id.city_group, R.id._label_city, 0, collectionArray[0]);
                        }
                    });
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(this);

        getContacts();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Update(null, false);
            }
        });
    }

    @Override
    public boolean onSearchRequested() {
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.viewing_collection, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                int current = contacts.size();
                contacts.clear();
                mAdapter.notifyParentRangeRemoved(0, current);

                JsonObject obj;
                obj = new JsonObject();
                obj.addProperty("name", search);
                query = obj.toString();
                Log.d("obj", obj.toString());
                skip = 0;
                getContacts();
                if (contacts.isEmpty()) {
                    obj = new JsonObject();
                    obj.addProperty("street_address", search);
                    query = obj.toString();
                }
                getContacts();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.getQuery();

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeActivity(String city) {
        Intent intent = new Intent(this, CityView.class);
        intent.putExtra("ACCESS_TOKEN", token);
        intent.putExtra("TOKEN_TYPE", token_type);
        intent.putExtra("DB", db);
        intent.putExtra("COLLECTION", collection);
        intent.putExtra("CITY", city);
        startActivity(intent);
    }

    public void getContacts() {

        final MediaType MEDIA_TYPE_JSON
                = MediaType.parse("application/json");

        Request requestContacts = new Request.Builder()
                .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/get-contact-by-attr?db=" + db + "&collection=" + collection + "&exact=" + exact + "&skip=" + skip)
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
                    final String responseString = responseBody.string();
                    if (!response.isSuccessful())
                        throw new IOException(String.valueOf(response));
                    else {
                        ArrayList conList = new ArrayList();
                        final JsonArray collectionArray = gson.fromJson(responseString, JsonArray.class);
                        for (Iterator i = collectionArray.iterator(); i.hasNext(); ) {
                            ContactInfo con = gson.fromJson(i.next().toString(), ContactInfo.class);
                            conList.add(con);
                        }
                        final int old = contacts.size();
                        final List<ContactParentObject> list = InsertData(conList);

                        ViewingCollectionActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < collectionArray.size(); i++) {
                                    contacts.add(list.get(i));
                                    mAdapter.notifyParentInserted(old + i);
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    ViewingCollectionActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("traceback", e.getMessage());
                        }
                    });
                }
            }
        });
    }

    private ArrayList InsertData(List<ContactInfo> conList) {
        ArrayList<ContactParentObject> aTitles = new ArrayList<>();
        for (int i = 0; i < conList.size(); i++) {
            ContactParentObject par = new ContactParentObject(conList.get(i).name);
            aTitles.add(par);
        }
        List<ContactParentObject> lTitles = aTitles;
        ArrayList parentObjects = new ArrayList<>();
        for (int i = 0; i < conList.size(); i++) {
            List<Object> childList = new ArrayList<>();
            ContactChild contactChild = new ContactChild(conList.get(i));
            childList.add(contactChild);
            lTitles.get(i).setChildObjectList(childList);
            parentObjects.add(lTitles.get(i));
        }
        return parentObjects;
    }

    public void Update(String ID, boolean exists) {
        Intent intent = new Intent(this, UpdateContact.class);
        intent.putExtra("ACCESS_TOKEN", token);
        intent.putExtra("TOKEN_TYPE", token_type);
        intent.putExtra("DB", db);
        intent.putExtra("COLLECTION", collection);
        intent.putExtra("CONTACT_ID", ID);
        intent.putExtra("EXISTS", exists);
        startActivity(intent);
    }

    public void advancedSearch() {
        Intent intent = new Intent(this, Search.class);
        intent.putExtra("ACCESS_TOKEN", token);
        intent.putExtra("TOKEN_TYPE", token_type);
        intent.putExtra("DB", db);
        intent.putExtra("COLLECTION", collection);
        startActivity(intent);
    }

    public void sendEmail() {
        Intent intent = new Intent(this, Search.class);
        intent.putExtra("ACCESS_TOKEN", token);
        intent.putExtra("TOKEN_TYPE", token_type);
        intent.putExtra("DB", db);
        intent.putExtra("COLLECTION", collection);
        intent.putExtra("EMAIL", true);
        startActivity(intent);
    }


    public class MyAdapter extends ExpandableRecyclerAdapter {

        LayoutInflater inflater;

        public MyAdapter(Context context, List<ContactParentObject> parentItemList) {
            super(parentItemList);
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public com.example.merkoscontacts.ContactParentViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
            View view = inflater.inflate(R.layout.contact_card, parentViewGroup, false);
            final ContactParentViewHolder contactParentViewHolder = new ContactParentViewHolder(view);
            return contactParentViewHolder;
        }

        @NonNull
        @Override
        public com.example.merkoscontacts.ContactsChildViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
            View view = inflater.inflate(R.layout.child, childViewGroup, false);
            ContactsChildViewHolder contactsChildViewHolder;
            contactsChildViewHolder = new ContactsChildViewHolder(view);
            return contactsChildViewHolder;
        }

        @Override
        public void onBindParentViewHolder(@NonNull ParentViewHolder parentViewHolder, int parentPosition, @NonNull Parent parent) {
            ContactParentViewHolder parHolder = (ContactParentViewHolder) parentViewHolder;
            ContactParentObject par = (ContactParentObject) parent;
            parHolder.mTitleTextView.setText(par.title);
        }

        @Override
        public void onBindChildViewHolder(@NonNull ChildViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull Object C) {
            String phones;
            String text = "";

            ContactsChildViewHolder contactsChildViewHolder = (ContactsChildViewHolder) childViewHolder;
            final ContactChild child = (ContactChild) C;
            if (child.country != null)
                contactsChildViewHolder.country.setText(child.country);
            else
                contactsChildViewHolder.country.setVisibility(View.GONE);
            if (child.city != null) {
                contactsChildViewHolder.city.setText(child.city);
            }
            else {
                contactsChildViewHolder.city.setVisibility(View.GONE);
            }
            if (child.street_address != null)
                contactsChildViewHolder.address.setText(child.street_address);
            else {
                contactsChildViewHolder.address.setVisibility(View.GONE);
            }
            if (child.jewish_knowledge != null) {
                text = "Jewish Knowledge: " + child.jewish_knowledge;
                contactsChildViewHolder.jewish_knowledge.setText(text);
            } else {
                contactsChildViewHolder.jewish_knowledge.setVisibility(View.GONE);
            }
            if (child.email != null) {
                text = "Email: " + child.email;
                contactsChildViewHolder.email.setText(text);
            } else {
                contactsChildViewHolder.email.setVisibility(View.GONE);
            }
            if (child.affiliation != null) {
                text = "Affiliation: " + child.affiliation;
                contactsChildViewHolder.affiliation.setText(text);
            } else {
                contactsChildViewHolder.affiliation.setVisibility(View.GONE);
            }
            if (child.children != null) {
                text = "Children: " + child.children;
                contactsChildViewHolder.children.setText(text);
            } else {
                contactsChildViewHolder.children.setVisibility(View.GONE);
            }
            if (child.marital_status != null) {
                text = "Marital Status: " + child.marital_status;
                contactsChildViewHolder.marital_status.setText(text);
            } else {
                contactsChildViewHolder.marital_status.setVisibility(View.GONE);
            }
            if (child.spouse != null) {
                text = "Spouse: " + child.spouse;
                contactsChildViewHolder.spouse.setText(text);
            } else {
                contactsChildViewHolder.spouse.setVisibility(View.GONE);
            }
            if (child.languages != null) {
                text = "Languages: " + child.languages;
                contactsChildViewHolder.languages.setText(text);
            } else {
                contactsChildViewHolder.languages.setVisibility(View.GONE);
            }
            if (child.phone != null) {
                phones = "";
                for (int i = 0; i < child.phone.length; i++) {
                    if (i != child.phone.length -1)
                        phones += child.phone[i].toString() + ", ";
                    else
                        phones += child.phone[i].toString();
                }
                phones = "Phone: " + phones;
                contactsChildViewHolder.phone.setText(phones);
            } else {
                contactsChildViewHolder.phone.setVisibility(View.GONE);
            }
            if (child.notes != null) {
                if (child.notes.length == 1)
                    text = child.notes[0];
                else {
                    text = "";
                    for (int i = 0; i < child.notes.length; i++) {
                        if (i-1 != child.notes.length)
                            text += child.notes[i] +"\n\t\t\t\t\t";
                        else
                            text += child.notes[i];
                    }
                }
                text = "Notes: " + text;
                contactsChildViewHolder.notes.setText(text);
            } else {
                contactsChildViewHolder.notes.setVisibility(View.GONE);
            }
            if (child.age != null) {
                text = "Age: " + child.age;
                contactsChildViewHolder.age.setText(text);
            } else {
                contactsChildViewHolder.age.setVisibility(View.GONE);
            }
            contactsChildViewHolder.updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewingCollectionActivity.this.Update(child._id, true);
                    onBackPressed();
                }
            });
        }
    }
}