package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.components.Visitor;
import ru.vachok.money.services.TimeWorms;
import ru.vachok.money.services.sockets.TellNetSockets;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 @since 20.08.2018 (17:08)
 */
@Controller
public class Index {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class.getSimpleName());

    private Visitor visitor;

    private TimeWorms timeWorms;

    private String dateTimeLoc;

    private TellNetSockets tellNetSockets;

    public String getDateTimeLoc() {
        return dateTimeLoc;
    }

    public void setDateTimeLoc(String dateTimeLoc) {
        this.dateTimeLoc = dateTimeLoc;
    }

    private static final InitProperties GENERAL_LIFE = new DBRegProperties("general-life");

    private static final Properties PROPERTIES = GENERAL_LIFE.getProps();

    /*Instances*/
    @Autowired
    public Index(Visitor visitor, TellNetSockets tellNetSockets, TimeWorms timeWorms) {
        this.visitor = visitor;
        this.tellNetSockets = tellNetSockets;
        this.timeWorms = timeWorms;
    }

    @GetMapping ("/")
    public String indexString(HttpServletRequest request, HttpServletResponse response, Model model) {
        try{
            visitor.getVisitorSrv().makeVisit(request, response);
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        long lasts = System.currentTimeMillis() - Long.parseLong(PROPERTIES.getProperty("lasts"));

        float hrsWont = ( float ) TimeUnit.MILLISECONDS
            .toSeconds(lasts) / ConstantsFor.ONE_HOUR / ConstantsFor.ONE_HOUR / ConstantsFor.ONE_DAY_HOURS;

        model.addAttribute("title", hrsWont + " days");
        model.addAttribute("timeleft", timeWorms.timeLeft());

        model.addAttribute("dateTimeLoc", dateTimeLoc);
        model.addAttribute("footer", new PageFooter().getTheFooter());

        if(ConstantsFor.localPc().equalsIgnoreCase("home")){
            return "index-start";
        }
        return "home";
    }
}
