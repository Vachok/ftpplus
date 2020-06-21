package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.util.Properties;


/**
 @see JSONSSHCommandExecutorTest
 @since 14.03.2020 (13:11) */
public class JSONSSHCommandExecutor implements RestApiHelper {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, JSONSSHCommandExecutor.class.getSimpleName());

    private static final String COM_SSH = "uname -a;uptime;";

    private static final String TAG = "JSONSSHCommandExecutor";

    public JSONSSHCommandExecutor() {
    }

    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        JsonObject jsonObjectResult = connectToSrv(jsonObject);
        return jsonObjectResult.toString();
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, TAG);
        return jsonObject.toString();
    }

    private JsonObject connectToSrv(@NotNull JsonObject jsonObject) {
        JsonObject result = new JsonObject();
        boolean codeValid = checkCodeVersion(jsonObject);
        boolean userValid = checkValidUID(String.valueOf(jsonObject.get(ConstantsFor.AUTHORIZATION).asString()));
        boolean isValid = userValid & codeValid;
        if (isValid) {
            result = makeActions(jsonObject);
        }
        else {
            result.add(ConstantsFor.JSON_PARAM_NAME_BAD_AUTH, getClass().getSimpleName());
            result.add(ConstantsFor.JSON_PARAM_NAME_USER, userValid);
            result.add(ConstantsFor.JSON_PARAM_NAME_CODE, codeValid);
            result.add(ConstantsFor.JSON_PARAM_NAME_REQUEST_JSON, jsonObject.toString());
        }
        return result;
    }

    private JsonObject makeActions(JsonObject jsonObject) {
        String commandSSH = ConstantsFor.SSH_UNAMEA;
        String ser = SshActs.whatSrvNeed();
        if (jsonObject.names().contains(ConstantsFor.JSON_PARAM_NAME_SERVER)) {
            JsonValue value = jsonObject.get(ConstantsFor.JSON_PARAM_NAME_SERVER);
            ser = jsonObject.get(ConstantsFor.JSON_PARAM_NAME_SERVER).asString();
            commandSSH = value.asString();
        }
        if (jsonObject.names().contains(ConstantsFor.PARM_NAME_COMMAND)) {
            commandSSH = jsonObject.getString(ConstantsFor.PARM_NAME_COMMAND, COM_SSH);
        }
        return serverAnswer(commandSSH, ser);
    }

    private JsonObject serverAnswer(String commandSSH, String server) {
        int secTimeOut = getTimeOut();
        SSHFactory.Builder sshFactoryB = new SSHFactory.Builder(server, commandSSH, getClass().getSimpleName());
        SSHFactory buildED = sshFactoryB.build();
        buildED.setCommandSSH(commandSSH);
        String serverAnswerString = AppConfigurationLocal.getInstance().submitAsString(buildED, secTimeOut);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(ConstantsFor.JSON_PARAM_NAME_SERVER, SshActs.whatSrvNeed());
        jsonObject.add(buildED.getCommandSSH(), serverAnswerString);
        return jsonObject;
    }

    private int getTimeOut() {
        int secTimeOut = 12;
        try {
            secTimeOut = Integer.parseInt(InitProperties.getTheProps().getProperty(PropertiesNames.REST_SSH_TIMEOUT));
        }
        catch (NumberFormatException e) {
            Properties props = InitProperties.getTheProps();
            props.setProperty(PropertiesNames.REST_SSH_TIMEOUT, String.valueOf(secTimeOut));
            InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(props);
        }
        return secTimeOut;
    }
}