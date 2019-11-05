package com.example.kasvikullat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


public class HomeFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUid;
    private CollectionReference flowerRef;
    private FlowerAdapter adapter;
    private View view;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();
        buildRecyclerView();

        FloatingActionButton fab = view.findViewById(R.id.fab_home);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("userUID", userUid);
                Fragment fragment = new AddNewFragment();
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });


        return view;
    }

    private void buildRecyclerView() {
        flowerRef = db.collection("users").document(userUid).collection("flowers");
        Query query = flowerRef.orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Flower> options = new FirestoreRecyclerOptions.Builder<Flower>()
                .setQuery(query, Flower.class)
                .build();

        adapter = new FlowerAdapter(getActivity(), options);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new FlowerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Flower flower = documentSnapshot.toObject(Flower.class);
                String id = documentSnapshot.getId();

                Intent intent = new Intent(getActivity(), EditFlower.class);
                intent.putExtra("Id", id);
                intent.putExtra("userUID", userUid);
                intent.putExtra("Flower", flower);
                startActivity(intent);
            }
        });

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

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FlowerAdapter.FlowerHolder) {
            final Flower deletedFlower = adapter.getSnapshots().getSnapshot(position).toObject(Flower.class);
            // backup of removed item for undo purpose
            String name = deletedFlower.getName();
            // get the removed item name to display it in snack bar

            adapter.deleteFlower(viewHolder.getAdapterPosition());
            // remove the item from recyclerview and Firestore


            final Snackbar snackbar = Snackbar
                    .make(view, name + " poistettu!", Snackbar.LENGTH_LONG);
            snackbar.setAction("PALAUTA", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flowerRef.add(deletedFlower);
                    //restores the flower back to Firestore db

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event != DISMISS_EVENT_ACTION) {
                        // if flower is not restored, then the alarm will be cancelled
                        adapter.cancelAlarm(deletedFlower);
                        if (deletedFlower.getImageUrl() != null) {
                            // if flower has ref to storage, then the image will be deleted
                            adapter.deleteFlowerImage(deletedFlower);
                        }
                    }
                }
            });
            snackbar.show();

        }
    }
}
