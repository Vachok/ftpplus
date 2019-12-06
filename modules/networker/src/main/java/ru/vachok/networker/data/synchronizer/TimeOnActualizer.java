package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


public class TimeOnActualizer implements Runnable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TimeOnActualizer.class.getSimpleName());
    
    private String pcName;
    
    @Contract(pure = true)
    public TimeOnActualizer(@NotNull String pcName) {
        this.pcName = pcName.replace(ConstantsFor.DOMAIN_EATMEATRU, "");
    }
    
    @Override
    public void run() {
        setTimeOnFromBigDB();
    }
    
    private void setTimeOnFromBigDB() {
        List<String> pcNames = new ArrayList<>();
        if (pcName == null) {
            pcNames = getPcNames();
        }
        else {
            pcNames.add(pcName);
        }
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMVELKOMPC)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            for (String pcName : pcNames) {
                final String sql = "SELECT idrec FROM velkompc WHERE NamePP LIKE '" + pcName
                        .replace(ConstantsFor.DOMAIN_EATMEATRU, "") + "%' AND OnlineNow = 0 ORDER BY idrec DESC LIMIT 1";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setQueryTimeout(150);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            int idrec = resultSet.getInt(ConstantsFor.DBCOL_IDREC);
                            if (idrec > 0) {
                                actualizeDB(idrec, pcName);
                            }
                            else {
                                messageToUser.warn(this.getClass().getSimpleName(), "setTimeOnFromBigDB", preparedStatement.toString());
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("TimeOnActualizer.setTimeOnFromBigDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
    
    private @NotNull List<String> getPcNames() {
        final String sql = "SELECT DISTINCT pcName FROM online";
        List<String> pcNames = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String pcName = resultSet.getString(ConstantsFor.DBFIELD_PCNAME);
                if (pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
                    pcNames.add(pcName);
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("TimeOnActualizer.getPcNames", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return pcNames;
    }
    
    private void actualizeDB(int idRec, String pcName) {
        final String sql = "SELECT TimeNow FROM velkompc WHERE idrec > ? AND NamePP LIKE ? AND OnlineNow=1 ORDER BY idrec asc LIMIT 1";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMVELKOMPC)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, idRec);
                preparedStatement.setString(2, pcName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Timestamp actualTimeOn = resultSet.getTimestamp(ConstantsFor.DBFIELD_TIMENOW);
                        setInPcUserDB(pcName, actualTimeOn);
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("TimeOnActualizer.actualizeDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
    
    private void setInPcUserDB(String pcName, Timestamp actualTimeOn) {
        final String sql = String.format("UPDATE `velkom`.`pcuser` SET `timeon`= ? WHERE `pcName` like '%s%%'", pcName);
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, actualTimeOn);
    
            messageToUser.info(this.getClass().getSimpleName(),
                MessageFormat.format("setInPcUserDB executeUpdate: {0}\n", preparedStatement.executeUpdate()), preparedStatement.toString());
        }
        catch (SQLException e) {
            messageToUser.error("TimeOnActualizer.setInPcUserDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeOnActualizer{");
        sb.append("pcName='").append(pcName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
