package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.inet.AllowDomainHelper;
import ru.vachok.networker.ad.inet.TempInetRestControllerHelper;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.net.ssh.JSONSSHCommandExecutor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.props.InitProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@FunctionalInterface
public interface RestApiHelper {


    String INVALID_USER = "INVALID USER";

    String DOMAIN = "allowdomainhelper";

    String SSH = "sshcommandexec";

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @NotNull
    static RestApiHelper getInstance(@NotNull String type) {
        switch (type) {
            case "TempInetRestControllerHelper":
                return new TempInetRestControllerHelper();
            case DOMAIN:
                return new AllowDomainHelper();
            case SSH:
                return new JSONSSHCommandExecutor();
        }
        return new RestError();
    }

    default boolean checkValidUID(String headerAuthorization) {
        boolean isValid = false;
        List<String> validUIDs = getFromDB();
        if (validUIDs.size() == 0) {
            FileSystemWorker.readFileToList("uid.txt");
        }
        for (String validUID : validUIDs) {
            if (headerAuthorization.equals(validUID)) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

    @NotNull
    default List<String> getFromDB() {
        List<String> validUIDs = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_UIDS_FULL);
             PreparedStatement preparedStatement = connection.prepareStatement("select * from velkom.restuids");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                validUIDs.add(resultSet.getString("uid"));
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return validUIDs;
    }

    default boolean checkCodeVersion(@NotNull JsonObject jsonObject) {
        int codeVer = 1600;
        if (jsonObject.names().contains(ConstantsFor.JSON_PARAM_NAME_CODE)) {
            try {
                codeVer = jsonObject.getInt(ConstantsFor.JSON_PARAM_NAME_CODE, codeVer);
            }
            catch (RuntimeException e) {
                codeVer = Integer.parseInt(jsonObject.getString(ConstantsFor.JSON_PARAM_NAME_CODE, "2000"));
            }
        }
        return codeVer >= (Integer.parseInt(InitProperties.getTheProps().getProperty("minMobAppVersion")));
    }

    String getResult(@NotNull JsonObject jsonObject);
}
