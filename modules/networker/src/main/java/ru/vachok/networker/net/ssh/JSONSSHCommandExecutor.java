package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;


/**
 Class ru.vachok.networker.net.ssh.SSHCommander
 <p>

 @since 14.03.2020 (13:11) */
public class JSONSSHCommandExecutor implements RestApiHelper {


    private String serverName = SshActs.whatSrvNeed();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, JSONSSHCommandExecutor.class.getSimpleName());

    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        JsonObject jsonObjectResult = connectToSrv(jsonObject);
        jsonObjectResult.add(ConstantsFor.JSON_PARAM_NAME_SERVER, serverName);
        return jsonObjectResult.toString();
    }

    private JsonObject connectToSrv(@NotNull JsonObject jsonObject) {
        JsonObject result = new JsonObject();
        boolean codeValid = checkCodeVersion(jsonObject);
        boolean userValid = checkValidUID(String.valueOf(jsonObject.get(ConstantsFor.AUTHORIZATION).asString()));
        boolean isValid = userValid & codeValid;
        if (isValid) {
            try {
                result = makeActions(jsonObject);
            }
            catch (RuntimeException e) {
                messageToUser.warn(JSONSSHCommandExecutor.class.getSimpleName(), e.getMessage(), " see line: 41 ***");
                result.add(ConstantsFor.STR_ERROR, e.getClass().getSimpleName() + " " + e.getMessage() + ": \n" + AbstractForms.fromArray(e));
            }
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
        if (jsonObject.names().contains(ConstantsFor.JSON_PARAM_NAME_SERVER)) {
            JsonValue value = jsonObject.get(ConstantsFor.JSON_PARAM_NAME_SERVER);
            this.serverName = value.asString();
        }
        String commandForSH = "uname -a;uptime;";
        if (jsonObject.names().contains(ConstantsFor.PARM_NAME_COMMAND)) {
            commandForSH = jsonObject.getString(ConstantsFor.PARM_NAME_COMMAND, commandForSH);
        }
        SSHFactory.Builder sshFB = new SSHFactory.Builder(serverName, commandForSH, getClass().getSimpleName());
        return serverAnswer(sshFB);
    }

    private JsonObject serverAnswer(SSHFactory.Builder fb) {
        String serverAnswerString = AppConfigurationLocal.getInstance().submitAsString(fb.build(), 21);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(ConstantsFor.JSON_PARAM_NAME_SERVER, this.serverName);
        jsonObject.add(fb.getCommandSSH(), serverAnswerString);
        return jsonObject;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, JSONSSHCommandExecutor.class.getSimpleName());
        jsonObject.add("serverName", serverName);
        return jsonObject.toString();
    }
}