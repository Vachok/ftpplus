package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageCons;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;


/**
 <b>Ищет имя пользователя</b>

 @since 02.10.2018 (17:32) */
@Service
public class PCUserResolver {

    /**
     {@link Logger}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PCUserResolver.class.getSimpleName());

    private static final String REC_AUTO_DB = ".recAutoDB";

    /**
     <i>private cons</i>
     */
    private static final PCUserResolver pcUserResolver = new PCUserResolver();

// --Commented out by Inspection START (25.01.2019 13:58):
//    /**
//     {@link AppComponents#lastNetScan()}.getNetWork()
//     */
//    private static final Map<String, Boolean> lastScanMap = AppComponents.lastNetScan().getNetWork();
// --Commented out by Inspection STOP (25.01.2019 13:58)

    private static final String PC_USER_RESOLVER_CLASS_NAME = "PCUserResolver";

    /**
     {@link RegRuMysql#getDefaultConnection(String)} - u0466446_velkom
     */
    private static Connection connection = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);

    private String lastFileUse;

    private PCUserResolver() {
    }

    /**
     @return {@link #pcUserResolver}
     */
    public static PCUserResolver getPcUserResolver() {
        return pcUserResolver;
    }

    /**
     Записывает содержимое c-users в файл с именем ПК <br> 1 {@link #recAutoDB(String, String)}

     @param pcName имя компьютера
     @see NetScannerSvc
     */
    public synchronized void namesToFile(String pcName) {
        Thread.currentThread().setName(pcName);
        Thread.currentThread().setPriority(3);
        File[] files;
        try (OutputStream outputStream = new FileOutputStream(pcName);
             PrintWriter writer = new PrintWriter(outputStream, true)) {
            String pathAsStr = "\\\\" + pcName + "\\c$\\Users\\";
            lastFileUse = getLastTimeUse(pathAsStr).split("Users")[1];
            files = new File(pathAsStr).listFiles();
            writer
                .append(Arrays.toString(files).replace(", ", "\n"))
                .append("\n\n\n")
                .append(lastFileUse);

        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            FileSystemWorker.error("PCUserResolver.namesToFile", e);
        }
        if (lastFileUse != null) {
            recAutoDB(pcName, lastFileUse);
        }
    }

    /**
     Записывает инфо о пльзователе в <b>pcuserauto</b> <br> Записи добавляются к уже имеющимся.
     <p>
     Usages: {@link PCUserResolver#namesToFile(String)} <br> Uses: -

     @param pcName      имя ПК
     @param lastFileUse строка - имя последнего измененного файла в папке пользователя.
     */
    private synchronized void recAutoDB(String pcName, String lastFileUse) {
        String sql = "insert into pcuser (pcName, userName, lastmod, stamp) values(?,?,?,?)";
        String classMeth = "PCUserResolver.recAutoDB";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql
            .replaceAll(ConstantsFor.STR_PCUSER, ConstantsFor.STR_PCUSERAUTO))){
            String[] split = lastFileUse.split(" ");
            preparedStatement.setString(1, pcName);
            preparedStatement.setString(2, split[0]);
            preparedStatement.setString(3, IntStream.of(2, 3, 4).mapToObj(i -> split[i]).collect(Collectors.joining()));
            preparedStatement.setString(4, split[7]);
            preparedStatement.executeUpdate();
        }
        catch(SQLException e){
            FileSystemWorker.error(classMeth, e);
            connection = reconnectToDB();
        }
        catch(ArrayIndexOutOfBoundsException | NullPointerException e){
            FileSystemWorker.error(classMeth, e);
        }
    }

    private static Connection reconnectToDB() {
        try{
            connection.close();
            connection = null;
            connection = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);
        }
        catch(SQLException e){
            FileSystemWorker.error("PCUserResolver.reconnectToDB", e);
        }
        return connection;
    }

    /**
     Читает БД на предмет наличия юзера для <b>offline</b> компьютера.<br>

     @param pcName имя ПК
     @return имя юзера, время записи.
     @see ADSrv#getDetails(String)
     */
    synchronized String offNowGetU(CharSequence pcName) {
        StringBuilder v = new StringBuilder();
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM)) {

            try (PreparedStatement p = c.prepareStatement("select * from pcuser");
                 PreparedStatement pAuto = c.prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by pcName asc limit 203");
                 ResultSet resultSet = p.executeQuery();
                 ResultSet resultSetA = pAuto.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getString(ConstantsFor.DB_FIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v
                            .append("<b>")
                            .append(resultSet.getString(ConstantsFor.DB_FIELD_USER))
                            .append("</b> <br>At ")
                            .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
                while (resultSetA.next()) {
                    if (resultSetA.getString(ConstantsFor.DB_FIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v
                            .append("<p>")
                            .append(resultSet.getString(ConstantsFor.DB_FIELD_USER))
                            .append(" auto QUERY at: ")
                            .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
            }
        } catch (SQLException e) {
            new MessageCons().errorAlert(PC_USER_RESOLVER_CLASS_NAME, "offNowGetU", e.getMessage());
            FileSystemWorker.error("PCUserResolver.offNowGetU", e);
            NetScannerSvc.getI();
            connection = reconnectToDB();
        }
        return v.toString();
    }

// --Commented out by Inspection START (25.01.2019 13:03):
//    /**
//     Запрос на установку пользователя
//     <p>
//     Usages:  {@link AppComponents#pcUserResolver()} <br>
//     Uses: {@link AppComponents#adSrv()} <br>
//
//     @return {@link ADSrv#getAdUser()}
//     @see ActDirectoryCTRL
//     */
//    ADUser adUsersSetter() {
//        ADSrv adSrv = AppComponents.adSrv();
//        ADUser adUser = adSrv.getAdUser();
//        try{
//            String resolvedName = getResolvedName();
//            LOGGER.info(resolvedName);
//            adUser.setUserName(resolvedName);
//        }
//        catch(NullPointerException e){
//            LOGGER.warn("I cant set User for");
//            Thread.currentThread().interrupt();
//        }
//        Thread.currentThread().interrupt();
//        return adUser;
//    }
// --Commented out by Inspection STOP (25.01.2019 13:03)

// --Commented out by Inspection START (25.01.2019 13:45):
//    /**
//     <b>Рабочий метод</b>
//     Делает запрос в {@code \\c$\Users}, ищет там папки, записывает в массив. <br> Сортирует по дате изменения.
//
//     @return {@link String}, имя последнего измененного объекта.
//     @see #adUsersSetter()
//     */
//    private synchronized String getResolvedName() {
//        List<String> onlineNow = new ArrayList<>();
//        List<String> offNow = new ArrayList<>();
//        StringBuilder stringBuilder = new StringBuilder();
//        if(!lastScanMap.isEmpty()){
//            lastScanMap.forEach((x, y) -> {
//                if(y){
//                    onlineNow.add(x);
//                }
//                else{
//                    offNow.add(x);
//                }
//            });
//        }
//        else{
//            NetScannerSvc.getI().getPCsAsync();
//        }
//        onlineNow.stream().map(x -> x.replaceAll("<br><b>", "").split("</b><br>")[0]).forEach(x -> {
//            File filesAsFile = new File("\\\\" + x + "\\c$\\Users\\");
//            File[] files = filesAsFile.listFiles();
//            ConstantsFor.COMPNAME_USERS_MAP.put(x, filesAsFile);
//            SortedMap<Long, String> lastMod = new TreeMap<>();
//            if (files != null) {
//                for (File file : files) {
//                    lastMod.put(file.lastModified(), file.getName() + " user " + x + " comp\n");
//
//                }
//            } else {
//                stringBuilder
//                    .append(System.currentTimeMillis())
//                    .append(" millis. Can't set user for: ").append(x).append("\n");
//            }
//            Optional<Long> max = lastMod.keySet().stream().max(Long::compareTo);
//            boolean aLongPresent = max.isPresent();
//            if (aLongPresent) {
//                Long aLong = max.get();
//
//                stringBuilder
//                    .append(lastMod.get(aLong));
//            }
//        });
//        offNow.forEach((String x) -> stringBuilder.append(offNowGetU(x)));
//        String msg = ConstantsFor.COMPNAME_USERS_MAP.size() + ConstantsFor.STR_COMPNAME_USERS_MAP_SIZE;
//        LOGGER.warn(msg);
//        return stringBuilder.toString();
//    }
// --Commented out by Inspection STOP (25.01.2019 13:45)

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private synchronized String getLastTimeUse(String pathAsStr) {
        WalkerToUserFolder walkerToUserFolder = new WalkerToUserFolder();
        try {
            Files.walkFileTree(Paths.get(pathAsStr), Collections.singleton(FOLLOW_LINKS), 2, walkerToUserFolder);
            List<String> timePath = walkerToUserFolder.getTimePath();
            Collections.sort(timePath);
            return timePath.get(timePath.size() - 1);
        } catch (IOException | IndexOutOfBoundsException e) {
            return e.getMessage();
        }
    }

    /**
     Запись в БД <b>pcuser</b><br> Запись по-запросу от браузера.
     <p>
     pcName - уникальный (таблица не переписывается или не дополняется, при наличиизаписи по-компу)
     <p>
     Лог - <b>PCUserResolver.recToDB</b> в папке запуска.
     <p>
     @param userName имя юзера
     @param pcName   имя ПК
     @see ADSrv#getDetails(String)
     */
    synchronized void recToDB(String userName, String pcName) {
        String sql = "insert into pcuser (pcName, userName) values(?,?)";
        ConcurrentMap<String, String> pcUMap = ConstantsNet.PC_U_MAP;
        String msg = userName + " on pc " + pcName + " is set.";
        try(PreparedStatement p = connection.prepareStatement(sql)){
            p.setString(1, userName);
            p.setString(2, pcName);
            p.executeUpdate();
            LOGGER.info(msg);
            pcUMap.put(pcName, msg);
        }
        catch(SQLException e){
            FileSystemWorker.error("PCUserResolver.recToDB", e);
        }
    }

    /**
     Поиск файлов в папках {@code c-users}.

     @since 22.11.2018 (14:46)
     */
    static class WalkerToUserFolder extends SimpleFileVisitor<Path> {

        private final List<String> timePath = new ArrayList<>();

        List<String> getTimePath() {
            return timePath;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            timePath.add(file.toFile().lastModified() + " " + file + " " + new Date(file.toFile().lastModified()) + " " + file.toFile().lastModified());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }
}
