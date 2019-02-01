package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.systray.ActionCloseMsg;
import ru.vachok.networker.systray.MessageToTray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 Получение более детальной информации о ПК
 <p>

 @since 25.01.2019 (11:06) */
class MoreInfoGetter {

    private MoreInfoGetter() {
        new MessageCons().infoNoTitles("MoreInfoGetter.MoreInfoGetter");
    }

    /**
     Поиск имён пользователей компьютера <br> Обращения: <br> 1 {@link ConditionChecker#onLinesCheck(String, String)} 1.1
     {@link ThreadConfig#threadPoolTaskExecutor()} 1.2 {@link PCUserResolver#namesToFile(String)}
     <br> 2. {@link ConditionChecker#offLinesCheckUser(String, String)}

     @param pcName   имя компьютера
     @param isOnline онлайн = true
     @return выдержка из БД (когда последний раз был онлайн + кол-во проверок) либо хранимый в БД юзернейм (для offlines)
     @see NetScannerSvc#getPCNamesPref(String)
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static String getSomeMore(String pcName, boolean isOnline) {
        String sql;
        if (isOnline) {
            sql = "select * from velkompc where NamePP like ?";
            NetScannerSvc.onLinePCs = NetScannerSvc.onLinePCs + 1;
            return ConditionChecker.onLinesCheck(sql, pcName) + " | " + NetScannerSvc.onLinePCs;
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
    @SuppressWarnings ("MethodWithMultipleReturnPoints")
    static String getUserFromDB(String userInputRaw) {
        StringBuilder retBuilder = new StringBuilder();
        String sql = "select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20";
        try{
            userInputRaw = userInputRaw.split(": ")[1];
        }
        catch(ArrayIndexOutOfBoundsException e){
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        try(Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + ConstantsFor.STR_VELKOM);
            PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, "%" + userInputRaw + "%");
            try(ResultSet r = p.executeQuery()){
                StringBuilder stringBuilder = new StringBuilder();
                String headER = "<h3><center>LAST 20 USER PCs</center></h3>";
                stringBuilder.append(headER);
                while(r.next()){
                    String pcName = r.getString(ConstantsFor.DB_FIELD_PCNAME);
                    String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " +
                        r.getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER;
                    stringBuilder.append(returnER);
                    if(r.last()){
                        MessageToUser messageToUser = new MessageToTray(new ActionCloseMsg(AppComponents.getLogger()));
                        messageToUser.info(
                            r.getString(ConstantsFor.DB_FIELD_PCNAME),
                            r.getString("whenQueried"),
                            r.getString(ConstantsFor.DB_FIELD_USER));
                    }
                }
                return stringBuilder.toString();
            }
        }
        catch(SQLException e){
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }
}
