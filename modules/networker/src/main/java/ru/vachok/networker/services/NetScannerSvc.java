package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.logic.DBMessenger;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 @since 21.08.2018 (14:40) */
@Service ("netScannerSvc")
public class NetScannerSvc {

    /*Fields*/

    /**
     Префиксы имён ПК Велком.
     */
    private static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a"};

    private static final String SOURCE_CLASS = NetScannerSvc.class.getSimpleName();

    private MessageToUser messageToUser;

    @Autowired
    public NetScannerSvc() {
        this.messageToUser = new DBMessenger();
    }

    private static Logger logger = AppComponents.getLogger();

    private String qer;

    public String getQer() {
        return qer;
    }

    public void setQer(String qer) {
        this.qer = qer;
    }

    public List<String> getPCsAsync() {
        final List<String> pcNames = new ArrayList<>();
        final long startMethod = System.currentTimeMillis();
        for(String s : PC_PREFIXES){
            pcNames.addAll(getPCNames(s));
            messageToUser.info(SOURCE_CLASS, "PC Prefix set to", s + " | Scan starts...");
        }
        String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethod) + " sec.";
        messageToUser.info(SOURCE_CLASS, "Scan OK!", elapsedTime);
        pcNames.add(elapsedTime);
        MessageToUser mailMSG = new ESender("143500@gmail.com");
        Map<String, String> lastLogs = new DataBases().getLastLogs("ru_vachok_ethosdistro");
        String retLogs = new TForms().fromArray(lastLogs);
        new Thread(() -> mailMSG.info(SOURCE_CLASS,
            (( float ) (TimeUnit.MILLISECONDS
                            .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP)) / 60f) +
                " min uptime",
            retLogs + " \n" +
                new TForms().fromArray(pcNames))).start();
        return pcNames;
    }

    public List<String> getPCNames(String prefix) {
        this.qer = prefix;
        final long startMethTime = System.currentTimeMillis();
        List<String> pcNames = new ArrayList<>();
        boolean reachable;
        InetAddress byName;
        for(String pcName : getCycleNames(prefix)){
            try{
                byName = InetAddress.getByName(pcName);
                reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
                if(!reachable){
                    String onLines = ("online " + false + "");
                    pcNames.add(pcName + ":" + byName.getHostAddress() + " " + onLines + "");
                    String format = MessageFormat.format("{0} {1}", pcName, onLines);
                    logger.warn(format);
                }
                else{
                    String onLines = (" online " + true);
                    pcNames.add(pcName + ":" + byName.getHostAddress() + onLines);
                    String format = MessageFormat.format("{0} {1}", pcName, onLines);
                    logger.warn(format);
                }
            }
            catch(IOException ignore){
                //
            }
        }
        String pcsString = writeDB(pcNames);
        logger.info(pcsString);
        pcNames.add("<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b>");
        return pcNames;
    }

    private Collection<String> getCycleNames(String userQuery) {
        if(userQuery==null){
            userQuery = "pp";
        }
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

    /*Private methods*/

    private String writeDB(Collection<String> pcNames) {
        DataConnectTo dataConnectTo = new RegRuMysql();
        List<String> list = new ArrayList<>();
        try(Connection c = dataConnectTo.getDefaultConnection("u0466446_velkom");
            PreparedStatement p = c.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")){
            pcNames.stream().sorted().forEach(x -> {
                String pcSerment = "Я не знаю...";
                logger.info(x);
                if(x.contains("200.200")){
                    pcSerment = "Торговый дом";
                }
                if(x.contains("200.201")){
                    pcSerment = "IP телефоны";
                }
                if(x.contains("200.202")){
                    pcSerment = "Техслужба";
                }
                if(x.contains("200.203")){
                    pcSerment = "СКУД";
                }
                if(x.contains("200.204")){
                    pcSerment = "Упаковка";
                }
                if(x.contains("200.205")){
                    pcSerment = "МХВ";
                }
                if(x.contains("200.206")){
                    pcSerment = "Здание склада 5";
                }
                if(x.contains("200.207")){
                    pcSerment = "Сырокопоть";
                }
                if(x.contains("200.208")){
                    pcSerment = "Участок убоя";
                }
                if(x.contains("200.209")){
                    pcSerment = "Да ладно?";
                }
                if(x.contains("200.210")){
                    pcSerment = "Мастера колб";
                }
                if(x.contains("200.212")){
                    pcSerment = "Мастера деликатесов";
                }
                if(x.contains("200.213")){
                    pcSerment = "2й этаж. АДМ.";
                }
                if(x.contains("200.214")){
                    pcSerment = "WiFiCorp";
                }
                if(x.contains("200.215")){
                    pcSerment = "WiFiFree";
                }
                if(x.contains("200.217")){
                    pcSerment = "1й этаж АДМ";
                }
                if(x.contains("192.168")){
                    pcSerment = "Может быть в разных местах...";
                }
                if(x.contains("172.16.200")){
                    pcSerment = "Open VPN авторизация - сертификат";
                }
                boolean onLine = false;
                try{
                    if(x.contains("true")){
                        onLine = true;
                    }
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
                    messageToUser.errorAlert(this.getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e.getStackTrace()));
                    logger.error(e.getMessage(), e);
                }
            });
            return new TForms().fromArray(list);
        }
        catch(SQLException e){
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    private int getNamesCount(String qer) {
        int inDex = 0;
        if(qer.equals("no")){
            inDex = ConstantsFor.NOPC;
        }
        if(qer.equals("pp")){
            inDex = ConstantsFor.PPPC;
        }
        if(qer.equals("do")){
            inDex = ConstantsFor.DOPC;
        }
        if(qer.equals("a")){
            inDex = ConstantsFor.APC;
        }
        if(qer.equals("td")){
            inDex = ConstantsFor.TDPC;
        }
        return inDex;
    }
}
