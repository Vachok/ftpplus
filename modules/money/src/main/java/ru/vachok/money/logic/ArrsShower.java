package ru.vachok.money.logic;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.DBMessage;

import java.util.*;


/**
 * @since 28.08.2018 (0:28)
 */
public class ArrsShower {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ArrsShower.class.getSimpleName();
    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new DBMessage();

    private List<String> fromArray = new ArrayList<>();


    public ArrsShower( List<String> fromArray ) {
        this.fromArray = fromArray;
    }

    /*Constru*/
    public ArrsShower(Object[] fromOBJArray) {
        Objects.requireNonNull(fromArray).add(Arrays.toString(fromOBJArray));
    }


    public String strFromArr() {
        String s = fromArray
            .toString()
            .replaceAll(", ", "<br>")
            .replace("\\Q]\\E", "")
            .replace("\\Q[\\E", "");
        return s;
    }
}