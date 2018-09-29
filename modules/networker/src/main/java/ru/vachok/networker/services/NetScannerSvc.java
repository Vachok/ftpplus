package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.LastNetScan;
import ru.vachok.networker.logic.DBMessenger;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
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

    private static final List<String> PC_NAMES = new ArrayList<>();

    private static Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");

    private static Logger logger = AppComponents.getLogger();

    private MessageToUser messageToUser;

    private String thePc;

    private LastNetScan lastNetScan;

    private Map<String, Boolean> netWork;

    private String qer;

    public List<String> getPcNames() {
        getPCsAsync();
        return PC_NAMES;
    }

    public void getPCsAsync() {
        new Thread(() -> {
            final long startMethod = System.currentTimeMillis();
            for(String s : PC_PREFIXES){
                PC_NAMES.addAll(getPCNamesPref(s));
                messageToUser.info(SOURCE_CLASS, "PC Prefix set to", s + " | Scan starts...");
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethod) + " sec.";
            PC_NAMES.add(elapsedTime);
            new Thread(() -> {
                MessageToUser mailMSG = new ESender("143500@gmail.com");
                float upTime = ( float ) (TimeUnit.MILLISECONDS
                                              .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP)) / 60f;
                Map<String, String> lastLogs = new DataBasesSRV().getLastLogs("ru_vachok_ethosdistro");
                String retLogs = new TForms().fromArray(lastLogs);
                mailMSG.info(
                    SOURCE_CLASS,
                    upTime + " min uptime. " + AppComponents.versionInfo().toString(),
                    retLogs + " \n" + new TForms().fromArray(PC_NAMES));
            }).start();
        }).start();
    }

    public List<String> getPCNamesPref(String prefix) {
        this.netWork = AppComponents.lastNetScanMap();
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
                    netWork.put(pcName, false);
                    String format = MessageFormat.format("{0} {1}", pcName, onLines);
                    logger.warn(format);
                }
                else{
                    String someMore = getSomeMore(pcName);
                    String onLines = (" online " + true + "<br>");
                    pcNames.add(pcName + ":" + byName.getHostAddress() + onLines);

                    netWork.put("<br><b>" + pcName + "</b><br>" + someMore, true);
                    String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
                    logger.info(format);
                }
            }
            catch(IOException ignore){
                //
            }
        }
        netWork.put("<h4>" + prefix + "     " + pcNames.size() + "</h4>", true);
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

    private String getSomeMore(String pcName) {
        List<Integer> onLine = new ArrayList<>();
        List<Integer> offLine = new ArrayList<>();
        try(PreparedStatement statement = c.prepareStatement("select * from velkompc where NamePP like ?")){
            statement.setString(1, pcName);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    int onlineNow = resultSet.getInt("OnlineNow");
                    if(onlineNow==1){
                        onLine.add(onlineNow);
                    }
                    if(onlineNow==0){
                        offLine.add(onlineNow);
                    }
                }
            }
        }
        catch(SQLException e){
            LoggerFactory.getLogger(SOURCE_CLASS).error(e.getMessage(), e);
        }
        return offLine.size() + " offline times and " + onLine.size() + " online times.";
    }

    private String writeDB(Collection<String> pcNames) {
        List<String> list = new ArrayList<>();
        try(PreparedStatement p = c.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")){
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

    public String getInfoFromDB() {
        if(thePc.isEmpty()){
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            return argumentException.getMessage();
        }
        try(PreparedStatement preparedStatement = c.prepareStatement("select * from velkompc where NamePP like '%" + thePc + "%'")){
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                List<String> timeNow = new ArrayList<>();
                List<Integer> integersOff = new ArrayList<>();
                while(resultSet.next()){
                    int onlineNow = resultSet.getInt("OnlineNow");
                    if(onlineNow==1){
                        timeNow.add(resultSet.getString("TimeNow"));
                    }
                    else{
                        integersOff.add(onlineNow);
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    String namePP = resultSet.getString("NamePP") +
                        " ok! <br>" +
                        "OnLines = " +
                        timeNow.size() +
                        "<br>Offlines = " +
                        integersOff.size() +
                        "<br>TOTAL: " + (integersOff.size() + timeNow.size());
                    stringBuilder
                        .append("<p>")
                        .append(namePP)
                        .append("</p>");
                    setThePc(stringBuilder.toString());
                }
                Collections.sort(timeNow);
                setThePc(getThePc() + "Last online: " + timeNow.get(timeNow.size() - 1));
            }
        }
        catch(SQLException | IndexOutOfBoundsException e){
            setThePc(e.getMessage());
        }
        return "ok";
    }

    public String getThePc() {
        return thePc;
    }

    public void setThePc(String thePc) {
        this.thePc = thePc;
    }

    public LastNetScan getLastNetScan() {
        return lastNetScan;
    }

    public String getQer() {
        return qer;
    }

    public void setQer(String qer) {
        this.qer = qer;
    }

    /*Instances*/
    /*Private methods*/
    @Autowired
    public NetScannerSvc(LastNetScan lastNetScan) {
        this.messageToUser = new DBMessenger();
        this.lastNetScan = lastNetScan;
    }
}
