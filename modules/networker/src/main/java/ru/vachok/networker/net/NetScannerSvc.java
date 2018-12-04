package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ActDirectoryCTRL;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;


/**
 @since 21.08.2018 (14:40) */
@Service ("netScannerSvc")
public class NetScannerSvc {

    /**
     Префиксы имён ПК Велком.
     */
    private static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a"};

    /**
     {@link NetScannerSvc#getClass()}.getSimpleName()
     */
    private static final String SOURCE_CLASS = NetScannerSvc.class.getSimpleName();

    /**
     {@link ConstantsFor#DB_PREFIX} + velkom
     */
    private static final String DB_NAME = ConstantsFor.DB_PREFIX + "velkom";

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link RegRuMysql#getDefaultConnection(String)}
     */
    private static Connection c;

    /**
     {@link AppComponents#adComputers()}
     */
    private static List<ADComputer> adComputers = AppComponents.adComputers();

    /**
     new {@link HashSet}
     */
    private static Set<String> pcNames = new HashSet<>();

    /**
     {@link AppComponents#lock()}
     */
    private static ReentrantLock lock = AppComponents.lock();

    /**
     new {@link NetScannerSvc}
     */
    private static NetScannerSvc netScannerSvc = new NetScannerSvc();

    /**
     /netscan POST форма
     <p>

     @see NetScanCtr
     {@link }
     */
    private String thePc;

    /**
     /netscan {@link HttpServletRequest#getQueryString()}
     */
    private String qer;

    /**
     {@link AppComponents#lastNetScan()}
     */
    private Map<String, Boolean> netWork;

    /**
     {@link ThreadConfig}
     */
    private ThreadConfig threadConfig = new ThreadConfig();

    /**
     {@link ThreadConfig#threadPoolTaskExecutor()}
     */
    private ThreadPoolTaskExecutor threadPoolTaskExecutor = threadConfig.threadPoolTaskExecutor();

    /**
     @return {@link #netScannerSvc}
     */
    public static NetScannerSvc getI() {
        return netScannerSvc;
    }

    /**
     @return {@link #qer}
     */
    public String getQer() {
        return qer;
    }

    /**
     {@link #qer}
     Usage: {@link NetScanCtr#scanIt(HttpServletRequest, Model)} <br>
     Uses: - <br>
     @param qer {@link HttpServletRequest}.getQueryString()
     */
    void setQer(String qer) {
        this.qer = qer;
    }

    /**
     Выполняет запрос в БД по-пользовательскому вводу <br> Устанавливает {@link ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)}

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

    /**
     @return атрибут модели.
     */
    @SuppressWarnings ("WeakerAccess")
    public String getThePc() {
        return thePc;
    }

    /**
     {@link #thePc}

     @param thePc имя ПК
     */
    public void setThePc(String thePc) {
        this.thePc = thePc;
    }

    /**
     1 {@link ThreadConfig#threadPoolTaskExecutor()}

     @return {@link #pcNames}
     @see NetScanCtr#scanIt(HttpServletRequest, Model)
     */
    Set<String> getPcNames() {
        ThreadConfig threadConfig = new ThreadConfig();
        ThreadPoolTaskExecutor executor = threadConfig.threadPoolTaskExecutor();
        Runnable getPCs = this::getPCsAsync;
        executor.execute(getPCs);
        executor.destroy();
        return pcNames;
    }

/*Instances*/

    /**
     @see AppComponents#lastNetScanMap()
     */
    private NetScannerSvc() {
        this.netWork = AppComponents.lastNetScanMap();
    }

    static {
        try{
            c = new RegRuMysql().getDefaultConnection(DB_NAME);
        }
        catch(Exception e){
            c = new RegRuMysql().getDefaultConnection(DB_NAME);
        }
    }

