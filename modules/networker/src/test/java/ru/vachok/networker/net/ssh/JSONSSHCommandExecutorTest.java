package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.restapi.RestApiHelper;

import java.io.File;


public class JSONSSHCommandExecutorTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(JSONSSHCommandExecutorTest.class.getSimpleName(), System
        .nanoTime());

    private static final File FILE_TEST_JSON = new File("for_test.txt");

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
        Assert.assertTrue(FILE_TEST_JSON.exists());
        RestApiHelper sshExec = RestApiHelper.getInstance(RestApiHelper.SSH);
        JsonObject jsonObject = Json.parse(FileSystemWorker.readRawFile(FILE_TEST_JSON.getAbsolutePath())).asObject();
        String result = sshExec.getResult(jsonObject);
        Assert.assertTrue(result.contains("!_passwords.xlsx"), result);
        jsonObject.add(ConstantsFor.JSON_PARAM_NAME_CODE, "2001");
        result = sshExec.getResult(jsonObject);
        Assert.assertTrue(result.contains("\"ls\""), result);
        jsonObject.add(ConstantsFor.JSON_PARAM_NAME_SERVER, OtherKnownDevices.SRV_INETSTAT);
        result = sshExec.getResult(jsonObject);
        Assert.assertTrue(result.contains("\"server\":\"srv-inetstat.eatmeat.ru\""), result);
    }
}