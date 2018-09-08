package ru.vachok.networker.beans;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.TForms;
import ru.vachok.networker.config.AppComponents;

import java.sql.*;
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


    public String getWorkPosition(String sql) {
        Map<String, String> doljAndAccess = new ConcurrentHashMap<>();
        try (PreparedStatement statement = c.prepareStatement(sql);
             ResultSet r = statement.executeQuery()) {
            while (r.next()) {
                doljAndAccess.put("<h5>" + r.getString("Doljnost") + ":</h5><br>",
                    r.getInt("fullinet") + " полный достув в интернет, <br>" +
                        r.getInt("stdinet") + " - доступ с ограничениями (mail.ru и тп). Стандарт для большинства, <br>" +
                        r.getInt("limitinet") + " - досту только к определённым спискам сайтов, <br>" +
                        r.getInt("owaasync") + " - owa and async (почта удалённо), <br>" +
                        r.getInt("VPN") + " - VPN, <br>" +
                        r.getInt("sendmail") + " - отправка за пределы компании. <br>");
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String s = new TForms().fromArray(doljAndAccess);
        this.workPos = s;
        return s;
    }
}
