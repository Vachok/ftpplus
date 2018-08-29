package ru.vachok.networker.logic;


import ru.vachok.messenger.MessageToUser;

import java.util.Date;
import java.util.List;


/**
 @since 29.08.2018 (22:21) */
public class StringFromArr {


    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = StringFromArr.class.getSimpleName();

    /*Constru*/
    public StringFromArr() {
        MessageToUser messageToUser = new FileMessenger();
        messageToUser.info(SOURCE_CLASS, "START", new Date().toString());
    }

    public String fromArr(StackTraceElement[] stackTrace) {
        StringBuilder stringBuilder = new StringBuilder();
        for(StackTraceElement stackTraceElement : stackTrace){
            stringBuilder.append(stackTraceElement.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String fromArr(List<String> list) {
        return list.toString().replaceAll(", ", "<br>")
            .replace("\\Q:\\E", "")
            .replace("\\Q[\\E", "");
    }

}