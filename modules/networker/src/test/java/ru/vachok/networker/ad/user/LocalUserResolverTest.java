package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.info.InformationFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @see LocalUserResolver
 * @since 22.08.2019 (14:14)
 */
public class LocalUserResolverTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(LocalUserResolver.class.getSimpleName(), System.nanoTime());
    
    private LocalUserResolver userInfo = new LocalUserResolver();
    
    private ThreadPoolExecutor poolExecutor;
    
    @BeforeMethod
    public void initExecutor() {
        this.poolExecutor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
    }
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetPossibleVariantsOfPC() {
        try {
            Thread.sleep(500);
            List<String> variantsOfPC = userInfo.getLogins("do0134", 10);
            Assert.assertTrue(variantsOfPC.size() > 0, AbstractForms.fromArray(variantsOfPC));
        }
        catch (InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        this.userInfo = new LocalUserResolver();
        String infoAbout = userInfo.getInfoAbout("do0086");
        boolean isUser = infoAbout.contains("msc") || infoAbout.contains("d.yu.podbuckii");
        Assert.assertTrue(infoAbout.contains("msc"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        userInfo.setClassOption("do0086");
        String info = userInfo.getInfo();
        boolean isUser = info.contains("msc") || info.contains("d.yu.podbuckii");
        Assert.assertTrue(isUser, info);
        
        userInfo.setClassOption("do0091");
        info = userInfo.getInfo();
        System.out.println("info = " + info);
        userInfo.setClassOption("do0045");
        info = userInfo.getInfo();
        System.out.println("info = " + info);
    }
    
    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> varUsersList = userInfo.getLogins("do0212", 20);
        String varUsers = new TForms().fromArray(varUsersList, true);
        System.out.println("varUsers = " + varUsers);
    }
    
    @Test
    public void testTestToString() {
        userInfo.setClassOption("do0213");
        String toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("LocalUserResolver["), toStr);
        Assert.assertTrue(toStr.contains("pcName = do0213,"), toStr);
    }
    
    @Test
    public void testGetPCLogins() {
        String pcName = "do0045";
        
        try {
            List<String> logins1 = poolExecutor.submit(()->userInfo.getLogins(pcName, 1)).get();
            Assert.assertTrue(logins1.size() == 1, new TForms().fromArray(logins1));
        }
        catch (InterruptedException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        List<String> logins2 = userInfo.getLogins(pcName, 2);
        Assert.assertTrue(logins2.size() == 2, new TForms().fromArray(logins2));
    }
    
    @Test
    public void testUserDO0091() {
        InformationFactory userInFac051 = InformationFactory.getInstance("do0045.eatmeat.ru");
        String info451 = userInfo.getInfo();
        System.out.println("info451 = " + info451);
        InformationFactory userInFac = InformationFactory.getInstance("do0091.eatmeat.ru");
        String info = userInfo.getInfo();
        System.out.println("info91 = " + info);
        InformationFactory userInFac89 = InformationFactory.getInstance("do0089.eatmeat.ru");
        String info89 = userInfo.getInfo();
        System.out.println("info89 = " + info89);
        InformationFactory userInFac05 = InformationFactory.getInstance("do0045.eatmeat.ru");
        String info45 = userInfo.getInfo();
        System.out.println("info45 = " + info45);
        
        System.out.println("new TForms().fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()) = " + new TForms()
            .fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()));
        System.out
            .println("new TForms().fromArray(NetKeeper.getUsersScanWebModelMapWithHTMLLinks()) = " + new TForms().fromArray(NetKeeper.getPcNamesForSendToDatabase()));
    }
    
    @Test
    public void testBadCredentials() {
        String pcName = "do0";
        String info = userInfo.getInfo();
        Assert.assertTrue(info.contains("Unknown user:"), info);
        Assert.assertTrue(info.contains("LocalUserResolver["), info);
        
        info = userInfo.getInfoAbout(pcName);
        System.out.println("info = " + info);
    }
}