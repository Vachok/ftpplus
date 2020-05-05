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


/**
 Class ru.vachok.networker.net.ssh.SSHCommander
 <p>

 @since 14.03.2020 (13:11) */
public class JSONSSHCommandExecutor implements RestApiHelper {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, JSONSSHCommandExecutor.class.getSimpleName());

    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        String result;
        int codeVer = -666;
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
            result = connectToSrv(jsonObject, codeVer);
            messageToUser.info(getClass().getSimpleName(), jsonObject.toString(), result);
        }
        return result;
    }

    private String connectToSrv(@NotNull JsonObject jsonObject, int codeVer) {
        String result = getClass().getSimpleName();
        String authorizationHeader = "null";
        boolean isValid;
        try {
            authorizationHeader = jsonObject.getString(ConstantsFor.AUTHORIZATION, "");
        }
        catch (Exception e) {
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
                result = AbstractForms.networkerTrace(e);
            }
        }
        else {
            result = result + "\n" + authorizationHeader + ":" + codeVer + " BAD AUTH!";
        }
        return result;
    }

    private String makeActions(JsonObject jsonObject) {
        String serverName = SshActs.whatSrvNeed();
        if (jsonObject.names().contains(ConstantsFor.PARAM_NAME_SERVER)) {
            JsonValue value = jsonObject.get(ConstantsFor.PARAM_NAME_SERVER);
            serverName = value.asString();
        }
        String commandForSH = ConstantsFor.SSH_UNAMEA + ";uptime;sudo pgrep -f -v -l -u root;exit";
        if (jsonObject.names().contains(ConstantsFor.PARM_NAME_COMMAND)) {
            commandForSH = jsonObject.getString(ConstantsFor.PARM_NAME_COMMAND, commandForSH);
        }
        SSHFactory.Builder sshFB = new SSHFactory.Builder(serverName, commandForSH, getClass().getSimpleName());
        MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName()).info(serverName, commandForSH, getClass().getSimpleName());
        String finalServerName = serverName;
        AppConfigurationLocal.getInstance()
            .execute(()->messageToUser.info(new SSHFactory.Builder(finalServerName, "uname -a;uptime;exit", getClass().getSimpleName()).build().call()));
        return serverAnswer(sshFB);
    }

    private String serverAnswer(SSHFactory.Builder fb) {
        String serverAnswerString = AppConfigurationLocal.getInstance().submitAsString(fb.build(), 10);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(fb.getCommandSSH(), serverAnswerString);
        return jsonObject.toString();
    }
}