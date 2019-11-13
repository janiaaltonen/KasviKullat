package com.example.kasvikullat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;

public class AddNewFragment extends Fragment implements FlowerNameAdapter.OnItemClickListener, View.OnClickListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference flowerNamesRef;
    private Button buttonSave;
    private String flowerName, flowerName2;
    private FirebaseAuth mAuth;
    private String userUid;
    private RecyclerView recyclerView;
    private FlowerNameAdapter adapter;
    EditText et;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();

        buttonSave = view.findViewById(R.id.button_save_fragment_new);
        buttonSave.setOnClickListener(this);
        et = view.findViewById(R.id.editText_test);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty()) {
                    buildRecyclerView();
                } else {
                    filteredRecyclerView(editable.toString());
                }
            }
        });

        recyclerView = view.findViewById(R.id.flowerName_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        buildRecyclerView();

        return view;
    }


    private void buildRecyclerView(){
        flowerNamesRef = db.collection("flower_names");
        Query query = flowerNamesRef.orderBy("name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Flower> options = new FirestoreRecyclerOptions.Builder<Flower>()
                .setQuery(query, Flower.class)
                .build();
        adapter = new FlowerNameAdapter(getActivity(), options);

        recyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.setOnItemClickListener(this);
    }

    private void filteredRecyclerView(String string) {
        String text = string.substring(0,1).toUpperCase() + string.substring(1).toLowerCase();
        Query query = flowerNamesRef.orderBy("name").startAt(text).endAt(text + "\uf8ff");
        FirestoreRecyclerOptions<Flower> options = new FirestoreRecyclerOptions.Builder<Flower>()
                .setQuery(query, Flower.class)
                .build();
        adapter = new FlowerNameAdapter(getActivity(), options);
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.setOnItemClickListener(this);

    }


    private void saveFlower() {
        long createdAt = System.currentTimeMillis() / 1000;

        CollectionReference flowerRef = FirebaseFirestore.getInstance().collection("users").document(userUid).collection("flowers");
        flowerRef.add(new Flower(flowerName, flowerName2, null, null, 0, 1, 1, createdAt));
        Toast.makeText(getContext(), "Tiedot tallennettu", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
        Flower flower = documentSnapshot.toObject(Flower.class);
        flowerName = flower.getName();
        flowerName2 = flower.getName2();
        String text = "Lisää " + flowerName + " (" + flowerName2 + ")";
        buttonSave.setText(text);
        buttonSave.setEnabled(true);
        ViewCompat.setBackgroundTintList(buttonSave, ContextCompat.getColorStateList(getActivity(), R.color.colorButtonSaveFlower));

    }

    @Override
    public void onClick(View view) {
        saveFlower();
    }
}
