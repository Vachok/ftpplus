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
    
    MessageToUser DB_MESSENGER = new DBMessenger("init");
    
    MessageToUser SWING_M = new ru.vachok.networker.restapi.message.MessageSwing("init");
    
    MessageToUser FILE_LOC_M = new MessageFileLocal("init");
    
    @Contract("null, !null -> new")
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static MessageToUser getInstance(String messengerType, String messengerHeader) {
        if (messengerHeader == null) {
            messengerHeader = MessageToUser.class.getSimpleName();
        }
        if (messengerType == null) {
            return MESSAGE_LOCAL;
        }
        else if (messengerType.equals(LOCAL_CONSOLE)) {
            MESSAGE_LOCAL.setBodyMsg(MessageToUser.LOCAL_CONSOLE);
            MESSAGE_LOCAL.setTitleMsg(messengerHeader);
            return MESSAGE_LOCAL;
        }
        else if (messengerType.equals(TRAY)) {
            return MessageToTray.getInstance(messengerHeader);
        }
        else if (messengerType.equalsIgnoreCase(SWING)) {
            MESSAGE_LOCAL.setHeaderMsg(messengerHeader);
            return SWING_M;
        }
        else if (messengerType.equalsIgnoreCase(FILE)) {
            MESSAGE_LOCAL.setHeaderMsg(messengerHeader);
            return FILE_LOC_M;
        }
        else {
            return MESSAGE_LOCAL;
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