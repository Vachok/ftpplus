package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.RestApiHelper;

import static ru.vachok.networker.IntoApplication.getContext;


/**
 Class ru.vachok.networker.net.ssh.SSHCommander
 <p>

 @since 14.03.2020 (13:11) */
public class SSHCommander implements RestApiHelper {


    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        int codeVer = jsonObject.getInt("code", -1);
        if (checkValidUID(jsonObject.getString(ConstantsFor.AUTHORIZATION, ""), codeVer)) {
            return makeActions(jsonObject);
        }
        else {
            throw new InvokeIllegalException(jsonObject.toString());
        }
    }

    private String makeActions(JsonObject jsonObject) {
        JsonValue value = jsonObject.get(ConstantsFor.PARM_NAME_COMMAND);
        SshActs sshActs = (SshActs) getContext().getBean(ModelAttributeNames.ATT_SSH_ACTS);
        return sshActs.execSSHCommand(value.asString());
    }
}