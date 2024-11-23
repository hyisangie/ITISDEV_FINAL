package com.itisdev.itisdev_final.Domain;

public class RatingRange {
    private int id;
    private String range;

    public RatingRange() {
    }

    @Override
    public String toString() {
        return range;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }
}
