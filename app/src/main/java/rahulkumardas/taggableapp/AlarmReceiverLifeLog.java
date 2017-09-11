package rahulkumardas.taggableapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Rahul Kumar Das on 11-09-2017.
 */

public class AlarmReceiverLifeLog extends BroadcastReceiver {

    private static final String TAG = "LL24";
    static Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v(TAG, "Alarm for LifeLog...");

        Intent ll24Service = new Intent(context, NotificationExtender.class);
        context.startService(ll24Service);
    }

}
