// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.regex.Pattern;


/**
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    
    String DBNAME_VELKOM_POINT = "velkom.";
    
    String DBUSER_KUDR = "u0466446_kudr";
    
    String DBUSER_NETWORK = "u0466446_network";
    
    String LIB_REGRU = "RegRuMysql";
    
    String LOC_INETSTAT = "MySqlInetStat";
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static @NotNull ru.vachok.mysqlandprops.DataConnectTo getInstance(@NotNull String type) {
        switch (type) {
            case ConstantsFor.DBBASENAME_U0466446_PROPERTIES:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
            case ConstantsFor.DBBASENAME_U0466446_WEBAPP:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_WEBAPP);
            case LIB_REGRU:
                return new RegRuMysql();
            case ConstantsFor.DBBASENAME_U0466446_TESTING:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
            case LOC_INETSTAT:
                return new MySqlLocalSRVInetStat();
            default:
                return getDefaultI();
        }
    }
    
    @Contract(value = " -> new", pure = true)
    static @NotNull DataConnectTo getDefaultI() {
        if (UsefulUtilities.thisPC().toLowerCase().contains("srv-")) {
            return new MySqlLocalSRVInetStat();
        }
        else {
            return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
    }
    
    int uploadFileTo(Path filePath, String tableName);
    
    int uploadFileTo(Collection stringsCollection, String tableName);
    
    @Contract("_ -> new")
    default @NotNull String[] getCreateQuery(@NotNull String dbPointTableName, boolean isUpstringUniq) {
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = DBNAME_VELKOM_POINT + dbPointTableName;
        }
        String[] dbTable = dbPointTableName.split("\\Q.\\E");
        if (dbTable[1].startsWith(String.valueOf(Pattern.compile("\\d")))) {
            throw new IllegalArgumentException(dbTable[1]);
        }
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(dbTable[0])
                .append(".")
                .append(dbTable[1])
                .append("(\n")
                .append("  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n")
                .append("  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n")
                .append("  `upstring` varchar(260) NOT NULL COMMENT ''\n")
                .append(") ENGINE=MyIsam DEFAULT CHARSET=utf8;\n");
        
        stringBuilder2.append(ConstantsFor.SQL_ALTERTABLE)
                .append(dbTable[0])
                .append(".")
                .append(dbTable[1])
                .append("\n")
                .append("  ADD PRIMARY KEY (`idrec`);\n");
        
        stringBuilder1.append(ConstantsFor.SQL_ALTERTABLE).append(dbPointTableName).append("\n")
                .append("  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';").toString();
        stringBuilder3.append("ALTER TABLE ").append(dbTable[1]).append(" ADD UNIQUE (`upstring`);");
        if (!isUpstringUniq) {
            return new String[]{stringBuilder.toString(), stringBuilder2.toString(), stringBuilder1.toString()};
        }
        else {
            return new String[]{stringBuilder.toString(), stringBuilder2.toString(), stringBuilder1.toString(), stringBuilder3.toString()};
        }
    }
    
    @Override
    default void setSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:44)");
    }
    
    @Override
    MysqlDataSource getDataSource();
    
    @Override
    Connection getDefaultConnection(String dbName);
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:45)");
    }
    
}
