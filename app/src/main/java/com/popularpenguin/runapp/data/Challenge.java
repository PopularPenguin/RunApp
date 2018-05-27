package com.popularpenguin.runapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/** Describes the challenge */
public class Challenge implements Parcelable {

    public static final String CHALLENGE_EXTRA = "challenge";

    public static final int EASY = 1;
    public static final int MEDIUM = 2;
    public static final int HARD = 3;

    private long id;
    private String name;
    private String description;
    private long distance; // distance in feet
    private long timeToComplete;
    private long fastestTime;
    private boolean isCompleted;
    private int challengeRating; // 1 = easy, 2 = medium, 3 = hard

    public Challenge(long id,
                     String name,
                     String description,
                     long distance,
                     long timeToComplete,
                     boolean isCompleted,
                     int challengeRating) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.distance = distance;
        this.timeToComplete = timeToComplete;
        this.isCompleted = isCompleted;
        this.challengeRating = challengeRating;
    }

    private Challenge(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
        description = parcel.readString();
        distance = parcel.readLong();
        timeToComplete = parcel.readLong();
        fastestTime = parcel.readLong();
        isCompleted = parcel.readInt() == 1;
        challengeRating = parcel.readInt();
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
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

    public int getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(int challengeRating) {
       this.challengeRating = challengeRating;
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
        parcel.writeLong(distance);
        parcel.writeLong(timeToComplete);
        parcel.writeLong(fastestTime);
        parcel.writeInt(isCompleted ? 1: 0);
        parcel.writeInt(challengeRating);
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
