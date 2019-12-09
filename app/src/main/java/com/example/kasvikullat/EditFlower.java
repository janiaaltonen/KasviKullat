package com.example.kasvikullat;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;


public class EditFlower extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView flowerImage, settings;
    private NavigationTabAdapter adapter;
    private HeightWrappingViewPager viewPager;
    private TabLayout tabLayout;
    private int [] tabIcons;
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

        initViews();
        setViews();
        setButtons();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        flowerImage = findViewById(R.id.editFlower_imageView_flower);
        settings = findViewById(R.id.settings);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        tabIcons = new int[] {R.drawable.watering_can, R.drawable.fertilizing_bag,
                                R.drawable.ic_info, R.drawable.ic_pen, R.drawable.ic_info};

    }

    private void setViews() {
        if (getSupportActionBar() != null) {
            String title = flower.getName() + " (" + flower.getName2() + ")";
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Glide.with(this)
                .load(flower.getImageUrl())
                .error(R.drawable.ic_local_florist)
                .centerCrop()
                .into(flowerImage);

        adapter = new NavigationTabAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new WateringFragment());
        adapter.addFragment(new FertilizingFragment());
        adapter.addFragment(new FlowerInfoFragment());
        adapter.addFragment(new NotesFragment());
        adapter.addFragment(new FlowerExtraFragment());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabIcons.length; i++) {
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }
        // set the "start fragment" to overall info
        viewPager.setCurrentItem(2);
    }



    private void setButtons() {
        settings.setOnClickListener(this);
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
           case R.id.settings:
                openFileChooser();
                break;
        }
    }
}
