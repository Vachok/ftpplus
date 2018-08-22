package ru.vachok.money.logic;



import org.slf4j.Logger;
import ru.vachok.money.ApplicationConfiguration;

import java.io.UnsupportedEncodingException;


/**
 * @since 20.08.2018 (23:13)
 */
public class UTF8 implements DecoderEnc {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = UTF8.class.getSimpleName();


    @Override
    public String toAnotherEnc( String s ) {
        try {
            return new String(s.getBytes() , "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger logger = new ApplicationConfiguration().getLogger();
            logger.error(e.getMessage() , e);
        }
        return s;
    }
}