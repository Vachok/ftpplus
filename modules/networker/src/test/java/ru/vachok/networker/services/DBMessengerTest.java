package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageToUser;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see DBMessenger
 @since 10.07.2019 (9:26) */
public class DBMessengerTest {
    
    
    private MessageToUser messageToUser = new DBMessenger(this.getClass().getSimpleName());
    
    @Test
    public void sendMessage() {
        messageToUser.info(getClass().getSimpleName());
        checkFile();
    }
    
    private void checkFile() {
        File file = new File("sql.properties");
        Assert.assertTrue(file.exists());
        Assert.assertTrue((file.lastModified() + TimeUnit.MINUTES.toMillis(1)) > System.currentTimeMillis());
    }
}