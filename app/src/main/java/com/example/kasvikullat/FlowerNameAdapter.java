package com.example.kasvikullat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class FlowerNameAdapter extends ArrayAdapter<Flower> {

    public FlowerNameAdapter (Context context, ArrayList<Flower> flowerNames) {
        super(context, 0, flowerNames);
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
                    R.layout.flower_spinner_row, parent, false);
        }

        TextView flowerName = convertView.findViewById(R.id.textView_spinner_flowerName);
        Flower currentText = getItem(position);

        if (currentText != null) {
            flowerName.setText(currentText.getName());
        }

        return convertView;
    }
}

