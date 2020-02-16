package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import okhttp3.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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

import java.io.IOException;


/**
 @see RestCTRL */
public class RestCTRLTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(RestCTRLTest.class.getSimpleName(), System.nanoTime());

    private InformationFactory instance;

    private RestCTRL restCTRL;

    @BeforeMethod
    public void initInst() {
        this.instance = InformationFactory.getInstance(InformationFactory.REST_PC_UNIQ);
        this.restCTRL = new RestCTRL();
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
    public void testAppStatus() {
        String s = restCTRL.appStatus();
        Assert.assertTrue(s.contains("amd64 Arch"));
    }

    @Test
    public void uniqPC() {

        String info = instance.getInfo();
        Assert.assertTrue(info.contains("10.10.10.1"));
        instance.setClassOption(true);
        info = instance.getInfo();
        Assert.assertTrue(info.contains("{\"ip\":\"10.10.10.1\",\"pcname\":\"10.10.10.1\"}"));
    }

    @Test
    public void testFileShow() {
        String fS = restCTRL.fileShow(new MockHttpServletRequest());
        Assert.assertTrue(fS.contains("exit.last"));
    }

    @Test
    public void testDbInfoRest() {
        String dbInfo = restCTRL.dbInfoRest();
        System.out.println("dbInfo = " + dbInfo);
    }

    @Test
    public void testInetTemporary() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ConstantsFor.AUTHORIZATION, "IAn51aza2rUeegZX6WIH8ozCkBP2");
        request.setContentType(ConstantsFor.JSON);
        JsonObject jsonObject = getJSONObject();
        request.setContent(jsonObject.toString().getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        String s = restCTRL.inetTemporary(request, response);
        Assert.assertTrue(s.contains("10.200.213.233"));
    }

    private JsonObject getJSONObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("ip", "10.200.213.233");
        jsonObject.add("hour", "1");
        jsonObject.add(ConstantsFor.OPTION, "add");
        jsonObject.add(ConstantsFor.WHOCALLS, "ikudryashov@velkomfood.ru");
        return jsonObject;
    }

    @Test
    public void okTest() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url("http://rups00.eatmeat.ru:8880/tempnet");
        builder.addHeader(ConstantsFor.AUTHORIZATION, "IAn51aza2rUeegZX6WIH8ozCkBP2");
        builder.addHeader("Content-Type", ConstantsFor.JSON);
        RequestBody requestBody = RequestBody.create(getJSONObject().toString().getBytes());
        builder.post(requestBody);
        Call newCall = okHttpClient.newCall(builder.build());
        try (Response execute = newCall.execute();
             ResponseBody body = execute.body()) {
            String string = body != null ? body.string() : "null";
            Assert.assertTrue(string.contains("10.200.213.233"), string);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}