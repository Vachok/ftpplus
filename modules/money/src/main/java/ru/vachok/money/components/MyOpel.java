package ru.vachok.money.components;


import org.springframework.stereotype.Component;


/**
 @since 01.09.2018 (20:26) */
@Component
public class MyOpel {

    private String carName;

    private int carMiletage;

    private double avgSpeedA107;

    private double avgSpeedRiga;

    private double avgTime;

    private String gosNum;

    public String getGosNum() {
        return gosNum;
    }

    public void setGosNum(String gosNum) {
        this.gosNum = gosNum;
    }

    public int getCarMiletage() {
        return carMiletage;
    }

    public void setCarMiletage(int carMiletage) {
        this.carMiletage = carMiletage;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public double getAvgSpeedA107() {
        return avgSpeedA107;
    }

    public void setAvgSpeedA107(double avgSpeedA107) {
        this.avgSpeedA107 = avgSpeedA107;
    }

    public double getAvgSpeedRiga() {
        return avgSpeedRiga;
    }

    public void setAvgSpeedRiga(double avgSpeedRiga) {
        this.avgSpeedRiga = avgSpeedRiga;
    }

}