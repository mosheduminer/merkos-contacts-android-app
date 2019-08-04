package com.example.merkoscontacts;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class Comment {
    String title;
    String info;
}

public class Comments extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;

    FloatingActionButton fab;

    String token, token_type, db, city;

    int skip;

    OkHttpClient client = new OkHttpClient();

    private ArrayList<Comment> comments = new ArrayList<>();

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        final Intent intent = getIntent();

        token = intent.getStringExtra("ACCESS_TOKEN");
        token_type = intent.getStringExtra("TOKEN_TYPE");
        db = intent.getStringExtra("DB");
        city = intent.getStringExtra("CITY");

        skip = 0;

        recyclerView = findViewById(R.id.comments_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(comments);
        recyclerView.setAdapter(mAdapter);
        getComments();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity();
            }
        });
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private ArrayList<Comment> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {

            public CardView cardView;

            public MyViewHolder(CardView cv) {
                super(cv);
                cardView = cv;
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(ArrayList<Comment> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_card, parent, false);
            cv.setCardBackgroundColor(253);
            cv.setContentPadding(25, 25, 25, 25);

            MyViewHolder vh = new MyViewHolder(cv);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Comment cmnt = mDataset.get(position);

            TextView tvTitle = holder.cardView.findViewById(R.id.comment_title);
            tvTitle.setText(cmnt.title);

            TextView tvInfo = holder.cardView.findViewById(R.id.comment_info);
            tvInfo.setText(cmnt.info);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            if (mDataset == null || mDataset.isEmpty()) {
                return 0;
            } else {
                return mDataset.size();
            }
        }
    }

    public void getComments() {
        final MediaType MEDIA_TYPE_JSON
                = MediaType.parse("application/json");

        Request requestContacts = new Request.Builder()
                .url("http://ec2-54-204-161-244.compute-1.amazonaws.com/get-cities-info?db=" + db + "&city=" + city)
                .header("Authorization", token_type + " " + token)
                .post(RequestBody.create("", MEDIA_TYPE_JSON))
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
                        final ArrayList<Comment> comList = new ArrayList<>();
                        final int old = comments.size();
                        int j = 0;
                        final JsonArray collectionArray = gson.fromJson(responseString, JsonArray.class);
                        for (Iterator i = collectionArray.iterator(); i.hasNext(); ) {
                            Map comment = gson.fromJson(i.next().toString(), Map.class);
                            Set keys = comment.keySet();
                            for (Object key : keys) {
                                if (key.toString().equals("_id") || key.toString().equals("city")) {

                                } else {
                                    Comment cmnt = new Comment();
                                    String[] title = key.toString().split("T");
                                    cmnt.title = title[0] + "\t" + title[1];
                                    cmnt.info = comment.get(key).toString();
                                    comList.add(cmnt);
                                }
                            }
                        }

                        Comments.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < comList.size(); i++) {
                                    comments.add(comList.get(i));
                                    mAdapter.notifyItemInserted(old + i);
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.d("Traceback", e.getMessage());
                }
            }
        });
    }

    public void changeActivity() {
        Intent intent1 = new Intent(this, AddComment.class);
        intent1.putExtra("ACCESS_TOKEN", token);
        intent1.putExtra("TOKEN_TYPE", token_type);
        intent1.putExtra("DB", db);
        intent1.putExtra("CITY", city);
        startActivity(intent1);
    }
}
