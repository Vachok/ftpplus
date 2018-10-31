package ru.vachok.money.calc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.services.TForms;
import ru.vachok.money.services.TimeWorms;
import ru.vachok.money.services.WhoIsWithSRV;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @since 19.09.2018 (21:44) */
@Service ("CalcSrv")
public class CalcSrv {

    /*Fields*/
    private CalculatorForSome calculatorForSome;

    /*Instances*/
    @Autowired
    public CalcSrv(CalculatorForSome calculatorForSome) {
        this.calculatorForSome = calculatorForSome;
    }

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalcSrv.class.getSimpleName();

    /**
     {@link }
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    public String resultCalc(String userInp) {
        if(userInp.toLowerCase().contains("time")){
            return time(userInp);
        }
        if(userInp.toLowerCase().contains("whois:") || userInp.toLowerCase().contains("wh:")){
            userInp = userInp.split(":")[1].trim();
            return whoIs(userInp);
        }
        if(userInp.equalsIgnoreCase("?")){
            return helpS();
        }
        else{
            return parseInp(userInp);
        }
    }

    private String time(String userInp) {
        try{
            userInp = userInp.split(" ")[1];
        }
        catch(ArrayIndexOutOfBoundsException e){
            return new Date().toString() + " = " + new Date().getTime() +
                "<br>" +
                new Date(ConstantsFor.START_STAMP) +
                " start time" +
                "<br>Uptime = " + ( float ) TimeUnit
                .MILLISECONDS
                .toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP) / ConstantsFor.ONE_HOUR;
        }
        long timeLong = Long.parseLong(userInp);
        String s = System.currentTimeMillis() + " uTime<br>" + ( float ) TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP) / 60 + " min Uptime<br>" +
            new Date(ConstantsFor.START_STAMP);
        if(calculatorForSome.getuLong() > 0){
            s = s + "<br>" + new TimeWorms().fromMillisToDate(calculatorForSome.getuLong());
        }
        s = s + "<br>" + new Date(timeLong).toString() + " result";
        return s;
    }

    private String whoIs(String userInp) {
        WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
        return whoIsWithSRV.whoIs(userInp);
    }

    private String helpS() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<p><h3><center>Помощь</h3></center></h3>");
        stringBuilder.append("Помоги себе сам, пока не готово.");
        return stringBuilder.toString();
    }

    public String parseInp(String userInp) {
        List<String> parsedInp = new ArrayList<>();
        List<Double> doubleList = new ArrayList<>();
        String[] toParse = {"\\+", "\\-", "\\:", "\\*"};
        try{
            for(String s : toParse){
                parsedInp.addAll(Arrays.asList(userInp.split(s)));
            }
            if(parsedInp.isEmpty()){
                throw new ArrayStoreException("Nothing to parse");
            }
        }
        catch(ArrayIndexOutOfBoundsException e){
            return e.getMessage() + "<p>" + new TForms().toStringFromArray(e);
        }
        try{
            for(String s : parsedInp){
                double parsDouble = Double.parseDouble(s);
                doubleList.add(parsDouble);
            }
            calculatorForSome.setUserDouble(doubleList);
            return Arrays.toString(calculatorForSome.getUserDouble().toArray());
        }
        catch(Exception ignore){
            //
        }
        return "";
    }

    public String destinyGetter(String userInp) {
        String[] split;
        try{
            split = userInp.split(" or ");
            String destinyCooser = new ChooseYouDestiny().destinyCooser(split);
            destinyCooser = destinyCooser.toUpperCase();
            return destinyCooser;
        }
        catch(ArrayIndexOutOfBoundsException e){
            split = userInp.split(" ");
            return new ChooseYouDestiny().destinyCooser(split) + "<p><textarea>" + e.getMessage() + "</textarea>";
        }
    }
}