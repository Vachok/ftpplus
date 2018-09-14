package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.other.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * <h1>Тянет курсы USD и EURO</h1>
 *
 * @since 12.08.2018 (16:12)
 */
@Service("parsercb")
public class ParserCBRru {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ParserCBRru.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();

    private final String spec = "http://cbr.ru/currency_base/daily/";

    private URL url = getUrl();

    private URL getUrl() {
        URL url = null;
        try {
            url = new URL(spec);
            return url;
        } catch (MalformedURLException e) {
            LOGGER.info(e.getMessage(), e);
        }
        throw new IllegalArgumentException();
    }

    private ParserCBRru() {
    }

    public static ParserCBRru getParser() {
        return new ParserCBRru();
    }

    public String usdCur() {
        String usdToday = "USD Today";
        try {
            List<String> list = parseList();
        } catch (XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return usdToday;
    }

    private String parseCbr() {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            url = new URL(spec);
            try (InputStream inputStream = url.openStream();
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                byte[] bytes = new byte[ConstantsFor.MEGABYTE];
                while (inputStream.available() > 0) {
                    int read = bufferedInputStream.read(bytes);

                }
                stringBuilder.append(new String(bytes, StandardCharsets.UTF_8));
                return stringBuilder.toString();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "No Currency!";
    }


    public String euroCur() {
        return "No EURO";
    }

    public List<String> parseList() throws XMLStreamException {
        List<String> list = new ArrayList<>();
        try (InputStream inputStream = url.openStream()) {
            StaxStreamProcessor staxStreamProcessor = new StaxStreamProcessor(inputStream);
            list = staxStreamProcessor.readAllTagNames();
            LOGGER.info(new TForms().toStringFromArray(list));
            return list;
        } catch (IOException e) {
            LOGGER.info(e.getMessage(), e);
        }
        return list;
    }
}