// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
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
 {@link Service} : {@link ConstantsFor#BEANNAME_MATRIX}
 <p>
 Сервис-класс для {@link MatrixCtr}.
 
 @since 07.09.2018 (9:45) */
@Service(ConstantsFor.BEANNAME_MATRIX)
public class MatrixSRV {
    
    
    /**
     Логгер
     <p>
     {@link LoggerFactory#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixSRV.class.getSimpleName());
    
    /**
     Имя колонки в SQL_SELECT_DIST-таблице.
     
     @see #getWorkPos()
     */
    private static final String SQLCOL_CHANGES = "changes";
    
    /**
     Пользовательский ввод
     
     @see MatrixCtr
     */
    private String workPos = "whois: ";
    
    /**
     Header
     <p>
     Кол-во соответствий в базе.
     <p>
     {@link MatrixCtr#showResults(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.ui.Model)}
     */
    private int countDB;
    
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
    
    /**
     Качает информацию из БД
     <p>
     {@link DataConnectTo#getDefaultConnection(java.lang.String)} . База: {@link ConstantsFor#DBPREFIX} + {@link ConstantsFor#STR_VELKOM}. <br>
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
    public String getWorkPosition(final String sql) {
        Map<String, String> doljAndAccess = new ConcurrentHashMap<>();
        DataConnectTo dataConnectTo = new RegRuMysql();
        MysqlDataSource connectToDataSource = dataConnectTo.getDataSource(); //fixme ru.vachok.mysqlandprops.RegRuMysql.getDataSource 13.06.2019 (15:26) dataSource.setPassword(userPass[1])
        
        connectToDataSource.setUser("u0466446_default");
        connectToDataSource.setPassword("5xN_QlJG");
        connectToDataSource.setDatabaseName("u0466446_velkom");
        connectToDataSource.setUseSSL(false);
        connectToDataSource.setContinueBatchOnError(true);
        
        try (Connection c = connectToDataSource.getConnection();
             PreparedStatement statement = c.prepareStatement(sql);
             ResultSet r = statement.executeQuery()
        ) {
            while (r.next()) {
                getInfo(r, doljAndAccess);
            }
        }
        catch (SQLException e) {
            LOGGER.error("MatrixSRV", "getWorkPosition", e.getMessage());
            FileSystemWorker.error("MatrixSRV.getWorkPosition", e);
        }
        String s = new TForms().fromArray(doljAndAccess, true);
        this.workPos = s;
        return s;
    }
    
    private static String downBlob(byte[] bytes, String fileType) {
        String fileName = "theBlob." + fileType;
        File file = new File(fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(bytes);
        }
        catch (IOException e) {
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
        
        resourceCache.forEach((x, y)->stringBuilder
            .append(x)
            .append("<br>")
            .append(y));
        
        return stringBuilder.toString();
    }
    
    private void getInfo(ResultSet r, Map<String, String> doljAndAccess) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        countDB += 1;
        int fullInet = r.getInt("fullinet");
        int stdInet = r.getInt("stdinet");
        int limitInet = r.getInt("limitinet");
        int owaAsync = r.getInt("owaasync");
        int vpn = r.getInt("VPN");
        int sendmail = r.getInt("sendmail");
        if (fullInet == 1) {
            stringBuilder.append(" - полный достув в интернет <br>");
        }
        if (stdInet == 1) {
            stringBuilder.append(" - доступ с ограничениями (mail.ru и тп). Стандарт для большинства <br>");
        }
        if (limitInet == 1) {
            stringBuilder.append(" - досту только к определённым спискам сайтов <br>");
        }
        if (owaAsync == 1) {
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
        Blob changes = r.getBlob(SQLCOL_CHANGES);
        if (changes.length() > ConstantsFor.KBYTE) {
            r.getBlob(SQLCOL_CHANGES);
            String fileType = r.getString("filetype");
            doljAndAccess.put("Reason", downBlob(changes.getBytes(1, Math.toIntExact(changes.length())), fileType));
        }
    }
}