    /**
     Сканирующий метод. Запускает отдельный {@link Thread}, который блокируется с помощью {@link ReentrantLock} <br> 1 {@link #getPCNamesPref(String)} 1.1 {@link #getCycleNames(String)} 1.1.1 {@link
    #getNamesCount(String)} 1.2 {@link #getSomeMore(String, boolean)} 1.2.1 {@link #onLinesCheck(String, String)} 1.2.1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 1.2.1.2 {@link
    PCUserResolver#namesToFile(String)} 1.2.2 {@link #offLinesCheckUser(String, String)} 1.3 {@link #getSomeMore(String, boolean)} 1.3.1 {@link #onLinesCheck(String, String)} 1.3.1.1 {@link
    ThreadConfig#threadPoolTaskExecutor()} 1.3.1.2 {@link PCUserResolver#namesToFile(String)} 1.4 {@link #getSomeMore(String, boolean)} 1.4.1 {@link #onLinesCheck(String, String)} 1.4.1.1 {@link
    ThreadConfig#threadPoolTaskExecutor()} 1.4.1.2 {@link PCUserResolver#namesToFile(String)} 1.4.2 {@link #offLinesCheckUser(String, String)} 1.5 {@link #writeDB(Collection)} 1.5.1 {@link
    TForms#fromArray(List, boolean)} <br>
     <p>
     2 {@link TForms#fromArray(Map)} <br>
     <p>
     3 {@link TForms#fromArray(java.util.concurrent.ConcurrentMap, boolean)} <br>
     <p>
     4 {@link TForms#fromArrayUsers(ConcurrentMap, boolean)}

     @see PCUserResolver#getResolvedName()
     @see #getPcNames()
     */
    public void getPCsAsync() {
        AtomicReference<String> msg = new AtomicReference<>("");
        new Thread(() -> {
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
                Thread.currentThread().setName(lock.isLocked() + " lock*" + s);
                pcNames.clear();
                pcNames.addAll(getPCNamesPref(s));
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethod) + " sec.";
            pcNames.add(elapsedTime);
            lock.unlock();
            LOGGER.warn(msg.get());
            new Thread(() -> {
                threadPoolTaskExecutor.destroy();
                Thread.currentThread().setName(lock.isLocked() + " lock*SMTP");
                MessageToUser mailMSG = new ESender("143500@gmail.com");
                float upTime = ( float ) (TimeUnit.MILLISECONDS
                                              .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP)) / 60f;
                Map<String, String> lastLogs = new AppComponents().getLastLogs();
                String retLogs = new TForms().fromArray(lastLogs);
                String fromArray = new TForms().fromArray(ConstantsFor.COMPNAME_USERS_MAP, false);
                String psUser = new TForms().fromArrayUsers(ConstantsFor.PC_U_MAP, false);
                String thisPCStr;
                thisPCStr = ConstantsFor.thisPC();
                mailMSG.info(
                    SOURCE_CLASS,
                    upTime + " min uptime. " + thisPCStr + " COMPNAME_USERS_MAP size",
                    retLogs + " \n" + psUser + "\n" + fromArray);
                try(OutputStream outputStream = new FileOutputStream("lasusers.txt")){
                    outputStream.write(fromArray.getBytes());
                }
                catch(IOException e){
                    LOGGER.error(e.getMessage(), e);
                }
                String s = Thread.activeCount() + " active threads now.";
                LOGGER.warn(s);
            }).start();
        }).start();
    }

    /**
     Сборщик для {@link #pcNames} <br> 1. {@link #getCycleNames(String)} 1.1 {@link #getNamesCount(String)} <br> 2. {@link #getSomeMore(String, boolean)} 2.1 {@link #onLinesCheck(String, String)} 2
     .1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 2.1.2 {@link PCUserResolver#namesToFile(String)} <br> 2.2 {@link #offLinesCheckUser(String, String)} <br> 3. {@link #getSomeMore(String,
        boolean)} 3.1 {@link #onLinesCheck(String, String)} 3.1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 3.1.2 {@link PCUserResolver#namesToFile(String)} <br> 4. {@link #getSomeMore(String,
        boolean)} 4.1 {@link ThreadConfig#threadPoolTaskExecutor()} 4.1.2 {@link PCUserResolver#namesToFile(String)} 4.2 {@link #offLinesCheckUser(String, String)} <br> 5.
     {@link #writeDB(Collection)} 5.1
     {@link TForms#fromArray(List, boolean)}

     @param prefixPcName префикс имени ПК
     @return {@link #pcNames}
     @see NetScanCtr#scanIt(HttpServletRequest, Model)
     @see #getPCsAsync()
     */
    Set<String> getPCNamesPref(String prefixPcName) {
        this.qer = prefixPcName;
        final long startMethTime = System.currentTimeMillis();
        boolean reachable;
        InetAddress byName;
        Thread.currentThread().setPriority(8);
        for(String pcName : getCycleNames(prefixPcName)){
            try{
                byName = InetAddress.getByName(pcName);
                reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
                if(!reachable){
                    String someMore = getSomeMore(pcName, false);
                    String onLines = new StringBuilder()
                        .append("online ")
                        .append(false)
                        .append("<br>").toString();

                    pcNames.add(pcName + ":" + byName.getHostAddress() + " " + onLines);
                    String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
                    netWork.putIfAbsent(pcName + " last name is " + someMore, false);
                    LOGGER.warn(format);
                }
                else{
                    String someMore = new StringBuilder().append("<i><font color=\"yellow\">last name is ")
                        .append(getSomeMore(pcName, false)).append("</i></font> ")
                        .append(getSomeMore(pcName, true))
                        .toString();
                    String onLines = new StringBuilder()
                        .append(" online ")
                        .append(true)
                        .append("<br><br>").toString();
                    String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
                    pcNames.add(pcName + ":" + byName.getHostAddress() + onLines);
                    String printStr = new StringBuilder().append("<br><b><a href=\"/ad?")
                        .append(pcName.split(".eatm")[0]).append("\" >")
                        .append(pcName).append("</b></a>     ")
                        .append(someMore).append(". ")
                        .toString();
                    netWork.putIfAbsent(printStr, true);
                    LOGGER.info(format);
                }
            }
            catch(IOException e){
                Thread.currentThread().interrupt();
            }
        }
        netWork.put("<h4>" + prefixPcName + "     " + pcNames.size() + "</h4>", true);
        String pcsString = writeDB(pcNames);
        LOGGER.info(pcsString);
        pcNames.add("<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b>");
        return pcNames;
    }

    /**
     1. {@link #getNamesCount(String)}

     @param namePCPrefix префикс имени ПК
     @return обработанные имена ПК, для пинга
     @see #getPCNamesPref(String)
     */
    private Collection<String> getCycleNames(String namePCPrefix) {
        if(namePCPrefix==null){
            namePCPrefix = "pp";
        }
        int inDex = getNamesCount(namePCPrefix);
        String nameCount;
        Collection<String> list = new ArrayList<>();
        int pcNum = 0;
        for(int i = 1; i < inDex; i++){
            if(namePCPrefix.equals("no") || namePCPrefix.equals("pp") || namePCPrefix.equals("do")){
                nameCount = String.format("%04d", ++pcNum);
            }
            else{
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(namePCPrefix + nameCount + ".eatmeat.ru");
        }
        return list;
    }

    /**
     Поиск имён пользователей компьютера <br> Обращения: <br> 1 {@link #onLinesCheck(String, String)} 1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 1.2 {@link PCUserResolver#namesToFile(String)}
     <br> 2. {@link #offLinesCheckUser(String, String)}

     @param pcName   имя компьютера
     @param isOnline онлайн = true
     @return выдержка из БД (когда последний раз был онлайн + кол-во проверок) либо хранимый в БД юзернейм (для offlines)
     @see #getPCNamesPref(String)
     */
    private String getSomeMore(String pcName, boolean isOnline) {
        String sql;
        if(isOnline){
            sql = "select * from velkompc where NamePP like ?";
            return onLinesCheck(sql, pcName);
        }
        else{
            sql = "select * from pcuser where pcName like ?";
            return offLinesCheckUser(sql, pcName);
        }
    }

    /**
     Запись в таблицу <b>velkompc</b> текущего состояния. <br>
     <p>
     1 {@link TForms#fromArray(List, boolean)}

     @param pcNames имена компьютеров
     @return строка в html-формате
     @see #getPCNamesPref(String)
     */
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

    /**
     @param qer префикс имени ПК
     @return кол-во ПК, для пересичления
     @see #getCycleNames(String)
     */
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

    /**
     Проверяет имя пользователя на ПК онлайн

     @param sql    запрос
     @param pcName имя ПК
     @return кол-во проверок и сколько был вкл/выкл
     @see #getSomeMore(String, boolean)
     */
    private String onLinesCheck(String sql, String pcName) {
        PCUserResolver pcUserResolver = new PCUserResolver();
        List<Integer> onLine = new ArrayList<>();
        List<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        threadPoolTaskExecutor = threadConfig.threadPoolTaskExecutor();
        execSet(threadPoolTaskExecutor);
        threadPoolTaskExecutor.execute(() -> pcUserResolver.namesToFile(pcName), ConstantsFor.TIMEOUT_5);
        try(PreparedStatement statement = c.prepareStatement(sql)){
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
            return e.getMessage();
        }
        return stringBuilder
            .append(offLine.size())
            .append(" offline times and ")
            .append(onLine.size())
            .append(" online times.").toString();
    }

    /**
     <b>Проверяет есть ли в БД имя пользователя</b>

     @param sql    запрос
     @param pcName имя ПК
     @return имя юзера, если есть.
     */
    private String offLinesCheckUser(String sql, String pcName) {
        StringBuilder stringBuilder = new StringBuilder();
        try(PreparedStatement p = c.prepareStatement(sql)){
            try(PreparedStatement p1 = c.prepareStatement(sql.replaceAll("pcuser", "pcuserauto"))){
                p.setString(1, pcName);
                p1.setString(1, pcName);
                try(ResultSet resultSet = p.executeQuery()){
                    while(resultSet.next()){
                        stringBuilder.append("<b>")
                            .append(resultSet.getString("userName").trim()).append("</b> (time: ")
                            .append(resultSet.getString("whenQueried")).append(")");
                    }
                }
                try(ResultSet resultSet1 = p1.executeQuery()){
                    while(resultSet1.next()){
                        if(resultSet1.last()){
                            return stringBuilder
                                .append("    (AutoResolved name: ")
                                .append(resultSet1.getString("userName").trim()).append(" (time: ")
                                .append(resultSet1.getString("whenQueried")).append("))").toString();
                        }
                    }
                }
                catch(SQLException ignore){
                    //
                }
            }
        }
        catch(SQLException e){
            stringBuilder.append(e.getMessage());

        }
        return "<font color=\"orange\">EXCEPTION in SQL dropped. <br>" + stringBuilder.toString() + "</font>";
    }

    /**
     Сетает {@link org.springframework.core.task.TaskExecutor} для запуска сканирования отдельного ПК.

     @param executor {@link ThreadConfig}
     */
    private void execSet(ThreadPoolTaskExecutor executor) {
        executor.setThreadGroup(new ThreadGroup("online"));
        executor.setMaxPoolSize(30);
        executor.setCorePoolSize(3);
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setQueueCapacity(317);
    }
}
