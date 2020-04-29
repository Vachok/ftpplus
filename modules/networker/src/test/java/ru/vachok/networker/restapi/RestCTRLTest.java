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
import java.text.MessageFormat;
import java.util.stream.Stream;


/**
 @see RestCTRL */
public class RestCTRLTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(RestCTRLTest.class.getSimpleName(), System.nanoTime());

    private static final String SRV_VPS = "http://194.67.86.51:8880/";

    private static final String SRV_RUPS = "http://rups00.eatmeat.ru:8880/";

    private static final String SRV_LOCAL = "http://10.10.111.65:8880/";

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
        System.out.println("s = " + s);
        Assert.assertTrue(s.contains("amd64 Arch"));
    }

    @Test
    public void testUniqPC() {
        String info = instance.getInfo();
        Assert.assertTrue(info.contains("10.10.10.1"));
        instance.setClassOption(true);
        info = instance.getInfo();
        Assert.assertTrue(info.contains("{\"ip\":\"10.10.10.1\",\"pcname\":\"10.10.10.1\"}"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("do0213");
        String do0213Info = restCTRL.uniqPC(request);
        Assert.assertTrue(do0213Info.contains("ikudryashov"), do0213Info);
        System.out.println(do0213Info);
        MockHttpServletRequest requestIP = new MockHttpServletRequest();
        requestIP.setQueryString("10.200.213.85");
        String ip21385Info = restCTRL.uniqPC(request);
        Assert.assertTrue(do0213Info.contains("ikudryashov"), ip21385Info);
        System.out.println(ip21385Info);
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
        Assert.assertTrue(s.contains("INVALID"));
    }

    @Test
    public void okTest() {
        Request.Builder builder = getBuilder(ConstantsFor.TEMPNET);
        RequestBody requestBody = RequestBody.create(getJSONObject().toString().getBytes());
        builder.post(requestBody);
        Call newCall = new OkHttpClient().newCall(builder.build());
        try (Response execute = newCall.execute();
             ResponseBody body = execute.body()) {
            String string = body != null ? body.string() : "null";
            boolean contains = Stream.of("INVALID", ConstantsFor.RULES, "ikudryashov@velkomfood.ru").anyMatch(string::contains);
            Assert.assertTrue(contains, string);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testSshCommandAddDomain() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url("http://rups00.eatmeat.ru:8880" + ConstantsFor.SSHADD);
        builder.addHeader(ConstantsFor.AUTHORIZATION, "j3n38xrqKNUgcCeFiILvvLSpSuw1");
        builder.addHeader("Content-Type", ConstantsFor.JSON);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("domain", "https://www.eatmeat.ru/");
        jsonObject.add("option", "add");
        RequestBody body = RequestBody.create(jsonObject.toString().getBytes());
        builder.post(body);
        try (Response execute = okHttpClient.newCall(builder.build()).execute();
             ResponseBody resBody = execute.body()) {
            Assert.assertNotNull(resBody);
            String string = resBody.string();
            Assert.assertTrue(string.contains("eatmeat"), string);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
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
    public void getNetworkerPOSTResponse() {
        OkHttpClient okHttpClient = new OkHttpClient();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(ConstantsFor.OPTION, "add");
        jsonObject.add("domain", "http://www.2ip.ru");
        Request.Builder newBuilder = getBuilder(ConstantsFor.SSHADD);
        newBuilder.addHeader(ConstantsFor.AUTHORIZATION, "j3n38xrqKNUgcCeFiILvvLSpSuw1");
        newBuilder.addHeader("Content-Type", ConstantsFor.JSON);
        RequestBody requestBody = RequestBody.create(jsonObject.toString().getBytes());
        Request request = newBuilder.post(requestBody).build();
        try (Response response = okHttpClient.newCall(request).execute();
             ResponseBody responseBody = response.body()) {
            String reqResp = MessageFormat.format("Requested url: {0}\n{1}", request.url().toString(), response.toString());
            if (responseBody != null) {
                String bodyString = responseBody.string();
                if (bodyString.isEmpty()) {
                    bodyString = reqResp;
                    System.out.println("bodyString = " + bodyString);
                }

            }
            else {
                System.out.println("bodyNull = " + reqResp);
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void sshCommandAddDomain() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("domain", "https://www.eatmeat.ru/");
        jsonObject.add("option", "add");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(ConstantsFor.JSON);
        request.addHeader(ConstantsFor.AUTHORIZATION, "j3n38xrqKNUgcCeFiILvvLSpSuw1");
        request.setContent(jsonObject.toString().getBytes());
        String s = restCTRL.helpDomain(request, new MockHttpServletResponse());
        Assert.assertTrue(s.contains("vachok.ru"), s);
    }

    @Test
    public void testTestToString() {
        String s = restCTRL.toString();
        Assert.assertTrue(s.contains("RestCTRL["));
    }

    @Test
    public void addDomainRESTTest() {
        Request.Builder builder = getBuilder(ConstantsFor.SSHADD);
        JsonObject jsonObject = getJSONObject();
        jsonObject.add(ConstantsFor.OPTION, "add");
        jsonObject.add("domain", "http://www.eatmeat.ru");
        RequestBody requestBody = RequestBody.create(jsonObject.toString().getBytes());
        builder.post(requestBody);
        Call call = new OkHttpClient().newCall(builder.build());
        try (Response execute = call.execute();
             ResponseBody responseBody = execute.body()) {
            String stringResp = "null";
            if (responseBody != null) {
                stringResp = responseBody.string();
            }
            Assert.assertFalse(stringResp.isEmpty(), requestBody.toString());
            Assert.assertTrue(stringResp.contains(".eatmeat.ru"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testSshRest() {
    }

    @Test
    public void testGetAllowDomains() {
        String domains = restCTRL.getAllowDomains();
        Assert.assertTrue(domains.contains(".www.eatmeat.ru"), domains);
    }

    @Test
    public void testCollectOldFiles() {
        String s = restCTRL.collectOldFiles();
        Assert.assertTrue(s.contains("Total file size in DB now:"), s);
    }

    @Test
    public void testDelOldFiles() {
        try {
            String s = restCTRL.delOldFiles(new MockHttpServletRequest());
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testSshCommandExecute() {
        JsonObject jsonObject = new JsonObject();
        MockHttpServletRequest request = new MockHttpServletRequest();
        jsonObject.set(ConstantsFor.PARM_NAME_COMMAND, "ls;uname -a;exit");
        request.addHeader(ConstantsFor.AUTHORIZATION, "j3n38xrqKNUgcCeFiILvvLSpSuw1");
        request.setContentType(ConstantsFor.JSON);
        request.setContent(jsonObject.toString().getBytes());
        String sshExec = restCTRL.sshCommandExecute(request);
        Assert.assertTrue(sshExec.contains("!_passwords.xlsx"), sshExec);
        Assert.assertTrue(sshExec.contains("Srv-GIT"), sshExec);
    }

    @Test
    public void testDelDomain() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        JsonObject jsonObject = getJSONObject();
        jsonObject.add(ConstantsFor.OPTION, ConstantsFor.DELETE);
        jsonObject.add("domain", "http://www.eatmeat.ru");
        request.addHeader(ConstantsFor.AUTHORIZATION, "j3n38xrqKNUgcCeFiILvvLSpSuw1");
        request.setContentType(ConstantsFor.JSON);
        request.setContent(jsonObject.toString().getBytes());
        String respStr = restCTRL.helpDomain(request, new MockHttpServletResponse());
        Assert.assertFalse(respStr.contains("www.eatmeat.ru<br>"), respStr);
    }

    @NotNull
    private static Request.@NotNull Builder getBuilder(String urlPart) {
        String local = new SshActs("delete", "http://www.velkomfood.ru").allowDomainAdd();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(RestCTRLTest.SRV_LOCAL + urlPart);
//        builder.url("http://rups00.eatmeat.ru:8880/tempnet");
        builder.addHeader(ConstantsFor.AUTHORIZATION, "j3n38xrqKNUgcCeFiILvvLSpSuw1");
        builder.addHeader("Content-Type", ConstantsFor.JSON);
        return builder;
    }

    @Test
    public void getSSHLists() {
        Request.Builder builder = getBuilder("getsshlists");
        Call newCall = new OkHttpClient().newCall(builder.build());
        try (Response execute = newCall.execute();
             ResponseBody body = execute.body()) {
            String string = body != null ? body.string() : "null";
            Assert.assertTrue(string.contains("### TEST ###"), string);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}