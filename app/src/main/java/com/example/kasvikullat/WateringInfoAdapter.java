package com.example.kasvikullat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class WateringInfoAdapter extends ArrayAdapter<Integer> {

    public WateringInfoAdapter (Context context, ArrayList<Integer> integers) {
        super(context, 0, integers);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.watering_info_spinner_row, parent, false);
        }

        TextView textview = convertView.findViewById(R.id.textView_spinner_wateringInfo);
        Integer currentInt = getItem(position);

        if (currentInt != 0) {
            textview.setText(String.valueOf(currentInt));
        } else {
            textview.setText("Valitse");
        }

        return convertView;
    }
}
