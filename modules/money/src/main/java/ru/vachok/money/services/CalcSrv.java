package ru.vachok.money.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.config.ConstantsFor;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 19.09.2018 (21:44) */
@Service ("CalcSrv")
public class CalcSrv {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalcSrv.class.getSimpleName();

    /**
     {@link }
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    private CalculatorForSome calculatorForSome = new CalculatorForSome();

    public String resultCalc(String userInp) {

        if(userInp.equalsIgnoreCase("time")){
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
            Object o = parseInp(userInp);
            String msg = o.toString() + "      |   Object returned";
            LOGGER.warn(msg);
            return o.toString();
        }
    }

    private String time(String userInp) {

        String s = System.currentTimeMillis() + " uTime<br>" + ( float ) TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP) / 60 + " min Uptime<br>" +
            new Date(ConstantsFor.START_STAMP);
        if(calculatorForSome.getuLong() > 0){
            s = s + "<br>" + new TimeWorks().fromMillisToDate(calculatorForSome.getuLong());
        }
        return s;
    }

    private String whoIs(String userInp) {
        WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
        String s = whoIsWithSRV.whoIs(userInp);
        return s;
    }

    private String helpS() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<p><h3><center>Помощь</h3></center></h3>");
        stringBuilder.append("Помоги себе сам, пока не готово.");
        return stringBuilder.toString();
    }

    private Object parseInp(String userInp) {
        String parsedInp;
        try{
            parsedInp = userInp.split(" -")[1].trim();
        }
        catch(ArrayIndexOutOfBoundsException e){
            return userInp + " , " + e.getMessage();
        }
        try{
            long parsLong = Long.parseLong(parsedInp);
            calculatorForSome.setuLong(parsLong);
            if(userInp.toLowerCase().contains("time")){
                return new Date(parsLong).toString();
            }
            else{
                return parsLong;
            }
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
            return mbDouble(parsedInp);
        }
    }

    private Object mbDouble(String parsedInp) {
        try{
            double parsedDouble = Double.parseDouble(parsedInp);
            calculatorForSome.setUserDouble(parsedDouble);
            return parsedDouble;
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
            return helpS();
        }
    }
}