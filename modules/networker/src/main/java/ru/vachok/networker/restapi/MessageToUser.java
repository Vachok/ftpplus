// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessagesNull;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;


/**
 @see MessageToUserTest */
public interface MessageToUser extends ru.vachok.messenger.MessageToUser {
    
    
    String DB = DBMessenger.class.getTypeName();
    
    String TRAY = MessageToTray.class.getTypeName();
    
    String LOCAL_CONSOLE = MessageLocal.class.getTypeName();
    
    String SWING = "MessageSwing.class.getTypeName()";
    
    String NULL = "null";
    
    @Contract("null, !null -> new")
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static MessageToUser getInstance(String messengerType, String messengerHeader) {
        if (messengerHeader == null) {
            messengerHeader = MessageToUser.class.getSimpleName();
        }
        
        if (messengerType == null) {
            return new MessageLocal(messengerHeader);
        }
        else if (messengerType.equals(LOCAL_CONSOLE)) {
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
    
    @Contract("_, _ -> new")
    static @NotNull ru.vachok.messenger.MessageToUser getLibInstance(@NotNull String messengerType){
        switch (messengerType){
            case SWING:
                return new MessageSwing();
            default:
                return new MessagesNull();
        }
    }
    
    @Override
    default String confirm(String s, String s1, String s2) {
        throw new InvokeIllegalException("06.08.2019 (11:39)");
    }
    
    @Override
    default void infoTimer(int i, String s) {
        throw new InvokeIllegalException("21.08.2019 (10:51)");
    }
}