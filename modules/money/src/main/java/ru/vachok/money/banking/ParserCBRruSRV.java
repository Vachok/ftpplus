package ru.vachok.money.banking;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.money.ConstantsFor;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Stream;


/**
 <h1>Тянет курсы USD и EURO</h1>

 @see MoneyCtrl
 @see ParserCBRruSRV
 @since 12.08.2018 (16:12) */
@Service (ConstantsFor.PARSER_CB_RRU_SRV)
public class ParserCBRruSRV {

    /**
     {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParserCBRruSRV.class.getSimpleName());

    /**
     {@link Currencies}
     */
    private Currencies currencies;

    /**
     * Пользовательский ввод сколько денег
     */
    private String userMoney = "117500";

    /**
     Пользовательский ввод под какой процент
     */
    private String userPrc = "7.5";

    /**
     @return {@link #userPrc}
     */
    public String getUserPrc() {
        return userPrc;
    }

    /**
     @param userPrc_p {@link #userPrc}
     */
    public void setUserPrc(String userPrc_p) {
        userPrc = userPrc_p;
    }

    /**
     Используется в модели! Must be <b>public</b>
     <p>
     Строка пользовательского ввода. <br>
     Usages: {@link MoneyCtrl#getMoney(ParserCBRruSRV, Model, Currencies)}
     */
    @SuppressWarnings("WeakerAccess")
    public String getUserMoney() {
        return userMoney;
    }

    /**
     @param userInput {@link #userMoney}
     */
    public void setUserMoney(String userInput) {
        this.userMoney = userInput;
    }

    /**
     @param currencies {@link #currencies}
     */
    @Autowired
    public ParserCBRruSRV(Currencies currencies) {
        this.currencies = currencies;
    }

    /**
     Usages: {@link MoneyCtrl#money(Model)}

     @return {@link #currencies}.toString
     */
    String countYourMoney() {
        curDownloader();
        return currencies.toString() + "<p>" + noBanks();
    }

    private String noBanks() {
        float inEur = Float.parseFloat(userMoney) / Currencies.E_2014;
        float inEurNow = Float.parseFloat(userMoney) / currencies.getEuro();
        float inUSD = Float.parseFloat(userMoney) / Currencies.USD_2014;
        float inUSDNow = Float.parseFloat(userMoney) / currencies.getUsDollar();
        return "In euro - " + inEur + " (now - " + inEurNow + ")<br>In USD - " + inUSD + " (now - " + inUSDNow + ")<p>" + procCount() + procInVal(inUSD, inUSDNow, inEur, inEurNow);
    }

    private String procCount() {
        int year = Year.now().getValue();
        int years = year - 2014;
        float withP = ((Float.parseFloat(userMoney) / 100) * Float.parseFloat(userPrc)) * years;
        withP = withP + Float.parseFloat(userMoney);
        float dovloJ = 422899.46f - withP;
        float inUSD = withP / currencies.getUsDollar();
        float inE = withP / currencies.getEuro();
        return "With % - " + withP + "<p>" + "Dollars: " + inUSD + "<br>In EUROs: " + inE + "<br>Dovloj: " + dovloJ;
    }

    private String procInVal(float inUSDP, float inUSDNowP, float inEurP, float inEurNowP) {
        float dollarsDiff = inUSDNowP - inUSDP;
        float eurDiff = inEurNowP - inEurP;
        return "<p>In dollar " + dollarsDiff + " ( " + dollarsDiff * currencies.getUsDollar() + " rub) in euro " + eurDiff + " (" + eurDiff * currencies.getEuro() + " rub)";
    }

    void curDownloader() {
        try (InputStream inputStream = getUrl().openStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             OutputStream outputStream = new FileOutputStream("cbr.ru");
             BufferedReader bufferedReader = new BufferedReader(reader);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            while (bufferedReader.ready()) {
                Stream<String> lines = bufferedReader.lines();
                Object[] objects = lines.toArray();
                for (int i = 0; i < objects.length; i++) {
                    Object o = objects[i];
                    if (o.toString().contains("<td>USD</td>") || o.toString().contains("<td>EUR</td>")) {
                        for (int j = 0; j < 4; j++) {
                            printWriter.println(objects[i + j]);
                        }
                    }
                }
            }
            parseCur();
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private URL getUrl() {
        try {
            return new URL(ConstantsFor.URL_AS_STRING);
        } catch (MalformedURLException e) {
            LOGGER.info(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    private String parseCur() {
        StringBuilder stringBuilder = new StringBuilder();
        List<Float> floatList = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("cbr.ru");
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                String s = bufferedReader.readLine();
                if (s.contains(",")) {
                    Float v = Float.parseFloat(s
                        .replace(',', '.')
                        .replaceAll("<td>", "")
                        .replaceAll("</td>", ""));
                    floatList.add(v);
                }
                String msg = floatList.size() + " floats";
                LOGGER.info(msg);
            }
            float euro = Math.max(floatList.get(0), floatList.get(1));
            float usd = Math.min(floatList.get(0), floatList.get(1));
            stringBuilder
                .append(euro)
                .append(" EURO ; ")
                .append(usd)
                .append(" USD");
            currencies.setEuro(euro);
            currencies.setUsDollar(usd);
            return stringBuilder.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private List<String> parseList() throws XMLStreamException {
        throw new IllegalAccessError();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParserCBRruSRV{");
        sb.append("currencies=").append(currencies);
        sb.append(", userMoney='").append(userMoney).append('\'');
        sb.append(", userPrc='").append(userPrc).append('\'');
        sb.append('}');
        return sb.toString();
    }
}