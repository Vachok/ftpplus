package ru.vachok.money.services;



import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.money.config.AppComponents;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * <h1>Тянет курсы USD и EURO</h1>
 *
 * @since 12.08.2018 (16:12)
 */
@Service("cbrfparsing")
public class ParserCBRru {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ParserCBRru.class.getSimpleName();

    private static Logger logger = AppComponents.getLogger();


    private ParserCBRru() {
    }


    public static ParserCBRru getParser() {
        return new ParserCBRru();
    }


    public String usdCur() {
        parseCbr();
        return "OOPS...";
    }


    private void parseCbr() {
        URL url;

        try {
            url = new URL("http://cbr.ru/currency_base/daily/");
            try (InputStream inputStream = url.openStream()) {
                while (inputStream.available() > 0) {

                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage() , e);
        }
    }


    public String euroCur() {
        throw new UnsupportedOperationException();
    }
}