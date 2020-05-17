package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;

import java.util.List;
import java.util.stream.Stream;


/**
 @see ResolveUserInDataBase
 @since 22.08.2019 (9:14) */
public class ResolveUserInDataBaseTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ResolveUserInDataBase.class.getSimpleName(), System
        .nanoTime());

    private ResolveUserInDataBase resolveUserInDataBase;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @BeforeMethod
    public void setResolveUserInDataBase() {
        this.resolveUserInDataBase = new ResolveUserInDataBase();
    }

    @Test
    public void testToString() {
        Assert.assertTrue(resolveUserInDataBase.toString().contains("ResolveUserInDataBase["), resolveUserInDataBase.toString());
    }

    @Test
    public void testGetInfoAbout() {
        Assert.assertFalse(ConstantsFor.argNORUNExist());
        String infoAbout = resolveUserInDataBase.getInfoAbout("no0015.eatmeat.ru");
        boolean strOk = Stream.of("msc", "d.yu.podbuckii", "mdc", "a.s.cedilin", "n.levitskaya").anyMatch(infoAbout::contains);
        Assert.assertTrue(strOk, infoAbout);
        testAbstract();
    }

    @Test
    public void testGetLogins() {
        List<String> loginsPC = resolveUserInDataBase.getLogins("do0132", 1);
        String logStr = AbstractForms.fromArray(loginsPC);
        Assert.assertTrue(logStr.contains("do0132"), logStr);
        Assert.assertTrue(logStr.contains("mdc") || logStr.contains("a.a.redkin"), logStr);
        List<String> kudrLogins = resolveUserInDataBase.getLogins("mdc", 1);
        String logStrKudr = AbstractForms.fromArray(kudrLogins);
        Assert.assertEquals(logStr, logStr);

    }

    private void testAbstract() {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String infoAbout = informationFactory.getInfoAbout("a323.eatmeat.ru");
        Assert.assertTrue(infoAbout.contains(": tbabicheva"), infoAbout);
    }

    @Test
    public void testGetBadCred() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("j.doe");
        Assert.assertTrue(infoAbout.contains("Unknown"), infoAbout);
        Assert.assertTrue(infoAbout.contains("j.doe"), infoAbout);
    }

    @Test
    public void testGetInfo() {
        Assert.assertFalse(ConstantsFor.argNORUNExist());
        this.resolveUserInDataBase.setClassOption("homya");
        String info = resolveUserInDataBase.getInfo();
        Assert.assertEquals(info, "10.200.217.83");
    }

    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> do0001 = ((UserInfo) resolveUserInDataBase).getLogins("do0001", 10);
        Assert.assertTrue(do0001.size() > 0);
        String listAsStr = AbstractForms.fromArray(do0001);
        Assert.assertFalse(listAsStr.isEmpty());
        Assert.assertTrue(listAsStr.contains("do0001"), listAsStr);
    }
}