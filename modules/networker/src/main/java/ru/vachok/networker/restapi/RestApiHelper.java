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

    String getResult(@NotNull JsonObject jsonObject);

}
