package ru.vachok.networker.accesscontrol;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
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
@Service (ConstantsFor.MATRIX_STRING_NAME)
public class MatrixSRV {

    /**
     Логгер
     <p>
     {@link LoggerFactory#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixSRV.class.getSimpleName());

    /**
     Имя колонки в sql-таблице.

     @see #getWorkPos()
     */
    private static final String SQLCOL_CHANGES = "changes";

    /**
     Пользовательский ввод

     @see MatrixCtr
     */
    private String workPos = "";

    private int countDB = 0;

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

    String getWorkPosition(String sql) {
        Map<String, String> doljAndAccess = new ConcurrentHashMap<>();
        Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + ConstantsFor.STR_VELKOM);
        try (PreparedStatement statement = c.prepareStatement(sql);
             ResultSet r = statement.executeQuery()) {
            while (r.next()) {
                getInfo(r, doljAndAccess);
            }
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
        }
        String s = new TForms().fromArray(doljAndAccess, true);
        this.workPos = s;
        return s;
    }

    private void getInfo(ResultSet r, Map<String, String> doljAndAccess) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        countDB = countDB + 1;
        int fullinet = r.getInt("fullinet");
        int stdinet = r.getInt("stdinet");
        int limitinet = r.getInt("limitinet");
        int owaasync = r.getInt("owaasync");
        int vpn = r.getInt("VPN");
        int sendmail = r.getInt("sendmail");
        if(fullinet==1){
            stringBuilder.append(" - полный достув в интернет <br>");
        }
        if(stdinet==1){
            stringBuilder.append(" - доступ с ограничениями (mail.ru и тп). Стандарт для большинства <br>");
        }
        if(limitinet==1){
            stringBuilder.append(" - досту только к определённым спискам сайтов <br>");
        }
        if(owaasync==1){
            stringBuilder.append(" - owa and async (почта удалённо) <br>");
        }
        if(vpn==1){
            stringBuilder.append(" - VPN <br>");
        }
        if(sendmail==1){
            stringBuilder.append(" - отправка за пределы компании<br>");
        }
        doljAndAccess.put("<p><h5>" + r.getString(2) + " - " + r.getString(3) + ":</h5>",
            stringBuilder.toString());
        Blob changes = r.getBlob(SQLCOL_CHANGES);
        if(changes.length() > ConstantsFor.KBYTE){
            r.getBlob(SQLCOL_CHANGES);
            String fileType = r.getString("filetype");
            doljAndAccess.put("Reason", downBlob(changes.getBytes(1, Math.toIntExact(changes.length())), fileType));
        }
    }

    private String downBlob(byte[] bytes, String fileType) {
        String fileName = "theBlob." + fileType;
        File file = new File(fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
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
