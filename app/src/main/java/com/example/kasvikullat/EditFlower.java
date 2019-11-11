package com.example.kasvikullat;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;


public class EditFlower extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView flowerImage, sun2, sun3, drop2, drop3;
    private TextView flowerName, flowerName2, addWatering, showWateringDate, changeWatering;
    private LinearLayout showWatering, suns, drops;
    private Button changeInfo;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    private Flower flower;
    private String userUid;
    private String id;
    private Uri imageUri;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference("flower_images");
    private SharedPreferences sharedPref;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String FLOWER_ID = "flowerId";
    private static final String FLOWER = "flower";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_flower);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userUid = mAuth.getCurrentUser().getUid();
        }
        sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        id = sharedPref.getString(FLOWER_ID, "");

        Intent intent = getIntent();
        flower = intent.getParcelableExtra("Flower");
        if (flower == null) {
            Gson gson = new Gson();
            String json = sharedPref.getString(FLOWER, "");
            flower = gson.fromJson(json, Flower.class);
        }

        docRef = db.collection("users").document(userUid).collection("flowers").document(id);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(flower.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setViews();
        setButtons();
    }

    private void initViews() {
        flowerImage = findViewById(R.id.editFlower_imageView_flower);
        flowerName = findViewById(R.id.editFlower_textView_flowerName);
        flowerName2 = findViewById(R.id.editFlower_textView_flowerName2);
        addWatering = findViewById(R.id.editFlower_textView_addWatering);
        showWatering = findViewById(R.id.editFlower_linearLayout_showWatering);
        showWateringDate = findViewById(R.id.editFlower_textView_showWatering);
        changeWatering = findViewById(R.id.editFlower_textView_changeWatering);
        changeInfo = findViewById(R.id.button_change_info);

        suns = findViewById(R.id.editFlower_linearLayout_suns);
        sun2 = findViewById(R.id.image_sun2);
        sun3 = findViewById(R.id.image_sun3);

        drops = findViewById(R.id.editFlower_linearLayout_drops);
        drop2 = findViewById(R.id.image_drop2);
        drop3 = findViewById(R.id.image_drop3);
    }

    private void setViews() {
        Glide.with(this)
                .load(flower.getImageUrl())
                .error(R.drawable.ic_local_florist)
                .centerCrop()
                .into(flowerImage);

        flowerName.setText(flower.getName());
        String name2 = "(" + flower.getName2() + ")";
        flowerName2.setText(name2);

        if(flower.getNextWateringDate() != null) {
            addWatering.setVisibility(View.GONE);
            showWatering.setVisibility(View.VISIBLE);
            showWateringDate.setText(nextWatering());

            //next two lines insert the suns below showWatering. Otherwise would be mixed with the showWatering's text
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) suns.getLayoutParams();
            layoutParams.addRule(RelativeLayout.BELOW, showWatering.getId());

        }

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

    private String nextWatering() {
        String nextWatering = flower.nextWatering(flower.daysToWatering());
        docRef.update("nextWateringDate", flower.getNextWateringDate());
        return nextWatering;
    }

    private void setButtons() {
        changeInfo.setOnClickListener(this);

        if (addWatering.getVisibility() == View.VISIBLE) {
            addWatering.setOnClickListener(this);
        } else {
            changeWatering.setOnClickListener(this);
        }
        suns.setOnClickListener(this);
        drops.setOnClickListener(this);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
        uploadImage();
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(flowerImage);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        if (flower.getImageUrl() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReferenceFromUrl(flower.getImageUrl());
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //actions decided later
                    //
                }
            });
        }
        if (imageUri != null) {
            final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    docRef.update("imageUrl", uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditFlower.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void startAddWateringInfo() {
        Intent intent = new Intent(EditFlower.this, AddWateringInfo.class);
        intent.putExtra("Id", id);
        intent.putExtra("UserUID", userUid);
        intent.putExtra("Flower", flower);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPref = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(flower);
        editor.putString(FLOWER, json).apply();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_change_info:
                openFileChooser();
                break;

            case R.id.editFlower_textView_addWatering | R.id.editFlower_textView_changeWatering:
                startAddWateringInfo();
                break;

            case R.id.editFlower_linearLayout_suns:
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
                break;

            case R.id.editFlower_linearLayout_drops:
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
                break;
        }
    }
}
