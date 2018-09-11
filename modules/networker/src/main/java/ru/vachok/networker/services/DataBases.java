package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.beans.AppComponents;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 07.09.2018 (0:39) */
@Service("dataBases")
public class DataBases {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link }
     */
    private String logTableName;

    static {
        try{
            Driver driver = new com.mysql.jdbc.Driver();
            DriverManager.registerDriver(driver);
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    public Map<String, String> getLastLogs(String logTableName) {
        this.logTableName = logTableName;
        int ind = 10;
        Map<String, String> lastLogsList = new ConcurrentHashMap<>();
        DataConnectTo d = new RegRuMysql();
        try(Connection c = d.getDefaultConnection("u0466446_webapp");
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

    public String getLogTableName() {
        return logTableName;
    }
}