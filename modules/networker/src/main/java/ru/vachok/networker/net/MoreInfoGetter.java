package ru.vachok.networker.net;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.ActionCloseMsg;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 Получение более детальной информации о ПК
 <p>

 @since 25.01.2019 (11:06) */
public class MoreInfoGetter {

    /**
     <b>ptv1</b> and <b>ptv2</b> ping stats

     @return статистика пинга ptv
     @see ru.vachok.networker.accesscontrol.MatrixCtr#getFirst(HttpServletRequest, Model, HttpServletResponse)
     */
    public static String getTVNetInfo() {
        List<String> readFileToList = FileSystemWorker.readFileToList(new File("ping.tv").getAbsolutePath());
        List<String> onList = new ArrayList<>();
        List<String> offList = new ArrayList<>();
        readFileToList.stream().flatMap((String x) -> Arrays.stream(x.split(", "))).forEach((String s) -> {
            if (s.contains("true")) onList.add(s.split("/")[0]);
            else offList.add(s.split("/")[0]);
        });
        String ptv1Str = OtherKnownDevices.PTV1_EATMEAT_RU;
        String ptv2Str = OtherKnownDevices.PTV2_EATMEAT_RU;
        int frequencyOffPTV1 = Collections.frequency(offList, ptv1Str);
        int frequencyOnPTV1 = Collections.frequency(onList, ptv1Str);
        int frequencyOnPTV2 = Collections.frequency(onList, ptv2Str);
        int frequencyOffPTV2 = Collections.frequency(offList, ptv2Str);
        String ptv1Stats = "<br><font color=\"#00ff69\">" + frequencyOnPTV1 + " on " + ptv1Str + "</font> | <font color=\"red\">" + frequencyOffPTV1 + " off " + ptv1Str + "</font>";
        String ptv2Stats = "<font color=\"#00ff69\">" + frequencyOnPTV2 + " on " + ptv2Str + "</font> | <font color=\"red\">" + frequencyOffPTV2 + " off " + ptv2Str + "</font>";
        return String.join("<br>\n", ptv1Stats, ptv2Stats);
    }

    /**
     Поиск имён пользователей компьютера
     <p>
     Вернуть: <br> 1. {@link ConditionChecker#onLinesCheck(String, String)}. Если ПК онлайн. Прибавить 1 к {@link NetScannerSvc#onLinePCsNum}. <br> {@code select * from velkompc where NamePP like ?} <br>
     2. {@link ConditionChecker#offLinesCheckUser(String, String)}. Если ПК офлайн. <br> {@code select * from pcuser where pcName like ?}
     <p>

     @param pcName   имя компьютера
     @param isOnline онлайн = true
     @return выдержка из БД (когда последний раз был онлайн + кол-во проверок) либо хранимый в БД юзернейм (для offlines)
     @see NetScannerSvc#getPCNamesPref(String)
     */
    static String getSomeMore(String pcName, boolean isOnline) {
        String sql;
        if (isOnline) {
            sql = "select * from velkompc where NamePP like ?";
            NetScannerSvc.onLinePCsNum += 1;
            return ConditionChecker.onLinesCheck(sql, pcName) + " | " + NetScannerSvc.onLinePCsNum;
        } else {
            sql = "select * from pcuser where pcName like ?";
            return ConditionChecker.offLinesCheckUser(sql, pcName);
        }
    }

    /**
     Достаёт инфо о пользователе из БД
     <p>

     @param userInputRaw {@link NetScannerSvc#getThePc()}
     @return LAST 20 USER PCs
     */
    static String getUserFromDB(String userInputRaw) {
        StringBuilder retBuilder = new StringBuilder();
        String sql = "select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20";
        try {
            userInputRaw = userInputRaw.split(": ")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + userInputRaw + "%");
            try (ResultSet r = p.executeQuery()) {
                StringBuilder stringBuilder = new StringBuilder();
                String headER = "<h3><center>LAST 20 USER PCs</center></h3>";
                stringBuilder.append(headER);
                List<String> stringList = new ArrayList<>();
                while (r.next()) {
                    String pcName = r.getString(ConstantsFor.DBFIELD_PCNAME);
                    stringList.add(pcName);
                    String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " +
                        r.getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER;
                    stringBuilder.append(returnER);
                }
                List<String> collect = stringList.stream().distinct().collect(Collectors.toList());
                for (String x : collect) {
                    int frequency = Collections.frequency(stringList, x);
                    stringBuilder.append(frequency).append(") ").append(x).append("<br>");
                }
                if (r.last()) {
                    MessageToUser messageToUser = new MessageToTray(new ActionCloseMsg(new MessageLocal()));
                    messageToUser.info(
                        r.getString(ConstantsFor.DBFIELD_PCNAME),
                        r.getString(ConstantsNet.DB_FIELD_WHENQUERIED),
                        r.getString(ConstantsFor.DB_FIELD_USER));
                }
                return stringBuilder.toString();
            }
        }
        catch(SQLException | IOException e){
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }
}
