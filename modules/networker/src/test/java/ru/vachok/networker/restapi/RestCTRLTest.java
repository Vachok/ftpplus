package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import okhttp3.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
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
        request.addHeader("Authorization", "Bearer 123");
        request.setContentType("application/json");
        JsonObject jsonObject = getJSONObject();
        request.setContent(jsonObject.toString().getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        restCTRL.inetTemporary(request, response);
    }
    
    private JsonObject getJSONObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("ip", "10.200.213.233");
        jsonObject.add("hour", "1");
        jsonObject.add("option", "add");
        jsonObject.add("whocalls", "ikudryashov@velkomfood.ru");
        return jsonObject;
    }
    
    @Test
    public void okTest() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url("http://172.16.200.18:8880/tempnet");
        builder.addHeader("Authorization", "Bearer 123");
        builder.addHeader("contentType", "application/json");
        RequestBody requestBody = RequestBody.create(getJSONObject().toString().getBytes());
        builder.post(requestBody);
        Call newCall = okHttpClient.newCall(builder.build());
        try (Response execute = newCall.execute()) {
            String string = execute.body().string();
            System.out.println("string = " + string);
        }
        catch (IOException e) {
            System.err.println(AbstractForms.fromArray(e));
        }
    }
}