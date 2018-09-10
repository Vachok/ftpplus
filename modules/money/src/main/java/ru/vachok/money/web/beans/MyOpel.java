package ru.vachok.money.web.beans;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.money.ApplicationConfiguration;
import ru.vachok.money.ConstantsFor;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.stream.IntStream;


/**
 @since 01.09.2018 (20:26) */
@Service ("MyOpel")
public class MyOpel {

    /*Fields*/

    private static final Logger LOGGER = ApplicationConfiguration.getLogger();

    private static DataConnectTo dataConnectTo = new RegRuMysql();

    private Map<String, String> obdDataMap = chkCar();

    private String speedStr = setSpeedStr();

    public Map<String, String> getObdDataMap() {
        return obdDataMap;
    }

    public void setObdDataMap(Map<String, String> obdDataMap) {
        this.obdDataMap = obdDataMap;
    }

    public String getSpeedStr() {
        return speedStr;
    }

    private Map<String, String> chkCar() {
        IntStream.Builder engineTempStream = IntStream.builder();
        Map<String, String> integerIntegerHashMap = new HashMap<>();
        String sql = "select * from obdrawdata limit 1000";
        try(Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "car");
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet schemas = p.executeQuery();
            ResultSet metaData = c.getMetaData().getTypeInfo()){
            while(schemas.next()){
                integerIntegerHashMap.put(schemas
                    .getString("GPS Time"), schemas
                    .getString("Engine Coolant " + "Temperature" + "(°C)"));

            }
        }
        catch(SQLException | NumberFormatException | NoSuchElementException e){
            LOGGER.error(e.getMessage(), e);
        }
        String format = MessageFormat.format("integerIntegerHashMap = {0}", integerIntegerHashMap.size());
        LOGGER.info(format);
        BiFunction<String, String, String> addBR = (x, y) -> {
            return x + "<br>" + y + "<br>";
        };
        integerIntegerHashMap.replaceAll(addBR);
        return integerIntegerHashMap;
    }

    private String setSpeedStr() {
        return ConstantsFor.dbSpeedCheck.apply(dataConnectTo);
    }

}