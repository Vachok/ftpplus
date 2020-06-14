package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


/**
 @see TempInetRestControllerHelperTest
 @since 24.02.2020 (11:12) */
public class TempInetRestControllerHelper extends TemporaryFullInternet implements RestApiHelper {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TempInetRestControllerHelper.class.getSimpleName());

    @NotNull
    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        String retStr = jsonObject.toString();
        String inputIP = jsonObject.get("ip").asString();
        String hourAsString = jsonObject.get("hour").asString();
        String option = jsonObject.get(ConstantsFor.OPTION).asString();
        long hoursToOpenInet = 0;
        if (hourAsString != null) {
            hoursToOpenInet = Long.parseLong(hourAsString);
        }
        else if (hoursToOpenInet > TimeUnit.DAYS.toHours(365)) {
            hoursToOpenInet = TimeUnit.DAYS.toHours(365);
        }
        String whocalls = jsonObject.get(ConstantsFor.WHOCALLS).asString();
        String[] params = {inputIP, String.valueOf(hoursToOpenInet), option, whocalls};
        String tempInetResult = getAnswer(params);
        return MessageFormat.format("{0}\n{1}", retStr, tempInetResult);
    }

    private String getAnswer(@NotNull String... params) {
        return AppConfigurationLocal.getInstance()
            .submitAsString(new TemporaryFullInternet(params[0], Long.parseLong(params[1]), params[2], params[3]), ConstantsFor.SSH_TIMEOUT);
    }
}