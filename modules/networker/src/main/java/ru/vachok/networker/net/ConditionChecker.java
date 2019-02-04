package ru.vachok.networker.net;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageCons;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;

import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 Проверки из классов.
 <p>
 Пинги, и тп

 @since 31.01.2019 (0:20) */
@SuppressWarnings ("StaticMethodOnlyUsedInOneClass")
class ConditionChecker {

    private static final String CLASS_NAME = ConditionChecker.class.getSimpleName();

    private static Connection c = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);

    private ConditionChecker() {
        new MessageCons().infoNoTitles("ConditionChecker.ConditionChecker");
    }

    /**
     Проверяет имя пользователя когда ПК онлайн
     <p>

     @param sql    запрос
     @param pcName имя ПК
     @return кол-во проверок и сколько был вкл/выкл
     @see MoreInfoGetter#getSomeMore(String, boolean)
     */
    static String onLinesCheck(String sql, String pcName) {
        PCUserResolver pcUserResolver = PCUserResolver.getPcUserResolver();
        List<Integer> onLine = new ArrayList<>();
        List<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        String classMeth = "ConditionChecker.onLinesCheck";

        String methName = "onLinesCheck";
        try(PreparedStatement statement = NetScannerSvc.c.prepareStatement(sql)){
            Runnable r = () -> pcUserResolver.namesToFile(pcName);
            ThreadConfig.executeAsThread(r);
            statement.setString(1, pcName);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    ADComputer adComputer = new ADComputer();
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if(onlineNow==1){
                        onLine.add(onlineNow);
                        adComputer.setDnsHostName(pcName);
                    }
                    if(onlineNow==0){
                        offLine.add(onlineNow);
                    }
                    ConstantsNet.AD_COMPUTERS.add(adComputer);
                }
            }
        }
        catch(SQLException e){
            c = reconnectToDB();
            new MessageCons().errorAlert(CLASS_NAME, methName, e.getMessage());
            FileSystemWorker.error(classMeth, e);
            stringBuilder.append(e.getMessage());
        }
        catch(NullPointerException e){
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder
            .append(offLine.size())
            .append(" offline times and ")
            .append(onLine.size())
            .append(" online times.").toString();
    }

    private static Connection reconnectToDB() {
        try{
            c.close();
            c = null;
            Connection connection = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);
            c = connection;
        }
        catch(SQLException e){
            new MessageCons().errorAlert("ConditionChecker", ConstantsNet.RECONNECT_TO_DB, e.getMessage());
        }
        return c;
    }

    /**
     <b>Проверяет есть ли в БД имя пользователя</b>

     @param sql    запрос
     @param pcName имя ПК
     @return имя юзера, если есть.
     */
    @SuppressWarnings ({"MethodWithMultipleLoops", "MethodWithMultipleReturnPoints"})
    static String offLinesCheckUser(String sql, String pcName) {
        StringBuilder stringBuilder = new StringBuilder();
        try(PreparedStatement p = c.prepareStatement(sql);
            PreparedStatement p1 = c.prepareStatement(sql.replaceAll(ConstantsFor.STR_PCUSER, ConstantsFor.STR_PCUSERAUTO))){
            p.setString(1, pcName);
            p1.setString(1, pcName);
            try(ResultSet resultSet = p.executeQuery();
                ResultSet resultSet1 = p1.executeQuery()){
                while(resultSet.next()){
                    stringBuilder.append("<b>")
                        .append(resultSet.getString(ConstantsFor.DB_FIELD_USER).trim()).append("</b> (time: ")
                        .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append(")");
                }
                while(resultSet1.next()){
                    if(resultSet1.last()){
                        return stringBuilder
                            .append("    (AutoResolved name: ")
                            .append(resultSet1.getString(ConstantsFor.DB_FIELD_USER).trim()).append(" (time: ")
                            .append(resultSet1.getString(ConstantsNet.DB_FIELD_WHENQUERIED)).append("))").toString();
                    }
                }
            }
        }
        catch(SQLException e){
            new MessageCons().errorAlert(CLASS_NAME, "offLinesCheckUser", e.getMessage());
            FileSystemWorker.error("ConditionChecker.offLinesCheckUser", e);
            stringBuilder.append(e.getMessage());
            c = reconnectToDB();
        }
        return "<font color=\"orange\">EXCEPTION in SQL dropped. <br>" + stringBuilder.toString() + "</font>";
    }

    static void qerNotNullScanAllDevices(Model model, NetScanFileWorker netScanFileWorker, HttpServletResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        if(ConstantsFor.ALL_DEVICES.remainingCapacity()==0){
            ConstantsFor.ALL_DEVICES.forEach(x -> stringBuilder.append(ConstantsFor.ALL_DEVICES.remove()));
            model.addAttribute("pcs", stringBuilder.toString());
        }
        else{
            allDevNotNull(model, netScanFileWorker, response);
        }
    }

    /**
     Если размер {@link ConstantsFor#ALL_DEVICES} более 0
     <p> <br>
     <b>Схема:</b> <br>
     Убедимся в правильности {@link NetScanFileWorker} : <br>
     1. {@link DiapazonedScan#getNetScanFileWorker()} <br>
     2. {@link NetScanFileWorker#equals(java.lang.Object)} <br><br>
     Если всё верно:
     3. {@link ScanOnline#getI()} + {@link ScanOnline#toString()}

     @param model             {@link Model}
     @param netScanFileWorker {@link NetScanFileWorker}
     @param response          {@link HttpServletResponse}
     */
    private static void allDevNotNull(Model model, NetScanFileWorker netScanFileWorker, HttpServletResponse response) {
        final float scansInMin = 45.9f;
        float minLeft = ConstantsFor.ALL_DEVICES.remainingCapacity() / scansInMin;
        String attributeValue = new StringBuilder().append(minLeft).append(" ~minLeft. ")
            .append(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(( long ) minLeft))).toString();
        model.addAttribute(ConstantsFor.ATT_TITLE, attributeValue);

        if(netScanFileWorker.equals(DiapazonedScan.getNetScanFileWorker())){
            String scOnLine = ScanOnline.getI().toString();
            String scOffLine = ScanOffline.getI().toString() + "<p>" + netScanFileWorker.getNewLanLastScanAsStr() + "<br>" + netScanFileWorker.getOldLanLastScanAsStr();
            model.addAttribute("pcs", scOnLine + "<br>" + scOffLine);
        }
        else{
            model.addAttribute("pcs", FileSystemWorker
                .readFile(ConstantsFor.AVAILABLE_LAST_TXT) + "<p>" + FileSystemWorker
                .readFile(ConstantsFor.OLD_LAN_TXT));
        }
        response.addHeader(ConstantsFor.HEAD_REFRESH, "60");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConditionChecker{");
        sb.append("c=").append(c.toString());
        sb.append(", CLASS_NAME='").append(CLASS_NAME).append('\'');
        sb.append('}');
        return sb.toString();
    }
}