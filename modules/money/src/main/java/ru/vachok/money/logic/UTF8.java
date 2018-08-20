package ru.vachok.money.logic;



import ru.vachok.money.ctrls.ErrCtrl;

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
            ErrCtrl.stackErr(e);
        }
        return s;
    }
}