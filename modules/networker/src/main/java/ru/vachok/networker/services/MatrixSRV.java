package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 07.09.2018 (9:45) */
@Service("matrix")
public class MatrixSRV {

    /*Fields*/
    private static final Connection c = new RegRuMysql().getDefaultConnection("u0466446_velkom");

    private static final Logger LOGGER = AppComponents.getLogger();

    private String workPos;

    private int countDB;

    public int getCountDB() {
        return countDB;
    }

    public void setCountDB(int countDB) {
        this.countDB = countDB;
    }

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
                StringBuilder stringBuilder = new StringBuilder();
                countDB = countDB + 1;
                int fullinet = r.getInt("fullinet");
                int stdinet = r.getInt("stdinet");
                int limitinet = r.getInt("limitinet");
                int owaasync = r.getInt("owaasync");
                int vpn = r.getInt("VPN");
                int sendmail = r.getInt("sendmail");
                if (fullinet == 1) {
                    stringBuilder.append(" - полный достув в интернет <br>");
                }
                if (stdinet == 1) {
                    stringBuilder.append(" - доступ с ограничениями (mail.ru и тп). Стандарт для большинства <br>");
                }
                if (limitinet == 1) {
                    stringBuilder.append(" - досту только к определённым спискам сайтов <br>");
                }
                if (owaasync == 1) {
                    stringBuilder.append(" - owa and async (почта удалённо) <br>");
                }
                if (vpn == 1) {
                    stringBuilder.append(" - VPN <br>");
                }
                if (sendmail == 1) {
                    stringBuilder.append(" - отправка за пределы компании<br>");
                }
                doljAndAccess.put("<p><h5>" + r.getString(2) + " - " + r.getString(3) + ":</h5>",
                    stringBuilder.toString());
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String s = new TForms().fromArray(doljAndAccess);
        this.workPos = s;
        return s;
    }
}
