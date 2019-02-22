package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ActDirectoryCTRL;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.LastNetScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.ActionCloseMsg;
import ru.vachok.networker.systray.ActionDefault;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.time.format.TextStyle.FULL_STANDALONE;


/**
 Управление сервисами LAN-разведки.
 <p>

 @since 21.08.2018 (14:40) */
@Service(ConstantsNet.STR_NETSCANNERSVC)
public final class NetScannerSvc {

    /**
     NetScannerSvc
     */
    private static final String CLASS_NAME = "NetScannerSvc";

    /**
     {@link LoggerFactory#getLogger(String)} - {@link #CLASS_NAME}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CLASS_NAME);

    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPS = AppComponents.getProps();

    /**
     Имя метода, как строка.
     <p>
     {@link NetScannerSvc#getPCsAsync()}
     */
    private static final String METHNAME_GET_PCS_ASYNC = "NetScannerSvc.getPCsAsync";

    /**
     Файл уникальных записей из БД velkom-pcuserauto
     */
    private static final String FILENAME_PCAUTODISTXT = "pcautodis.txt";

    private static Connection connection;

    private static MessageToUser messageToUser = new MessageLocal();

    /**
     Компьютеры онлайн
     */
    static int onLinePCsNum = 0;

    /**
     {@link ConstantsNet#getPcNames()}
     */
    private static Set<String> pcNamesSet = ConstantsNet.getPcNames();

    /**
     Неиспользуемые имена ПК

     @see #getPCNamesPref(String)
     */
    private static Collection<String> unusedNamesTree = new TreeSet<>();

    /**
     new {@link NetScannerSvc}
     */
    private static NetScannerSvc netScannerSvcInst = new NetScannerSvc();

    /**
     Время инициализации
     */
    private long startClassTime = System.currentTimeMillis();

    /**
     Статистика скана.
     <p>
     {@link #runAfterAllScan()}
     */
    private Runnable runAfterAll = this::runAfterAllScan;

    /**
     /netscan POST форма
     <p>

     @see NetScanCtr {@link }
     */
    private String thePc = "PC";

    /**
     Название {@link Thread}
     <p>
     {@link Thread#getName()}
     */
    private String thrName = Thread.currentThread().getName();

    /**
     {@link AppComponents#lastNetScan()}
     */
    private Map<String, Boolean> netWorkMap;

