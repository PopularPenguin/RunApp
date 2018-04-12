package com.popularpenguin.runapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/** Describes the challenge */
public class Challenge implements Parcelable {
    private long id;
    private String name;
    private String description;
    private long timeToComplete;
    private long fastestTime;
    private boolean isCompleted;

    public Challenge(long id,
                     String name,
                     String description,
                     long timeToComplete,
                     boolean isCompleted) {

        this.name = name;
        this.description = description;
        this.timeToComplete = timeToComplete;
        this.isCompleted = isCompleted;
    }

    private Challenge(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
        description = parcel.readString();
        timeToComplete = parcel.readLong();
        fastestTime = parcel.readLong();
        isCompleted = parcel.readInt() == 1;
    }

    public String getFastestTimeString() {
        if (fastestTime == 0L) {
            return "-:--:--";
        }

        long seconds = fastestTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimeToComplete() {
        return timeToComplete;
    }

    public void setTimeToComplete(long timeToComplete) {
        this.timeToComplete = timeToComplete;
    }

    public long getFastestTime() {
        return fastestTime;
    }

    public void setFastestTime(long fastestTime) {
        this.fastestTime = fastestTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeLong(timeToComplete);
        parcel.writeLong(fastestTime);
        parcel.writeInt(isCompleted ? 1: 0);
    }

    public static final Creator<Challenge> CREATOR = new Parcelable.Creator<Challenge>() {
        @Override
        public Challenge createFromParcel(Parcel parcel) {
            return new Challenge(parcel);
        }

        @Override
        public Challenge[] newArray(int size) {
            return new Challenge[size];
        }
    };
}
