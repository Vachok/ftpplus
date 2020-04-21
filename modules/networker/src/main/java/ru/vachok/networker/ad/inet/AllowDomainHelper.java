package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.net.ssh.PfLists;
import ru.vachok.networker.net.ssh.PfListsSrv;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.restapi.message.MessageToUser;


/**
 @see AllowDomainHelperTest
 @since 01.03.2020 (17:33) */
public class AllowDomainHelper extends SshActs implements RestApiHelper {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AllowDomainHelper.class.getSimpleName());

    private String allowDomains;

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
        String result = "Domain is exists";
        boolean finished = false;
        JsonValue ipValue = jsonObject.get(ConstantsFor.DOMAIN);
        JsonValue optValue = jsonObject.get(ConstantsFor.OPTION);
        boolean isAdd = optValue.toString().contains("add");
        try (ConfigurableApplicationContext context = IntoApplication.getContext();) {

            PfListsSrv bean = new PfListsSrv((PfLists) context.getBean(ConstantsFor.BEANNAME_PFLISTS));
            bean.setCommandForNatStr(ConstantsFor.SSHCOM_GETALLOWDOMAINS);
            this.allowDomains = bean.runCom();
        }
        catch (BeansException e) {
            messageToUser.error("AllowDomainHelper.makeActions", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        try {
            messageToUser.info(allowDomains);
            if (allowDomains.contains("." + ipValue.asString().split("://")[1].split("/")[0])) {
                finished = true;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            messageToUser.error("AllowDomainHelper.getResult", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        if (optValue.toString().contains(ConstantsFor.DELETE)) {
            setDelDomain(ipValue.asString());
            result = allowDomainDel();
        }
        else if (!finished) {
            if (isAdd) {
                setAllowDomain(ipValue.asString());
                result = allowDomainAdd();
            }
            else {
                result = "Error!";
            }
        }
        return result;

    }
}