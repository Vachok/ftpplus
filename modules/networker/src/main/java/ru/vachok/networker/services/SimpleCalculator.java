package ru.vachok.networker.services;


import org.springframework.stereotype.Service;
import ru.vachok.networker.TForms;

import java.util.Calendar;
import java.util.List;


/**
 @since 26.09.2018 (13:47) */
@Service()
public class SimpleCalculator {

    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public double countDoubles(List<Double> doubleList) {
        double resultDouble = 0d;
        for (Double aDouble : doubleList) {
            resultDouble += aDouble;
        }
        return resultDouble;
    }

    public String getStampFromDate(String workPos) {
        this.query = workPos;
        return getStampFromDate();
    }

    public String getStampFromDate() {
        query = query.replaceFirst("calctime: ", "");
        try {
            String[] stringsDate = query.split("-");
            Calendar.Builder builder = new Calendar.Builder();
            builder.setDate(Integer.parseInt(stringsDate[2]), Integer.parseInt(stringsDate[1]) - 1, Integer.parseInt(stringsDate[0]));
            stringsDate = query.split("\\Q%3A\\E");
            builder.setTimeOfDay(Integer.parseInt(stringsDate[0]), Integer.parseInt(stringsDate[1]), 0);
            return String.valueOf(builder.build().getTimeInMillis());
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return e.getMessage() + "<br>" + new TForms().fromArray(e, true);
        }
    }
}
