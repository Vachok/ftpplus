package ru.vachok.networker.restapi;


import com.eclipsesource.json.*;
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
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.ssh.PfLists;
import ru.vachok.networker.net.ssh.PfListsSrv;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.net.ssh.VpnHelper;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;

import static ru.vachok.networker.data.enums.ConstantsFor.BEANNAME_PFLISTS;


/**
 @see RestCTRLTest
 @since 15.12.2019 (19:42) */
@SuppressWarnings("unused")
@RestController("RestCTRL")
public class RestCTRL {


    public static final String JSON_OBJECT_LIMIT_SQUID = "limitSquid";

    private static final String OKHTTP = "okhttp";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RestCTRL.class.getSimpleName());

    private static final String GETOLDFILES = "/getoldfiles";

    private static final String SUDO_CAT_ETC_PF = "sudo cat /etc/pf/";

    /**
     @return no formatting pc name

     @see RestCTRLTest#testUniqPC()
     */
    @GetMapping("/pc")
    public String uniqPC(@NotNull HttpServletRequest request) {
        String result;
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.REST_PC_UNIQ);
        if (request.getQueryString() != null) {
            String queryStr = request.getQueryString();
            String infoAbout = informationFactory.getInfoAbout(queryStr);
            try {
                infoAbout = infoAbout.split("><")[1].split(">")[1].split("</")[0];
            }
            catch (IndexOutOfBoundsException e) {
                infoAbout = e.getMessage() + "\n" + infoAbout;
            }
            if (infoAbout.contains("Online =")) {
                infoAbout = clearStr(infoAbout);
            }
            infoAbout = infoAbout + " Realtime resolve: " + new NameOrIPChecker(queryStr).resolveInetAddress();
            return infoAbout + "\n\nQuery: " + request.getQueryString();
        }
        else {
            informationFactory.setClassOption(true);
            return informationFactory.getInfo();
        }
    }

    private String clearStr(String about) {
        try {
            return about.split("font color=")[1].split(">")[1].split("</")[0];
        }
        catch (IndexOutOfBoundsException e) {
            return about;
        }
    }

    @GetMapping("/db")
    public String dbInfoRest() {
        String sql = "SELECT * FROM `information_schema`.`GLOBAL_STATUS` WHERE `VARIABLE_VALUE`>0 ORDER BY `VARIABLE_NAME`;";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            Map<String, String> showMap = new TreeMap<>();
            while (resultSet.next()) {
                showMap.put(resultSet.getString("VARIABLE_NAME"), resultSet.getString("VARIABLE_VALUE"));
            }
            return AbstractForms.fromArray(showMap);
        }
        catch (SQLException e) {
            return e.getMessage() + " \n<br>\n" + AbstractForms.fromArray(e);
        }
    }

    @GetMapping("/file")
    public String fileShow(@NotNull HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String filesShow = userAgent;
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            File toShow = new File(request.getQueryString());
            if (toShow.exists()) {
                filesShow = FileSystemWorker.readFile(toShow);
            }
        }
        else {

            filesShow = getFileShow(userAgent);
        }

        String uAgent;
        try {
            uAgent = userAgent.toLowerCase();
        }
        catch (RuntimeException e) {
            uAgent = MessageFormat.format("{0} \n {1}", e.getMessage(), AbstractForms.fromArray(e));
        }
        if (uAgent.contains(OKHTTP)) {
            MessageToUser.getInstance(MessageToUser.EMAIL, "Get /file from " + UsefulUtilities.thisPC()).info(filesShow);
        }
        return filesShow;
    }

    @GetMapping("/sshgetdomains")
    public String getAllowDomains() {
        ConfigurableListableBeanFactory context = IntoApplication.getBeansFactory();
        PfListsSrv bean = (PfListsSrv) context.getBean(ConstantsFor.BEANNAME_PFLISTSSRV);
        bean.setCommandForNatStr(ConstantsFor.SSHCOM_GETALLOWDOMAINS);
        return bean.runCom();
    }

    /**
     @return статус приложения

     @see RestCTRLTest#testAppStatus()
     */
    @GetMapping("/status")
    public String appStatus() {
        String statusVpn = new VpnHelper().getStatus();
        String informationSys = UsefulUtilities.getRunningInformation();
        String sshAns = connectToSrvInetstat();
        return String.join("\n\n\n", statusVpn, informationSys, sshAns);
    }

    /**
     @see RestCTRLTest
     */
    @GetMapping("/getsshlists")
    public String sshRest() {
        StringBuilder sshAns = new StringBuilder();
        JsonArray resultArr = getSSHListsResult();
        JsonObject sshParamNames = new JsonObject();
        sshAns.append("\n\n\n");
        sshAns.append(resultArr.size()).append(" size of ").append(JsonArray.class.getCanonicalName()).append("\n");
        for (JsonValue jsonValue : resultArr.values()) {
            Object[] objNames = jsonValue.asObject().names().toArray();
            for (Object name : objNames) {
                sshAns.append(name.toString()).append(":");
                sshAns.append(jsonValue.asObject().get(name.toString()));
                sshParamNames.add(name.toString(), "toString:getString");
            }
        }
        sshAns.append("\n\n\n");
        FileSystemWorker.writeFile(FileNames.SSH_LISTS_LOG, MessageFormat.format("Json objects names in array: {0}\n\n{1}", sshParamNames, sshAns.toString()));
        MessageToUser.getInstance(MessageToUser.FILE, getClass().getSimpleName()).info(sshAns.toString());
        return resultArr.toString();
    }

    @PostMapping(GETOLDFILES)
    public String delOldFiles(HttpServletRequest request) {
        ConfigurableListableBeanFactory context = IntoApplication.getBeansFactory();
        Cleaner cleaner = (Cleaner) context.getBean(Cleaner.class.getSimpleName());
        AppConfigurationLocal.getInstance().execute(cleaner);
        return ((OldBigFilesInfoCollector) context.getBean(OldBigFilesInfoCollector.class.getSimpleName())).getFromDatabase();
    }

    /**
     @see RestCTRLTest#addDomainRESTTest()
     */
    @PostMapping(ConstantsFor.SSHADD)
    public String helpDomain(@NotNull HttpServletRequest request, HttpServletResponse response) {
        String retStr = "";
        if (request.getContentType().equals(ConstantsFor.JSON)) {
            JsonObject jsonO = getJSON(readRequestBytes(request));
            jsonO.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
            retStr = RestApiHelper.getInstance(RestApiHelper.DOMAIN).getResult(jsonO);
        }
        return retStr + "\n" + getAllowDomains();
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
            messageToUser.error("RestCTRL.readRequestBytes", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return contentBytes;
    }

    private JsonArray getSSHListsResult() {
        JsonArray retArr = new JsonArray();
        PfLists pfLists = (PfLists) IntoApplication.getBeansFactory().getBean(BEANNAME_PFLISTS);
        AppConfigurationLocal.getInstance().execute(()->{
            ((PfListsSrv) IntoApplication.getBeansFactory().getBean(ConstantsFor.BEANNAME_PFLISTSSRV)).makeListRunner();
        });

        for (String sshCommand : ConstantsFor.SSH_LIST_COMMANDS) {
            JsonObject jsonElements = new JsonObject();
            SSHFactory.Builder sshB = new SSHFactory.Builder(SshActs.whatSrvNeed(), sshCommand, this.getClass().getSimpleName());
            String objName = sshCommand.split(";")[0].replace(SUDO_CAT_ETC_PF, "");
            objName = findObjName(objName);
            jsonElements.add(objName, genJSON(sshB));
            retArr.add(jsonElements);
        }
        return retArr;
    }

    private JsonObject genJSON(SSHFactory.Builder sshB) {
        String srvAnswer = AppConfigurationLocal.getInstance().submitAsString(sshB.build(), 5);
        JsonObject jsonObject = new JsonObject();
        if (srvAnswer.contains("<br>")) {
            String[] split = srvAnswer.split("<br>");
            for (String s : split) {
                if (s.contains(" #")) {
                    String name = s.split(" #")[0];
                    jsonObject.add(name.replace("\n", "").replace(";exit", "").trim(), s.replace(name, "").replace("#", " ").trim());
                }
            }
        }
        if (jsonObject.names().size() <= 0) {
            jsonObject.add(sshB.getCommandSSH().replace(SUDO_CAT_ETC_PF, ""), srvAnswer.replace("<br>\n", "\n"));
        }
        return jsonObject;
    }

    /**
     @param request {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @return json

     @see RestCTRLTest#okTest()
     */
    @PostMapping("/tempnet")
    public String inetTemporary(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        RestApiHelper tempInetRestControllerHelper = RestApiHelper.getInstance(TempInetRestControllerHelper.class.getSimpleName());
        String contentType = request.getContentType(); //application/json
        response.setHeader(ConstantsFor.AUTHORIZATION, request.getRemoteHost());
        JsonObject jsonObject = getJSON(readRequestBytes(request));
        jsonObject.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
        return tempInetRestControllerHelper.getResult(jsonObject);
    }

    private String findObjName(String objName) {
        if (objName.toLowerCase().contains("full")) {
            objName = ConstantsFor.JSON_OBJECT_FULL_SQUID;
        }
        else if (objName.contentEquals(ConstantsFor.JSON_OBJECT_SQUID)) {
            objName = ConstantsFor.JSON_OBJECT_STD_SQUID;
        }
        else if (objName.toLowerCase().contains("lim")) {
            objName = JSON_OBJECT_LIMIT_SQUID;
        }
        else if (objName.contains("24")) {
            objName = "24hrs";
        }
        else if (objName.contains("ps ax")) {
            objName = ConstantsFor.JSON_OBJECT_RULES;
        }
        else if (objName.contains("uname")) {
            objName = ConstantsFor.JSON_OBJECT_NAT;
        }
        return objName;
    }

    @NotNull
    private String getFileShow(String userAgent) {
        StringBuilder stringBuilder = new StringBuilder();
        long totalSize = 0;
        File file = Paths.get(".").toAbsolutePath().normalize().toFile();
        if (file.listFiles() == null) {
            throw new IllegalArgumentException(file.getAbsolutePath());
        }
        else {
            stringBuilder.append(Objects.requireNonNull(file.listFiles()).length).append(" total files\n\n");
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                long fileSizeKB = listFile.length() / 1024;
                totalSize = totalSize + fileSizeKB;
                stringBuilder.append(listFile.getName()).append(" size=").append(fileSizeKB).append(" kb;");
                String uAgent;
                try {
                    uAgent = userAgent.toLowerCase();
                }
                catch (RuntimeException e) {
                    uAgent = MessageFormat.format("{0}\n {1}", e.getMessage(), AbstractForms.fromArray(e));
                }
                if (uAgent.contains(OKHTTP)) {
                    stringBuilder.append("\n");
                }
                else {
                    stringBuilder.append("<br>");
                }
            }
            stringBuilder.append("\n\n").append(ConstantsFor.TOTALSIZE).append(totalSize).append(" kbytes\n");
        }
        return stringBuilder.toString();
    }

    private String connectToSrvInetstat() {
        SSHFactory.Builder sshFactoryB = new SSHFactory.Builder(OtherKnownDevices.SRV_INETSTAT, "df -h&uname -a&exit", UsefulUtilities.class.getSimpleName());
        return AppConfigurationLocal.getInstance().submitAsString(sshFactoryB.build(), 10)
            .replace("Filesystem Size Used Avail Capacity Mounted on", "srv-inetstat.eatmeat.ru\n");
    }

    @PostMapping("/sshcommandexec")
    public String sshCommandExecute(HttpServletRequest request) {
        String result;
        try (ServletInputStream stream = request.getInputStream()) {
            JsonObject jsonO = getJSON(readRequestBytes(request));
            if (!jsonO.names().contains(ConstantsFor.AUTHORIZATION)) {
                jsonO.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
            }
            result = RestApiHelper.getInstance(RestApiHelper.SSH).getResult(jsonO);
        }
        catch (IOException e) {
            result = AbstractForms.networkerTrace(e.getStackTrace());
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
        String retStr = "";
        if (request.getContentType().equals(ConstantsFor.JSON)) {
            JsonObject jsonO = getJSON(readRequestBytes(request));
            jsonO.add(ConstantsFor.AUTHORIZATION, request.getHeader(ConstantsFor.AUTHORIZATION));
            retStr = RestApiHelper.getInstance(RestApiHelper.DOMAIN).getResult(jsonO);
        }
        return retStr + "\n" + getAllowDomains();
    }

    @GetMapping("/getvpnkey")
    public String getVPNKey(HttpServletRequest request) {
        if (request.getQueryString() == null && request.getQueryString().isEmpty()) {
            return "getvpnkey error: No argument!";
        }
        else {
            VpnHelper vpnHelper = new VpnHelper();
            return vpnHelper.getConfig(request.getQueryString());
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", RestCTRL.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}