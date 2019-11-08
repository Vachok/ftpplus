// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.message;


import org.jetbrains.annotations.Contract;
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
    
    MessageLocal MESSAGE_LOCAL = new MessageLocal("MessageLocal");
    
    String FILE = MessageFile.class.getTypeName();
    
    @Contract("null, !null -> new")
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static MessageToUser getInstance(String messengerType, String messengerHeader) {
        if (messengerHeader == null) {
            messengerHeader = MessageToUser.class.getSimpleName();
        }
        if (messengerType == null) {
            return new MessageLocal(messengerHeader, "null");
        }
        else if (messengerType.equals(LOCAL_CONSOLE)) {
            MESSAGE_LOCAL.setBodyMsg(MessageToUser.LOCAL_CONSOLE);
            MESSAGE_LOCAL.setTitleMsg(messengerHeader);
            return MESSAGE_LOCAL;
        }
        else if (messengerType.equals(TRAY)) {
            return MessageToTray.getInstance(messengerHeader);
        }
        else if (messengerType.equals(DB)) {
            return new DBMessenger(messengerHeader);
        }
        else if (messengerType.equalsIgnoreCase(SWING)) {
            return new ru.vachok.networker.restapi.message.MessageSwing(messengerHeader);
        }
        else if (messengerType.equalsIgnoreCase(FILE)) {
            return new MessageFileLocal(messengerHeader);
        }
        else {
            return new MessageLocal(messengerHeader);
        }
    }
    
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