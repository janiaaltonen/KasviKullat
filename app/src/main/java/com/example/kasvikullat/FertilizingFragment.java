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

public class FertilizingFragment extends Fragment implements View.OnClickListener {
    private View view;
    private TextView addFertilizing, nextFertilizingDate, prevFertilizingDate;
    private LinearLayout fertilizingDetails;
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
        view = inflater.inflate(R.layout.fragment_fertilizing, container, false);

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
        addFertilizing = view.findViewById(R.id.editFlower_textView_addFertilizingInfo);
        fertilizingDetails = view.findViewById(R.id.linearLayout_fertilizingDetails);
        nextFertilizingDate = view.findViewById(R.id.editFlower_textView_nextFertilizing);
        prevFertilizingDate = view.findViewById(R.id.editFlower_textView_previousFertilizing);

        if(flower.getNextFertilizingDate() != null) {
            fertilizingDetails.setVisibility(View.VISIBLE);
            addFertilizing.setVisibility(View.GONE);
            nextFertilizingDate.setText(nextWatering());
            if (flower.getPreviousFertilizingDate() != null) {
                prevFertilizingDate.setText(flower.getPreviousFertilizingDate());
            } else {
                prevFertilizingDate.setText(INFOTEXT);
            }
        }
    }

    private void setButtons() {
        if (addFertilizing.getVisibility() == View.VISIBLE) {
            addFertilizing.setOnClickListener(this);
        } else {
            fertilizingDetails.setOnClickListener(this);
        }
    }

    private String nextWatering() {
        String nextWatering = flower.nextWatering(flower.daysToWatering());
        docRef.update("nextFertilizingDate", flower.getNextFertilizingDate());
        docRef.update("previousFertilizingDate", flower.getPreviousFertilizingDate());
        return nextWatering;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.linearLayout_fertilizingDetails) {
            startAddWateringInfo(4);
        } else {
            startAddWateringInfo(3);
        }

    }

    private void startAddWateringInfo(int request) {
        Intent intent = new Intent(getActivity(), AddWateringInfo.class);
        intent.putExtra("Id", docId);
        intent.putExtra("UserUID", userUid);
        intent.putExtra("Flower", flower);
        intent.putExtra("Request", request);
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
