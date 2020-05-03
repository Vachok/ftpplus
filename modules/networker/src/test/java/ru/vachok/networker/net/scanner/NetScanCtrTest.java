// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.inet.InternetUse;
import ru.vachok.networker.componentsrepo.FakeRequest;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.NetScanService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

import static org.testng.Assert.assertTrue;
import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


/**
 @see NetScanCtr */
public class NetScanCtrTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private static final File FILE = new File(FileNames.SCAN_TMP);

    private NetScanCtr netScanCtr;

    private final HttpServletRequest request = new MockHttpServletRequest();

    private final HttpServletResponse response = new MockHttpServletResponse();

    private final Model model = new ExtendedModelMap();

    private PcNamesScanner pcNamesScanner;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @BeforeMethod
    public void initScan() {
        this.pcNamesScanner = (PcNamesScanner) NetScanService.getInstance(NetScanService.PCNAMESSCANNER);
        this.netScanCtr = new NetScanCtr(pcNamesScanner);
        netScanCtr.setModel(model);
        netScanCtr.setRequest(request);
        try {
            Files.deleteIfExists(FILE.toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        finally {
            Assert.assertFalse(FILE.exists());
        }
    }

    @Test
    @Ignore
    public void testNetScan() {
        try {
            Files.deleteIfExists(new File(FileNames.SCAN_TMP).toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        NetScanCtr netScanCtr = null;
        try {
            netScanCtr = new NetScanCtr(pcNamesScanner);
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
        HttpServletRequest request = new FakeRequest();
        HttpServletResponse response = this.response;
        Model model = this.model;
        try {
            String netScanStr = netScanCtr.netScan(request, response, model, pcNamesScanner);
            Assert.assertNotNull(netScanStr);
            assertTrue(netScanStr.equals(ModelAttributeNames.NETSCAN));
            assertTrue(model.asMap().size() >= 5, showModel(model.asMap()));
            assertTrue(model.asMap().get(ModelAttributeNames.FOOTER).toString().contains("Only Allow Domains"), showModel(model.asMap()));
        }
        catch (TaskRejectedException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testAbstractGetInetUsageByUser() {
        String thePc = "strel";
        Future<String> infoF = AppComponents.threadConfig().getTaskExecutor().submit(()->getInformation(thePc));
        try {
            String info = infoF.get(30, TimeUnit.SECONDS);
            System.out.println("info = " + info);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testPcNameForInfo() {
        Model model = this.model;
        HttpServletRequest request = this.request;
        HttpServletResponse response = this.response;
        pcNamesScanner.setThePc("do0001");
        try {
            String pcNameInfoStr = netScanCtr.pcNameForInfo(model, pcNamesScanner);
            Assert.assertTrue(pcNameInfoStr.contains("redirect:/ad"));
            Assert.assertTrue(model.asMap().get(ModelAttributeNames.THEPC).equals("do0001"));

        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
    }

    @Test
    public void testAbstractGetInetUsageByPc() {
        String thePC = "do0056";
        String info = getInformation(thePC);
        Assert.assertTrue(info.contains("do0056 : "), info);
        Assert.assertTrue(info.contains("время открытых сессий"), info);
    }

    private @NotNull String getInformation(String instanceType) {
        InternetUse informationFactory = InternetUse.getInstance("instanceType");
        informationFactory.setClassOption(instanceType);
        String infoAboutInet = informationFactory.getInfoAbout(instanceType);
        Assert.assertTrue(infoAboutInet.contains("время открытых сессий"), infoAboutInet);
        String detailedInfo = informationFactory.getInfo();
        return infoAboutInet + "\n" + detailedInfo;
    }

    private @NotNull String showModel(@NotNull Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }

    private @NotNull String testFromArray(@NotNull Map<String, String> mapDefObj) {
        StringBuilder brStringBuilder = new StringBuilder();
        brStringBuilder.append(STR_P);
        Set<?> keySet = mapDefObj.keySet();
        List<String> list = new ArrayList<>(keySet.size());
        keySet.forEach(x->list.add(x.toString()));
        Collections.sort(list);
        for (String keyMap : list) {
            String valueMap = mapDefObj.get(keyMap);
            brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
        }
        return brStringBuilder.toString();

    }
}