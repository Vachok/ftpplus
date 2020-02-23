package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
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
import ru.vachok.networker.net.ssh.SshActs;

import java.io.IOException;


/**
 @see RestCTRL */
public class RestCTRLTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(RestCTRLTest.class.getSimpleName(), System.nanoTime());

    private InformationFactory instance;

    private RestCTRL restCTRL;

    private static final String SRV_VPS = "http://194.67.86.51:8880/";

    private static final String SRV_RUPS = "http://rups00.eatmeat.ru:8880/";

    private static final String SRV_LOCAL = "http://10.10.111.65:8880/";

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

    @Test
    public void okTest() {
        Request.Builder builder = getBuilder(ConstantsFor.TEMPNET, SRV_LOCAL);
        RequestBody requestBody = RequestBody.create(getJSONObject().toString().getBytes());
        builder.post(requestBody);
        Call newCall = new OkHttpClient().newCall(builder.build());
        try (Response execute = newCall.execute();
             ResponseBody body = execute.body()) {
            String string = body != null ? body.string() : "null";
            boolean contains = string.contains("10.200.213.233") || string.contains(ConstantsFor.RULES);
            Assert.assertTrue(contains, string);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @NotNull
    private Request.Builder getBuilder(String urlPart, String srvName) {
        String local = new SshActs("delete", "http://www.velkomfood.ru").allowDomainAdd();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(srvName + urlPart);
//        builder.url("http://rups00.eatmeat.ru:8880/tempnet");
        builder.addHeader(ConstantsFor.AUTHORIZATION, "IAn51aza2rUeegZX6WIH8ozCkBP2");
        builder.addHeader("Content-Type", ConstantsFor.JSON);
        return builder;
    }

    @NotNull
    private static JsonObject getJSONObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("ip", "10.200.213.233");
        jsonObject.add("hour", "1");
        jsonObject.add(ConstantsFor.OPTION, "add");
        jsonObject.add(ConstantsFor.WHOCALLS, "ikudryashov@velkomfood.ru");
        return jsonObject;
    }

    @Test
    public void addDomainRESTTest() {
        Request.Builder builder = getBuilder(ConstantsFor.TEMPNET, SRV_LOCAL);
        JsonObject jsonObject = getJSONObject();
        jsonObject.set("ip", "add");
        jsonObject.set("hour", "-2");
        jsonObject.set(ConstantsFor.OPTION, "domain");
        jsonObject.set(ConstantsFor.WHOCALLS, "http://www.eatmeat.ru");
        RequestBody requestBody = RequestBody.create(jsonObject.toString().getBytes());
        builder.post(requestBody);
        Call call = new OkHttpClient().newCall(builder.build());
        try (Response execute = call.execute();
             ResponseBody responseBody = execute.body()) {
            Assert.assertFalse(responseBody.string().isEmpty());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void getSSHLists() {
        Request.Builder builder = getBuilder("getsshlists", SRV_LOCAL);
        Call newCall = new OkHttpClient().newCall(builder.build());
        try (Response execute = newCall.execute();
             ResponseBody body = execute.body()) {
            String string = body != null ? body.string() : null;
            System.out.println("string = " + string);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}