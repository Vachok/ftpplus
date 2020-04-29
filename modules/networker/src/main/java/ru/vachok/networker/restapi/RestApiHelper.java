package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.inet.AllowDomainHelper;
import ru.vachok.networker.ad.inet.TempInetRestControllerHelper;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.net.ssh.SSHCommander;
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
                return new SSHCommander();
        }
        return new RestError();
    }

    default boolean checkValidUID(String headerAuthorization, int minCodeVer) throws InvokeIllegalException {
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
        if (minCodeVer < (Integer.parseInt(InitProperties.getTheProps().getProperty("minMobAppVersion")))) {
            throw new InvokeIllegalException(minCodeVer + " " + getClass().getSimpleName());
        }
        return isValid;
    }

    String getResult(@NotNull JsonObject jsonObject);

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
}
