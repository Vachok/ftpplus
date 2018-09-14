package ru.vachok.money.components;


import org.springframework.stereotype.Component;

/**
 * @since 14.09.2018 (11:47)
 */
@Component
public class EURO {

    private String year;

    private String month;

    private String day;

    private float price;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

}
