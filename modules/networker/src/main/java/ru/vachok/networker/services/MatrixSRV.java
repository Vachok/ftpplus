package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.ResoCache;
import ru.vachok.networker.config.ResLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 07.09.2018 (9:45) */
@Service("matrix")
public class MatrixSRV {

    private static final Logger LOGGER = AppComponents.getLogger();

    private String workPos;

    private int countDB;

    public int getCountDB() {
        return countDB;
    }

    public void setCountDB(int countDB) {
        this.countDB = countDB;
    }

    public String getWorkPos() throws IllegalStateException {
        return workPos;
    }

    public void setWorkPos(String workPos) {
        this.workPos = workPos;
    }

    public String getWorkPosition(String sql) {
        Map<String, String> doljAndAccess = new ConcurrentHashMap<>();
        Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");
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
                Blob changes = r.getBlob("changes");
                if (changes.length() > ConstantsFor.KBYTE) {
                    r.getBlob("changes");
                    try {
                        doljAndAccess.put("Reason", downBlob(changes.getBytes(1, Math.toIntExact(changes.length()))));
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String s = new TForms().fromArray(doljAndAccess);
        this.workPos = s;
        return s;
    }

    private String downBlob(byte[] bytes) throws IOException {
        File file = new File("theBlob.msg");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(bytes);
        }
        ResoCache resoCache = ResoCache.getResoCache();
        resoCache.setFilePath(file.getAbsolutePath());
        resoCache.setBytes(bytes);
        resoCache.setFile(file);
        resoCache.setLastModif(System.currentTimeMillis());
        resoCache.setFileName(file.getName());

        ResLoader resLoader = new ResLoader();
        Map<Resource, ResoCache> resourceCache = resLoader.getResourceCache(ResoCache.class);
        StringBuilder stringBuilder = new StringBuilder();

        resourceCache.forEach((x, y) -> stringBuilder
            .append(x.toString())
            .append("<br>")
            .append(y.toString()));

        return stringBuilder.toString();
    }
}
