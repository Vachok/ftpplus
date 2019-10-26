package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


/**
 @see LocalUserResolver
 @since 22.08.2019 (14:14) */
public class LocalUserResolverTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(LocalUserResolver.class.getSimpleName(), System.nanoTime());
    
    private LocalUserResolver localUserResolver = new LocalUserResolver();
    
    @BeforeMethod
    public void initExecutor() {
        this.localUserResolver = new LocalUserResolver();
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
            List<String> variantsOfPC = localUserResolver.getLogins("do0134", 10);
            Assert.assertTrue(variantsOfPC.size() > 0, AbstractForms.fromArray(variantsOfPC));
        }
        catch (InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        this.localUserResolver = new LocalUserResolver();
        String infoAbout = localUserResolver.getInfoAbout("do0086");
        boolean isUser = Stream.of("msc", "d.yu.podbuckii", "a.v.nikolaev").anyMatch(infoAbout::contains);
        Assert.assertTrue(isUser, infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        localUserResolver.setClassOption("do0086");
        String info = localUserResolver.getInfo();
        boolean isUser = info.contains("msc") || info.contains("d.yu.podbuckii") || info.contains("a.v.nikolaev");
        Assert.assertTrue(isUser, info);
    
        localUserResolver.setClassOption("do0091");
        info = localUserResolver.getInfo();
        Assert.assertEquals(info, "do0091 : vkuharenko");
    }
    
    @Test
    public void testToString() {
        localUserResolver.setClassOption("do0213");
        String toStr = localUserResolver.toString();
        Assert.assertTrue(toStr.contains("LocalUserResolver["), toStr);
        Assert.assertTrue(toStr.contains("pcName = do0213,"), toStr);
    }
    
    @Test
    public void testGetPCLogins() {
        String pcName = "do0045";
        try {
            List<String> logins1 = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->localUserResolver.getLogins(pcName, 1)).get();
            Assert.assertTrue(logins1.size() == 1, AbstractForms.fromArray(logins1));
            List<String> logins5 = localUserResolver.getLogins(pcName, 5);
            String strLogins5 = AbstractForms.fromArray(logins5);
            Assert.assertTrue(logins5.size() == 5, strLogins5);
            Assert.assertTrue(strLogins5.contains("\\\\do0045.eatmeat.ru\\c$\\Users"));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void userDO0091() {
        localUserResolver.setClassOption("do0091");
        String resolverInfo = localUserResolver.getInfo();
        Assert.assertEquals(resolverInfo, "do0091 : vkuharenko");
    }
    
    @Test
    public void badCredentials() {
        String pcName = "d00";
        String info = localUserResolver.getInfo();
        Assert.assertTrue(info.contains("Unknown user "), info);
        Assert.assertTrue(info.contains("LocalUserResolver["), info);
    
        info = localUserResolver.getInfoAbout(pcName);
    
        Assert.assertEquals(info, "Unknown user d00 : ResolveUserInDataBase[\n" +
            "aboutWhat = Unknown PC: d00.eatmeat.ru\n" +
            " class ru.vachok.networker.ad.pc.PCInfo,\n" +
            "dataConnectTo = MySqlLocalSRVInetStat{tableName='pcuserauto', dbName='velkom'}\n" +
            "] : \n" +
            "ru.vachok.networker.ad.user.UnknownUser.getInfoAbout(UnknownUser.java:45)\n" +
            "ru.vachok.networker.ad.user.ResolveUserInDataBase.getInfoAbout(ResolveUserInDataBase.java:95)\n" +
            "ru.vachok.networker.ad.user.LocalUserResolver.getInfoAbout(LocalUserResolver.java:227)\n" +
            "ru.vachok.networker.ad.user.LocalUserResolverTest.badCredentials(LocalUserResolverTest.java:120)\n");
    }
    
    @Test
    public void complexInfoAboutDO0213() {
        localUserResolver.setClassOption("do0213");
        String info = localUserResolver.getInfo();
        System.out.println("\n\ninfo = " + info);
        
        String infoAbout = localUserResolver.getInfoAbout("do0213");
        Assert.assertTrue(infoAbout.contains("do0213 : ikudryashov :"));
        
        List<String> userResolverLogins = localUserResolver.getLogins("do0213", 4);
        System.out.println(MessageFormat.format("\n\nuserResolverLogins({0}) =\n {1}", userResolverLogins.size(), AbstractForms.fromArray(userResolverLogins)));
    }
}