package ru.vachok.networker.restapi;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.ad.common.Cleaner;
import ru.vachok.networker.ad.common.OldBigFilesInfoCollector;
import ru.vachok.networker.ad.inet.TempInetRestControllerHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.net.ssh.VpnHelper;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.StringJoiner;
import java.util.concurrent.Callable;


@RestController("RestCTRLPost")
public class RestCTRLPost {


    private final Callable<String> domainGetter = new SSHFactory.Builder(SshActs.whatSrvNeed(), ConstantsFor.SSH_COM_CATALLOWDOMAIN, this.getClass().getSimpleName())
        .build();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RestCTRLPost.class.getSimpleName());

    private static final String GETOLDFILES = "/getoldfiles";

    private static final String INCORRECT_REQUEST = "Incorrect request";

    @PostMapping(GETOLDFILES)
    public String delOldFiles(HttpServletRequest request) {
        ConfigurableListableBeanFactory context = IntoApplication.getBeansFactory();
        Cleaner cleaner = (Cleaner) context.getBean(Cleaner.class.getSimpleName());
        AppConfigurationLocal.getInstance().execute(cleaner);
        return ((OldBigFilesInfoCollector) context.getBean(OldBigFilesInfoCollector.class.getSimpleName())).getFromDatabase();
    }

    @PostMapping(ConstantsFor.SSHADD)
    public String helpDomain(@NotNull HttpServletRequest request, HttpServletResponse response) {
        String result;
        String retStr;
        if (request.getContentType() == null) {
            result = "No content type. What are you mean?";
        }
        else if (request.getContentType().equals(ConstantsFor.APPLICATION_JSON)) {
            JsonObject jsonO = getJSON(readRequestBytes(request));
            jsonO.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
            retStr = RestApiHelper.getInstance(RestApiHelper.DOMAIN).getResult(jsonO);
            result = MessageFormat.format("{0}\n{1}", retStr, AppConfigurationLocal.getInstance().submitAsString(domainGetter, 3));
        }
        else {
            result = INCORRECT_REQUEST;
        }
        return result;
    }

    private JsonObject getJSON(byte[] contentBytes) {
        try {
            return Json.parse(new String(contentBytes)).asObject();
        }
        catch (ParseException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(ConstantsFor.STR_ERROR, AbstractForms.fromArray(e));
            return jsonObject;
        }
    }

    @NotNull
    private byte[] readRequestBytes(@NotNull HttpServletRequest request) {
        byte[] contentBytes = new byte[request.getContentLength()];
        try (ServletInputStream inputStream = request.getInputStream()) {
            int iRead = inputStream.available();
            while (iRead > 0) {
                iRead = inputStream.read(contentBytes);
            }
        }
        catch (IOException e) {
            JsonObject jsonObject = new JsonObject();
            String errStr = "RestCTRL.readRequestBytes\n" + e.getMessage() + "\n***STACK***\n" + AbstractForms.networkerTrace(e.getStackTrace());
            jsonObject.add(PropertiesNames.ERROR, errStr);
            return jsonObject.toString().getBytes();
        }
        return contentBytes;
    }

    @PostMapping("/tempnet")
    public String inetTemporary(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        RestApiHelper tempInetRestControllerHelper = RestApiHelper.getInstance(TempInetRestControllerHelper.class.getSimpleName());
        String contentType = request.getContentType(); //application/json
        JsonObject jsonObject = new JsonObject();
        if (contentType == null || !contentType.equals(ConstantsFor.APPLICATION_JSON)) {
            jsonObject.add(PropertiesNames.ERROR, INCORRECT_REQUEST + "\nContent type is illegal");
        }
        else {
            jsonObject = getJSON(readRequestBytes(request));
            jsonObject.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
        }
        return tempInetRestControllerHelper.getResult(jsonObject);
    }

    @PostMapping("/sshcommandexec")
    public String sshCommandExecute(HttpServletRequest request) {
        String result = getClass().getSimpleName();
        JsonObject jsonO;
        try {
            jsonO = getJSON(readRequestBytes(request));
            if (!jsonO.names().contains(ConstantsFor.AUTHORIZATION)) {
                jsonO.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
                result = RestApiHelper.getInstance(RestApiHelper.SSH).getResult(jsonO);
            }
        }
        catch (UnsupportedOperationException | NegativeArraySizeException e) {
            result = MessageFormat
                .format("RestCTRLPost.sshCommandExecute\n{0}: {1}\n{2}", e.getClass().getSimpleName(), e.getMessage(), AbstractForms
                    .networkerTrace(e.getStackTrace()));
        }
        return result;
    }

    @GetMapping(GETOLDFILES)
    public String collectOldFiles() {
        ConfigurableListableBeanFactory context = IntoApplication.getBeansFactory();
        OldBigFilesInfoCollector oldBigFilesInfoCollector = (OldBigFilesInfoCollector) context
            .getBean(OldBigFilesInfoCollector.class.getSimpleName());
        AppConfigurationLocal.getInstance().execute(oldBigFilesInfoCollector);
        return oldBigFilesInfoCollector.getFromDatabase();
    }

    @PostMapping("/sshdel")
    public String delDomain(HttpServletRequest request) {
        String retStr;
        if (request.getContentType() != null && request.getContentType().equals(ConstantsFor.APPLICATION_JSON)) {
            JsonObject jsonO = getJSON(readRequestBytes(request));
            jsonO.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
            retStr = RestApiHelper.getInstance(RestApiHelper.DOMAIN).getResult(jsonO);
        }
        else {
            return INCORRECT_REQUEST;
        }
        return MessageFormat.format("{0}\n{1}", retStr, AppConfigurationLocal.getInstance().submitAsString(domainGetter, 3));
    }

    @GetMapping("/getvpnkey")
    public String getVPNKey(HttpServletRequest request) {
        if (request.getQueryString() == null || request.getQueryString().isEmpty()) {
            return "getvpnkey error: No argument!";
        }
        else {
            VpnHelper vpnHelper = new VpnHelper();
            return vpnHelper.getConfig(request.getQueryString());
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", RestCTRLPost.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}