package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.*;


/**
 @since 14.06.2019 (9:36) */
public class ServiceInfoCtrlTest {
    
    
    @Test
    public void testInfoMapping() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Model model = new ExtendedModelMap();
        ServiceInfoCtrl infoCtrl = new ServiceInfoCtrl(new AppComponents().visitor(request));
        System.out.println(new TForms().fromArray(request.getHeaderNames(), false));
        String[] modelKeys = {"title", "mail", "ping", "urls", "request", "visit", "res", "back", "footer"};
        try {
            String infoMapping = infoCtrl.infoMapping(model, request, response);
            assertTrue(infoMapping.equals("vir"));
            for (String modelKey : modelKeys) {
                assertTrue(model.asMap().containsKey(modelKey));
            }
            String res = model.asMap().get("res").toString();
            String mail = model.asMap().get("mail").toString();
            String urls = model.asMap().get("urls").toString();
            assertTrue(res.contains("getNextDayofWeek"));
            assertTrue(res.contains("VersionInfo"));
            assertTrue(res.contains("SSH Temp list"));
            assertTrue(res.contains("AppInfoOnLoad"));
            assertTrue(mail.contains("Работаем"));
            assertTrue(urls.contains("Запущено"));
            assertTrue(urls.contains("Состояние памяти"));
            assertTrue(urls.contains("disk usage by program"));
        }
        catch (AccessDeniedException | ExecutionException e) {
            assertNull(e, e.getMessage());
        }
        catch (InterruptedException e) {
            System.err.println("Date comeD = new Date(whenCome.get()) in ru.vachok.networker.controller.ServiceInfoCtrl.modModMaker was interrupted");
        }
    }
    
    @Test
    public void testOffPC() {
        HttpServletRequest request = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        ServiceInfoCtrl infoCtrl = new ServiceInfoCtrl(new AppComponents().visitor(request));
        try {
            infoCtrl.offPC(model);
            assertTrue(model.asMap().size() == 0);
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
}