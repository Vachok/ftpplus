package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.ad.inet.TempInetRestControllerHelper
 <p>

 @since 24.02.2020 (11:12) */
public class TempInetRestControllerHelper extends TemporaryFullInternet {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TempInetRestControllerHelper.class.getSimpleName());

    @NotNull
    public String getInetResult(@NotNull JsonObject jsonObject) {
        String retStr = jsonObject.toString();
        String inputIP = jsonObject.get("ip").asString();
        String hourAsString = jsonObject.get("hour").asString();
        long hoursToOpenInet = 0;
        if (hourAsString != null) {
            hoursToOpenInet = Long.parseLong(hourAsString);
        }
        else if (hoursToOpenInet > TimeUnit.DAYS.toHours(365)) {
            hoursToOpenInet = TimeUnit.DAYS.toHours(365);
        }
        String option = jsonObject.get(ConstantsFor.OPTION).asString();
        String whocalls = jsonObject.get(ConstantsFor.WHOCALLS).asString();

        String[] params = {inputIP, String.valueOf(hoursToOpenInet), whocalls};

        if (hoursToOpenInet == -2) {
            option = ConstantsFor.DOMAIN;
            params = new String[]{inputIP, whocalls};
        }
        String tempInetResult = getAnswer(option, params);

        return MessageFormat.format("{0}\n{1}", retStr, tempInetResult);
    }

    private String getAnswer(@NotNull String option, String... params) {
        if (ConstantsFor.DOMAIN.equals(option)) {
            String s = new SshActs(params[0], params[1]).allowDomainAdd();
            FirebaseDatabase.getInstance().getReference(params[0]).setValue(params[1], new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    messageToUser.info(getClass().getSimpleName(), ConstantsFor.TEMPNET, ref.getKey());
                    messageToUser.error("RestCTRL.onComplete", error.toException().getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace()));
                }
            });
            return s; //{"ip":"delete","option":"domain","whocalls":"http://www.velkomfood.ru"}
        }
        return new TemporaryFullInternet(params[0], Long.parseLong(params[1]), "add", params[2]).call();
    }

}