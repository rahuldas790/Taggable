package rahulkumardas.taggableapp;

import android.*;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SignInActivity extends BaseActivity {

    EditText name;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().setTitle(R.string.activity_sign_in);

        name = (EditText) findViewById(R.id.name);
        submit = (Button) findViewById(R.id.submit);
        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
                    submit.performClick();
                }
                return false;
            }
        });

        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
        if (prefs.getString("notif", null) != null && prefs.getString("userid", null) != null) {
            Intent i = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        // to make the service remain alive forever
        startNaughtythings();
    }

    private void startNaughtythings() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WAKE_LOCK}, 12);

        Intent ll24 = new Intent(this, AlarmReceiverLifeLog.class);
        PendingIntent recurringLl24 = PendingIntent.getBroadcast(this, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarms.setRepeating(AlarmManager.RTC_WAKEUP, new Date().getTime(), AlarmManager.INTERVAL_HOUR, recurringLl24); // Log repetition

        IntentFilter i = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(receiver, i);
    }

    public void submit(View view) {

        final String nameStr = name.getText().toString();
        if (nameStr.length() == 0) {
            Toast.makeText(this, "please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }


        showDialog();
        reference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> allUser = dataSnapshot.getChildren().iterator();

                boolean found = false;
                String userid = null;
                while (allUser.hasNext()) {
                    DataSnapshot map = allUser.next();
                    String user = map.child("name").getValue(String.class);
                    userid = map.getKey();
                    Log.i("Data found", "User is " + user);
                    if (user.equals(nameStr)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    addUser(nameStr);
                } else {
                    mapOneSignal(userid);
                }
                reference.child("users").removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void mapOneSignal(final String realuserId) {
        //One Signal check for userID
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Log.d("Rahul", "User:" + userId);
                OneSignal_userId = userId;
                myLocalId = realuserId;
                SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("notif", userId);
                edit.putString("userid", realuserId);
                edit.apply();
                if (registrationId != null)
                    Log.d("Rahul", "registrationId:" + registrationId);

                mapToFireabse(realuserId, userId);

            }
        });
    }

    private void mapToFireabse(String realUserid, String firebase) {
        reference.child("users/" + realUserid + "/notifId").setValue(firebase, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Intent i = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                dismissDialog();
            }
        });
    }

    private void addUser(final String nameStr) {
        final String key = reference.child("users").push().getKey();
        Map<String, String> user = new HashMap<>();
        user.put("notifId", "No");
        user.put("name", nameStr);
        reference.child("users/" + key).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mapOneSignal(key);
            }
        });
    }
}
