package ru.vachok.networker.accesscontrol;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.ResoCache;
import ru.vachok.networker.config.ResLoader;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 {@link Service} : {@link ConstantsFor#MATRIX_STRING_NAME}
 <p>
 Сервис-класс для {@link MatrixCtr}.

 @since 07.09.2018 (9:45) */
@Service (ConstantsFor.MATRIX_STRING_NAME)
public class MatrixSRV {

    /**
     Логгер

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

    /**
     Header
     <p>
     Кол-во соответствий в базе.

     {@link MatrixCtr#showResults(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.ui.Model)}
     */
    private int countDB = 0;

    /**
     @return {@link #countDB}
     */
    @SuppressWarnings("WeakerAccess")
    public int getCountDB() {
        return countDB;
    }

    /**
     @param countDB {@link #countDB}
     */
    public void setCountDB(int countDB) {
        this.countDB = countDB;
    }

    /**
     @return {@link #workPos}
     */
    public String getWorkPos() {
        return workPos;
    }

    /**
     @param workPos {@link #workPos}
     */
    public void setWorkPos(String workPos) {
        this.workPos = workPos;
    }

    private static String downBlob(byte[] bytes, String fileType) {
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

    /**
     Качает информацию из БД
     <p>
     {@link DataConnectTo#getDefaultConnection(java.lang.String)} . База: {@link ConstantsFor#DB_PREFIX} + {@link ConstantsFor#STR_VELKOM}. <br>
     {@link #getInfo(ResultSet, Map)}. Метод разбит на несколько.
     <p>
     <b>{@link SQLException}:</b><br>
     {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
     <p>
     {@link TForms#fromArray(java.util.Map, boolean)}. Переделываем из мапы с результатом в строку для возврата для WEB. <br>
     this.{@link #workPos} = returned {@link String}

     @param sql запрос
     @return {@link #workPos}
     @see MatrixCtr#matrixAccess(java.lang.String)
     */
    String getWorkPosition(String sql) {
        Map<String, String> doljAndAccess = new ConcurrentHashMap<>();

        try (Connection c = new AppComponents().connection(ConstantsFor.DB_PREFIX + ConstantsFor.STR_VELKOM);
             PreparedStatement statement = c.prepareStatement(sql);
             ResultSet r = statement.executeQuery()) {
            while (r.next()) {
                getInfo(r, doljAndAccess);
            }
        } catch (SQLException e) {
            FileSystemWorker.error("MatrixSRV.getWorkPosition", e);
        }
        String s = new TForms().fromArray(doljAndAccess, true);
        this.workPos = s;
        return s;
    }
}
