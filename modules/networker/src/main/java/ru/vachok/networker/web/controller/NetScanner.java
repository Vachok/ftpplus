package ru.vachok.networker.web.controller;


import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ApplicationConfiguration;
import ru.vachok.networker.web.ConstantsFor;
import ru.vachok.networker.web.beans.ToStringFrom;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 @since 21.08.2018 (14:40) */
@Controller
public class NetScanner implements Runnable {

    private static Logger logger = ApplicationConfiguration.logger();


    @GetMapping ("/netscan")
    public String getPCNames(HttpServletRequest request, Model model) throws IOException {
        String qer = request.getQueryString();
        Collection<String> pcNames = new ArrayList<>();
        boolean reachable;
        InetAddress byName;
        for(String pcName : getCycleNames(qer)){
            try{
                byName = InetAddress.getByName(pcName);
                reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
                if(!reachable){
                    String onLines = ("<pcsString> online </pcsString> <i>" + false + "</i>");
                    pcNames.add(pcName + ":" + byName.getHostAddress() + " <pcsString>" + onLines + " </pcsString>");
                    String format = MessageFormat.format("{0} {1}", pcName, onLines);
                    logger.warn(format);
                }
                else{
                    String onLines = ("<pcsString> online </pcsString>" + true);
                    pcNames.add(pcName + ":" + byName.getHostAddress() + "<pcsString>" + onLines + "</pcsString>");
                    String format = MessageFormat.format("{0} {1}", pcName, onLines);
                    logger.warn(format);
                }
            }
            catch(UnknownHostException | NullPointerException ignore){
                //
            }
        }
        String pcsString = writeDB(pcNames);
        pcNames.add(pcsString + " WRITE TO DB");
        model.addAttribute("pc", pcsString);
        return "netscan";
    }

    @Override
    public void run() {
        for(String s:ConstantsFor.PC_PREFIXES){
            getCycleNames(s);
        }
    }

    private Collection<String> getCycleNames(String userQuery) {
        if(userQuery==null) userQuery = "pp";
        int inDex = getNamesCount(userQuery);
        String nameCount;
        Collection<String> list = new ArrayList<>();
        int pcNum = 0;
        for(int i = 1; i < inDex; i++){
            if(userQuery.equals("no") || userQuery.equals("pp") || userQuery.equals("do")){
                nameCount = String.format("%04d", ++pcNum);
            }
            else{
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(userQuery + nameCount + ".eatmeat.ru");
        }
        return list;
    }


    private String writeDB(Collection<String> pcNames) {
        DataConnectTo dataConnectTo = new RegRuMysql();
        List<String> list = new ArrayList<>();
        try(Connection c = dataConnectTo.getDefaultConnection("u0466446_velkom");
            PreparedStatement p = c.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")){
            pcNames.stream().sorted().forEach(x -> {
                String pcSerment = "Я не знаю...";
                logger.info(x);
                if(x.contains("200.200")) pcSerment = "Торговый дом";
                if(x.contains("200.201")) pcSerment = "IP телефоны";
                if(x.contains("200.202")) pcSerment = "Техслужба";
                if(x.contains("200.203")) pcSerment = "СКУД";
                if(x.contains("200.204")) pcSerment = "Упаковка";
                if(x.contains("200.205")) pcSerment = "МХВ";
                if(x.contains("200.206")) pcSerment = "Здание склада 5";
                if(x.contains("200.207")) pcSerment = "Сырокопоть";
                if(x.contains("200.208")) pcSerment = "Участок убоя";
                if(x.contains("200.209")) pcSerment = pcSerment;
                if(x.contains("200.210")) pcSerment = "Мастера колб";
                if(x.contains("200.212")) pcSerment = "Мастера деликатесов";
                if(x.contains("200.213")) pcSerment = "2й этаж. АДМ.";
                if(x.contains("200.214")) pcSerment = "WiFiCorp";
                if(x.contains("200.215")) pcSerment = "WiFiFree";
                if(x.contains("200.217")) pcSerment = "1й этаж АДМ";
                if(x.contains("192.168")) pcSerment = "Может быть в разных местах...";
                if(x.contains("172.16.200")) pcSerment = "Open VPN авторизация - сертификат";
                boolean onLine = false;
                try{
                    if(x.contains("true")) onLine = true;
                    String x1 = x.split(":")[0];
                    p.setString(1, x1);
                    String x2 = x.split(":")[1];
                    p.setString(2, x2.split("<")[0]);
                    p.setString(3, pcSerment);
                    p.setBoolean(4, onLine);
                    p.executeUpdate();
                    list.add(x1 + " " + x2 + " " + pcSerment + " " + onLine);
                }
                catch(SQLException e){
                    logger.error(e.getMessage(), e);
                }
            });
            return new ToStringFrom().fromArr(list);
        }
        catch(SQLException e){
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    private int getNamesCount(String qer) {
        int inDex = 0;
        if(qer.equals("no")) inDex = ConstantsFor.NOPC;
        if(qer.equals("pp")) inDex = ConstantsFor.PPPC;
        if(qer.equals("do")) inDex = ConstantsFor.DOPC;
        if(qer.equals("a")) inDex = ConstantsFor.APC;
        if(qer.equals("td")) inDex = ConstantsFor.TDPC;
        return inDex;
    }
}
