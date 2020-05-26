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
    public void addInet() {
        String resultAdd = testGetInetResult(ConstantsFor.ADD);
        Assert.assertTrue(resultAdd.contains("INVALID USER"), resultAdd);
        Assert.assertTrue(resultAdd.split("}")[1].contains("8.8.8.8"), resultAdd);
    }

    private String testGetInetResult(String option) {
        TempInetRestControllerHelper tempInetRestControllerHelper = new TempInetRestControllerHelper();
        JsonObject object = new JsonObject();
        object.add("ip", "8.8.8.8");
        object.add("hour", "1");
        object.add(ConstantsFor.OPTION, option);
        object.add(ConstantsFor.WHOCALLS, "test");
        object.add(ConstantsFor.PARAM_NAME_CODE, Integer.MAX_VALUE);
        return tempInetRestControllerHelper.getResult(object);
    }

    @Test
    public void delInet() {
        String resultAdd = testGetInetResult(ConstantsFor.DELETE);
        Assert.assertTrue(resultAdd.contains("INVALID USER"), resultAdd);
        Assert.assertFalse(resultAdd.split("}")[1].contains("8.8.8.8"), resultAdd);
    }
}