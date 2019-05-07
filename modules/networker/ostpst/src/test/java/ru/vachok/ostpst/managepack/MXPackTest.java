package ru.vachok.ostpst.managepack;


import org.testng.annotations.Test;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;


public class MXPackTest {
    
    
    @Test
    public void mxPackTest() {
        MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
        messageToUser.info(getClass().getSimpleName() + ".mxPackTest", "true", " = " + true);
    }
}