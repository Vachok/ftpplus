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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Stream;


/**
 <h1>Тянет курсы USD и EURO</h1>

 @see MoneyCtrl
 @see ParserCBRruSRV
 @since 12.08.2018 (16:12) */
@Service("ParserCBRruSRV")
public class ParserCBRruSRV {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserCBRruSRV.class.getSimpleName());

    private Currencies currencies;

    private String userInput = "0 / 0";

    /**
     Используется в модели! Must be <b>public</b>
     <p>
     Строка пользовательского ввода. <br>
     Usages: {@link MoneyCtrl#getMoney(ParserCBRruSRV, Model, Currencies)}
     */
    @SuppressWarnings("WeakerAccess")
    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    @Autowired
    public ParserCBRruSRV(Currencies currencies) {
        this.currencies = currencies;
    }

    public String countYourMoney() {
        curDownloader();
        String[] stringsInput = userInput.split(" / ");
        currencies.setHowManyWas(Float.parseFloat(stringsInput[0]));
        currencies.setPercentBanka(Float.parseFloat(stringsInput[1]));
        return currencies.toString();
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
}