package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.RestApiHelper;


public class AllowDomainHelperTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(AllowDomainHelper.class.getSimpleName(), System
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
    public void testGetResult() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(ConstantsFor.DOMAIN, "https://www.eatmeat.ru/");
        jsonObject.add("option", "add");
        String result = RestApiHelper.getInstance(RestApiHelper.DOMAIN).getResult(jsonObject);
        Assert.assertTrue(result.contains("Bad AUTH for"), result);
    }
}