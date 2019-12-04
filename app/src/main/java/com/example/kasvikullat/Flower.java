package com.example.kasvikullat;


import android.os.Parcel;
import android.os.Parcelable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Flower implements Parcelable {
    private String name, name2, imageUrl, nextWateringDate, previousWateringDate, notes, nextFertilizingDate, previousFertilizingDate;
    private long createdAt;
    private int wateringFrequency, needOfLight, needOfWater, fertilizingFrequency;

    public Flower(){
        //public no-arg constructor needed
    }

    public Flower(String name, String name2, String imageUrl, String nextWateringDate, int wateringFrequency,
                  int needOfLight, int needOfWater, long createdAt, String previousWateringDate, String notes, String nextFertilizingDate, String previousFertilizingDate, int fertilizingFrequency) {
        this.name = name;
        this.name2 = name2;
        this.imageUrl = imageUrl;
        this.nextWateringDate = nextWateringDate;
        this.wateringFrequency = wateringFrequency;
        this.needOfLight = needOfLight;
        this.needOfWater = needOfWater;
        this.createdAt = createdAt;
        this.previousWateringDate = previousWateringDate;
        this.notes = notes;
        this.nextFertilizingDate = nextFertilizingDate;
        this.previousFertilizingDate = previousFertilizingDate;
        this.fertilizingFrequency = fertilizingFrequency;


    }

    protected Flower(Parcel in) { // order has to be same as in writeToParcel method!
        name = in.readString();
        name2 = in.readString();
        imageUrl = in.readString();
        nextWateringDate = in.readString();
        createdAt = in.readLong();
        wateringFrequency = in.readInt();
        needOfLight = in.readInt();
        needOfWater = in.readInt();
        previousWateringDate = in.readString();
        notes = in.readString();
        nextFertilizingDate = in.readString();
        previousFertilizingDate = in.readString();
        fertilizingFrequency = in.readInt();
    }

    public static final Creator<Flower> CREATOR = new Creator<Flower>() {
        @Override
        public Flower createFromParcel(Parcel in) {
            return new Flower(in);
        }

        @Override
        public Flower[] newArray(int size) {
            return new Flower[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getName2() {
        return name2;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getNextWateringDate() {
        return nextWateringDate;
    }

    public void setNextWateringDate(String nextWateringDate) {
        this.nextWateringDate = nextWateringDate;
    }

    public int getWateringFrequency() {
        return wateringFrequency;
    }

    public void setWateringFrequency(int wateringFrequency) {
        this.wateringFrequency = wateringFrequency;
    }

    public int getNeedOfLight() {
        return needOfLight;
    }

    public void setNeedOfLight(int needOfLight) {
        this.needOfLight = needOfLight;
    }

    public int getNeedOfWater() {
        return needOfWater;
    }

    public void setNeedOfWater(int needOfWater) {
        this.needOfWater = needOfWater;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getPreviousWateringDate() {
        return previousWateringDate;
    }

    public void setPreviousWateringDate(String previousWateringDate) {
        this.previousWateringDate = previousWateringDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNextFertilizingDate() {
        return nextFertilizingDate;
    }

    public void setNextFertilizingDate(String nextFertilizingDate) {
        this.nextFertilizingDate = nextFertilizingDate;
    }

    public String getPreviousFertilizingDate() {
        return previousFertilizingDate;
    }

    public void setPreviousFertilizingDate(String previousFertilizingDate) {
        this.previousFertilizingDate = previousFertilizingDate;
    }

    public int getFertilizingFrequency() {
        return fertilizingFrequency;
    }

    public void setFertilizingFrequency(int fertilizingFrequency) {
        this.fertilizingFrequency = fertilizingFrequency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) { // order has to be same as in parcel constructor!
        parcel.writeString(name);
        parcel.writeString(name2);
        parcel.writeString(imageUrl);
        parcel.writeString(nextWateringDate);
        parcel.writeLong(createdAt);
        parcel.writeInt(wateringFrequency);
        parcel.writeInt(needOfLight);
        parcel.writeInt(needOfWater);
        parcel.writeString(previousWateringDate);
        parcel.writeString(notes);
        parcel.writeString(nextFertilizingDate);
        parcel.writeString(previousFertilizingDate);
        parcel.writeInt(fertilizingFrequency);
    }

    public int daysToWatering() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        int daysToWatering = 0;

        try {
            // set NextWateringDate String to Date object (Calendar used for winter time, leap year etc., correct???)
            Calendar cNextWatering = Calendar.getInstance();
            cNextWatering.setTime(formatter.parse(getNextWateringDate()));
            Date dNextWatering = formatter.parse(formatter.format(cNextWatering.getTime()));

            // set current day
            Calendar cToday = Calendar.getInstance();
            Date dToday = formatter.parse(formatter.format(cToday.getTime()));

            // calculates the difference in days
            long difference = dNextWatering.getTime() - dToday.getTime();
            long days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
            daysToWatering = (int) days;

            // If nextWateringDate is in past, sets new watering date to today or in the future
            if (daysToWatering < 0) {
                int daysToAdd = getWateringFrequency() + daysToWatering;
                boolean negativeDaysToWatering = true;

                while (negativeDaysToWatering) {
                    cNextWatering = Calendar.getInstance();
                    cNextWatering.add(Calendar.DATE, daysToAdd);
                    dNextWatering = formatter.parse(formatter.format(cNextWatering.getTime()));

                    difference = dNextWatering.getTime() - dToday.getTime();
                    days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
                    daysToWatering = (int) days;

                    if (daysToWatering >= 0) {
                        negativeDaysToWatering = false;
                        setNextWateringDate(formatter.format(cNextWatering.getTime()));
                        // previous wateringDate is one round before this
                        // so decrement wateringFrequency amount of days from calendar will get the previous one
                        cNextWatering.add(Calendar.DATE, -getWateringFrequency());
                        //set the previousDate to previousWateringDate
                        setPreviousWateringDate(formatter.format(cNextWatering.getTime()));
                    }
                    daysToAdd += getWateringFrequency();
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return daysToWatering;
    }

    public String nextWatering(int daysToWatering) {
        String nextWatering;

       if (daysToWatering == 0) {
           nextWatering = "TÄNÄÄN";
       } else if (daysToWatering == 1) {
           nextWatering = "HUOMENNA";
       } else {
           nextWatering = daysToWatering + " päivän päästä";
       }

       return nextWatering;
    }
}
