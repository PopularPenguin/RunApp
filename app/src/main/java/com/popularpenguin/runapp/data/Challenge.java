package com.popularpenguin.runapp.data;

import android.os.Parcel;
import android.os.Parcelable;

/** Describes the challenge */
public class Challenge implements Parcelable {
    public static final String NAME_KEY = "name";
    public static final String DESCRIPTION_KEY = "description";
    public static final String COMPLETION_TIME_KEY = "timeToComplete";
    public static final String COMPLETED_KEY = "completed";

    private String name;
    private String description;
    private long timeToComplete;
    private boolean isCompleted;

    public Challenge(String name, String description, long timeToComplete, boolean isCompleted) {
        this.name = name;
        this.description = description;
        this.timeToComplete = timeToComplete;
        this.isCompleted = isCompleted;
    }

    private Challenge(Parcel parcel) {
        name = parcel.readString();
        description = parcel.readString();
        timeToComplete = parcel.readLong();
        isCompleted = parcel.readInt() == 1;
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeLong(timeToComplete);
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
