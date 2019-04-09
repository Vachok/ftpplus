
package ru.vachok.networker.net;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.InfoGetter;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.PCUserResolver;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
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
class ConditionChecker implements InfoGetter {


    private static final String CLASS_NAME = ConditionChecker.class.getSimpleName();

    private static Connection connection;

    private static MessageToUser messageToUser = new MessageLocal(ConditionChecker.class.getSimpleName());

    private String sql;

    private String pcName;


    ConditionChecker(String sql, String pcName) {
        this.sql = sql;
        this.pcName = pcName;
    }


    static {
        try {
            connection = new AppComponents().connection(ConstantsNet.DB_NAME);
        } catch (IOException e) {
            messageToUser.errorAlert(CLASS_NAME, ConstantsFor.METHNAME_STATIC_INITIALIZER, e.getMessage());
            FileSystemWorker.error("ConditionChecker.static initializer", e);
        }
    }

    @Override public String getInfoAbout() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if (InetAddress.getByName(pcName).isReachable(ConstantsFor.TIMEOUT_650 / 4)) {
                stringBuilder.append(getUserResolved());
                stringBuilder.append(onLinesCheck());
            }
            else {
                stringBuilder.append(offLinesCheckUser());
            }
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }


    private String getUserResolved() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" <font color=\"white\">");
        final String sqlLoc = "SELECT * FROM `pcuser` WHERE `pcName` LIKE ?";
        try(Connection c = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBDASENAME_U0466446_VELKOM).getConnection()){
            try(PreparedStatement p = c.prepareStatement(sqlLoc)){
                p.setString(1 , pcName);
                try(ResultSet r = p.executeQuery()){
                    while(r.next()) stringBuilder.append(r.getString(ConstantsFor.DB_FIELD_USER));
                }
            }
        }catch(SQLException e){
            stringBuilder.append(e.getMessage());
        }
        stringBuilder.append("</font> ");
        return stringBuilder.toString();
    }


    private String onLinesCheck() {
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

    @SuppressWarnings("MethodWithMultipleLoops")
    private String offLinesCheckUser() {
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
        catch (NullPointerException ignore) {
            //
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
     {@code pcs} = {@link ConstantsNet#FILENAME_NEWLAN210} + {@link ConstantsNet#FILENAME_OLDLANTXT0} и {@link ConstantsNet#FILENAME_OLDLANTXT1} + {@link ConstantsNet#FILENAME_SERVTXT}
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
        model.addAttribute("pcs",
            FileSystemWorker.readFile(ConstantsNet.FILENAME_NEWLAN210).replace(", ", "<br>") + "<p>" + FileSystemWorker.readFile(ConstantsNet.FILENAME_NEWLAN200210).replace(", ", "<br>") +
                "<p>" + FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT0).replace(", ", "<br>") + "<p>" +
                FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT1).replace(", ", "<br>") + "<p>" +
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