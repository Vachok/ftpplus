// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;


/**
 @since 26.09.2018 (13:47) */
@Service
public class SimpleCalculator {

    private String query;
    
    private static final Properties PROPS = AppComponents.getProps();
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getStampFromDate(String workPos) {
        this.query = workPos;
        return getStampFromDate();
    }

    /**
     @return или timeInMillis от введённой даты, или новая дата из timeInMillis
     */
    private String getStampFromDate() {
        boolean setTo = query.toLowerCase().contains("s");
        if(setTo){
            query = query.replaceFirst(ConstantsFor.COMMAND_CALCTIMES, "");
        } else if (query.contains("calc")) {
            query = query.replaceFirst(ConstantsFor.COMMAND_CALCTIME, "");
        } else {
            query = query.replaceFirst("[Tt]+:", "");
        }
        Calendar.Builder builder = new Calendar.Builder();
        try{
            String[] stringsDate = query.split("-");
            messageToUser.info(getClass().getSimpleName() + ".getStampFromDate", "", " = " + parseInput(stringsDate, builder));
            if(setTo){
                setToDB(builder.build().getTimeInMillis());
            }
        }
        catch(ArrayIndexOutOfBoundsException | NumberFormatException e){
            return String.valueOf(new Date(Long.parseLong(query.trim())));
        }
        return String.valueOf(builder.build().getTimeInMillis());
    }

    /**
     @param stringsDate массив ввода, делитель {@code -}.
     @param builder     new {@link Calendar.Builder}
     @return {@link Calendar.Builder} установленный на введённую дату-время.
     */
    private Calendar.Builder parseInput(String[] stringsDate, Calendar.Builder builder) throws RuntimeException {
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
        PROPS.setProperty(PropertiesNames.PR_LASTS, String.valueOf(timeInMillis));
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
