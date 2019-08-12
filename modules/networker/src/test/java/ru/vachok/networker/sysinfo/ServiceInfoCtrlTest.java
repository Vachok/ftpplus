// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
                assertTrue(model.asMap().containsKey(modelKey));
            }
            String res = model.asMap().get("res").toString();
            String mail = model.asMap().get("mail").toString();
            String urls = model.asMap().get("urls").toString();
            String dipScan = model.asMap().get(ModelAttributeNames.ATT_DIPSCAN).toString();
            assertTrue(res.contains("getNextDayofWeek"), res);
            assertTrue(res.contains("VersionInfo"), res);
            assertTrue(res.contains("AppInfoOnLoad"), res);
            if (LocalTime.now().getHour() > 9 && LocalTime.now().getHour() < 18) {
                assertTrue(mail.contains("Работаем"), mail);
            }
            else {
                assertTrue(mail.contains("GO HOME!"), mail);
            }
            System.out.println(mail);
            assertTrue(urls.contains("Запущено"));
            assertTrue(urls.contains("Состояние памяти"));
            assertTrue(urls.contains("disk usage by program"));
            assertTrue(dipScan.contains("DiapazonScan"));
            assertTrue(dipScan.contains(ConstantsFor.SHOWALLDEV));
        }
        catch (AccessDeniedException | ExecutionException e) {
            assertNull(e, e.getMessage());
        }
        catch (InterruptedException | TaskRejectedException | TimeoutException e) {
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
    }
    
    @Test
    public void testTestToString() {
    }
}