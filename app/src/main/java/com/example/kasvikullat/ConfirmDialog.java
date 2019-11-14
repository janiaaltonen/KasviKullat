package com.example.kasvikullat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmDialog extends DialogFragment {
    private ConfirmDialogListener confirmDialogListener;
    private String title;

    public ConfirmDialog(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        try {
            confirmDialogListener = (ConfirmDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(String.valueOf(getContext()));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage("Lisätäänkö omiin kasveihin?");
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Lisää", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                confirmDialogListener.onPositiveClicked();
            }
        });

        return builder.create();
    }

    public interface ConfirmDialogListener {
        void onPositiveClicked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}
