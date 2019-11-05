package com.example.kasvikullat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Ref;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddWateringInfo extends AppCompatActivity {

    private ArrayList<Integer> integers;
    private int selectedFrequency;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id;
    private String userUid;
    private EditText date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_watering_info);
        date =findViewById(R.id.addWateringInfo_editText_nextWatering);

        getSupportActionBar().setTitle("Kastelutiedot");

        Intent intent = getIntent();
        id = intent.getStringExtra("Id");
        userUid = intent.getStringExtra("UserUID");
        Flower flower = intent.getParcelableExtra("Flower");


        initList();
        initSpinner();
        initButton(flower);

    }

    private void initList() {
        integers = new ArrayList<>();

        for (int i = 0; i < 71; i++) {
            integers.add(i);
        }
    }

    private void initSpinner() {
        Spinner spinner = findViewById(R.id.spinner_watering_info);

        WateringInfoAdapter adapter = new WateringInfoAdapter(this, integers);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                if (position != 0) {
                    selectedFrequency = position;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initButton(final Flower flower) {
        if (flower.getNextWateringDate() != null) {
            date.setText(flower.getNextWateringDate());
        }
        Button button = findViewById(R.id.button_add_watering_info);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFlower(flower);
            }
        });
    }

    private void updateFlower(Flower flower) {
        String nextWateringDate = date.getText().toString();
        DocumentReference docRef = db.collection("users").document(userUid).collection("flowers").document(id);
        docRef.update("nextWateringDate", nextWateringDate);
        docRef.update("wateringFrequency", selectedFrequency);

        flower.setNextWateringDate(nextWateringDate);
        flower.setWateringFrequency(selectedFrequency);

        setAndStartAlarm(nextWateringDate, flower);

        Intent intent = new Intent(AddWateringInfo.this, EditFlower.class);
        intent.putExtra("Id", id);
        intent.putExtra("UserUID", userUid);
        intent.putExtra("Flower", flower);
        startActivity(intent);
    }

    private void setAndStartAlarm(String date, Flower flower) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(formatter.parse(date));
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String uniqueAction = flower.getName() + "," + flower.getCreatedAt(); // id added to make sure that pending intent is unique

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction(uniqueAction); // adds the name of correct flower to the notification and makes it unique compared other pending intents

        int requestCode = (int) flower.getCreatedAt();

        long interval = AlarmManager.INTERVAL_DAY * flower.getWateringFrequency();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, 0);

        alarmManager.cancel(pendingIntent);
        // delete existing alarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent);
        // change alarmManager to syncManager with FCM in some point to be more efficient
    }
}
