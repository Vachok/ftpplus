package ru.vachok.money.ctrls;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ApplicationConfiguration;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.logic.ArrsShower;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;


/**
 * @since 23.08.2018 (18:40)
 */
@Controller
public class CarDatabase {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CarDatabase.class.getSimpleName();
    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new MessageCons();
    private static DataConnectTo dataConnectTo = new RegRuMysql();


    @GetMapping("/chkcar")
    public String showEngineTMP( Model model ) {
        Function<String, String> addBR = ( x ) -> {
            x = x + "<br>";
            return x;
        };
        Map<String, String> engineTempStream = chkCar();
        List<String> fromMap = new ArrayList<>();
        engineTempStream.forEach(( x , y ) -> fromMap.add(x + " out " + y + " coolant"));
        model.addAttribute("helloMe" , new Date().getTime());
        model.addAttribute("dbStat" , new ArrsShower(fromMap).strFromArr());
        return "car_db";
    }


    public Map<String, String> chkCar() {
        IntStream.Builder engineTempStream = IntStream.builder();
        Map<String, String> integerIntegerHashMap = new HashMap<>();
        String sql = "select * from obdrawdata limit 1000";
        try (Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "car"); PreparedStatement p = c.prepareStatement(sql); ResultSet schemas = p.executeQuery()) {
            dataConnectTo.getSavepoint(c);
            while (schemas.next()) {
                try {
                    integerIntegerHashMap.put(schemas.getString("GPS Time") , schemas.getString("Engine Coolant " + "Temperature" + "(°C)"));
                } catch (NumberFormatException | NoSuchElementException e) {
                    ApplicationConfiguration.getLogger().error(e.getMessage() , e);
                }
            }
        } catch (SQLException e) {
            ApplicationConfiguration.getLogger().error(e.getMessage() , e);
        }
        System.out.println("integerIntegerHashMap = " + integerIntegerHashMap.size());
        return integerIntegerHashMap;
    }
}