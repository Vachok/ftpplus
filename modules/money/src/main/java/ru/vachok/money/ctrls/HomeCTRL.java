package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.services.TimeWorms;
import ru.vachok.mysqlandprops.RegRuMysql;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;


/**
 <h1>Request /home</h1>

 @since 22.08.2018 (10:55) */
@Controller
@Deprecated
public class HomeCTRL {

    /**
     {@link RegRuMysql#getDefaultConnection(String)}
     */
    private static final Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_LIFERPG);

    private static Logger logger = LoggerFactory.getLogger("deprec");

    private boolean myPC;

    public boolean isMyPC() {
        return myPC;
    }

    public void setMyPC(boolean myPC) {
        this.myPC = myPC;
    }

    /**
     Index string.

     @param model   the model
     @param request the request
     @return the string
     */
    @GetMapping("/home")
    public String index(Model model, HttpServletRequest request) {
        setMyPC(request.getRemoteAddr().contains("10.10.111.") || request.getRemoteAddr().contains("0:0:0:0:0"));
        String remoteAddr = request.getRemoteAddr();
        if (!myPC) throw new UnsupportedOperationException("Impossible... ");

        model.addAttribute("speed", getLastestSpeedInDB());
        long time = request.getSession().getCreationTime();

        String msg = new Date(time) + " was - " + remoteAddr;
        logger.info(msg);

        model.addAttribute("message", new TimeWorms().getDaysWOut());

        String timeLeft = "Время - деньги ... ";
        LocalTime localDateTimeNow = LocalTime.now();
        LocalTime endLocalDT = LocalTime.parse("17:30");
        long totalDay = endLocalDT.toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay();
        long l = endLocalDT.toSecondOfDay() - localDateTimeNow.toSecondOfDay();
        model.addAttribute("date", new Date().toString());
        model.addAttribute("timeleft", timeLeft + "" + l + "/" + totalDay + " sec left");
        return "home";
    }

    private static String getLastestSpeedInDB() {
        StringBuilder stringBuilder = new StringBuilder();
        try (PreparedStatement p = c.prepareStatement("select * from speed ORDER BY  speed.TimeStamp DESC LIMIT 0 , 7");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                stringBuilder
                    .append(r.getDouble("speed"))
                    .append(" speed, ")
                    .append(r.getInt("road"))
                    .append(" road, ")
                    .append(r.getDouble("TimeSpend"))
                    .append(" min spend, ")
                    .append(r.getString("TimeStamp"))
                    .append(" NOW: ").append(new Date().toString());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return stringBuilder.toString();
    }

    @GetMapping("/net")
    public String netChk(Model model) {
        String traceRt = "TRACE";
        try {
            InetAddress inetAddressNAT = InetAddress.getByName("srv-nat.eatmeat.ru");
            InetAddress inetAddressGIT = InetAddress.getByName("srv-git.eatmeat.ru");
            traceRt = Arrays.toString(inetAddressGIT.getAddress()) + "<p>" + Arrays.toString(inetAddressNAT.getAddress());
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
        model.addAttribute("trace", traceRt);
        return "net";
    }

}
