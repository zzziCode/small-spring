package com.zzzi.springframework.bean;

public class Husband {
    private Wife wife;

    public String queryWife() {
        return wife.toString();
    }

    public Wife getWife() {
        return wife;
    }

    public void setWife(Wife wife) {
        this.wife = wife;
    }
}
