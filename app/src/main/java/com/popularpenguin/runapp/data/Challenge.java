package com.popularpenguin.runapp.data;

/** Describes the challenge */
public class Challenge {
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
}
