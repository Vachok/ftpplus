
package ru.vachok.networker.net;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.PCUserResolver;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 Проверки из классов.
 <p>
 Пинги, и тп

 @since 31.01.2019 (0:20) */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class ConditionChecker {


    private static final String CLASS_NAME = ConditionChecker.class.getSimpleName();

    private static Connection connection = null;

    private static MessageToUser messageToUser = new MessageLocal(ConditionChecker.class.getSimpleName());

    private ConditionChecker() {
        AppComponents.threadConfig().thrNameSet("CondC");
    }


    static {
        try {
            connection = new AppComponents().connection(ConstantsNet.DB_NAME);
        } catch (IOException e) {
            messageToUser.errorAlert(CLASS_NAME, ConstantsFor.METHNAME_STATIC_INITIALIZER, e.getMessage());
            FileSystemWorker.error("ConditionChecker.static initializer", e);
        }
    }

    /**
     Проверяет имя пользователя когда ПК онлайн
     <p>

     @param sql запрос
     @param pcName имя ПК
     @return кол-во проверок и сколько был вкл/выкл

     @see MoreInfoGetter#getSomeMore(String, boolean)
     */
    static String onLinesCheck(String sql, String pcName) {
        AppComponents.threadConfig().thrNameSet("onChk");
        PCUserResolver pcUserResolver = PCUserResolver.getPcUserResolver();
        Collection<Integer> onLine = new ArrayList<>();
        Collection<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        String classMeth = "ConditionChecker.onLinesCheck";
        try (
            PreparedStatement statement = connection.prepareStatement(sql)) {
            Runnable rPCResolver = ()->pcUserResolver.namesToFile(pcName);
            AppComponents.threadConfig().execByThreadConfig(rPCResolver);
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
                }
            }
        } catch (SQLException e) {
            messageToUser.errorAlert(CLASS_NAME, "onLinesCheck", e.getMessage());
            FileSystemWorker.error(classMeth, e);
            stringBuilder.append(e.getMessage());
        } catch (NullPointerException e) {
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

     @param sql запрос
     @param pcName имя ПК
     @return имя юзера, если есть.
     */
    @SuppressWarnings("MethodWithMultipleLoops")
    static String offLinesCheckUser(String sql, String pcName) {
        AppComponents.threadConfig().thrNameSet("offChk");

        StringBuilder stringBuilder = new StringBuilder();
        try (
            PreparedStatement p = connection.prepareStatement(sql);
            PreparedStatement p1 = connection.prepareStatement(sql.replaceAll(ConstantsFor.DBFIELD_PCUSER, ConstantsFor.DBFIELD_PCUSERAUTO))) {
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
            messageToUser.errorAlert("ConditionChecker", "offLinesCheckUser", e.getMessage());
            FileSystemWorker.error("ConditionChecker.offLinesCheckUser", e);
            stringBuilder.append(e.getMessage());
        }
        return "<font color=\"orange\">EXCEPTION in SQL dropped. <br>" + stringBuilder + "</font>";
    }


    static void qerNotNullScanAllDevices(Model model, HttpServletResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ConstantsNet.getAllDevices().remainingCapacity() == 0) {
            ConstantsNet.getAllDevices().forEach(x -> stringBuilder.append(ConstantsNet.getAllDevices().remove()));
            model.addAttribute("pcs", stringBuilder.toString());
        } else {
            allDevNotNull(model, response);
        }
    }


    /**
     Если размер {@link ConstantsNet#getAllDevices()} более 0
     <p>
     {@code scansInMin} - кол-во сканирований в минуту для рассчёта времени. {@code minLeft} - примерное кол-во оставшихся минут.
     {@code attributeValue} - то, что видим на страничке.
     <p>
     <b>{@link Model#addAttribute(Object)}:</b> <br>
     {@link ConstantsFor#ATT_TITLE} = {@code attributeValue} <br>
     {@code pcs} = {@link ConstantsNet#FILENAME_AVAILABLELAST200210TXT} + {@link ConstantsNet#FILENAME_OLDLANTXT0} и {@link ConstantsNet#FILENAME_OLDLANTXT1} + {@link ConstantsNet#FILENAME_SERVTXT}
     <p>
     <b>{@link HttpServletResponse#addHeader(String, String)}:</b><br>
     {@link ConstantsFor#HEAD_REFRESH} = 45

     @param model {@link Model}
     @param response {@link HttpServletResponse}
     */
    private static void allDevNotNull(Model model, HttpServletResponse response) {
        final float scansInMin = Float.parseFloat(AppComponents.getOrSetProps().getProperty("scansInMin", "90"));
        float minLeft = ConstantsNet.getAllDevices().remainingCapacity() / scansInMin;
        String attributeValue = new StringBuilder()
            .append(minLeft).append(" ~minLeft. ")
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis((long) minLeft))).toString();
        model.addAttribute(ConstantsFor.ATT_TITLE, attributeValue);
        model.addAttribute("pcs", FileSystemWorker.readFile(ConstantsNet.FILENAME_AVAILABLELAST200210TXT).replace(", ", "<br>") + "<p>" + FileSystemWorker.readFile(ConstantsNet.FILENAME_AVAILABLELAST210220TXT).replace(", ", "<br>") + "<p>" + FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT0).replace(", ", "<br>") + "<p>" + FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT1).replace(", ", "<br>") + "<p>" +
            FileSystemWorker.readFile(ConstantsNet.FILENAME_SERVTXT_11SRVTXT).replace(", ", "<br>") + "<p>" +
            FileSystemWorker.readFile(ConstantsNet.FILENAME_SERVTXT_21SRVTXT).replace(", ", "<br>") + "<p>" +
            FileSystemWorker.readFile(ConstantsNet.FILENAME_SERVTXT_31SRVTXT).replace(", ", "<br>") + "<p>" +
            FileSystemWorker.readFile(ConstantsNet.FILENAME_SERVTXT_41SRVTXT).replace(", ", "<br>") + "<p>");
        response.addHeader(ConstantsFor.HEAD_REFRESH, "45");
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConditionChecker{");
        sb.append(ConstantsFor.TOSTRING_CLASS_NAME).append(CLASS_NAME).append('\'');
        sb.append('}');
        return sb.toString();
    }
}