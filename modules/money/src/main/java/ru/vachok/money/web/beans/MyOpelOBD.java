package ru.vachok.money.web.beans;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.money.ApplicationConfiguration;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.SpeedRunActualize;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 @since 01.09.2018 (20:26) */
@Service
public class MyOpelOBD {

    /*Fields*/

    private static final Logger LOGGER = ApplicationConfiguration.getLogger();

    private static DataConnectTo dataConnectTo = new RegRuMysql();

    public void chkCars() {
        Map<String, String> engineTempStream = chkCar();
        List<String> fromMap = new ArrayList<>();
        Stream<Double> attributeValue = new SpeedRunActualize().speedsOn(1);
        engineTempStream.forEach((x, y) -> fromMap.add(x + " out " + y + " coolant"));
    }

    public Map<String, String> chkCar() {
        IntStream.Builder engineTempStream = IntStream.builder();
        Map<String, String> integerIntegerHashMap = new HashMap<>();
        String sql = "select * from obdrawdata limit 1000";
        try(Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "car");
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet schemas = p.executeQuery()){
            String s = schemas.getMetaData().getColumnCount() + " columns";
            while(schemas.next()){
                integerIntegerHashMap.put(schemas
                    .getString("GPS Time"), schemas
                    .getString("Engine Coolant " + "Temperature" + "(°C)"));

            }
        }
        catch(SQLException | NumberFormatException | NoSuchElementException e){
            ApplicationConfiguration.getLogger().error(e.getMessage(), e);
        }
        String format = MessageFormat.format("integerIntegerHashMap = {0}", integerIntegerHashMap.size());
        BiFunction<String, String, String> addBR = (x, y) -> {
            return x + "<br>" + y + "<br>";
        };
        integerIntegerHashMap.replaceAll(addBR);
        return integerIntegerHashMap;
    }

}