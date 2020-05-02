package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;


/**
 Class ru.vachok.networker.net.ssh.SSHCommander
 <p>

 @since 14.03.2020 (13:11) */
public class JSONSSHCommandExecutor implements RestApiHelper {


    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        String result = jsonObject.toString();
        System.out.println("result = " + result);
        int codeVer = jsonObject.getInt(ConstantsFor.PARAM_NAME_CODE, -1);
        String authorizationHeader = jsonObject.getString(ConstantsFor.AUTHORIZATION, "");
        boolean isValid = false;
        try {
            isValid = checkValidUID(authorizationHeader, codeVer);
        }
        catch (InvokeIllegalException e) {
            result = AbstractForms.networkerTrace(e.getStackTrace());
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
            JsonValue value = jsonObject.get(ConstantsFor.PARM_NAME_COMMAND);
            serverName = value.asString();
        }
        String commandForSH = ConstantsFor.SSH_UNAMEA + ";uptime;sudo pgrep -f -v -l -u root;exit";
        if (jsonObject.names().contains(ConstantsFor.PARM_NAME_COMMAND)) {
            commandForSH = jsonObject.getString(ConstantsFor.PARM_NAME_COMMAND, commandForSH);
        }
        SSHFactory.Builder sshFB = new SSHFactory.Builder(serverName, commandForSH, jsonObject.get(ConstantsFor.AUTHORIZATION).asString());
        return serverAnswer(sshFB);
    }

    private String serverAnswer(SSHFactory.Builder fb) {
        String serverAnswerString = AppConfigurationLocal.getInstance().submitAsString(fb.build(), 10);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(fb.getCommandSSH(), serverAnswerString);
        return jsonObject.toString();
    }
}