package com.trevorhalvorson.quickstatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Trevor Halvorson on 1/6/2016.
 */
public class PermissionDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_permission, null);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(getString(R.string.app_name))
                .setPositiveButton("CLOSE", null)
                .create();
    }
}
