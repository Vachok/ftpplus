package ru.vachok.money.components;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;


/**
 <h1>Тянет курсы USD и EURO</h1>

 @since 12.08.2018 (16:12) */
@Service("ParserCBRruSRV")
public class ParserCBRruSRV {


    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ParserCBRruSRV.class.getSimpleName());

    private static final String URL_AS_STRING = "http://cbr.ru/currency_base/daily/";

    private Currencies currencies;

    private String userInput = "Сколько руб. / Процент";

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

    /*Instances*/

    public String usdCur() {
        String usdToday = "USD Today";
        try {
            List<String> list = parseList();
        } catch (XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return usdToday;
    }

    private URL getUrl() {
        try {
            URL url = new URL(URL_AS_STRING);
            return url;
        } catch (MalformedURLException e) {
            LOGGER.info(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    private List<String> parseList() throws XMLStreamException {
        throw new IllegalAccessError();
    }
}