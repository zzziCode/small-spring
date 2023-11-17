package com.zzzi.springframework.bean;

public class Wife {
    private Husband husband;

    public String queryHusband() {
        return husband.toString();
    }

    public Husband getHusband() {
        return husband;
    }

    public void setHusband(Husband husband) {
        this.husband = husband;
    }
}
