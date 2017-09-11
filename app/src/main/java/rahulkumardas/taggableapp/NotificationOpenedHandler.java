package rahulkumardas.taggableapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rahul Kumar Das on 10-09-2017.
 */

class NotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        String customKey;

        if (data != null) {
            customKey = data.optString("customkey", null);
            if (customKey != null)
                Log.i("OneSignalExample", "customkey set with value: " + customKey);
        }

        if (actionType == OSNotificationAction.ActionType.ActionTaken)
            Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

        Log.i("OneSignalExample", "Notification displayed with id: " + result.notification.payload.additionalData);
        JSONObject object = result.notification.payload.additionalData;
        String id = null;
        try {
            id = object.getString("abc");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // The following can be used to open an Activity of your choice.
        // Replace - getApplicationContext() - with any Android Context.
        Context context = App.getContext();
        SharedPreferences prefs = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        if (prefs.getString("notif", null) != null && prefs.getString("userid", null) != null) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("id", id);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
        //   if you are calling startActivity above.
     /*
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>
     */
    }
}