    /**
     Выполняет запрос в БД по-пользовательскому вводу
     <p>
     Устанавливает {@link ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)}

     @return web-страница с результатом
     */
    public static String getInfoFromDB() {
        StringBuilder sqlQBuilder = new StringBuilder();
        String thePcLoc = AppComponents.netScannerSvc().getThePc();
        if (thePcLoc.isEmpty()) {
            IllegalArgumentException argumentException = new IllegalArgumentException("Must be NOT NULL!");
            sqlQBuilder.append(argumentException.getMessage());
        } else {
            sqlQBuilder.append("select * from velkompc where NamePP like '%").append(thePcLoc).append("%'");
        }
        try(
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQBuilder.toString())) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> timeNow = new ArrayList<>();
                List<Integer> integersOff = new ArrayList<>();
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        timeNow.add(resultSet.getString("TimeNow"));
                    } else {
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
                    AppComponents.netScannerSvc().setThePc(stringBuilder.toString());
                }
                sortList(timeNow);
            }
        } catch (SQLException e) {
            FileSystemWorker.error("NetScannerSvc.getInfoFromDB", e);
        } catch (IndexOutOfBoundsException e) {
            AppComponents.netScannerSvc().setThePc(e.getMessage() + " " + new TForms().fromArray(e, false));
        }
        return "ok";
    }

    /**
     @return {@link #onLinePCsNum}
     */
    static int getOnLinePCs() {
        return onLinePCsNum;
    }

    /**
     Выполняет {@link #getPCsAsync()}.
     <p>

     @return {@link ConstantsNet#getPcNames()}
     @see #getPCNamesPref(String)
     @see NetScanCtr#scanIt(HttpServletRequest, Model, Date)
     */
    Set<String> getPcNames() {
        getPCsAsync();
        return pcNamesSet;
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
     @return {@link #netScannerSvcInst}
     */
    public static synchronized NetScannerSvc getInst() {
        return netScannerSvcInst;
    }

    /**
     @see AppComponents#lastNetScanMap()
     */
    private NetScannerSvc() {
        this.netWorkMap = AppComponents.lastNetScanMap();
    }

    /**
     Запись в таблицу <b>velkompc</b> текущего состояния. <br>
     <p>
     1 {@link TForms#fromArray(List, boolean)}

     @return строка в html-формате
     @see #getPCNamesPref(String)
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "resource"})
    private static String writeDB() throws SQLException {
        List<String> list = new ArrayList<>();
        PreparedStatement p = connection.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)");
        List<String> toSort = new ArrayList<>(pcNamesSet);
        toSort.sort(null);
        for (String x : toSort) {
            String pcSerment = "Я не знаю...";
            ConstantsNet.LOGGER.info(x);
            if (x.contains("200.200")) {
                pcSerment = "Торговый дом";
            }
            if (x.contains("200.201")) {
                pcSerment = "IP телефоны";
            }
            if (x.contains("200.202")) {
                pcSerment = "Техслужба";
            }
            if (x.contains("200.203")) {
                pcSerment = "СКУД";
            }
            if (x.contains("200.204")) {
                pcSerment = "Упаковка";
            }
            if (x.contains("200.205")) {
                pcSerment = "МХВ";
            }
            if (x.contains("200.206")) {
                pcSerment = "Здание склада 5";
            }
            if (x.contains("200.207")) {
                pcSerment = "Сырокопоть";
            }
            if (x.contains("200.208")) {
                pcSerment = "Участок убоя";
            }
            if (x.contains("200.209")) {
                pcSerment = "Да ладно?";
            }
            if (x.contains("200.210")) {
                pcSerment = "Мастера колб";
            }
            if (x.contains("200.212")) {
                pcSerment = "Мастера деликатесов";
            }
            if (x.contains("200.213")) {
                pcSerment = "2й этаж. АДМ.";
            }
            if (x.contains("200.214")) {
                pcSerment = "WiFiCorp";
            }
            if (x.contains("200.215")) {
                pcSerment = "WiFiFree";
            }
            if (x.contains("200.217")) {
                pcSerment = "1й этаж АДМ";
            }
            if (x.contains("192.168")) {
                pcSerment = "Может быть в разных местах...";
            }
            if (x.contains("172.16.200")) {
                pcSerment = "Open VPN авторизация - сертификат";
            }
            boolean onLine = false;
            if (x.contains("true")) {
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
        ConstantsNet.setPcNames(pcNamesSet);
        return new TForms().fromArray(list, true);
    }

    /**
     Сортирует по-алфавиту.
     <p>
     {@link NetScannerSvc#getThePc()} <br>
     {@link LastNetScan#getTimeLastScan()} <br>
     {@link NetScannerSvc#setThePc(java.lang.String)} <br>
     {@link ActDirectoryCTRL#setInputWithInfoFromDB(java.lang.String)}

     @param timeNow {@link ArrayList}, показываемый на странице.
     */
    private static void sortList(List<String> timeNow) {
        Collections.sort(timeNow);

        String str = timeNow.get(timeNow.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AppComponents.netScannerSvc().getThePc());
        stringBuilder.append("Last online: ");
        stringBuilder.append(str);
        stringBuilder.append(" (");
        stringBuilder.append(")<br>Actual on: ");
        stringBuilder.append(AppComponents.lastNetScan().getTimeLastScan());
        stringBuilder.append("</center></font>");

        String thePcWithDBInfo = stringBuilder.toString();
        AppComponents.netScannerSvc().setThePc(thePcWithDBInfo);
        ActDirectoryCTRL.setInputWithInfoFromDB(thePcWithDBInfo);

    }

    /**
     Обнуление счётчика онлайн ПК.
     <p>
     Устанавливает {@link #PROPS} {@link ConstantsNet#ONLINEPC} в "". <br> Устававливает {@link NetScannerSvc#onLinePCsNum} = 0.

     @see #runAfterAll
     */
    private static void setOnLinePCsToZero() {
        new MessageCons().errorAlert("NetScannerSvc.setOnLinePCsToZero");
        PROPS.setProperty(ConstantsNet.ONLINEPC, onLinePCsNum + "");
        NetScannerSvc.onLinePCsNum = 0;
    }

    static {
        try{
            connection = new AppComponents().connection(ConstantsNet.DB_NAME);
        }
        catch(SQLException | IOException e){
            messageToUser.errorAlert("NetScannerSvc", "static initializer", e.getMessage());
            FileSystemWorker.error("NetScannerSvc.static initializer", e);
        }
    }

    /**
     Сканирование с определённым префиксом.
     <p>
     1. {@link #getCycleNames(String)} создаёт имена, для конкретного префикса. <br>
     <i>ПК офлайн:</i> <br>
     2. {@link #pcNameUnreach(String, InetAddress)}. Если комп не пингуется. Добавить в {@link #netWorkMap}. <br>
     <i>ПК он-лайн:</i> <br>
     3. {@link MoreInfoGetter#getSomeMore(String, boolean)}. Когда копм онлайн. Получает последний известный username. 4.
     {@link MoreInfoGetter#getSomeMore(String, boolean)} получает статистику
     (сколько online, сколько offline) <br> Создаётся ссылка {@code a href=\"/ad?"<b>имя</b>/a}. Добавляет в {@link #netWorkMap} put форматированную строку
     {@code printStr, true} <br> Выводит в консоль
     через {@link #LOGGER} строку {@code printStr}. <br> Добавляет в {@link ConstantsNet#getPcNames()}, имя, ip и {@code online true}. <br> При
     возникновении {@link IOException}, например если имя ПК не
     существует, добавляет {@code getMessage} в {@link #unusedNamesTree}
     <p>
     <i>По завершении цикла:</i> <br>
     {@link #netWorkMap} put префикс, кол-во 5. {@link #writeDB()}. записывает в базу.
     <p>

     @param prefixPcName префикс имени ПК. {@link ConstantsNet#PC_PREFIXES}
     @return состояние запрошенного сегмента
     @see NetScanCtr#scanIt(HttpServletRequest, Model, Date)
     @see #getPCsAsync()
     */
    Set<String> getPCNamesPref(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        boolean reachable;
        InetAddress byName;
        Thread.currentThread().setPriority(8);
        String pcsString = null;
        for (String pcName : getCycleNames(prefixPcName)) {
            try {
                byName = InetAddress.getByName(pcName);
                reachable = byName.isReachable(ConstantsFor.TIMEOUT_650);
                if (!reachable) {
                    pcNameUnreach(pcName, byName);
                } else {
                    StringBuilder bild = new StringBuilder();
                    bild.append("<i><font color=\"yellow\">last name is ");
                    bild.append(MoreInfoGetter.getSomeMore(pcName, false));
                    bild.append("</i></font> ");
                    bild.append(MoreInfoGetter.getSomeMore(pcName, true));
                    String onOffCounterAndLastUser = bild.toString();

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(" online ");
                    stringBuilder.append(true);
                    stringBuilder.append("<br>");
                    StringBuilder builder = new StringBuilder();
                    builder.append("<br><b><a href=\"/ad?");
                    builder.append(pcName.split(".eatm")[0]);
                    builder.append("\" >");
                    builder.append(pcName);
                    builder.append("</b></a>     ");
                    builder.append(onOffCounterAndLastUser);
                    builder.append(". ");

                    String printStr = builder.toString();
                    String pcOnline = stringBuilder.toString();
                    String strToConsole = MessageFormat.format("{0} {1} | {2}", pcName, pcOnline, onOffCounterAndLastUser);

                    netWorkMap.put(printStr, true);
                    pcNamesSet.add(pcName + ":" + byName.getHostAddress() + pcOnline);
                    LOGGER.info(strToConsole);
                }
            } catch (IOException e) {
                unusedNamesTree.add(e.getMessage());
            }
        }
        netWorkMap.put("<h4>" + prefixPcName + "     " + pcNamesSet.size() + "</h4>", true);
        try {
            pcsString = writeDB();
        } catch (SQLException e) {
            messageToUser.errorAlert(CLASS_NAME, "getPCNamesPref", e.getMessage());
            FileSystemWorker.error("NetScannerSvc.getPCNamesPref", e);
        }
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        pcNamesSet.add(elapsedTime);
        ConstantsNet.setPcNames(pcNamesSet);

        LOGGER.info(pcsString);
        return pcNamesSet;
    }

    /**
     Основной скан-метод.
     <p>
     1. {@link #fileCreate(boolean)}. Убедимся, что файл создан. <br>
     2. {@link ActionCloseMsg} , 3. {@link MessageToTray}. Создаём взаимодействие с юзером. <br>
     3. {@link ConstantsFor#getUpTime()} - uptime приложения в 4. {@link MessageToTray#info(java.lang.String, java.lang.String, java.lang.String)}. <br>
     5. {@link NetScannerSvc#getPCNamesPref(java.lang.String)} - скан сегмента. <br>

     @see #getPcNames()
     */
    @SuppressWarnings("OverlyLongLambda")
    private void getPCsAsync() {
        AtomicReference<String> msg = new AtomicReference<>("");
        this.startClassTime = System.currentTimeMillis();
        boolean fileCreate = fileCreate(true);
        try{
            new MessageToTray(new ActionCloseMsg(new MessageLocal())).info("NetScannerSvc started scan", ConstantsFor.getUpTime(), " File: " + fileCreate);
        }
        catch(NoClassDefFoundError e){
            messageToUser.errorAlert(CLASS_NAME, "getPCsAsync", e.getMessage());
        }
        AppComponents.threadConfig().executeAsThread(() -> {
            Thread.currentThread().setName("E-" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP));
            msg.set(new StringBuilder()
                .append("Thread id ")
                .append(Thread.currentThread().getId())
                .append(" name ")
                .append(Thread.currentThread().getName())
                .toString());

            ConstantsNet.LOGGER.warn(msg.get());

            for (String s : ConstantsNet.PC_PREFIXES) {
                this.thrName = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec";
                pcNamesSet.clear();
                pcNamesSet.addAll(getPCNamesPref(s));
                Thread.currentThread().setName(thrName);
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            pcNamesSet.add(elapsedTime);
            ConstantsNet.LOGGER.warn(msg.get());
            AppComponents.threadConfig().executeAsThread(runAfterAll);
        });
    }

    /**
     1. {@link #getNamesCount(String)}

     @param namePCPrefix префикс имени ПК
     @return обработанные имена ПК, для пинга
     @see #getPCNamesPref(String)
     */
    private Collection<String> getCycleNames(String namePCPrefix) {
        new MessageCons().errorAlert("NetScannerSvc.getCycleNames");
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        int inDex = getNamesCount(namePCPrefix);
        String nameCount;
        Collection<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < inDex; i++) {
            if (namePCPrefix.equals("no") || namePCPrefix.equals("pp") || namePCPrefix.equals("do")) {
                nameCount = String.format("%04d", ++pcNum);
            } else {
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(namePCPrefix + nameCount + ConstantsFor.EATMEAT_RU);
        }
        new MessageCons().info(
            ConstantsFor.STR_INPUT_OUTPUT,
            "namePCPrefix = [" + namePCPrefix + "]",
            "java.util.Collection<java.lang.String>");
        return list;
    }

    /**
     Если ПК не пингуется
     <p>
     Добавить в {@link #netWorkMap} , {@code online = false}.
     <p>
     {@link MoreInfoGetter#getSomeMore(String, boolean)}. Получить более подробную информацию о ПК.
     <p>

     @param pcName имя ПК
     @param byName {@link InetAddress}
     @see #getPCNamesPref(String)
     */
    private void pcNameUnreach(String pcName, InetAddress byName) {
        String someMore = MoreInfoGetter.getSomeMore(pcName, false);
        String onLines = new StringBuilder()
            .append("online ")
            .append(false)
            .append("<br>").toString();
        pcNamesSet.add(pcName + ":" + byName.getHostAddress() + " " + onLines);
        String format = MessageFormat.format("{0} {1} | {2}", pcName, onLines, someMore);
        netWorkMap.put(pcName + " last name is " + someMore, false);
        LOGGER.warn(format);
    }

    /**
     Создание lock-файла
     <p>

     @param create создать или удалить файл.
     @return scan.tmp exist
     @see #getPCsAsync()
     */
    private boolean fileCreate(boolean create) {
        File file = new File("scan.tmp");
        try {
            if (create) {
                file = Files.createFile(file.toPath()).toFile();
            } else {
                Files.deleteIfExists(Paths.get("scan.tmp"));
            }
        } catch (IOException e) {
            FileSystemWorker.error("NetScannerSvc.fileCreate", e);
        }
        boolean exists = file.exists();
        if (exists) {
            file.deleteOnExit();
        }
        return exists;
    }

    /**
     Статистика по-сканированию.
     <p>
     {@link TForms#fromArray(java.util.Map, boolean)}. Преобразуем в строку {@link ConstantsNet#COMPNAME_USERS_MAP}. <br>
     {@link TForms#fromArrayUsers(java.util.concurrent.ConcurrentMap, boolean)} - преобразуем {@link ConstantsNet#PC_U_MAP}. <br>
     Создадим еще 2 {@link String}, {@code msgTimeSp} - сколько времени прощло после инициализации. {@code valueOfPropLastScan} - когда было последнее
     сканирование.
     Инфо из {@link #PROPS}. <br>
     Все строки + {@link TForms#fromArray(java.util.Properties, boolean)} - {@link #PROPS}, добавим в {@link ArrayList} {@code toFileList}.
     <p>
     {@link Properties#setProperty(java.lang.String, java.lang.String)} = {@code valueOfPropLastScan}. <br>
     {@link AppComponents#getProps(boolean)} - {@link #PROPS}.
     <p>
     {@link MessageToTray#info(java.lang.String, java.lang.String, java.lang.String)}
     <p>
     {@link NetScannerSvc#setOnLinePCsToZero()} <br>
     {@link LastNetScan#setTimeLastScan(java.util.Date)} - сейчас. <br>
     {@link NetScannerSvc#countStat()}. <br>
     {@link FileSystemWorker#recFile(java.lang.String, java.lang.String)} - {@link ConstantsNet#STR_LASTNETSCAN}.
     ({@link AppComponents#lastNetScanMap()}). <br>
     {@link ESender#info(java.lang.String, java.lang.String, java.lang.String)}.
     <p>
     {@link FileSystemWorker#recFile(java.lang.String, java.util.List)} - {@code toFileList}. <br>
     {@link FileSystemWorker#recFile(java.lang.String, java.util.stream.Stream)} - {@link #unusedNamesTree}.
     <p>
     {@link MessageSwing#infoTimer(int, java.lang.String)}
     */
    @SuppressWarnings("MagicNumber")
    private void runAfterAllScan() {
        float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
        List<String> toFileList = new ArrayList<>();
        MessageToUser mailMSG = new ESender(ConstantsFor.GMAIL_COM);

        String compNameUsers = new TForms().fromArray(ConstantsNet.COMPNAME_USERS_MAP, false);
        String psUser = new TForms().fromArrayUsers(ConstantsNet.PC_U_MAP, false);
        String msgTimeSp = "NetScannerSvc.getPCsAsync method. " + (float) (System.currentTimeMillis() - startClassTime) / 1000 + ConstantsFor.STR_SEC_SPEND;
        String valueOfPropLastScan = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY) + "";

        PROPS.setProperty(ConstantsNet.PR_LASTSCAN, valueOfPropLastScan);

        toFileList.add(compNameUsers);
        toFileList.add(psUser);
        toFileList.add(msgTimeSp);
        toFileList.add(new TForms().fromArray(PROPS, false));

        new MessageToTray(new ActionDefault(ConstantsNet.HTTP_LOCALHOST_8880_NETSCAN)).info(
            "Netscan complete!",
            "Online: " + onLinePCsNum,
            upTime + " min uptime.");

        NetScannerSvc.setOnLinePCsToZero();
        AppComponents.lastNetScan().setTimeLastScan(new Date());
        countStat();
        FileSystemWorker.recFile(ConstantsNet.STR_LASTNETSCAN, new TForms().fromArray(AppComponents.lastNetScanMap(), false));
        String bodyMsg = ConstantsFor.getMemoryInfo() + "\n" + " scan.tmp exist = " + fileCreate(false) + "\n" + new TForms().fromArray(toFileList, false);
        mailMSG.info(
            this.getClass().getSimpleName(),
            "getPCsAsync " + ConstantsFor.getUpTime() + " " + ConstantsFor.thisPC(),
            bodyMsg);
        FileSystemWorker.recFile(this.getClass().getSimpleName() + ".getPCsAsync", toFileList);
        FileSystemWorker.recFile("unused.ips", unusedNamesTree.stream());
        new MessageSwing(656, 550, 50, 53).infoTimer(50,
            "Daysec: " +
                LocalTime.now().toSecondOfDay() + " " +
                LocalDate.now().getDayOfWeek().getDisplayName(FULL_STANDALONE, Locale.getDefault()) + "\n" +
                bodyMsg);
        Properties properties = AppComponents.getProps(true);
        LOGGER.info(new TForms().fromArray(properties, false) + "\n\n" + properties.size());
    }

    /**
     Подсчёт статистики по {@link ConstantsNet#VELKOM_PCUSERAUTO_TXT}
     <p>
     {@link List} readFileAsList - читает по-строкам {@link ConstantsNet#VELKOM_PCUSERAUTO_TXT}.
     <p>
     {@link Stream#distinct()} - запись файла {@link #FILENAME_PCAUTODISTXT}.
     <p>
     {@link MessageCons#info(java.lang.String, java.lang.String, java.lang.String)} - покажем в консоль. <br>
     Cкопируем на 111.1, if {@link String#contains(java.lang.CharSequence)} "home". {@link ConstantsFor#thisPC()}.
     */
    private void countStat() {
        List<String> readFileAsList = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(ConstantsNet.VELKOM_PCUSERAUTO_TXT);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            while (inputStreamReader.ready()) {
                readFileAsList.add(bufferedReader.readLine().split("\\Q0) \\E")[1]);
            }
        } catch (IOException e) {
            new MessageCons().errorAlert(CLASS_NAME, "countStat", e.getMessage());
        }
        FileSystemWorker.recFile(FILENAME_PCAUTODISTXT, readFileAsList.parallelStream().distinct());
        String valStr = FileSystemWorker.readFile(FILENAME_PCAUTODISTXT);
        new MessageCons().info(ConstantsFor.SOUTV, "NetScannerSvc.countStat", valStr);
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + FILENAME_PCAUTODISTXT;
            FileSystemWorker.copyOrDelFile(new File(FILENAME_PCAUTODISTXT), toCopy, true);

        }
    }

    /**
     @param qer префикс имени ПК
     @return кол-во ПК, для пересичления
     @see #getCycleNames(String)
     */
    private int getNamesCount(String qer) {
        int inDex = 0;
        if (qer.equals("no")) {
            inDex = ConstantsNet.NOPC;
        }
        if (qer.equals("pp")) {
            inDex = ConstantsNet.PPPC;
        }
        if (qer.equals("do")) {
            inDex = ConstantsNet.DOPC;
        }
        if (qer.equals("a")) {
            inDex = ConstantsNet.APC;
        }
        if (qer.equals("td")) {
            inDex = ConstantsNet.TDPC;
        }
        return inDex;
    }

    @Override
    public int hashCode() {
        int result = (int) (startClassTime ^ (startClassTime >>> 32));
        result = 31 * result + getThePc().hashCode();
        result = 31 * result + (netWorkMap != null ? netWorkMap.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetScannerSvc)) return false;

        NetScannerSvc that = (NetScannerSvc) o;

        if (startClassTime != that.startClassTime) return false;
        if (!getThePc().equals(that.getThePc())) return false;
        return netWorkMap != null ? netWorkMap.equals(that.netWorkMap) : that.netWorkMap == null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        sb.append("CLASS_NAME='").append(CLASS_NAME).append('\'');
        sb.append(", PROPS=").append(PROPS.equals(AppComponents.getProps()));
        sb.append(", METHNAME_GET_PCS_ASYNC='").append(METHNAME_GET_PCS_ASYNC).append('\'');
        sb.append(", FILENAME_PCAUTODISTXT='").append(FILENAME_PCAUTODISTXT).append('\'');
        sb.append(", pcNamesSet=").append(pcNamesSet.size());
        sb.append(", onLinePCsNum=").append(onLinePCsNum);
        sb.append(", unusedNamesTree=").append(unusedNamesTree.size());
        sb.append(", netScannerSvcInst=").append(netScannerSvcInst.hashCode());
        sb.append(", startClassTime=").append(new Date(startClassTime));
        sb.append(", runAfterAll=").append(runAfterAll.toString());
        sb.append(", thePc='").append(thePc).append('\'');
        sb.append(", thrName='").append(thrName).append('\'');
        sb.append(", netWorkMap=").append(netWorkMap.size());
        sb.append('}');
        return sb.toString();
    }

}
