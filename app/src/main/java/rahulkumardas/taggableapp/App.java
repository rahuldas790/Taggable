package rahulkumardas.taggableapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.onesignal.OneSignal;

/**
 * Created by Rahul Kumar Das on 10-09-2017.
 */

public class App extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationReceivedHandler(new NotificationReceivedHandler())
                .setNotificationOpenedHandler(new NotificationOpenedHandler())
                .init();

        // Call syncHashedEmail anywhere in your app if you have the user's email.
        // This improves the effectiveness of OneSignal's "best-time" notification scheduling feature.
        // OneSignal.syncHashedEmail(userEmail);
    }

    public static Context getContext(){
        return context;
    }
}
