package com.zzzi.springframework.bean;

import java.time.LocalDate;

public class Husband {
    private String wifeName;
    private LocalDate marriageDate;

    @Override
    public String toString() {
        return "husband{" +
                "\nwifeName='" + wifeName +
                "\nmarriageDate=" + marriageDate +
                "\nmarriageDate.class=" + marriageDate.getClass() +
                "\n}";
    }

    public String getWifeName() {
        return wifeName;
    }

    public void setWifeName(String wifeName) {
        this.wifeName = wifeName;
    }

    public LocalDate getMarriageDate() {
        return marriageDate;
    }

    public void setMarriageDate(LocalDate marriageDate) {
        this.marriageDate = marriageDate;
    }
}
