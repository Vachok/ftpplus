package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.EURO;
import ru.vachok.money.components.UsDollar;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.other.StaxStreamProcessor;
import ru.vachok.money.other.XmlNode;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;


/**
 * <h1>Тянет курсы USD и EURO</h1>
 *
 * @since 12.08.2018 (16:12)
 */
@Service("ParserCBRru")
public class ParserCBRru {

    /*Fields*/

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ParserCBRru.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();

    private URL url = getUrl();

    private String userInput = "Сколько руб. / Процент";

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public static String getWelcomeNewUser() {
        return welcomeNewUser;
    }

    private static final String URL_AS_STRING = "http://cbr.ru/currency_base/daily/";

    private static final float USD_2014 = 34.26f;

    private static final float E_2014 = 46.8985f;

    private static String welcomeNewUser = USD_2014 + " usd, " + E_2014 + " EURO in long time ago...";

    public static ParserCBRru getParser() {
        LOGGER.info(welcomeNewUser);
        ParserCBRru parserCBRru = new ParserCBRru();
        UsDollar usDollar = new UsDollar();
        EURO euro = new EURO();
        euro.setDay("26");
        usDollar.setDay("26");
        euro.setMonth("JAN");
        usDollar.setMonth("JAN");
        euro.setYear("2014");
        usDollar.setYear("2014");
        euro.setPrice(E_2014);
        usDollar.setPrice(USD_2014);

        ConstantsFor.CONTEXT.registerBean("usd", UsDollar.class);
        ConstantsFor.CONTEXT.registerBean("euro", EURO.class);

        LOGGER.info(new TForms().toStringFromArray(ConstantsFor.CONTEXT.getBeanDefinitionNames()));
        return parserCBRru;

    }

    private URL getUrl() {

        try {
            url = new URL(URL_AS_STRING);
            return url;
        } catch (MalformedURLException e) {
            LOGGER.info(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    /*Instances*/
    private ParserCBRru() {
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

    private List<String> parseList() throws XMLStreamException {
        throw new IllegalAccessError();
    }

    public String parseTag(String table) {
        String retTag = "";
        try {
            StaxStreamProcessor staxStreamProcessor = new StaxStreamProcessor(getUrl().openStream());
            retTag = staxStreamProcessor.readAttribute(table);
        } catch (XMLStreamException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return e.getMessage() + "\n" + new TForms().toStringFromArray(e);
        }
        return retTag;
    }

    public Map<Integer, XmlNode> getMap() {
        try {
            StaxStreamProcessor staxStreamProcessor = new StaxStreamProcessor(getUrl().openStream());
            Map<Integer, XmlNode> integerXmlNodeMap = staxStreamProcessor.buildXmlElementsTree();
            if (integerXmlNodeMap.size() > 2) {
                return integerXmlNodeMap;
            } else {
                throw new IllegalStateException();
            }
        } catch (XMLStreamException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    public String euroCur() {
        return "No EURO";
    }

    private String parseCbr() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            url = new URL(URL_AS_STRING);
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
}