package ru.vachok.money.banking;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.services.TForms;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;


/**
 <h1>Тянет курсы USD и EURO</h1>

 @since 12.08.2018 (16:12) */
@Service ("ParserCBRruSRV")
public class ParserCBRruSRV {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ParserCBRruSRV.class.getSimpleName());

    private Currencies currencies;

    private String userInput = "Сколько руб. / Процент";

    /**
     <i>Используется в модели! Must be <b>public</b></i>.

     @return {@link MoneyCtrl#getMoney(ParserCBRruSRV, Model, BindingResult, HttpServletRequest)}
     */
    @SuppressWarnings ("WeakerAccess")
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

    String usdCur() {
        List<String> readiedXML = new ArrayList<>();
        try(InputStream inputStream = getUrl().openStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader)){
            while(reader.ready()){
                readiedXML.add(br.readLine());
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        return new TForms().toStringFromArray(readiedXML, true);
    }

    private URL getUrl() {
        try{
            URL url = new URL(ConstantsFor.URL_AS_STRING);
            return url;
        }
        catch(MalformedURLException e){
            LOGGER.info(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    private List<String> parseList() throws XMLStreamException {
        throw new IllegalAccessError();
    }
}