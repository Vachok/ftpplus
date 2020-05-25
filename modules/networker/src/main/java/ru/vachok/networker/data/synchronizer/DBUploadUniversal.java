package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see DBUploadUniversalTest
 @since 15.09.2019 (13:07) */
class DBUploadUniversal extends SyncData {


    private Collection toUploadCollection;

    private final List<String> columnsList = new ArrayList<>();

    private String dbToSync;

    @Override
    public void run() {
        superRun();
    }

    @Override
    public Object getRawResult() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBUploadUniversal.getRawResult( Object ) at 17.05.2020 - (13:10)");
    }

    @Override
    protected void setRawResult(Object value) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBUploadUniversal.setRawResult( void ) at 17.05.2020 - (13:10)");
    }

    DBUploadUniversal(Collection toUploadCollection, String dbToSync) {
        this.toUploadCollection = toUploadCollection;
        this.dbToSync = dbToSync;
    }

    DBUploadUniversal(String dbToSync) {
        this.dbToSync = dbToSync;
    }

    @Override
    public int hashCode() {
        int result = toUploadCollection != null ? toUploadCollection.hashCode() : 0;
        result = 31 * result + columnsList.hashCode();
        result = 31 * result + (dbToSync != null ? dbToSync.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBUploadUniversal)) {
            return false;
        }

        DBUploadUniversal universal = (DBUploadUniversal) o;

        if (toUploadCollection != null ? !toUploadCollection.equals(universal.toUploadCollection) : universal.toUploadCollection != null) {
            return false;
        }
        if (!columnsList.equals(universal.columnsList)) {
            return false;
        }
        return dbToSync != null ? dbToSync.equals(universal.dbToSync) : universal.dbToSync == null;
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", DBUploadUniversal.class.getSimpleName() + "[\n", "\n]")
            .add("toUploadCollection = " + toUploadCollection)
            .add("dbToSync = '" + dbToSync + "'")
            .toString();
    }

    @Override
    public String getDbToSync() {
        return dbToSync;
    }

    @Override
    public void setDbToSync(String dbToSync) {
        this.dbToSync = dbToSync;
    }

    @Override
    public void setOption(Object option) {
        if (option instanceof Collection) {
            this.toUploadCollection = (Collection) option;
        }
        else {
            throw new IllegalArgumentException(option.toString());
        }
    }

    @Override
    public String syncData() {
        if (toUploadCollection.size() <= 0 && dbToSync.isEmpty()) {
            throw new IllegalArgumentException(this.dbToSync + "\n" + this.toUploadCollection.size());
        }

        return MessageFormat.format("{0} rows uploaded to {1}", uploadCollection(toUploadCollection, dbToSync), dbToSync);
    }

    @Override
    public int uploadCollection(Collection stringsCollection, @NotNull String tableName) {
        this.dbToSync = tableName;
        this.toUploadCollection = stringsCollection;
        String onlyTableName = dbToSync.split("\\Q.\\E")[1];
        String onlyDBName = dbToSync.split("\\Q.\\E")[0];
        try (Connection connection = CONNECT_TO_LOCAL.getDataSource().getConnection()) {
            try (ResultSet columns = connection.getMetaData().getColumns(onlyDBName, onlyTableName, onlyTableName, "%")) {
                while (columns.next()) {
                    columnsList.add(columns.getString(4).toLowerCase());
                }
            }
            if (columnsList.size() == 0) {
                @NotNull String query = getCreateQuery();
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    int createInt = preparedStatement.executeUpdate();
                    messageToUser.info(this.getClass().getSimpleName(), createInt + " executed: ", query);
                }
            }
        }
        catch (SQLException e) {
            int errCode = e.getErrorCode();
            messageToUser.error(this.getClass().getSimpleName(), errCode + " error code", e.getMessage() + " see line: 61");
        }
        uploadReal();
        return columnsList.size();
    }

    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("0");

    }

    @Contract(pure = true)
    private @NotNull String getCreateQuery() {
        return "CREATE TABLE `build_gradle` (\n" +
            "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
            "\t`stamp` BIGINT(13) UNSIGNED ZEROFILL NOT NULL DEFAULT '0442278000000',\n" +
            "\t`upstring` VARCHAR(1024) NOT NULL DEFAULT '1024 symbols max',\n" +
            "\tPRIMARY KEY (`idrec`),\n" +
            "\tUNIQUE INDEX `upstring` (`upstring`)\n" +
            ")\n" +
            "COMMENT='path'\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=MyISAM\n" +
            "ROW_FORMAT=COMPRESSED\n" +
            ";";
    }

    private void uploadReal() {
        Deque<String> colDeq = new ConcurrentLinkedDeque<>(toUploadCollection);
        String sql;
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(dbToSync)) {
            while (!colDeq.isEmpty()) {
                sql = getSQL(colDeq.removeFirst());
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.executeUpdate();
                }
                catch (SQLException e) {
                    messageToUser.warn(this.getClass().getSimpleName(), e.getMessage(), sql);
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
    }

    private @NotNull String getSQL(String first) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] values = new String[columnsList.size()];
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = Json.parse(first.toLowerCase()).asObject();
        }
        catch (ParseException e) {
            jsonObject.add("upstring", first);
        }
        for (int i = 0; i < columnsList.size(); i++) {
            values[i] = jsonObject.getString(columnsList.get(i), columnsList.get(i));
        }
        stringBuilder.append(ConstantsFor.SQL_INSERTINTO).append(dbToSync.split("\\Q.\\E")[1]);
        stringBuilder.append(" (");
        for (String s : columnsList) {
            stringBuilder.append(s).append(", ");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");
        stringBuilder.append(ConstantsFor.VALUES);
        for (int i = 0; i < columnsList.size(); i++) {
            stringBuilder.append("'").append(values[i]).append("', ");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBUploadUniversal.makeColumns( Map<String, String> ) at 15.09.2019 - (13:08)");
    }

    @Override
    public void superRun() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBUploadUniversal.superRun( void ) at 15.09.2019 - (13:08)");
    }
}