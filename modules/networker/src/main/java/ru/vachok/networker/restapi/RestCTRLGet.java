package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.ad.common.OldBigFilesInfoCollector;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.ssh.PfListsSrv;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static ru.vachok.networker.restapi.RestCTRLPost.GETOLDFILES;


/**
 @see RestCTRLGetTest
 @since 03.05.2020 (10:20) */
@RestController("RestCTRLGet")
public class RestCTRLGet {


    private static final String OKHTTP = "okhttp";

    private static final String SUDO_CAT_ETC_PF = "sudo cat /etc/pf/";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RestCTRLGet.class.getSimpleName());

    private static final String GLOBAL_STATUS = "SELECT * FROM `information_schema`.`GLOBAL_STATUS` WHERE `VARIABLE_VALUE`>0 ORDER BY `VARIABLE_NAME`;";

    @GetMapping("/sshgetdomains")
    public String getAllowDomains() {
        ConfigurableListableBeanFactory context = IntoApplication.getBeansFactory();
        PfListsSrv bean = (PfListsSrv) context.getBean(ConstantsFor.BEANNAME_PFLISTSSRV);
        bean.setCommandForNatStr(ConstantsFor.SSHCOM_GETALLOWDOMAINS);
        return bean.runCom();
    }

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

    @GetMapping("/status")
    public String appStatus() {
        Executors.newSingleThreadExecutor().execute(this::printDiskInfoToFile);
        String statusVpn = FileSystemWorker.readFile(FileNames.OPENVPN_STATUS);
        String informationSys = UsefulUtilities.getRunningInformation();
        File fileDFInetstat = new File(FileNames.DFINETSTAT);
        String sshAns = fileDFInetstat.getAbsolutePath();
        if (fileDFInetstat.exists()) {
            sshAns = new Date(fileDFInetstat.lastModified()).toString() + "\n" + FileSystemWorker.readFile(fileDFInetstat);
        }
        return String.join("\n\n\n", statusVpn, informationSys, sshAns);
    }

    public static JsonArray getSSHListsResult() {
        JsonArray retArr = new JsonArray();
        AppConfigurationLocal.getInstance().execute(()->{
            ((PfListsSrv) IntoApplication.getBeansFactory().getBean(ConstantsFor.BEANNAME_PFLISTSSRV)).makeListRunner();
        });
        for (String sshCommand : ConstantsFor.SSH_LIST_COMMANDS) {
            JsonObject jsonElements = new JsonObject();
            SSHFactory.Builder sshB = new SSHFactory.Builder(SshActs.whatSrvNeed(), sshCommand, RestCTRLGet.class.getSimpleName());
            String objName = sshCommand.split(";")[0].replace(SUDO_CAT_ETC_PF, "");
            objName = findObjName(objName);
            jsonElements.add(objName, genJSON(sshB));
            retArr.add(jsonElements);
        }
        return retArr;
    }

    @GetMapping("/db")
    public String dbInfoRest() {

        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER);
             PreparedStatement preparedStatement = connection.prepareStatement(GLOBAL_STATUS);
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

    @GetMapping(GETOLDFILES)
    public String collectOldFiles() {
        ConfigurableListableBeanFactory context = IntoApplication.getBeansFactory();
        OldBigFilesInfoCollector oldBigFilesInfoCollector = (OldBigFilesInfoCollector) context
            .getBean(OldBigFilesInfoCollector.class.getSimpleName());
        AppConfigurationLocal.getInstance().execute(oldBigFilesInfoCollector);
        return oldBigFilesInfoCollector.getFromDatabase();
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

    @NotNull
    private String getFileShow(String userAgent) {
        @NotNull String result;
        JsonObject jsonObject = new JsonObject();
        StringBuilder stringBuilder = new StringBuilder();
        long totalSize = 0;
        File file = Paths.get(".").toAbsolutePath().normalize().toFile();
        if (file.listFiles() == null) {
            throw new IllegalArgumentException(file.getAbsolutePath());
        }
        else {
            String uAgent = checkAgent(userAgent);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-kkmm");
            jsonObject.add(file.getAbsolutePath(), Objects.requireNonNull(file.listFiles()).length);
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                long fileSizeKB = listFile.length() / 1024;
                totalSize = totalSize + fileSizeKB;
                stringBuilder.append(listFile.getName()).append(" size=").append(fileSizeKB).append(" kb;");
                jsonObject.add(listFile.getName(), MessageFormat.format("{0} kbytes {1}", fileSizeKB, simpleDateFormat.format(new Date(listFile.lastModified()))));
                stringBuilder.append("<br>");
            }
            stringBuilder.append("\n\n").append(ConstantsFor.TOTALSIZE).append(totalSize).append(" kbytes\n");
            jsonObject.add(ConstantsFor.TOTALSIZE, totalSize);
            if (uAgent.contains(OKHTTP) || uAgent.toLowerCase().contains("android")) {
                result = jsonObject.toString();
            }
            else {
                result = stringBuilder.toString();
            }
        }
        return result;
    }

    private String checkAgent(String userAgent) {
        String uAgent;
        try {
            uAgent = userAgent.toLowerCase();
        }
        catch (RuntimeException e) {
            uAgent = MessageFormat.format("{0}\n {1}", e.getMessage(), AbstractForms.fromArray(e));
        }
        return uAgent;
    }

    @GetMapping("/getsshlists")
    public String sshRest(HttpServletRequest request) {
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

    @SuppressWarnings("IfStatementWithTooManyBranches")
    private static String findObjName(String objName) {
        if (objName.toLowerCase().contains("full")) {
            objName = ConstantsFor.JSON_OBJECT_FULL_SQUID;
        }
        else if (objName.contentEquals(ConstantsFor.JSON_OBJECT_SQUID)) {
            objName = ConstantsFor.JSON_OBJECT_STD_SQUID;
        }
        else if (objName.toLowerCase().contains("lim")) {
            objName = ConstantsFor.JSON_LIST_LIMITSQUID;
        }
        else if (objName.contains("24")) {
            objName = ConstantsFor.JSON_LIST_24HRS;
        }
        else if (objName.contains("ps ax")) {
            objName = ConstantsFor.JSON_OBJECT_RULES;
        }
        else if (objName.contains("uname")) {
            objName = ConstantsFor.JSON_OBJECT_NAT;
        }
        return objName;
    }

    private static JsonObject genJSON(SSHFactory.Builder sshB) {
        String srvAnswer = AppConfigurationLocal.getInstance().submitAsString(sshB.build(), 8);
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

    private void printDiskInfoToFile() {
        SSHFactory.Builder sshFactoryB = new SSHFactory.Builder(OtherKnownDevices.SRV_INETSTAT, "df -h;exit", UsefulUtilities.class.getSimpleName());
        FileSystemWorker.writeFile(FileNames.DFINETSTAT, AppConfigurationLocal.getInstance().submitAsString(sshFactoryB.build(), 21));
    }

    @GetMapping("/props")
    public String showAppProps() {
        ((FilePropsLocal) InitProperties.getInstance(InitProperties.FILE)).reloadPropsFromDB();
        Properties props = InitProperties.getTheProps();
        Preferences pref = InitProperties.getUserPref();
        List<String> propsPref = new ArrayList<>();
        props.forEach((k, v)->propsPref.add("1_props_" + k + ":" + v));
        try {
            for (String key : pref.keys()) {
                propsPref.add("2_pref_" + key + ":" + pref.get(key, "no value"));
            }
        }
        catch (BackingStoreException e) {
            messageToUser.warn(RestCTRLGet.class.getSimpleName(), e.getMessage(), " see line: 359 ***");
        }
        Collections.sort(propsPref);
        return AbstractForms.fromArray(propsPref);
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", RestCTRLGet.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}