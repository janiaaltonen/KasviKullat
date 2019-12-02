package com.example.kasvikullat;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AddNewFragment extends Fragment implements FlowerNameAdapter.OnItemClickListener, ConfirmDialog.ConfirmDialogListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference flowerNamesRef;
    private String flowerName, flowerName2;
    private FirebaseAuth mAuth;
    private String userUid;
    private RecyclerView recyclerView;
    private FlowerNameAdapter adapter;
    private static final String APP_NAME = "KasviKullat";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        Toolbar toolbar = view.findViewById(R.id._collapsingToolbar);
        toolbar.setTitle(APP_NAME);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();

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
        flowerRef.add(new Flower(flowerName, flowerName2, null, null, 0, 1, 1, createdAt,null));
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

        openDialog(flowerName);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE); //for the keyboard button

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    buildRecyclerView();
                }
                else {
                    filteredRecyclerView(newText);
                }
                return false;
            }
        });

    }

    private void openDialog(String flowerName) {
        ConfirmDialog dialog = new ConfirmDialog(flowerName);
        dialog.setTargetFragment(this, 1);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), "confirmDialogFragment");
        }
    }

    @Override
    public void onPositiveClicked() {
        saveFlower();
    }
}
