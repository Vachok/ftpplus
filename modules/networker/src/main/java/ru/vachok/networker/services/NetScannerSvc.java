package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.Locked;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ActDirectoryCTRL;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;


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

    private static final String DB_NAME = ConstantsFor.DB_PREFIX + "velkom";

    private static Connection c = new RegRuMysql().getDefaultConnection(DB_NAME);

    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    private static List<ADComputer> adComputers = AppComponents.adComputers();

    private static Set<String> pcNames = new HashSet<>();

    private static ReentrantLock lock = AppComponents.lock();

    private static NetScannerSvc netScannerSvc = new NetScannerSvc();

    private String thePc;

    private String qer;

    private Map<String, Boolean> netWork;

    public Set<String> getPcNames() {
        ThreadConfig threadConfig = new ThreadConfig();
        ThreadPoolTaskExecutor executor = threadConfig.threadPoolTaskExecutor();
        Runnable getPCs = this::getPCsAsync;
        executor.execute(getPCs);
        executor.destroy();
        return pcNames;
    }

    public static NetScannerSvc getI() {
        return netScannerSvc;
    }

    public String getQer() {
        return qer;
    }

    public void setQer(String qer) {
        this.qer = qer;
    }

    /**
     Выполняет запрос в БД по-пользовательскому вводу <br>

     @return web-страница с результатом
     */
    public String getInfoFromDB() {
        if(thePc.isEmpty()){
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            return argumentException.getMessage();
        }
        StringBuilder sql = new StringBuilder();
        sql
            .append("select * from velkompc where NamePP like '%")
            .append(thePc)
            .append("%'");
        try(PreparedStatement preparedStatement = c.prepareStatement(sql.toString())){
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
                    String namePP = "<center><h2>" + resultSet.getString("NamePP") +
                        " information.<br></h2>" +
                        "<font color = \"silver\">OnLines = " +
                        timeNow.size() +
                        ". Offlines = " +
                        integersOff.size() +
                        ". TOTAL: " + (integersOff.size() + timeNow.size());
                    stringBuilder
                        .append(namePP)
                        .append(". <br>");
                    setThePc(stringBuilder.toString());
                }
                Collections.sort(timeNow);
                String str = timeNow.get(timeNow.size() - 1);
                String thePcWithDBInfo = new StringBuilder()
                    .append(getThePc())
                    .append("Last online: ")
                    .append(str)
                    .append(" (")
                    .append(")<br>Actual on: ").toString();
                thePcWithDBInfo = thePcWithDBInfo + AppComponents.lastNetScan().getTimeLastScan() + "</center></font>";
                setThePc(thePcWithDBInfo);
                ActDirectoryCTRL.setInputWithInfoFromDB(thePcWithDBInfo);
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

    /*Instances*/
    private NetScannerSvc() {
        this.netWork = AppComponents.lastNetScanMap();
    }

    /**
     Сканирующий метод. Запускает отдельный {@link Thread}, который блокируется с помощью {@link ReentrantLock}
     */
    @Locked (id = Thread.State.BLOCKED)
    void getPCsAsync() {
        AtomicReference<String> msg = new AtomicReference<>("");
        new Thread(() -> {
            Thread.currentThread().setName("PC_SCANNER_PROGRESS*********LOCKED");
            lock.lock();
            msg.set(new StringBuilder()
                .append("Thread ")
                .append(Thread.currentThread().getId())
                .append(" with name ")
                .append(Thread.currentThread().getName())
                .append(" is locked = ")
                .append(lock.isLocked()).toString());
            final long startMethod = System.currentTimeMillis();
            LOGGER.warn(msg.get());
            for(String s : PC_PREFIXES){
                pcNames.clear();
                pcNames.addAll(getPCNamesPref(s));
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethod) + " sec.";
            pcNames.add(elapsedTime);
            lock.unlock();
            LOGGER.warn(msg.get());
            new Thread(() -> {
                MessageToUser mailMSG = new ESender("143500@gmail.com");
                float upTime = ( float ) (TimeUnit.MILLISECONDS
                                              .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP)) / 60f;
                Map<String, String> lastLogs = new AppComponents().getLastLogs();
                String retLogs = new TForms().fromArray(lastLogs);
                String fromArray = new TForms().fromArray(ConstantsFor.COMPNAME_USERS_MAP, false);
                mailMSG.info(
                    SOURCE_CLASS,
                    upTime + " min uptime. " + ConstantsFor.COMPNAME_USERS_MAP.size() + " COMPNAME_USERS_MAP size",
                    retLogs + " \n" + fromArray);
                try(OutputStream outputStream = new FileOutputStream("lasusers.txt")){
                    outputStream.write(fromArray.getBytes());
                }
                catch(IOException e){
                    LOGGER.error(e.getMessage(), e);
                }
            }).start();
        }).start();
    }

    public Set<String> getPCNamesPref(String prefix) {
        this.qer = prefix;
        final long startMethTime = System.currentTimeMillis();
        boolean reachable;
        InetAddress byName;
        for(String pcName : getCycleNames(prefix)){
            try{
                byName = InetAddress.getByName(pcName);
                reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
                if(!reachable){
                    String onLines = ("online " + false + "");
                    pcNames.add(pcName + ":" + byName.getHostAddress() + " " + onLines + "");
                    netWork.putIfAbsent(pcName, false);
                    String format = MessageFormat.format("{0} {1}", pcName, onLines);
                    LOGGER.warn(format);
                }
                else{
                    String someMore = getSomeMore(pcName);
                    String onLines = (" online " + true + "<br>");
                    pcNames.add(pcName + ":" + byName.getHostAddress() + onLines);
                    netWork.putIfAbsent("<br><b><a href=\"/ad?" + pcName.split(".eatm")[0] + "\" >" + pcName + "</b></a><br>" + someMore, true);
                    String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
                    LOGGER.info(format);
                }
            }
            catch(IOException ignore){
                //
            }
        }
        netWork.put("<h4>" + prefix + "     " + pcNames.size() + "</h4>", true);
        String pcsString = writeDB(pcNames);
        LOGGER.info(pcsString);
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

    /**
     @param pcName имя компьютера
     @return выдержка из БД (когда последний раз был онлайн + кол-во проверок)
     */
    private String getSomeMore(String pcName) {
        List<Integer> onLine = new ArrayList<>();
        List<Integer> offLine = new ArrayList<>();
        try(PreparedStatement statement = c.prepareStatement("select * from velkompc where NamePP like ?")){
            statement.setString(1, pcName);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    ADComputer adComputer = new ADComputer();
                    int onlineNow = resultSet.getInt("OnlineNow");
                    if(onlineNow==1){
                        onLine.add(onlineNow);
                        adComputer.setDnsHostName(pcName);
                    }
                    if(onlineNow==0){
                        offLine.add(onlineNow);
                    }
                    adComputers.add(adComputer);
                }
            }
        }
        catch(SQLException | NullPointerException e){
            LOGGER.error(e.getMessage(), e);
        }
        return offLine.size() + " offline times and " + onLine.size() + " online times.";
    }

    private static String writeDB(Collection<String> pcNames) {
        List<String> list = new ArrayList<>();
        try(PreparedStatement p = c.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")){
            pcNames.stream().sorted().forEach(x -> {
                String pcSerment = "Я не знаю...";
                LOGGER.info(x);
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
                    LOGGER.error(e.getMessage(), e);
                    c = new RegRuMysql().getDefaultConnection(DB_NAME);
                }
            });
            return new TForms().fromArray(list, true);
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
            c = new RegRuMysql().getDefaultConnection(DB_NAME);
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
