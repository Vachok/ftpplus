package ru.vachok.networker.net;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 @see UniqPCInformatorTest
 @since 19.12.2019 (21:16) */
public class UniqPCInformator implements InformationFactory {


    public static final String SQL_PCNAME = "pcname";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UniqPCInformator.class.getSimpleName());

    private static final String SQL_ONLINE = "select distinct pcname AS pcname, ip from lan.online order by pcname asc";

    private boolean isJson = false;

    @NotNull
    private List<JsonObject> getAsJson() {
        List<JsonObject> jsonObjects = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE);
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ONLINE);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("ip", resultSet.getString("ip"));
                jsonObject.add(SQL_PCNAME, resultSet.getString(SQL_PCNAME));
                jsonObjects.add(jsonObject);
            }
        }
        catch (SQLException e) {
            messageToUser.warn(UniqPCInformator.class.getSimpleName(), e.getMessage(), " see line: 43 ***");
        }
        return jsonObjects;
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

    @Override
    public String getInfo() {
        if (!isJson) {
            Map<String, String> pcs = getPcs();
            String retStr = AbstractForms.fromArray(pcs);
            retStr = pcs.size() + " unique PC in net.\n\n" + retStr;
            return retStr;
        }
        else {
            return AbstractForms.fromArray(getAsJson());
        }
    }

    /**
     @see UniqPCInformatorTest#testGetInfoAbout()
     */
    @Override
    public String getInfoAbout(String aboutWhat) {
        String info;
        info = aboutWhat.isEmpty() ? AbstractForms.fromArray(getAsJson()) : PCInfo.getInstance(aboutWhat).getInfo();
        if (!info.contains(" : ")) {
            info = getInfo();
        }
        return info;
    }

    @NotNull
    private Map<String, String> getPcs() {
        Map<String, String> uniqPCs = new TreeMap<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE);
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ONLINE);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                uniqPCs.put(resultSet.getString(SQL_PCNAME), resultSet.getString("ip"));
            }
        }
        catch (SQLException e) {
            messageToUser.warn(UniqPCInformator.class.getSimpleName(), e.getMessage(), " see line: 43 ***");
        }
        return uniqPCs;
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", UniqPCInformator.class.getSimpleName() + "[\n", "\n]")
            .add("isJson = " + isJson)
            .toString();
    }
}