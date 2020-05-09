package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.text.MessageFormat;


/**
 Class ru.vachok.networker.net.ssh.SSHCommander
 <p>

 @since 14.03.2020 (13:11) */
public class JSONSSHCommandExecutor implements RestApiHelper {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, JSONSSHCommandExecutor.class.getSimpleName());

    private String serverName = SshActs.whatSrvNeed();

    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        int codeVer = -666;
        String result;
        try {
            codeVer = jsonObject.getInt(ConstantsFor.PARAM_NAME_CODE, -1);
        }
        catch (ParseException | UnsupportedOperationException e) {
            codeVer = Integer.parseInt(jsonObject.getString(ConstantsFor.PARAM_NAME_CODE, "-1"));
        }
        catch (RuntimeException e) {
            codeVer = Integer.parseInt(jsonObject.getString(ConstantsFor.PARAM_NAME_CODE, "-1"));
            messageToUser.error("JSONSSHCommandExecutor.getResult", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        finally {
            JsonObject jsonObjectResult = connectToSrv(jsonObject, codeVer);
            jsonObjectResult.add(ConstantsFor.PARAM_NAME_SERVER, serverName);
            result = jsonObjectResult.toString();
            messageToUser.info(getClass().getSimpleName(), result, jsonObject.toString());
        }
        return result;
    }

    private JsonObject connectToSrv(@NotNull JsonObject jsonObject, int codeVer) {
        JsonObject result = new JsonObject();
        String authorizationHeader = "null";
        boolean isValid;
        try {
            authorizationHeader = jsonObject.getString(ConstantsFor.AUTHORIZATION, "");
        }
        catch (RuntimeException e) {
            authorizationHeader = e.getMessage();
        }
        finally {
            isValid = checkValidUID(authorizationHeader, codeVer);
        }
        if (isValid) {
            try {
                result = makeActions(jsonObject);
            }
            catch (RuntimeException e) {
                result.add(e.getClass().getSimpleName(), AbstractForms.networkerTrace(e));
            }
        }
        else {
            result.add("BAD AUTH!", MessageFormat.format("{0}\n{1}:{2}", result, authorizationHeader, codeVer));
        }
        return result;
    }

    private JsonObject makeActions(JsonObject jsonObject) {
        if (jsonObject.names().contains(ConstantsFor.PARAM_NAME_SERVER)) {
            JsonValue value = jsonObject.get(ConstantsFor.PARAM_NAME_SERVER);
            this.serverName = value.asString();
        }
        String commandForSH = "uname -a;uptime;";
        if (jsonObject.names().contains(ConstantsFor.PARM_NAME_COMMAND)) {
            String commandFromJSON = jsonObject.getString(ConstantsFor.PARM_NAME_COMMAND, commandForSH);
            if (commandFromJSON.contains("ps ax")) {
                commandForSH = commandForSH + ";exit";
            }
            else {
                commandForSH = commandFromJSON;
            }
        }
        SSHFactory.Builder sshFB = new SSHFactory.Builder(serverName, commandForSH, getClass().getSimpleName());
        MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName()).info(serverName, commandForSH, getClass().getSimpleName());
        String finalServerName = serverName;
        AppConfigurationLocal.getInstance()
            .execute(()->messageToUser.info(new SSHFactory.Builder(finalServerName, "uname -a;uptime;exit", getClass().getSimpleName()).build().call()));
        return serverAnswer(sshFB);
    }

    private JsonObject serverAnswer(SSHFactory.Builder fb) {
        String serverAnswerString = AppConfigurationLocal.getInstance().submitAsString(fb.build(), 10);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(fb.getCommandSSH(), serverAnswerString);
        return jsonObject;
    }
}