package rahulkumardas.taggableapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Rahul Kumar Das on 10-09-2017.
 */

public class MyProgressDialog extends DialogFragment {

    String message = "Loading...";
    TextView text;

    public MyProgressDialog() {

    }

    public static MyProgressDialog getInstance(String message) {
        MyProgressDialog dialog = new MyProgressDialog();
        Bundle b = new Bundle();
        b.putString("message", message);
        dialog.setArguments(b);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if (b != null)
            message = b.getString("message", "Loading...");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_dialog, null);
        text = view.findViewById(R.id.message);
        text.setText(message);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(R.style.MyAnimation_Window, R.style.AppTheme);
        return super.onCreateDialog(savedInstanceState);
    }
}
