package ru.vachok.networker.services;


import org.testng.annotations.Test;
import ru.vachok.messenger.MessageToUser;


/**
 @see DBMessenger
 @since 10.07.2019 (9:26) */
public class DBMessengerTest {
    
    
    private MessageToUser messageToUser = new DBMessenger(this.getClass().getSimpleName());
    
    @Test
    public void sendMessage() {
        messageToUser.info(getClass().getSimpleName());
    }
}