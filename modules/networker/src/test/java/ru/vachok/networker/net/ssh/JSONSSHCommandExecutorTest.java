package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.RestApiHelper;


public class JSONSSHCommandExecutorTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(JSONSSHCommandExecutorTest.class.getSimpleName(), System
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
        RestApiHelper sshExec = RestApiHelper.getInstance(RestApiHelper.SSH);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("command", "ls");
        String result = sshExec.getResult(jsonObject);
        Assert.assertTrue(result.contains("BAD AUTH!"), result);
    }
}