package com.example.kasvikullat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class FlowerExtraFragment extends Fragment implements View.OnClickListener{
    private View view;
    private ImageView sun2, sun3, drop2, drop3;
    private LinearLayout suns, drops;
    private Flower flower;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    private SharedPreferences sharedPref;
    private String userUid, docId;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String FLOWER = "flower";
    private static final String FLOWER_ID = "flowerId";
    private static final String UUID = "uuid";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_flower_extra, container, false);

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
        suns = view.findViewById(R.id.editFlower_linearLayout_suns);
        sun2 = view.findViewById(R.id.image_sun2);
        sun3 = view.findViewById(R.id.image_sun3);

        drops = view.findViewById(R.id.editFlower_linearLayout_drops);
        drop2 = view.findViewById(R.id.image_drop2);
        drop3 = view.findViewById(R.id.image_drop3);

        // set correct amount of "bright" suns
        int brightness = flower.getNeedOfLight();
        if(brightness == 2) {
            sun2.setAlpha(1.0f);
        } else if (brightness == 3) {
            sun2.setAlpha(1.0f);
            sun3.setAlpha(1.0f);
        }

        // set correct amount of "bright" water drops
        int moist = flower.getNeedOfWater();
        if(moist == 2) {
            drop2.setAlpha(1.0f);
        } else if (moist == 3) {
            drop2.setAlpha(1.0f);
            drop3.setAlpha(1.0f);
        }

    }

    private void setButtons() {
        suns.setOnClickListener(this);
        drops.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.editFlower_linearLayout_suns) {
            int moduloBrightness = (flower.getNeedOfLight() + 1) % 3;

            if(moduloBrightness == 1) {
                sun2.setAlpha(0.5f);
                sun3.setAlpha(0.5f);
            } else if(moduloBrightness == 2) {
                sun2.setAlpha(1.0f);
            } else {
                sun2.setAlpha(1.0f);
                sun3.setAlpha(1.0f);
                moduloBrightness = 3;
            }
            flower.setNeedOfLight(moduloBrightness);
            docRef.update("needOfLight", moduloBrightness);
        } else {
            int moduloMoist = (flower.getNeedOfWater() + 1) % 3;

            if (moduloMoist == 1) {
                drop2.setAlpha(0.4f);
                drop3.setAlpha(0.4f);
            } else if(moduloMoist == 2) {
                drop2.setAlpha(1.0f);
            } else {
                drop2.setAlpha(1.0f);
                drop3.setAlpha(1.0f);
                moduloMoist = 3;
            }
            flower.setNeedOfWater(moduloMoist);
            docRef.update("needOfWater", moduloMoist);
        }
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
