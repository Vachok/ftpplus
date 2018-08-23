package ru.vachok.money.logic;



import org.slf4j.Logger;
import ru.vachok.money.ApplicationConfiguration;

import java.io.UnsupportedEncodingException;


/**
 * @since 20.08.2018 (23:43)
 */
public class W1251 implements DecoderEnc {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = W1251.class.getSimpleName();


    @Override
    public String toAnotherEnc( String s ) {
        try {
            return new String(s.getBytes() , "Windows-1251");
        } catch (UnsupportedEncodingException e) {
            Logger logger = new ApplicationConfiguration().getLogger();
            logger.error(e.getMessage() , e);
        }
        return s;
    }
}