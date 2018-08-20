package ru.vachok.money.logic;



import ru.vachok.money.ctrls.ErrCtrl;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @since 20.08.2018 (23:23)
 */
public class Utilit {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = Utilit.class.getSimpleName();


    public static String thisPCName() {
        String ret = "no address...";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            ErrCtrl.stackErr(e);
        }
        return ret;
    }
}