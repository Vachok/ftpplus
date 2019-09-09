// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Map;

import static org.testng.Assert.*;


/**
 @see ServiceInfoCtrl
 @since 14.06.2019 (9:36) */
public class ServiceInfoCtrlTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private HttpServletRequest request = new MockHttpServletRequest();
    
    private HttpServletResponse response = new MockHttpServletResponse();
    
    private Model model = new ExtendedModelMap();
    
    private ServiceInfoCtrl infoCtrl = new ServiceInfoCtrl(new AppComponents().visitor(request));
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testInfoMappingCOPY() {
    
        System.out.println(new TForms().fromArray(request.getHeaderNames(), false));
        String[] modelKeys = {"title", "mail", "ping", "urls", ModelAttributeNames.ATT_REQUEST, ModelAttributeNames.ATT_DIPSCAN, "res", "back", "footer"};
        try {
            String infoMapping = infoCtrl.infoMapping(model, request, response);
            assertTrue(infoMapping.equals("vir"));
            for (String modelKey : modelKeys) {
                assertTrue(model.asMap().containsKey(modelKey), modelKey);
            }
            String res = model.asMap().get("res").toString();
            String mail = model.asMap().get("mail").toString();
            String urls = model.asMap().get("urls").toString();
            String dipScan = model.asMap().get(ModelAttributeNames.ATT_DIPSCAN).toString();
            assertTrue(res.contains("getNextDayofWeek"), res);
            assertTrue(res.contains("USER PREFS"), res);
            assertTrue(res.contains("Property: dbuser, value: u0466446_network"), res);
            if (LocalTime.now().getHour() > 9 && LocalTime.now().getHour() < 18) {
                assertTrue(mail.contains("Работаем"), mail);
            }
            else {
                assertTrue(mail.contains("GO HOME!"), mail);
            }
            System.out.println(mail);
            assertTrue(urls.contains("Запущено"), urls);
            assertTrue(urls.contains("Состояние памяти"), urls);
            assertTrue(urls.contains("Total size ="), urls);
            assertTrue(dipScan.contains("DiapazonScan"), dipScan);
            assertTrue(dipScan.contains(ConstantsFor.SHOWALLDEV), dipScan);
        }
        catch (AccessDeniedException e) {
            assertNull(e, e.getMessage());
        }
        catch (TaskRejectedException e) {
            messageToUser.error(MessageFormat.format("ServiceInfoCtrlTest.testInfoMappingCOPY says: {0}. Parameters: \n[]: {1}", e.getMessage(), false));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testOffPC() {
        ServiceInfoCtrl infoCtrl = new ServiceInfoCtrl(new AppComponents().visitor(request));
        try {
            infoCtrl.offPC(model);
            assertTrue(0 == model.asMap().size());
        }
        catch (IOException e) {
            assertNotNull(e, e.getMessage());
            System.err.println(e.getMessage());
        }
    }
    
    @Test
    public void testCloseApp() {
        HttpServletRequest request = new MockHttpServletRequest();
        ServiceInfoCtrl infoCtrl = new ServiceInfoCtrl(new AppComponents().visitor(request));
        try {
            String closeAppStr = infoCtrl.closeApp(request);
            assertTrue(closeAppStr.equals("ok"));
        }
        catch (AccessDeniedException e) {
            assertTrue(e.getMessage().contains("DENY for"));
        }
    }
    
    @Test
    public void testInfoMapping() {
        try {
            String backStr = infoCtrl.infoMapping(model, request, response);
            Map<String, Object> mapMod = model.asMap();
            Assert.assertTrue(mapMod.size() > 9);
            Assert.assertTrue(mapMod.get("title").toString().contains("Total CPU time for all threads"), mapMod.get("title").toString());
            Assert.assertTrue(mapMod.get("head").toString().contains("PCMonitoring{"), mapMod.get("head").toString());
            Assert.assertTrue(mapMod.get("dipscan").toString().contains("DiapazonScan. Running "), mapMod.get("dipscan").toString());
            Assert.assertTrue(mapMod.get("request").toString().contains("Заголовки</h3></center>HOST:"), mapMod.get("request").toString());
            Assert.assertTrue(mapMod.get("footer").toString().contains("icons8-плохие-поросята"), mapMod.get("footer").toString());
            if (LocalTime.now().toSecondOfDay() > LocalTime.parse("18:00").toSecondOfDay()) {
                Assert.assertTrue(mapMod.get("mail").toString().contains("</b><br>"), mapMod.get("mail").toString());
            }
            else {
                Assert.assertTrue(mapMod.get("mail").toString().contains("Работаем"));
            }
            Assert.assertTrue(mapMod.get("ping").toString().contains("ClassPath {"), mapMod.get("ping").toString());
            Assert.assertTrue(mapMod.get("urls").toString().contains("Запущено -"), mapMod.get("urls").toString());
            Assert.assertTrue(mapMod.get("res").toString().contains("MyCalen"), mapMod.get("res").toString());
        }
        catch (AccessDeniedException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String s = infoCtrl.toString();
        Assert.assertTrue(s.contains("ServiceInfoCtrl{"), s);
    }
}