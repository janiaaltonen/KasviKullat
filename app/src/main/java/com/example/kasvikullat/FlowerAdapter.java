package com.example.kasvikullat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



public class FlowerAdapter extends FirestoreRecyclerAdapter<Flower, FlowerAdapter.FlowerHolder> {
    private OnItemClickListener listener;
    private Context context;

    public FlowerAdapter(Context context, @NonNull FirestoreRecyclerOptions<Flower> options) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FlowerHolder flowerHolder, int i, @NonNull Flower flower) {
        Glide.with(context)
                .load(flower.getImageUrl())
                .error(R.drawable.ic_local_florist)
                .centerCrop()
                .into(flowerHolder.flowerPhoto);

        flowerHolder.flowerName.setText(flower.getName());
        String name2 = "(" + flower.getName2() + ")";
        flowerHolder.flowerName2.setText(name2);
        if (flower.getNextWateringDate() != null) {
            flowerHolder.nextWatering.setText(flower.nextWatering(flower.daysToWatering()));
        } else {
            flowerHolder.nextWatering.setText(context.getResources().getString(R.string.next_watering_default));
        }

        // set the selected amount of light
        int brightness = flower.getNeedOfLight();
        if(brightness == 2) {
            flowerHolder.sun2.setAlpha(1.0f);
        } else if (brightness == 0) {
            flowerHolder.sun2.setAlpha(1.0f);
            flowerHolder.sun3.setAlpha(1.0f);
        } else {
            flowerHolder.sun2.setAlpha(0.5f);
            flowerHolder.sun3.setAlpha(0.5f);
        }

        int moist = flower.getNeedOfWater();
        if(moist == 2) {
            flowerHolder.drop2.setAlpha(1.0f);
        } else if (moist == 0) {
            flowerHolder.drop2.setAlpha(1.0f);
            flowerHolder.drop3.setAlpha(1.0f);
        } else {
            flowerHolder.drop2.setAlpha(0.5f);
            flowerHolder.drop3.setAlpha(0.5f);
        }

    }

    @NonNull
    @Override
    public FlowerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flower_item, parent, false);
        return new FlowerHolder(view);
    }

    public void deleteFlower(final int position) {      // delete the image from FireBase Storage and if successful then the file from FireStore
        Flower flower = getSnapshots().getSnapshot(position).toObject(Flower.class);
        if (flower.getImageUrl() == null) {
            getSnapshots().getSnapshot(position).getReference().delete();
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReferenceFromUrl(flower.getImageUrl());
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    getSnapshots().getSnapshot(position).getReference().delete();
                }
            });
        }
    }

    public void cancelAlarm(int position) {
        Flower flower = getSnapshots().getSnapshot(position).toObject(Flower.class);
        String uniqueAction = flower.getName() + "," + flower.getCreatedAt(); // this unique action has to be same as in AddWateringInfo.class to delete correct alarm

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlertReceiver.class);
        intent.setAction(uniqueAction); // make sure to delete the correct pending intent

        int requestCode = (int) flower.getCreatedAt(); // this requestCode has to be same as in AddWateringInfo.class to delete correct alarm

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

    class FlowerHolder extends RecyclerView.ViewHolder {
        TextView flowerName, flowerName2, nextWatering;
        ImageView flowerPhoto, sun2, sun3, drop2, drop3;
        RelativeLayout viewBackground;
        CardView viewForeground;

        public FlowerHolder(@NonNull View itemView) {
            super(itemView);
            flowerPhoto = itemView.findViewById(R.id.imageView_flower);
            flowerName = itemView.findViewById(R.id.textView_name);
            flowerName2 = itemView.findViewById(R.id.textView_name2);
            nextWatering = itemView.findViewById(R.id.textView_nextWatering);

            sun2 = itemView.findViewById(R.id.image_sun2);
            sun3 = itemView.findViewById(R.id.image_sun3);

            drop2 = itemView.findViewById(R.id.image_drop2);
            drop3 = itemView.findViewById(R.id.image_drop3);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);




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
