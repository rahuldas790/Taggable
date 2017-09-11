package rahulkumardas.taggableapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

public class ViewPostActivity extends BaseActivity {

    ImageView image;
    TextView tags;
    AutoCompleteTextView newTag;
    Button submit;
    ProgressBar progressBar;
    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = findViewById(R.id.image);
        tags = findViewById(R.id.tags);
        newTag = findViewById(R.id.newTag);
        submit = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progress);
        Bundle b = getIntent().getExtras();
        post = b.getParcelable("post");
        final int position = b.getInt("pos");

        getSupportActionBar().setTitle(post.getName());
        Glide.with(this)
                .load(post.getImageUrl())
                .into(image);
        List<String> tags = post.getTags();
        if (tags.size() >= 1)
            this.tags.setText("Tags : " + tags.get(0));
        else
            this.tags.setText("Tags : No tags");
        for (int i = 1; i < tags.size(); i++) {
            this.tags.append("," + tags.get(i));
        }

        EventListener listener = new EventListener();
        reference.child("tags/" + post.getTagId() + "/tags").addValueEventListener(listener);

        newTag.setThreshold(1);
        newTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 1) {
                    newTag.setAdapter(new ArrayAdapter(ViewPostActivity.this, android.R.layout.simple_list_item_1, users));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        newTag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
                    submit.performClick();
                }
                return false;
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable text = newTag.getText();
                if (text.length() == 0) {
                    newTag.setError("Please tag a person");
                    return;
                }
                if (!text.toString().startsWith("@")) {
                    newTag.setError("Tagging must start with prefix '@'");
                    return;
                }
                int pos = searchUser(text.toString());
                if (pos < 0) {
                    newTag.setError("This is a invalid tag");
                    return;
                }
                submit.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                tagIt(pos, position);
            }
        });
        getAllUsers();
    }

    private class EventListener implements ValueEventListener {
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
            post.setTags(tags);

            if (tags.size() >= 1)
                ViewPostActivity.this.tags.setText("Tags : " + tags.get(0));
            else
                ViewPostActivity.this.tags.setText("Tags : No tags");
            for (int i = 1; i < tags.size(); i++) {
                ViewPostActivity.this.tags.append("," + tags.get(i));
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    int searchUser(String text) {
        return users.indexOf(text);
    }

    void tagIt(final int userPos, final int postPos) {
        String tagId = post.getTagId();
        String key = reference.child("tags/" + tagId + "/tags").push().getKey();
        String userName = users.get(userPos);
        String userid = keys.get(userPos);
        Map<String, String> map = new HashMap<>();
        map.put("id", userid);
        map.put("name", userName.replace("@", ""));
        reference.child("tags/" + tagId + "/tags/" + key).setValue(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                progressBar.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);
                newTag.clearFocus();
                newTag.setText("");

                Toast.makeText(ViewPostActivity.this, "Tagging successful!", Toast.LENGTH_SHORT).show();

                try {
                    OneSignal.postNotification(new JSONObject("{'contents': {'en':'You have been tagged in " + post.getName() + "'}, 'include_player_ids': ['" + notifKeys.get(userPos) + "']}"), null);
                    Log.i("Notification sender", "Success to " + notifKeys.get(userPos));
                } catch (JSONException e) {
                    Log.i("Notification sender", "Failed " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
