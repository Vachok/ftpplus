package ru.vachok.money.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 07.09.2018 (0:39) */
@Service ("databases")
public class DataBasesSRV {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBasesSRV.class.getSimpleName());

    /**
     {@link }
     */
    private String logTableName;

    public String getLogTableName() {
        return logTableName;
    }

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
}