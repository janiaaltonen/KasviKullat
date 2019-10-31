package com.example.kasvikullat;

import android.content.ContentResolver;
import android.content.Intent;
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


public class EditFlower extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView flowerImage, sun2, sun3, drop2, drop3;
    TextView flowerName, flowerName2, addWatering, showWateringDate, changeWatering;
    LinearLayout showWatering, suns, drops;
    Button changeInfo;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    private Flower flower;
    private String userUid;
    private String id;
    private FirebaseAuth mAuth;
    private Uri imageUri;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference("flower_images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_flower);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        id = intent.getStringExtra("Id");
        flower = intent.getParcelableExtra("Flower");

        docRef = db.collection("users").document(userUid).collection("flowers").document(id);

        getSupportActionBar().setTitle(flower.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        flowerImage = findViewById(R.id.editFlower_imageView_flower);
        flowerName = findViewById(R.id.editFlower_textView_flowerName);
        flowerName2 = findViewById(R.id.editFlower_textView_flowerName2);
        addWatering = findViewById(R.id.editFlower_textView_addWatering);
        showWatering = findViewById(R.id.editFlower_linearLayout_showWatering);
        showWateringDate = findViewById(R.id.editFlower_textView_showWatering);
        changeWatering = findViewById(R.id.editFlower_textView_changeWatering);

        suns = findViewById(R.id.editFlower_linearLayout_suns);
        sun2 = findViewById(R.id.image_sun2);
        sun3 = findViewById(R.id.image_sun3);

        drops = findViewById(R.id.editFlower_linearLayout_drops);
        drop2 = findViewById(R.id.image_drop2);
        drop3 = findViewById(R.id.image_drop3);


        setViews();
        initButtons();
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

            initButtons();
        }

        // set correct amount of "bright" suns
        int brightness = flower.getNeedOfLight();
        if(brightness == 2) {
            sun2.setAlpha(1.0f);
        }
        if (brightness == 0) {
            sun2.setAlpha(1.0f);
            sun3.setAlpha(1.0f);
        }

        // set correct amount of "bright" water drops
        int moist = flower.getNeedOfWater();
        if(moist == 2) {
            drop2.setAlpha(1.0f);
        }
        if (moist == 0) {
            drop2.setAlpha(1.0f);
            drop3.setAlpha(1.0f);
        }

    }

    private String nextWatering() {
        String nextWatering = flower.nextWatering(flower.daysToWatering());
        docRef.update("nextWateringDate", flower.getNextWateringDate());
        return nextWatering;
    }

    private void initButtons() {
        changeInfo = findViewById(R.id.button_change_info);
        changeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        addWatering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditFlower.this, AddWateringInfo.class);
                intent.putExtra("Id", id);
                intent.putExtra("UserUID", userUid);
                intent.putExtra("Flower", flower);
                startActivity(intent);
            }
        });

        changeWatering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditFlower.this, AddWateringInfo.class);
                intent.putExtra("Id", id);
                intent.putExtra("UserUID", userUid);
                intent.putExtra("Flower", flower);
                startActivity(intent);
            }
        });

        suns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int brightness = flower.getNeedOfLight() + 1;
                int moduloBrightness = brightness % 3;

                if(moduloBrightness == 1) {
                    sun2.setAlpha(0.5f);
                    sun3.setAlpha(0.5f);
                    flower.setNeedOfLight(brightness);
                } else if(moduloBrightness == 2) {
                    sun2.setAlpha(1.0f);
                    flower.setNeedOfLight(brightness);
                } else {
                    sun2.setAlpha(1.0f);
                    sun3.setAlpha(1.0f);
                    flower.setNeedOfLight(brightness);
                }
                docRef.update("needOfLight", moduloBrightness);
            }
        });

        drops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int moist = flower.getNeedOfWater() + 1;
                int moduloMoist = moist % 3;

                if (moduloMoist == 1) {
                    drop2.setAlpha(0.5f);
                    drop3.setAlpha(0.5f);
                    flower.setNeedOfWater(moist);
                } else if(moduloMoist == 2) {
                    drop2.setAlpha(1.0f);
                    flower.setNeedOfWater(moist);
                } else {
                    drop2.setAlpha(1.0f);
                    drop3.setAlpha(1.0f);
                    flower.setNeedOfWater(moist);
                }
                docRef.update("needOfWater", moduloMoist);
            }
        });
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
}
