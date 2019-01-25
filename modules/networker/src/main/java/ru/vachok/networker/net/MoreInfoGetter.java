package ru.vachok.networker.net;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 Получение более детальной информации о ПК
 <p>

 @since 25.01.2019 (11:06) */
class MoreInfoGetter extends NetScannerSvc {

// --Commented out by Inspection START (25.01.2019 13:45):
//    MoreInfoGetter() {
//        super();
//    }
// --Commented out by Inspection STOP (25.01.2019 13:45)

    /**
     Поиск имён пользователей компьютера <br> Обращения: <br> 1 {@link #onLinesCheck(String, String)} 1.1 {@link ThreadConfig#threadPoolTaskExecutor()} 1.2 {@link PCUserResolver#namesToFile(String)}
     <br> 2. {@link #offLinesCheckUser(String, String)}

     @param pcName   имя компьютера
     @param isOnline онлайн = true
     @return выдержка из БД (когда последний раз был онлайн + кол-во проверок) либо хранимый в БД юзернейм (для offlines)
     @see #getPCNamesPref(String)
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static String getSomeMore(String pcName, boolean isOnline) {
        String sql;
        if (isOnline) {
            sql = "select * from velkompc where NamePP like ?";
            onLinePCs = onLinePCs + 1;
            return onLinesCheck(sql, pcName) + " | " + onLinePCs;
        } else {
            sql = "select * from pcuser where pcName like ?";
            return offLinesCheckUser(sql, pcName);
        }
    }

    /**
     Проверяет имя пользователя на ПК онлайн

     @param sql    запрос
     @param pcName имя ПК
     @return кол-во проверок и сколько был вкл/выкл
     @see #getSomeMore(String, boolean)
     */
    private static String onLinesCheck(String sql, String pcName) {
        PCUserResolver pcUserResolver = PCUserResolver.getPcUserResolver(c);
        List<Integer> onLine = new ArrayList<>();
        List<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            Runnable r = () -> pcUserResolver.namesToFile(pcName);
            ThreadConfig.executeAsThread(r);
            statement.setString(1, pcName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ADComputer adComputer = new ADComputer();
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        onLine.add(onlineNow);
                        adComputer.setDnsHostName(pcName);
                    }
                    if (onlineNow == 0) {
                        offLine.add(onlineNow);
                    }
                    ConstantsNet.AD_COMPUTERS.add(adComputer);
                }
            }
        } catch (SQLException e) {
            reconnectToDB();
            FileSystemWorker.recFile(
                NetScannerSvc.class.getSimpleName() + ConstantsNet.ONLINES_CHECK + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e, false)));
            stringBuilder.append(e.getMessage());
        } catch (NullPointerException e) {
            FileSystemWorker.recFile(NetScannerSvc.class.getSimpleName() + ConstantsNet.ONLINES_CHECK + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e, false)));
            stringBuilder.append(e.getMessage());
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
    @SuppressWarnings({"MethodWithMultipleLoops", "MethodWithMultipleReturnPoints"})
    private static String offLinesCheckUser(String sql, String pcName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (PreparedStatement p = c.prepareStatement(sql);
             PreparedStatement p1 = c.prepareStatement(sql.replaceAll(ConstantsFor.STR_PCUSER, ConstantsFor.STR_PCUSERAUTO))) {
            p.setString(1, pcName);
            p1.setString(1, pcName);
            try (ResultSet resultSet = p.executeQuery();
                 ResultSet resultSet1 = p1.executeQuery()) {
                while (resultSet.next()) {
                    stringBuilder.append("<b>")
                        .append(resultSet.getString(ConstantsFor.DB_FIELD_USER).trim()).append("</b> (time: ")
                        .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append(")");
                }
                while (resultSet1.next()) {
                    if (resultSet1.last()) {
                        return stringBuilder
                            .append("    (AutoResolved name: ")
                            .append(resultSet1.getString(ConstantsFor.DB_FIELD_USER).trim()).append(" (time: ")
                            .append(resultSet1.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append("))").toString();
                    }
                }
            }
        } catch (SQLException e) {
            FileSystemWorker.recFile(
                NetScannerSvc.class.getSimpleName() + "offLinesCheckUser" + ConstantsFor.LOG,
                Collections.singletonList(new TForms().fromArray(e, false)));
            stringBuilder.append(e.getMessage());
            reconnectToDB();
        }
        return "<font color=\"orange\">EXCEPTION in SQL dropped. <br>" + stringBuilder.toString() + "</font>";
    }
}
