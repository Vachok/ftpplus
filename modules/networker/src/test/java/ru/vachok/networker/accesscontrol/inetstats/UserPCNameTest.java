// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.text.MessageFormat;
import java.util.concurrent.RejectedExecutionException;


/**
 @since 09.06.2019 (21:30) */
@SuppressWarnings("ALL")
public class UserPCNameTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testGetInfoAbout() {
        UserPCName userPCName = new UserPCName();
        String usageInet = "null";
        try {
            usageInet = userPCName.getInfoAbout(OtherKnownDevices.DO0213_KUDR);
            Assert.assertEquals(usageInet, checkIF());
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat
                .format("InetUserPCNameTest.testGetUsage {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        Assert.assertTrue(usageInet.contains("DENIED SITES:"), usageInet);
    }
    
    private String checkIF() {
        informationFactory.setClassOption(OtherKnownDevices.DO0213_KUDR);
        return informationFactory.getInfoAbout(OtherKnownDevices.DO0213_KUDR);
    }
}