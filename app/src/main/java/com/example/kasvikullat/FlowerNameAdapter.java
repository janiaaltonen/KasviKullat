package com.example.kasvikullat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FlowerNameAdapter extends FirestoreRecyclerAdapter<Flower, FlowerNameAdapter.FlowerNameHolder> {
    private OnItemClickListener listener;
    private Context context;

    public FlowerNameAdapter(Context context, @NonNull FirestoreRecyclerOptions<Flower> options) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FlowerNameHolder flowerHolder, int i, @NonNull Flower flower) {
        flowerHolder.flowerName.setText(flower.getName());
        String name2 = "(" + flower.getName2() + ")";
        flowerHolder.flowerName2.setText(name2);

    }

    @NonNull
    @Override
    public FlowerNameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flower_list_item, parent, false);
        return new FlowerNameHolder(view);
    }

    class FlowerNameHolder extends RecyclerView.ViewHolder {
        TextView flowerName, flowerName2;


        public FlowerNameHolder(@NonNull View itemView) {
            super(itemView);
            flowerName = itemView.findViewById(R.id.textView_flowerName);
            flowerName2 = itemView.findViewById(R.id.textView_flowerName2);



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

