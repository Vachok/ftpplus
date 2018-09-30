package ru.vachok.money.components;


import org.springframework.stereotype.Component;

/**
 * @since 14.09.2018 (11:50)
 */
@Component("roadstat")
public class RoadStatistics {

    private String weekDay;

    private String roadAsString;

    private float speed;

    private float time;

    private long timeStampLast;

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getRoadAsString() {
        return roadAsString;
    }

    public void setRoadAsString(String roadAsString) {
        this.roadAsString = roadAsString;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public long getTimeStampLast() {
        return timeStampLast;
    }

    public void setTimeStampLast(long timeStampLast) {
        this.timeStampLast = timeStampLast;
    }
}
