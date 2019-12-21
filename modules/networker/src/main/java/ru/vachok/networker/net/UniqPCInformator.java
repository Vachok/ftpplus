package ru.vachok.networker.net;


import com.eclipsesource.json.JsonObject;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 Class ru.vachok.networker.net.UniqPCInformator
 <p>

 @since 19.12.2019 (21:16) */
public class UniqPCInformator implements InformationFactory {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UniqPCInformator.class.getSimpleName());

    private static final String SQL_ONLINE = "select distinct pcname as ip, pcname from lan.online";

    private boolean isJson = false;

    @Override
    public String getInfo() {
        if (!isJson) {
            return AbstractForms.fromArray(getPcs());
        }
        else {
            return getInfoAbout("");
        }
    }

    @Override
    public void setClassOption(Object option) {
        if (option instanceof Boolean) {
            this.isJson = (boolean) option;
        }
        else {
            throw new InvokeIllegalException("Only boolean accepted");
        }
    }

    private Map<String, String> getPcs() {
        Map<String, String> uniqPCs = new TreeMap<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE);
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ONLINE);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                uniqPCs.put(resultSet.getString("ip"), resultSet.getString(ConstantsFor.SQL_PCNAME));
            }
        }
        catch (SQLException e) {
            messageToUser.warn(UniqPCInformator.class.getSimpleName(), e.getMessage(), " see line: 43 ***");
        }
        return uniqPCs;
    }

    @Override
    public String getInfoAbout(String aboutWhat) {
        return AbstractForms.fromArray(getAsJson());
    }

    private List<JsonObject> getAsJson() {
        List<JsonObject> jsonObjects = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE);
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ONLINE + " order by pcname asc");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("ip", resultSet.getString("ip"));
                jsonObject.add(ConstantsFor.SQL_PCNAME, resultSet.getString(ConstantsFor.SQL_PCNAME));
                jsonObjects.add(jsonObject);
            }
        }
        catch (SQLException e) {
            messageToUser.warn(UniqPCInformator.class.getSimpleName(), e.getMessage(), " see line: 43 ***");
        }
        return jsonObjects;
    }
}