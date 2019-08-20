// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;

import java.util.Collection;


public interface MessageToUser extends ru.vachok.messenger.MessageToUser {
    
    
    String DB = DBMessenger.class.getTypeName();
    
    String TRAY = MessageToTray.class.getTypeName();
    
    String LOCAL_CONSOLE = MessageLocal.class.getTypeName();
    
    default void info(Collection<?> fromCollection) {
        Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
        logger.info(new TForms().fromArray(fromCollection));
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static MessageToUser getInstance(@NotNull String messengerType, String messengerHeader) {
        if (messengerType.equals(LOCAL_CONSOLE)) {
            return new MessageLocal(messengerHeader);
        }
        else if (messengerType.equals(TRAY)) {
            return MessageToTray.getInstance(messengerHeader);
        }
        else if (messengerType.equals(DB)) {
            return DBMessenger.getInstance(messengerHeader);
        }
        else {
            return new MessageLocal(messengerHeader);
        }
    }
    
    @Override
    default String confirm(String s, String s1, String s2) {
        throw new InvokeIllegalException("06.08.2019 (11:39)");
    }
}