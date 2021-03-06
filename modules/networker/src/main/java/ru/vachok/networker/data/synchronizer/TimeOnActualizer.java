package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.MyISAMRepair;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 @see TimeOnActualizerTest */
public class TimeOnActualizer implements Runnable {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TimeOnActualizer.class.getSimpleName());

    private final String pcName;

    private final boolean isOnNow;

    public TimeOnActualizer(@NotNull String pcName, boolean isOnNow) {
        this.pcName = pcName.replace(ConstantsFor.DOMAIN_EATMEATRU, "");
        this.isOnNow = isOnNow;
    }

    @Override
    public void run() {
        if (System.getProperty("os.name").toLowerCase().contains(PropertiesNames.WINDOWSOS)) {
            setTimeOnFromBigDB();
        }
        else {
            messageToUser.warn(getClass().getSimpleName(), "not run on ", System.getProperty("os.name"));
        }
    }

    private void setTimeOnFromBigDB() {
        List<String> pcNames = new ArrayList<>();
        if (pcName == null) {
            pcNames = getPcNames();
        }
        else {
            pcNames.add(pcName);
        }
        try (Connection connectionVelkomPC = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMVELKOMPC)) {
            connectionVelkomPC.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            for (String pcName : pcNames) {
                final String sql = "SELECT idrec FROM velkompc WHERE NamePP LIKE '" + pcName
                    .replace(ConstantsFor.DOMAIN_EATMEATRU, "") + "%' AND OnlineNow = 0 ORDER BY idrec DESC LIMIT 1";
                try (PreparedStatement preparedStatement = connectionVelkomPC.prepareStatement(sql)) {
                    preparedStatement.setQueryTimeout(35);
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
                    catch (SQLException e) {
                        if (e.getMessage().contains(ConstantsFor.MARKEDASCRASHED)) {
                            messageToUser.error(this.getClass().getSimpleName(), e.getMessage(), new MyISAMRepair()
                                .repairTable(ConstantsFor.REPAIR_TABLE + ConstantsFor.DB_ARCHIVEVELKOMPC));
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("TimeOnActualizer.setTimeOnFromBigDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }

    @NotNull
    private List<String> getPcNames() {
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
        final String newSql = String.format("UPDATE `velkom`.`pcuser` SET `timeon`=?, `onNow`=? WHERE  `pcName` like '%s%%'", pcName);
        final String sql = String.format("UPDATE `velkom`.`pcuser` SET `timeon`= ? WHERE `pcName` like '%s%%'", pcName);
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(newSql)) {
                preparedStatement.setQueryTimeout((int) ConstantsFor.DELAY);
                preparedStatement.setTimestamp(1, actualTimeOn);
                if (isOnNow) {
                    preparedStatement.setInt(2, 1);
                }
                else {
                    preparedStatement.setInt(2, 0);
                }
                preparedStatement.executeUpdate();
            }
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
