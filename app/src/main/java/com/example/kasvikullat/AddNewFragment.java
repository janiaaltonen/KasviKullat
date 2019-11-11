package com.example.kasvikullat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class AddNewFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference flowerNamesRef;
    private ArrayList<Flower> flowerNames;
    private String flowerName, flowerName2;
    FirebaseAuth mAuth;
    private String userUid;
    private FlowerNameAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();


        buildRecyclerView(view);

        return view;
    }


    private void buildRecyclerView(View view){
        flowerNamesRef = db.collection("flower_names");
        Query query = flowerNamesRef.orderBy("name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Flower> options = new FirestoreRecyclerOptions.Builder<Flower>()
                .setQuery(query, Flower.class)
                .build();
        adapter = new FlowerNameAdapter(getActivity(), options);

        RecyclerView recyclerView = view.findViewById(R.id.flowerName_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }


    private void saveFlower() {
        long createdAt = System.currentTimeMillis() / 1000;

       /* if (name.trim().isEmpty() || payMethod.trim().isEmpty() || dueDate.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }*/

        CollectionReference flowerRef = FirebaseFirestore.getInstance().collection("users").document(userUid).collection("flowers");
        flowerRef.add(new Flower(flowerName, flowerName2, null, null, 0, 1, 1, createdAt));
        Toast.makeText(getContext(), "Tiedot tallennettu", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
