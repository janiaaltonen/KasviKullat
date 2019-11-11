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
    private CollectionReference flowerNamesRef = db.collection("flower_names");
    private ArrayList<Flower> flowerNames;
    private String flowerName, flowerName2;
    FirebaseAuth mAuth;
    private String userUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();


        initList(view);
        initButton(view);

        return view;
    }


    private void initList(final View view) {
        flowerNames = new ArrayList<>();
        Query query = flowerNamesRef.orderBy("name", Query.Direction.ASCENDING);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Flower flower = documentSnapshot.toObject(Flower.class);
                            flowerNames.add(flower);
                        }
                        initSpinner(view);
                    }
                });
    }
    private void initSpinner(View view) {
        Spinner spinner = view.findViewById(R.id.spinner_flowerNames);

        FlowerNameAdapter adapter = new FlowerNameAdapter(getContext(), flowerNames);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Flower clickedFlower = (Flower) parent.getItemAtPosition(position);
                flowerName = clickedFlower.getName();
                flowerName2 = clickedFlower.getName2();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initButton(View view) {
        Button button = view.findViewById(R.id.button_saveFlowerChoice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFlower();
            }
        });
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
}
