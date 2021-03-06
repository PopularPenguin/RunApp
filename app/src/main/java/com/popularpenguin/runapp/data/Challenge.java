package com.popularpenguin.runapp.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/** Describes the challenge */
@Entity(tableName = "challenges")
public class Challenge implements Parcelable {

    @Ignore
    public static final String CHALLENGE_EXTRA = "challenge";

    @Ignore
    public static final int EASY = 1;
    @Ignore
    public static final int MEDIUM = 2;
    @Ignore
    public static final int HARD = 3;

    @PrimaryKey
    private long id; // id assigned automatically
    private String name; // challenge's name
    private String description; // description of the challenge
    private long distance; // distance in feet
    private long timeToComplete; // goal time
    private long fastestTime; // user's best time
    private boolean isCompleted; // has it been completed by the user?
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

    @Ignore
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

    @SuppressWarnings("unused")
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unused")
    public void setDescription(String description) {
        this.description = description;
    }

    public long getDistance() {
        return distance;
    }

    @SuppressWarnings("unused")
    public void setDistance(long distance) {
        this.distance = distance;
    }

    public long getTimeToComplete() {
        return timeToComplete;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getChallengeRating() {
        return challengeRating;
    }

    @SuppressWarnings("unused")
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
