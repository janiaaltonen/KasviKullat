package com.example.kasvikullat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddWateringInfo extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, NumberPickerDialog.NumberPickerDialogListener, View.OnClickListener {
    private int selectedFrequency = 0, request;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button datePickerButton, saveButton, numberPickerButton;
    private String id, userUid, nextDate;
    private Flower flower;
    private static final String FLOWER = "Flower";
    private static final String ID = "Id";
    private static final String USERUID = "UserUID";
    private static final String REQUEST = "Request";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_watering_info);

        Intent intent = getIntent();
        id = intent.getStringExtra(ID);
        userUid = intent.getStringExtra(USERUID);
        request = intent.getIntExtra(REQUEST,0);
        flower = intent.getParcelableExtra(FLOWER);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            if (request == 1 || request == 2) getSupportActionBar().setTitle("Kastelutiedot");
            else getSupportActionBar().setTitle("Lannoitetiedot");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initButtons();
        setViews();
    }


    private void initButtons() {
        datePickerButton = findViewById(R.id.addWateringInfo_datePickerButton);
        datePickerButton.setOnClickListener(this);

        numberPickerButton = findViewById(R.id.addWateringInfo_numberPickerButton);
        numberPickerButton.setOnClickListener(this);

        saveButton = findViewById(R.id.button_add_watering_info);
        saveButton.setOnClickListener(this);
    }

    private void setViews() {
        TextView tv1 = findViewById(R.id.textView_when_next);
        TextView tv2 = findViewById(R.id.textView_how_often);

        if (request == 3 || request == 4){
            String text1 = "Mikä päivä minut lannoitetaan seuraavaksi?";
            tv1.setText(text1);
            String text2 = "Kuinka monen päivän välein minut lannoitetaan?";
            tv2.setText(text2);
        }
        if (request == 2 || request == 4) {
            numberPickerButton.setEnabled(true);
            saveButton.setEnabled(true);
            String save = "Tallenna muutokset";
            saveButton.setText(save);
        }
        String text1, text2, frequency;
        switch (request) {
            case 2:
                text1 = "Muuta seuraavaa kastelupäivää";
                tv1.setText(text1);
                text2 = "Muuta kastelutiheyttä";
                tv2.setText(text2);
                nextDate = flower.getNextWateringDate();
                datePickerButton.setText(nextDate);
                selectedFrequency = flower.getWateringFrequency();
                frequency = "joka " + selectedFrequency + ". päivä";
                numberPickerButton.setText(frequency);
                break;

            case 3:
                String button1 = "Valitse seuraava lannoituspäivä";
                datePickerButton.setText(button1);
                String button2 = "Valitse lannoitustiheys";
                numberPickerButton.setText(button2);
                break;

            case 4:
                text1 = "Muuta seuraavaa lannoituspäivää";
                tv1.setText(text1);
                text2 = "Muuta lannoitustiheyttä";
                tv2.setText(text2);
                datePickerButton.setText(flower.getNextFertilizingDate());
                frequency = "joka " + flower.getFertilizingFrequency() + ". päivä";
                numberPickerButton.setText(frequency);
                break;
        }

    }

    @Override //called when date set in datePickerDialog
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        // months start from 0
        month = month + 1;
        nextDate = dayOfMonth + "." + month + "." + year;
        datePickerButton.setText(nextDate);
        if(request == 1 || request == 3) numberPickerButton.setEnabled(true);
    }

    private void updateFlower() {
        DocumentReference docRef = db.collection("users").document(userUid).collection("flowers").document(id);
        if (request == 1 || request == 2) {
            // updates watering data only if it has changed
            if (!nextDate.equals(flower.getNextWateringDate())) {
                docRef.update("nextWateringDate", nextDate);
                flower.setNextWateringDate(nextDate);
            }
            if (selectedFrequency != flower.getWateringFrequency()) {
                docRef.update("wateringFrequency", selectedFrequency);
                flower.setWateringFrequency(selectedFrequency);
            }

            setAndStartAlarm();

        } else {
            if (!nextDate.equals(flower.getNextFertilizingDate())) {
                docRef.update("nextFertilizingDate", nextDate);
                flower.setNextFertilizingDate(nextDate);
            }
            if (selectedFrequency != flower.getFertilizingFrequency()) {
                docRef.update("fertilizingFrequency", selectedFrequency);
                flower.setFertilizingFrequency(selectedFrequency);
            }

            setAndStartAlarm();
        }

        SharedPreferences prefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(flower);
        editor.putString("flower", json).apply();


        Intent intent = new Intent(AddWateringInfo.this, EditFlower.class);
        intent.putExtra(ID, id);
        intent.putExtra(USERUID, userUid);
        intent.putExtra(FLOWER, flower);
        startActivity(intent);
    }

    private void setAndStartAlarm() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(formatter.parse(nextDate));
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // make each alarm unique
        // and set correct interval for pending intents
        String action;
        long interval;
        if (request == 1 || request == 2){
            action = "1,";
            interval = AlarmManager.INTERVAL_DAY * flower.getWateringFrequency();
        }
        else {
            action = "2,";
            interval = AlarmManager.INTERVAL_DAY * flower.getFertilizingFrequency();
        }
        // uniqueAction makes every intent unique compared to other flowers
        // and also unique between watering and fertilizing
        String uniqueAction = action + flower.getName() + "," + flower.getCreatedAt();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction(uniqueAction);

        int requestCode = (int) flower.getCreatedAt();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, 0);

        if (alarmManager != null) {
            // delete existing alarm
            alarmManager.cancel(pendingIntent);
            // change alarmManager to syncManager with FCM in some point to be more efficient
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent);
        }
    }

    private void openNumberPickerDialog() {
        NumberPickerDialog dialog = new NumberPickerDialog();
        dialog.show(getSupportFragmentManager(), "numberPickerDialog");
    }

    @Override
    public void onPositiveClicked(int value) {
        selectedFrequency = value;
        String frequency = "joka " + selectedFrequency + ". päivä";
        numberPickerButton.setText(frequency);
        if(request == 1 || request == 3) saveButton.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addWateringInfo_datePickerButton:
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "datePicker");
                break;

            case R.id.addWateringInfo_numberPickerButton:
                openNumberPickerDialog();
                break;

            case R.id.button_add_watering_info:
                updateFlower();
                break;
        }
    }
}

