package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.services.DBMessage;
import ru.vachok.money.services.TForms;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 07.09.2018 (0:39) */
@Controller
public class DataBases {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = DataBases.class.getSimpleName();

    private static final Logger LOGGER = ConstantsFor.getLogger();

    /**
     {@link }
     */
    private static MessageToUser messageToUser = new DBMessage();

    static {
        try{
            Driver driver = new com.mysql.jdbc.Driver();
            DriverManager.registerDriver(driver);
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @GetMapping ("/db")
    public String db(Model m) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] namesDB = {"rez", "speed"};
        for(String s : namesDB){
            String s1 = new TForms().toStringFromArray(getLastLogs(s));
            stringBuilder.append(s1);
        }
        m.addAttribute("dbinfo", stringBuilder.toString());
        m.addAttribute("start", new Date(ConstantsFor.START_STAMP));
        return "db";
    }

    public Map<String, String> getLastLogs(String logTableName) {
        int ind = 10;
        Map<String, String> lastLogsList = new ConcurrentHashMap<>();
        DataConnectTo d = new RegRuMysql();
        try(Connection c = d.getDefaultConnection("u0466446_liferpg");
            PreparedStatement p = c.prepareStatement(String.format("select * from %s ORDER BY timewhen DESC LIMIT 0 , 50", logTableName));
            ResultSet r = p.executeQuery()
        ){
            while(r.next()){
                lastLogsList.put(++ind + ") " + r.getString("classname") + " - " + r.getString("msgtype"),
                    r.getString("msgvalue") + " at: " + r.getString("timewhen"));
            }
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
        }
        return lastLogsList;
    }
}