package ru.vachok.money.services;


import org.slf4j.Logger;
import ru.vachok.money.ConstantsFor;

import java.io.UnsupportedEncodingException;


/**
 * @since 20.08.2018 (23:43)
 */
public class W1251 implements DecoderEnc {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = W1251.class.getSimpleName();

    private static final Logger LOGGER = ConstantsFor.getLogger();


    @Override
    public String toAnotherEnc( String s ) {
        try {
            return new String(s.getBytes() , "Windows-1251");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return s;
    }
}