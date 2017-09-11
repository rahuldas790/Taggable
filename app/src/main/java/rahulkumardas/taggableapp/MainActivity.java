package rahulkumardas.taggableapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    private List<Post> list;
    ProgressBar progressBar;
    PostAdapter adapter;
    TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.activity_posts);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        progressBar = (ProgressBar) findViewById(R.id.progress);

        list = new ArrayList<>();
        if (getIntent().hasExtra("id")) {
            fromNotification = true;
            id = getIntent().getStringExtra("id");
            showDialog("Please wait...");
        } else {
            fromNotification = false;
        }

        adapter = new PostAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(5));

        users = new ArrayList<>();
        loadPosts();
        getAllUsers();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View ll = navigationView.getHeaderView(0);
        login = ll.findViewById(R.id.name);

    }


    String id = "";
    boolean fromNotification = false;


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
            final SharedPreferences.Editor edit = prefs.edit();
            showDialog();
            reference.child("users/" + myLocalId + "/notifId").setValue("No", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    dismissDialog();
                    edit.remove("notif");
                    edit.remove("userid");
                    edit.apply();
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getAllUsers() {
        reference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                keys = new ArrayList<String>();
                users = new ArrayList<String>();
                notifKeys = new ArrayList<String>();
                Iterator<DataSnapshot> allUser = dataSnapshot.getChildren().iterator();
                boolean found = false;
                while (allUser.hasNext()) {
                    DataSnapshot user = allUser.next();
                    String name = user.child("name").getValue(String.class);
                    String key = user.getKey();
                    String notifKey = user.child("notifId").getValue(String.class);
                    users.add("@" + name);
                    keys.add(key);
                    notifKeys.add(notifKey);

                    Log.i("Data found", "User is " + name);
                }
                try {
                    login.setText(users.get(keys.indexOf(myLocalId)).replace("@", ""));
                } catch (Exception e) {
                    login.setText("Unknown");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadPosts() {
        reference.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> allposts = dataSnapshot.getChildren().iterator();
                progressBar.setVisibility(View.GONE);
                list.clear();
                while (allposts.hasNext()) {
                    DataSnapshot post = allposts.next();
                    Post post1 = new Post();
                    post1.setPostId(post.getKey());
                    post1.setName(post.child("title").getValue(String.class));
                    post1.setImageUrl(post.child("image").getValue(String.class));
                    String tagid = post.child("tagid").getValue(String.class);
                    post1.setTagId(tagid);
                    list.add(post1);
                    EventListener listener = new EventListener(list.size() - 1);
                    reference.child("tags/" + tagid + "/tags").addValueEventListener(listener);

                    if (fromNotification && id.equals(post1.getPostId())) {
                        dismissDialog();
                        Intent i = new Intent(MainActivity.this, ViewPostActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable("post", post1);
                        b.putInt("pos", list.size() - 1);
                        i.putExtras(b);
                        startActivity(i);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class EventListener implements ValueEventListener {
        public EventListener(int pos) {
            this.pos = pos;
        }

        int pos;

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<String> tags = new ArrayList<>();
            Iterator<DataSnapshot> allTags = dataSnapshot.getChildren().iterator();
            while (allTags.hasNext()) {
                DataSnapshot tag = allTags.next();
                tags.add(tag.child("name").getValue(String.class));
            }
            if (tags.size() == 0) {
                tags.add("No tags");
            }
            list.get(pos).setTags(tags);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
        if (OneSignal_userId == null || myLocalId == null) {
            OneSignal_userId = prefs.getString("notif", null);
            myLocalId = prefs.getString("userid", null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class PostAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_post, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
            final MyHolder holder = (MyHolder) viewHolder;
            holder.title.setText(list.get(position).getName());
            List<String> tags = list.get(position).getTags();
            if (list.get(position).getTags().size() != 0)
                holder.tag.setText("Tags : " + tags.get(0));
            for (int i = 1; i < tags.size(); i++) {
                holder.tag.append(", " + tags.get(i));
            }
            Glide.with(MainActivity.this)
                    .load(list.get(position).getImageUrl())
                    .into(holder.imageView);
            holder.newTag.setThreshold(1);
            holder.newTag.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() >= 1) {
                        holder.newTag.setAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, users));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            holder.newTag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    holder.submit.performClick();
                    return false;
                }
            });
            holder.submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Editable text = holder.newTag.getText();
                    if (text.length() == 0) {
                        holder.newTag.setError("Please tag a person");
                        return;
                    }
                    if (!text.toString().startsWith("@")) {
                        holder.newTag.setError("Tagging must start with prefix '@'");
                        return;
                    }
                    int pos = searchUser(text.toString());
                    if (pos < 0) {
                        holder.newTag.setError("This is a invalid tag");
                        return;
                    }
                    holder.submit.setVisibility(View.GONE);
                    holder.progressBar.setVisibility(View.VISIBLE);
                    tagIt(pos, position, holder);
                }
            });

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this, ViewPostActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("post", list.get(position));
                    b.putInt("pos", position);
                    i.putExtras(b);
                    startActivity(i);
                }
            });
        }

        void tagIt(final int userPos, final int postPos, final MyHolder holder) {
            String tagId = list.get(postPos).getTagId();
            String key = reference.child("tags/" + tagId + "/tags").push().getKey();
            String userName = users.get(userPos);
            String userid = keys.get(userPos);
            Map<String, String> map = new HashMap<>();
            map.put("id", userid);
            map.put("name", userName.replace("@", ""));
            reference.child("tags/" + tagId + "/tags/" + key).setValue(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.submit.setVisibility(View.VISIBLE);
                    holder.newTag.clearFocus();
                    holder.newTag.setText("");
                    Toast.makeText(MainActivity.this, "Tagging successful!", Toast.LENGTH_SHORT).show();

                    try {

                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'You have been tagged in \"" + list.get(postPos).getName() + "\"'},'data':{'abc': '" + list.get(postPos).getPostId() + "', 'sender': 'not required'}, 'include_player_ids': ['" + notifKeys.get(userPos) + "']}"), null);
                        Log.i("Notification sender", "Success to " + notifKeys.get(userPos));
                    } catch (JSONException e) {
                        Log.i("Notification sender", "Failed " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }

        int searchUser(String text) {
            return users.indexOf(text);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {

            TextView title, tag;
            AutoCompleteTextView newTag;
            ImageView imageView;
            Button submit;
            ProgressBar progressBar;

            public MyHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                tag = itemView.findViewById(R.id.tag);
                newTag = itemView.findViewById(R.id.newTag);
                submit = itemView.findViewById(R.id.submit);
                imageView = itemView.findViewById(R.id.image);
                progressBar = itemView.findViewById(R.id.progress);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

