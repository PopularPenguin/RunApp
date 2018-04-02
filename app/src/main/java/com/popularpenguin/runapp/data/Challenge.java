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
}
