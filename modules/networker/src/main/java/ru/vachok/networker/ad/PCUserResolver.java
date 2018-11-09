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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


/**<b>Ищет имя пользователя</b>
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
     * <i>private cons</i>
     */
    private static PCUserResolver pcUserResolver = new PCUserResolver();

    /**
     @see AppComponents#pcUserResolver()
     @return {@link #pcUserResolver}
     */
    public static PCUserResolver getPcUserResolver() {
        return pcUserResolver;
    }

    /**<b>Запрос</b>
     @see ActDirectoryCTRL
     @return {@link ADSrv#getAdUser()}
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

    /**<b>Рабочий метод</b>
     Делает запрос в {@code \\c$\Users}, ищет там папки, записывает в массив. <br>
     Сортирует по дате изменения.
     @return {@link String}, имя последнего измененного объекта.
     */
    private String getResolvedName() {
        List<String> onlineNow = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        if (!lastScanMap.isEmpty()) {
            lastScanMap.forEach((x, y) -> {
                if (y) onlineNow.add(x);
            });
        } else NetScannerSvc.getI().getPCsAsync();
        onlineNow.forEach(x -> {
            x = x.replaceAll("<br><b>", "").split("</b><br>")[0];
            File filesAsFile = new File("\\\\" + x + "\\c$\\Users\\");
            File[] files = filesAsFile.listFiles();
            ConstantsFor.COMPNAME_USERS_MAP.put(x, filesAsFile);
            SortedMap<Long, String> lastMod = new TreeMap<>();
            if (files != null) {
                for (File file : files) {
                    lastMod.put(file.lastModified(), file.getName() + " user " + x + " comp\n");

                }
            } else lastMod.put(System.currentTimeMillis(), "Can't set user for: " + x + "\n");

            Optional<Long> max = lastMod.keySet().stream().max(Long::compareTo);
            boolean aLongPresent = max.isPresent();
            if (aLongPresent) {
                Long aLong = max.get();

                stringBuilder
                    .append(lastMod.get(aLong));
            }
        });
        String msg = ConstantsFor.COMPNAME_USERS_MAP.size() + " COMPNAME_USERS_MAP size";
        LOGGER.warn(msg);
        return stringBuilder.toString();
    }

    void recToDB(String userName, String pcName) {
        DataConnectTo dataConnectTo = new RegRuMysql();
        String msg = userName + " on pc " + pcName + " is set.";
        Connection connection = dataConnectTo.getDefaultConnection("u0466446_velkom");
        String sql = "insert into pcuser (pcName, userName) values(?,?)";
        try(PreparedStatement p = connection.prepareStatement(sql)){
            p.setString(1, userName);
            p.setString(2, pcName);
            p.executeUpdate();

            LOGGER.info(msg);
        }
        catch(SQLException e){
            LOGGER.warn(msg.replace(" set.", " not set!"));
        }
    }
}
