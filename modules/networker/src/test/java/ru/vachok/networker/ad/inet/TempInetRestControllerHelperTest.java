package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;


public class TempInetRestControllerHelperTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(TempInetRestControllerHelper.class.getSimpleName(), System
        .nanoTime());

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
    public void testGetInetResult() {
        TempInetRestControllerHelper tempInetRestControllerHelper = new TempInetRestControllerHelper();
        JsonObject object = new JsonObject();
        object.add("ip", "8.8.8.8");
        object.add("hour", "1");
        object.add(ConstantsFor.OPTION, "add");
        object.add(ConstantsFor.WHOCALLS, "test");
        String resultAdd = tempInetRestControllerHelper.getInetResult(object);
        boolean assertion = resultAdd.contains("exist!") || resultAdd.contains("ok");
        Assert.assertTrue(assertion, resultAdd);
    }
}