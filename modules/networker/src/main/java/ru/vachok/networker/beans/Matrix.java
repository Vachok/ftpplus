package ru.vachok.networker.beans;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.TForms;
import ru.vachok.networker.config.AppComponents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 07.09.2018 (9:45)
 */
@Service("matrix")
public class Matrix {

    private static final Connection c = new RegRuMysql().getDefaultConnection("u0466446_velkom");

    private static final Logger LOGGER = AppComponents.getLogger();

    private String workPos;

    public String getWorkPos() {
        return workPos;
    }

    public void setWorkPos(String workPos) {
        this.workPos = workPos;
    }

    @GetMapping("/matrix")
    public String getWorkPosition(String sql) {
        Map<String, String> doljAndAccess = new ConcurrentHashMap<>();
        try (PreparedStatement statement = c.prepareStatement(sql);
             ResultSet r = statement.executeQuery()) {
            while (r.next()) {
                doljAndAccess.put(r.getString("Doljnost"),
                    r.getInt("fullinet") + " - fullinet, <br>" +
                        r.getInt("stdinet") + " - stdinet, <br>" +
                        r.getInt("limitinet") + " - limitinet, <br>" +
                        r.getInt("owaasync") + " - owa and async, <br>" +
                        r.getInt("VPN") + " - VPN, <br>" +
                        r.getInt("sendmail") + " - sendmail. <br>");
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String s = new TForms().fromArray(doljAndAccess);
        this.workPos = s;
        return s;
    }

    public void showResults(Model model) {
        model.addAttribute("workPos", workPos);
    }
}
