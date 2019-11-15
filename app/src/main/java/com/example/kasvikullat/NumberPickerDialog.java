package com.example.kasvikullat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NumberPickerDialog extends DialogFragment {
    private NumberPickerDialogListener valueChangeListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final NumberPicker numberPicker = new NumberPicker(getActivity());

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Kasteluväli");
        builder.setMessage("Esim. 1 = jokapäivä jne.");

        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }). setPositiveButton("Valitse", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int value = numberPicker.getValue();
                valueChangeListener.onPositiveClicked(value);
            }
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public interface NumberPickerDialogListener {
        void onPositiveClicked(int value);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            valueChangeListener = (NumberPickerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }
}
