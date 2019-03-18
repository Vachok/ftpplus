package ru.vachok.networker.services;


import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;


/**
 @since 26.09.2018 (13:47) */
@Service
public class SimpleCalculator {

    private String query;

    private static final Properties PROPS = AppComponents.getOrSetProps();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     @return или timeInMillis от введённой даты, или новая дата из timeInMillis
     */
    private String getStampFromDate() {
        boolean setTo = query.toLowerCase().contains("s");
        if(setTo){
            query = query.replaceFirst("calctimes:", "");
        } else if (query.contains("calc")) {
            query = query.replaceFirst("calctime:", "");
        } else {
            query = query.replaceFirst("t:", "");
        }
        Calendar.Builder builder = new Calendar.Builder();
        try{
            String[] stringsDate = query.split("-");
            parseInput(stringsDate, builder);
            if(setTo){
                setToDB(builder.build().getTimeInMillis());
            }
        }
        catch(ArrayIndexOutOfBoundsException | NumberFormatException e){
            return String.valueOf(new Date(Long.parseLong(query.trim())));
        }
        return String.valueOf(builder.build().getTimeInMillis());
    }

    public String getStampFromDate(String workPos) {
        this.query = workPos;
        return getStampFromDate();
    }

    /**
     @param stringsDate массив ввода, делитель {@code -}.
     @param builder     new {@link Calendar.Builder}
     @return {@link Calendar.Builder} установленный на введённую дату-время.
     */
    private Calendar.Builder parseInput(String[] stringsDate, Calendar.Builder builder) {
        int year = Integer.parseInt(stringsDate[2]);
        int month = Integer.parseInt(stringsDate[1]) - 1;
        int day = Integer.parseInt(stringsDate[0]);
        int hour = Integer.parseInt(stringsDate[3]);
        int minute = Integer.parseInt(stringsDate[4]);

        builder.setDate(year, month, day);
        builder.setTimeOfDay(hour, minute, 0);
        return builder;

    }

    private void setToDB(long timeInMillis) {
        PROPS.setProperty("lasts", String.valueOf(timeInMillis));
    }

    public double countDoubles(List<Double> doubleList) {
        double resultDouble = 0d;
        for(Double aDouble : doubleList){
            resultDouble += aDouble;
        }
        return resultDouble;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleCalculator{");
        sb.append("query='").append(query).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
