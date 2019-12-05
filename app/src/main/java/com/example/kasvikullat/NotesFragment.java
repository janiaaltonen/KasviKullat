package com.example.kasvikullat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class NotesFragment extends Fragment {
    private View view;
    private EditText notes;
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
        view = inflater.inflate(R.layout.fragment_notes, container, false);

        sharedPref = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString(FLOWER,"");
        flower = gson.fromJson(json, Flower.class);

        userUid = sharedPref.getString(UUID,"");
        docId = sharedPref.getString(FLOWER_ID,"");

        docRef = db.collection("users").document(userUid).collection("flowers").document(docId);

        setView();

        return view;
    }

    private void setView() {
        notes = view.findViewById(R.id.editFlower_notes);
        if (flower.getNotes() != null && flower.getNotes().length() != 0) {
            notes.setText(flower.getNotes());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        String text = notes.getText().toString().trim();
        flower.setNotes(text);
        docRef.update("notes", text);

        sharedPref = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(flower);
        editor.putString(FLOWER, json).apply();
    }
}
