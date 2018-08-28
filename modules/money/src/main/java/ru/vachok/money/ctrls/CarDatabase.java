package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ApplicationConfiguration;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.DBMessage;
import ru.vachok.money.logic.ArrsShower;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    /**
     {@link }
     */
    private static MessageToUser messageToUser = new DBMessage();

    private static DataConnectTo dataConnectTo = new RegRuMysql();

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CarDatabase.class.getSimpleName();

    private static final Logger LOGGER = ApplicationConfiguration.getLogger();


    @GetMapping ("/chkcar")
    public String showEngineTMP(Model model) {
        Map<String, String> engineTempStream = chkCar();
        List<String> fromMap = new ArrayList<>();
        engineTempStream.forEach((x, y) -> fromMap.add(x + " out " + y + " coolant"));
        model.addAttribute("helloMe", new Date().getTime());
        model.addAttribute("dbStat", new ArrsShower(fromMap).strFromArr());
        return "car_db";
    }


    public Map<String, String> chkCar() {
        IntStream.Builder engineTempStream = IntStream.builder();
        Map<String, String> integerIntegerHashMap = new HashMap<>();
        String sql = "select * from obdrawdata limit 1000";
        try(Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "car");
            PreparedStatement p = c.prepareStatement(sql); ResultSet schemas = p.executeQuery()){
            String s = schemas.getMetaData().getColumnCount() + " columns";
            messageToUser.info(SOURCE_CLASS, "DB Count columns",sql+"\n"+s);
            while(schemas.next()){
                try{
                    integerIntegerHashMap.put(schemas.getString("GPS Time"), schemas.getString("Engine Coolant " + "Temperature" + "(°C)"));
                }
                catch(NumberFormatException | NoSuchElementException e){
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
        }
        String format = MessageFormat.format("integerIntegerHashMap = {0}", integerIntegerHashMap.size());
        LOGGER.info(format);
        BiFunction<String, String, String> addBR = (x,y) -> {
            return x + "<br>"+y + "<br>";
        };
        integerIntegerHashMap.replaceAll(addBR);
        return integerIntegerHashMap;
    }
}