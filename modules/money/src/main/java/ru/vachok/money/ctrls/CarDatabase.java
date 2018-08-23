package ru.vachok.money.ctrls;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ConstantsFor;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;


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
    public String chkCar( HttpServletRequest request , HttpServletResponse response , Model model ) {

        String hiString = new Date() + request.getRemoteHost() + ":" + request.getRemotePort();
        List<String> cursorS = new ArrayList<>();
        try (Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "car"); ResultSet schemas = c.getMetaData().getSchemas()) {
            dataConnectTo.getSavepoint(c);
            while (schemas.next()) {
                cursorS.add(schemas.getCursorName());
            }
        } catch (SQLException e) {
        }
        Function<String, String> addBR = ( x ) -> {
            x = x + "<br>";
            return x;
        };
        Stream<String> stringStream = cursorS.stream().map(addBR);
        model.addAttribute("helloMe" , hiString);
        model.addAttribute("dbStat" , Arrays.toString(stringStream.toArray()));
        return "car_db";
    }
}