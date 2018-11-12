package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.net.NetScannerSvc;

import java.io.File;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;


/**
 <b>Ищет имя пользователя</b>

 @since 02.10.2018 (17:32) */
@Service
public class PCUserResolver {

    /*Fields*/

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
     @return {@link #pcUserResolver}
     @see AppComponents#pcUserResolver()
     */
    public static PCUserResolver getPcUserResolver() {
        return pcUserResolver;
    }

    /**
     @param onlineAddr адреса онлайн ПК.
     @see NetScannerSvc
     */
    public void resolveNamesAuto(List<InetAddress> onlineAddr) {

    }

    /**
     <b>Запрос</b>

     @return {@link ADSrv#getAdUser()}
     @see ActDirectoryCTRL
     */
    ADUser adUsersSetter() {
        ADSrv adSrv = AppComponents.adSrv();
        ADUser adUser = adSrv.getAdUser();
        try{
            String resolvedName = getResolvedName();
            LOGGER.info(resolvedName);
            adUser.setUserName(resolvedName);
        }
        catch(NullPointerException e){
            LOGGER.warn("I cant set User for");
        }
        return adUser;
    }

    /**
     <b>Рабочий метод</b>
     Делает запрос в {@code \\c$\Users}, ищет там папки, записывает в массив. <br>
     Сортирует по дате изменения.

     @return {@link String}, имя последнего измененного объекта.
     */
    private String getResolvedName() {
        List<String> onlineNow = new ArrayList<>();
        List<String> offNow = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        if(!lastScanMap.isEmpty()){
            lastScanMap.forEach((x, y) -> {
                if(y){
                    onlineNow.add(x);
                }
                else{
                    offNow.add(x);
                }
            });
        }
        else{
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
            }
            else{
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

    String offNowGetU(String pcName) {
        StringBuilder v = new StringBuilder();
        Connection c = new RegRuMysql().getDefaultConnection("u0466446_velkom");
        String sql = "select * from pcuser";
        try(PreparedStatement p = c.prepareStatement(sql);
            ResultSet resultSet = p.executeQuery()){
            while(resultSet.next()){
                if(resultSet.getString("pcName").toLowerCase().contains(pcName)){
                    v
                        .append("<b>")
                        .append(resultSet.getString("userName"))
                        .append("</b> <br>At ")
                        .append(resultSet.getString("whenQueried"));
                }
            }
        }
        catch(SQLException e){
            return e.getMessage();
        }
        return v.toString();
    }

    void recToDB(String userName, String pcName) {
        ConcurrentMap<String, String> pcUMap = ConstantsFor.PC_U_MAP;
        DataConnectTo dataConnectTo = new RegRuMysql();
        Connection connection = dataConnectTo.getDefaultConnection("u0466446_velkom");

        String msg = userName + " on pc " + pcName + " is set.";
        String sql = "insert into pcuser (pcName, userName) values(?,?)";

        try(PreparedStatement p = connection.prepareStatement(sql)){
            p.setString(1, userName);
            p.setString(2, pcName);
            p.executeUpdate();
            LOGGER.info(msg);
            pcUMap.put(pcName, msg);
        }
        catch(SQLException e){
            LOGGER.warn(msg.replace(" set.", " not set!"));
        }
    }
}
