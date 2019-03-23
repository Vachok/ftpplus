package ru.vachok.networker.net;



import org.springframework.ui.Model;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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
}
