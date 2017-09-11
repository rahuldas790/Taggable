package rahulkumardas.taggableapp;

import android.*;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;

/**
 * Created by Rahul Kumar Das on 10-09-2017.
 */

public class BaseActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();
    MyProgressDialog dialog;
    String OneSignal_userId;
    String myLocalId;
    List<String> keys;
    List<String> notifKeys;
    List<String> users;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());

            Intent ll24Service = new Intent(context, NotificationExtender.class);
            context.startService(ll24Service);
            // do something
            WakeLocker.release();
        }
    };


    void showDialog() {
        dialog = new MyProgressDialog();
        dialog.show(getSupportFragmentManager(), "");
    }

    void showDialog(String message) {
        dialog = MyProgressDialog.getInstance(message);
        dialog.show(getSupportFragmentManager(), "");
    }

    void dismissDialog() {
        if (dialog == null)
            return;

        dialog.dismiss();
    }
}
