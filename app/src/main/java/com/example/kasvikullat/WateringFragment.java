package com.example.kasvikullat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class WateringFragment extends Fragment implements View.OnClickListener {
    private View view;
    private TextView addWatering, nextWateringDate, prevWateringDate;
    private LinearLayout wateringDetails;
    private Flower flower;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    private SharedPreferences sharedPref;
    private String userUid, docId;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String FLOWER = "flower";
    private static final String FLOWER_ID = "flowerId";
    private static final String UUID = "uuid";
    private static final String INFOTEXT = "Ei aiempia kastelukertoja";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_watering, container, false);


        sharedPref = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString(FLOWER,"");
        flower = gson.fromJson(json, Flower.class);

        userUid = sharedPref.getString(UUID,"");
        docId = sharedPref.getString(FLOWER_ID,"");

        docRef = db.collection("users").document(userUid).collection("flowers").document(docId);

        setView();
        setButtons();

        return view;
    }

    private void setView() {
        addWatering = view.findViewById(R.id.editFlower_textView_addWateringInfo);
        wateringDetails = view.findViewById(R.id.linearLayout_wateringDetails);
        nextWateringDate = view.findViewById(R.id.editFlower_textView_nextWatering);
        prevWateringDate = view.findViewById(R.id.editFlower_textView_previousWatering);

        if(flower.getNextWateringDate() != null) {
            wateringDetails.setVisibility(View.VISIBLE);
            addWatering.setVisibility(View.GONE);
            nextWateringDate.setText(nextWatering());
            if (flower.getPreviousWateringDate() != null) {
                prevWateringDate.setText(flower.getPreviousWateringDate());
            } else {
                prevWateringDate.setText(INFOTEXT);
            }
        }
    }

    private void setButtons() {
        if (addWatering.getVisibility() == View.VISIBLE) {
            addWatering.setOnClickListener(this);
        } else {
            wateringDetails.setOnClickListener(this);
        }
    }

    private String nextWatering() {
        String nextWatering = flower.nextWatering(flower.daysToWatering());
        docRef.update("nextWateringDate", flower.getNextWateringDate());
        docRef.update("previousWateringDate", flower.getPreviousWateringDate());
        return nextWatering;
    }


    @Override
    public void onClick(View view) {
        startAddWateringInfo();
    }

    private void startAddWateringInfo() {
        Intent intent = new Intent(getActivity(), AddWateringInfo.class);
        intent.putExtra("Id", docId);
        intent.putExtra("UserUID", userUid);
        intent.putExtra("Flower", flower);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        sharedPref = getActivity().getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(flower);
        editor.putString(FLOWER, json).apply();
    }
}
