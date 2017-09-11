package rahulkumardas.taggableapp;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;

import java.math.BigInteger;

/**
 * Created by Rahul Kumar Das on 10-09-2017.
 */

public class NotificationExtender extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // Sets the background notification color to Green on Android 5.0+ devices.
                return builder.setColor(new BigInteger("FF00FF00", 16).intValue());
            }
        };

        OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
        Log.d("OneSignalExample", "Notification displayed with id: " + displayedResult.androidNotificationId);
        Log.i("OneSignalExample", "Notification displayed with id: " + receivedResult.payload.body);
        Log.i("OneSignalExample", "Notification displayed with id: " + receivedResult.payload.additionalData);
        Log.i("OneSignalExample", "Notification displayed with id: " + receivedResult.payload.rawPayload);

        return true;

    }


}
