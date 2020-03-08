package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
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

    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        JsonValue ipValue = jsonObject.get(ConstantsFor.DOMAIN);
        JsonValue optValue = jsonObject.get(ConstantsFor.OPTION);
        boolean isAdd = optValue.toString().contains("add");
        PfListsSrv bean = new PfListsSrv((PfLists) IntoApplication.getConfigurableApplicationContext().getBean(ConstantsFor.BEANNAME_PFLISTS));
        bean.setCommandForNatStr(ConstantsFor.SSHCOM_GETALLOWDOMAINS);
        String allowDomains = bean.runCom();
        try {
            messageToUser.info(allowDomains);
            if (allowDomains.contains("." + ipValue.asString().split("://")[1].split("/")[0])) {
                return "Domain is exists";
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            messageToUser.error("AllowDomainHelper.getResult", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        if (isAdd) {
            setAllowDomain(ipValue.asString());
            return allowDomainAdd();
        }
        else if (optValue.toString().contains(ConstantsFor.DELETE)) {
            setDelDomain(ipValue.asString());
            return allowDomainDel();
        }
        else {
            return "Error!";
        }
    }
}