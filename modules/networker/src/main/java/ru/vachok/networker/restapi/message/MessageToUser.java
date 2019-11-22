// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageFile;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;


/**
 @see MessageToUserTest */
public interface MessageToUser extends ru.vachok.messenger.MessageToUser {
    
    
    String DB = DBMessenger.class.getTypeName();
    
    String TRAY = MessageToTray.class.getTypeName();
    
    String LOCAL_CONSOLE = MessageLocal.class.getTypeName();
    
    String NULL = "null";
    
    String SWING = ru.vachok.networker.restapi.message.MessageSwing.class.getTypeName();
    
    String FILE = MessageFile.class.getTypeName();
    
    String EMAIL = "MessageEmail";
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("null, !null -> new")
    static @NotNull MessageToUser getInstance(String messengerType, @NotNull String messengerHeader) {
        final MessageToUser messageToUser;
        
        
        if (messengerType == null) {
            messageToUser = new MessageLocal(messengerHeader);
            return messageToUser;
        }
        else if (messengerType.equals(LOCAL_CONSOLE)) {
            messageToUser = new MessageLocal(messengerHeader);
            return messageToUser;
        }
        else if (messengerType.equals(TRAY)) {
            messageToUser = MessageToTray.getInstance(messengerHeader);
            messageToUser.setHeaderMsg(messengerHeader);
            return messageToUser;
        }
        else if (messengerType.equalsIgnoreCase(SWING)) {
            messageToUser = new ru.vachok.networker.restapi.message.MessageSwing(messengerHeader);
            return messageToUser;
        }
        else if (messengerType.equalsIgnoreCase(FILE)) {
            messageToUser = new MessageFileLocal(messengerHeader);
            return messageToUser;
        }
        else if (messengerType.equalsIgnoreCase(EMAIL)) {
            messageToUser = new MessageEmail(messengerHeader);
            return messageToUser;
        }
        else {
            return new MessageLocal(messengerHeader);
        }
    }
    
    void setHeaderMsg(String headerMsg);
    
    @Override
    default void infoTimer(int timeOut, String headerMsg) {
        MessageSwing messageSwing = AppComponents.getMessageSwing(headerMsg);
        messageSwing.infoTimer(timeOut, headerMsg);
    }
    
    @Override
    default String confirm(String s, String s1, String s2) {
        throw new InvokeIllegalException("06.08.2019 (11:39)");
    }
}