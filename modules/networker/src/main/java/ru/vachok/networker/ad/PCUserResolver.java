package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.net.NetScannerSvc;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 <b>Ищет имя пользователя</b>

 @since 02.10.2018 (17:32) */
@Service
public class PCUserResolver {
    /**
     {@link Logger}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PCUserResolver.class.getSimpleName());

    /**
     {@link AppComponents#lastNetScan()}.getNetWork()
     */
    private static Map<String, Boolean> lastScanMap = AppComponents.lastNetScan().getNetWork();

    /**
     <i>private cons</i>
     */
    private static PCUserResolver pcUserResolver = new PCUserResolver();

    /**
     {@link RegRuMysql#getDefaultConnection(String)} - u0466446_velkom
     */
    private static Connection connection = new RegRuMysql().getDefaultConnection("u0466446_velkom");

    /**
     @return {@link #pcUserResolver}
     @see AppComponents#pcUserResolver()
     */
    public static PCUserResolver getPcUserResolver() {
        return pcUserResolver;
    }

    /**
     Записывает содержимое c-users в файл с именем ПК <br> 1 {@link #recAutoDB(String, File[])}
     @param pcName имя компьютера
     @see NetScannerSvc#onLinesCheck(String, String)
     */
    public void namesToFile(String pcName) {
        File[] files = new File("\\\\" + pcName + "\\c$\\Users\\").listFiles();
        try (OutputStream outputStream = new FileOutputStream(pcName);
             PrintWriter writer = new PrintWriter(outputStream, true)) {
            writer.append(Arrays.toString(files).replace(", ", "\n"));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (files != null) {
            recAutoDB(pcName, files);
        }
    }

    /**
     Записывает инфо о пльзователе в <b>pcuserauto</b> <br> Записи добавляются к уже имеющимся.

     @param pcName имя ПК
     @param files  файлы из Users
     @see #namesToFile(String)
     */
    private void recAutoDB(String pcName, File[] files) {
        ConcurrentMap<Long, String> timeName = new ConcurrentHashMap<>();
        for (File f : files) {
            timeName.put(f.lastModified(), f.getName());
        }
        List<String> sortedTimeName = new ArrayList<>();
        timeName.forEach((timeStamp, fileName) -> sortedTimeName.add(timeStamp + "___" + new Date(timeStamp) + "___" + fileName));
        Collections.sort(sortedTimeName);
        String sql = "insert into pcuser (pcName, userName, lastmod, stamp) values(?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql
            .replaceAll("pcuser", "pcuserauto"))) {
            String[] split = sortedTimeName.get(sortedTimeName.size() - 1).split("___");
            preparedStatement.setString(1, pcName);
            preparedStatement.setString(2, split[2]);
            preparedStatement.setString(3, split[1]);
            preparedStatement.setString(4, split[0]);
            preparedStatement.executeUpdate();
        } catch (SQLException | ArrayIndexOutOfBoundsException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     Запрос на установку пользователя

     @return {@link ADSrv#getAdUser()}
     @see ActDirectoryCTRL#adUserString()
     @see ActDirectoryCTRL
     */
    ADUser adUsersSetter() {
        ADSrv adSrv = AppComponents.adSrv();
        ADUser adUser = adSrv.getAdUser();
        try {
            String resolvedName = getResolvedName();
            LOGGER.info(resolvedName);
            adUser.setUserName(resolvedName);
        } catch (NullPointerException e) {
            LOGGER.warn("I cant set User for");
        }
        return adUser;
    }

    /**
     <b>Рабочий метод</b>
     Делает запрос в {@code \\c$\Users}, ищет там папки, записывает в массив. <br> Сортирует по дате изменения.

     @return {@link String}, имя последнего измененного объекта.
     @see #adUsersSetter()
     */
    private String getResolvedName() {
        List<String> onlineNow = new ArrayList<>();
        List<String> offNow = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        if(!lastScanMap.isEmpty()){
            lastScanMap.forEach((x, y) -> {
                if (y) {
                    onlineNow.add(x);
                } else {
                    offNow.add(x);
                }
            });
        } else {
            NetScannerSvc.getI().getPCsAsync();
        }
        onlineNow.forEach(x -> {
            x = x.replaceAll("<br><b>", "").split("</b><br>")[0];
            File filesAsFile = new File("\\\\" + x + "\\c$\\Users\\");
            File[] files = filesAsFile.listFiles();
            ConstantsFor.COMPNAME_USERS_MAP.put(x, filesAsFile);
            SortedMap<Long, String> lastMod = new TreeMap<>();
            if(files!=null){
                for(File file : files){
                    lastMod.put(file.lastModified(), file.getName() + " user " + x + " comp\n");

                }
            } else {
                stringBuilder
                    .append(System.currentTimeMillis())
                    .append(" millis. Can't set user for: ").append(x).append("\n");
            }
            Optional<Long> max = lastMod.keySet().stream().max(Long::compareTo);
            boolean aLongPresent = max.isPresent();
            if(aLongPresent){
                Long aLong = max.get();

                stringBuilder
                    .append(lastMod.get(aLong));
            }
        });
        offNow.forEach(x -> {
            stringBuilder.append(offNowGetU(x));
        });
        String msg = ConstantsFor.COMPNAME_USERS_MAP.size() + " COMPNAME_USERS_MAP size";
        LOGGER.warn(msg);
        return stringBuilder.toString();
    }

    /**
     Читает БД на предмет наличия юзера для <b>offline</b> компьютера.<br> {@link #getResolvedName()}

     @param pcName имя ПК
     @return имя юзера, время записи.
     @see ADSrv#getDetails(String)
     */
    String offNowGetU(String pcName) {
        StringBuilder v = new StringBuilder();
        try (Connection c = new RegRuMysql().getDefaultConnection("u0466446_velkom")) {
            String userName = "userName";
            String whenQueried = "whenQueried";
            String columnLabel = "pcName";
            try (PreparedStatement p = c.prepareStatement("select * from pcuser");
                 PreparedStatement pAuto = c.prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by pcName asc limit 203");
                 ResultSet resultSet = p.executeQuery();
                 ResultSet resultSetA = pAuto.executeQuery()) {
                while (resultSet.next()) {

                    if (resultSet.getString(columnLabel).toLowerCase().contains(pcName)) {
                        v
                            .append("<b>")
                            .append(resultSet.getString(userName))
                            .append("</b> <br>At ")
                            .append(resultSet.getString(whenQueried));
                    }
                }
                while (resultSetA.next()) {
                    if (resultSetA.getString(columnLabel).toLowerCase().contains(pcName)) {
                        v
                            .append("<p>")
                            .append(resultSet.getString(userName))
                            .append(" auto QUERY at: ")
                            .append(resultSet.getString(whenQueried));
                    }
                }
            }
        } catch (SQLException e) {
            return e.getMessage();
        }
        return v.toString();
    }

    /**
     Запись в БД <b>pcuser</b><br> Запись по-запросу от браузера. <br> pcName - уникальный (таблица не переписывается или не дополняется, при наличии записи по-компу)

     @param userName имя юзера
     @param pcName   имя ПК
     @see ADSrv#getDetails(String)
     */
    void recToDB(String userName, String pcName) {
        String sql = "insert into pcuser (pcName, userName) values(?,?)";
        ConcurrentMap<String, String> pcUMap = ConstantsFor.PC_U_MAP;
        String msg = userName + " on pc " + pcName + " is set.";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, userName);
            p.setString(2, pcName);
            p.executeUpdate();
            LOGGER.info(msg);
            pcUMap.put(pcName, msg);
        } catch (SQLException e) {
            LOGGER.warn(msg.replace(" set.", " not set!"));
        }
    }
}
