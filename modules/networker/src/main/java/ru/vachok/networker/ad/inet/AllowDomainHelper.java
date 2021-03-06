package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.net.ssh.PfLists;
import ru.vachok.networker.net.ssh.PfListsSrv;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.text.MessageFormat;


/**
 @see AllowDomainHelperTest
 @since 01.03.2020 (17:33) */
public class AllowDomainHelper extends SshActs implements RestApiHelper {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AllowDomainHelper.class.getSimpleName());

    private JsonObject jsonObject;

    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        messageToUser.info(AbstractForms.fromArray(jsonObject));
        String result;
        this.jsonObject = jsonObject;
        String authStr = this.jsonObject.getString(ConstantsFor.AUTHORIZATION, "");
        try {
            if (checkValidUID(authStr) & checkCodeVersion(jsonObject)) {
                result = makeActions();
            }
            else {
                result = MessageFormat.format("Bad AUTH for {0}, code: {1}", authStr, checkCodeVersion(jsonObject));
            }
        }
        catch (InvokeIllegalException e) {
            result = e.getMessage();
        }
        return result;
    }

    private String makeActions() throws InvokeIllegalException {
        String result = "Domain is exists";
        boolean finished = false;
        JsonValue ipValue = jsonObject.get(ConstantsFor.DOMAIN);
        JsonValue optValue = jsonObject.get(ConstantsFor.OPTION);
        boolean isAdd = optValue.toString().contains("add");
        ConfigurableListableBeanFactory context = IntoApplication.getBeansFactory();
        PfListsSrv bean = new PfListsSrv((PfLists) context.getBean(ConstantsFor.BEANNAME_PFLISTS));
        bean.setCommandForNatStr(ConstantsFor.SSHCOM_GETALLOWDOMAINS);
        String allowDomains = bean.runCom();
        try {
            messageToUser.info(allowDomains);
            if (allowDomains.contains("." + ipValue.asString().split("://")[1].split("/")[0])) {
                finished = true;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            result = e.getMessage() + " " + getClass().getSimpleName() + ".makeActions";
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

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}