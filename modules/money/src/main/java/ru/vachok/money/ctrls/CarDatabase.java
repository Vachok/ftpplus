package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ApplicationConfiguration;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.DBMessage;
import ru.vachok.money.logic.TForms;
import ru.vachok.money.web.beans.MyOpelOBD;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.util.Map;


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
        DataConnectTo dataConnectTo = new RegRuMysql();
        Map<String, String> map = new MyOpelOBD().chkCar();
        String speed = ConstantsFor.dbSpeedCheck.apply(dataConnectTo);
        model.addAttribute("roadStat", speed);
        model.addAttribute("dbStat", new TForms().toStringFromArray(map));
        return "car_db";
    }
}