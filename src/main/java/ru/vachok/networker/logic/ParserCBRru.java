package ru.vachok.networker.logic;



import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <h1>Тянет курсы USD и EURO</h1>
 *
 * @since 12.08.2018 (16:12)
 */
public class ParserCBRru {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ParserCBRru.class.getSimpleName();
    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new MessageCons();


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
        byte siteBytes[] = new byte[ConstantsFor.MBYTE];
        try {
            url = new URL("http://cbr.ru/currency_base/daily/");
            try (InputStream inputStream = url.openStream()) {
                while (inputStream.available() > 0) {

                }
            }
        } catch (IOException e) {
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING , String.format("%s%n%n%s" , e.getMessage() , Arrays.toString(e.getStackTrace()).replaceAll(", " , "\n").replace("{" , "").replace("}" , "")));
            new ru.vachok.networker.logic.ErrControl().internError(e);
        }
    }


    public String euroCur() {
        throw new UnsupportedOperationException();
    }
}