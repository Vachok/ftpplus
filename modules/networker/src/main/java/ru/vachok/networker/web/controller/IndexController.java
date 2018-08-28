package ru.vachok.networker.web.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.InetorApplication;
import ru.vachok.networker.logic.ssh.ListInternetUsers;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 The type Index controller.
 */
@Controller
public class IndexController {

    private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

    private static final String SOURCE_CLASS = IndexController.class.getName();

    private static Logger logger = LoggerFactory.getLogger("Index");

    private MessageToUser messageToUser = new MessageCons();


    /**
     Map to show map.

     @param httpServletRequest  the http servlet request
     @param httpServletResponse the http servlet response
     @return the map
     @throws IOException the io exception
     */
    @RequestMapping ("/docs")
    public Map<String, String> mapToShow(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        SHOW_ME.put("addr", httpServletRequest.getRemoteAddr());
        SHOW_ME.put("host", httpServletRequest.getRequestURL().toString());
        SHOW_ME.forEach((x, y) -> messageToUser.info(this.getClass().getSimpleName(), x, y));
        SHOW_ME.put("status", httpServletResponse.getStatus() + " " + httpServletResponse.getBufferSize() + " buff");
        String s = httpServletRequest.getQueryString();
        if(s!=null){
            SHOW_ME.put(this.toString(), s);
            if(s.contains("go")) httpServletResponse.sendRedirect("http://ftpplus.vachok.ru/docs");
        }
        return SHOW_ME;
    }

    /**
     Addr in locale stream.

     @param httpServletRequest  the http servlet request
     @param httpServletResponse the http servlet response
     @return the stream
     @throws IOException the io exception
     */
    @GetMapping ("/vir")
    public String addrInLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Model model) {
        List<String> namesFile = new ArrayList<>();
        String re = "redirect:https://vachok.testquality.com/project/3260/plan/6672/test/86686";
        Cookie cooki = new Cookie("hi", re);
        httpServletResponse.addCookie(cooki);
        byte[] bs = new byte[0];
        try(ServletInputStream in = httpServletRequest.getInputStream()){

            while(in.isReady()){
                in.read(bs);
            }
        }
        catch(IOException e){
            logger.error(e.getMessage(), e);
        }
        messageToUser.info("HTTP Servlets Controller", httpServletRequest.getServletPath() + re, "1 КБ resp: " + new String(bs, StandardCharsets.UTF_8));
        String s = LocalDateTime.of(2018, 10, 14, 7, 0).format(DateTimeFormatter.ofPattern("dd/MM/yy"));
        Map<String, String> getEnv = System.getenv();
        getEnv.forEach((x, y) -> namesFile.add(x + "\n" + y));
        namesFile.add(re);
        namesFile.add(new String(bs, StandardCharsets.UTF_8));
        namesFile.add(s);
        namesFile.add(httpServletRequest.toString());
        namesFile.add(httpServletRequest.getSession().getServletContext().getServerInfo());
        namesFile.add(httpServletRequest.getSession().getServletContext().getServletContextName());
        namesFile.add(httpServletRequest.getSession().getServletContext().getVirtualServerName());
        namesFile.add(httpServletRequest.getSession().getServletContext().getContextPath());
        namesFile.add(Arrays.toString(httpServletResponse.getHeaderNames().toArray()));
        for(String name : namesFile){
            model.addAttribute("virTxt", name);
        }
        throw new UnsupportedOperationException();
    }

    @GetMapping ("/")
    public String indexModel(HttpServletRequest request, HttpServletResponse response, Model model) {
        Map<String, String> sshResults = new ListInternetUsers().call();
        List<String> commsndsSSH = new ListInternetUsers().getCommand();
        for(String s : commsndsSSH){
            String sshRes = sshResults.get(s);
            model.addAttribute(s.split("cat /etc/pf/")[1], sshRes);
        }
        boolean b = InetorApplication.dataSender(response, request, SOURCE_CLASS);
        model.addAttribute("dbsend", b + " db");
        return "index";
    }

    private String getAttr(HttpServletRequest request) {
        Enumeration<String> attributeNames = request.getServletContext().getAttributeNames();
        StringBuilder stringBuilder = new StringBuilder();
        while(attributeNames.hasMoreElements()){
            stringBuilder.append(attributeNames.nextElement());
            stringBuilder.append("<p>");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
