package ru.vachok.networker.restapi;


import com.eclipsesource.json.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.ad.common.OldBigFilesInfoCollector;
import ru.vachok.networker.ad.inet.TempInetRestControllerHelper;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.ssh.PfLists;
import ru.vachok.networker.net.ssh.PfListsSrv;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see RestCTRLTest
 @since 15.12.2019 (19:42) */
@RestController
public class RestCTRL {


    private static final String OKHTTP = "okhttp";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RestCTRL.class.getSimpleName());

    @GetMapping("/status")
    public String appStatus() {
        return UsefulUtilities.getRunningInformation();
    }

    @GetMapping("/pc")
    public String uniqPC(@NotNull HttpServletRequest request) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.REST_PC_UNIQ);
        if (request.getQueryString() != null) {
            return informationFactory.getInfo();
        }
        else {
            informationFactory.setClassOption(true);
            return informationFactory.getInfoAbout("");
        }
    }

    @NotNull
    private List<String> getFromDB() {
        List<String> validUIDs = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_UIDS_FULL);
             PreparedStatement preparedStatement = connection.prepareStatement("select * from velkom.restuids");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                validUIDs.add(resultSet.getString("uid"));
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        return validUIDs;
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

    /**
     @param request {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @return json

     @see RestCTRLTest#okTest()
     */
    @PostMapping("/tempnet")
    public String inetTemporary(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        RestApiHelper tempInetRestControllerHelper = RestApiHelper.getInstance(TempInetRestControllerHelper.class.getSimpleName());
        boolean ipForInetValid = checkValidUID(request.getHeader(ConstantsFor.AUTHORIZATION));
        String contentType = request.getContentType(); //application/json
        response.setHeader(ConstantsFor.AUTHORIZATION, request.getRemoteHost());
        String retStr;
        if (contentType.equalsIgnoreCase(ConstantsFor.JSON) & ipForInetValid) {
            JsonObject jsonObject = getJSON(readRequestBytes(request));
            retStr = tempInetRestControllerHelper.getResult(jsonObject);
        }
        else {
            retStr = "INVALID USER";
        }
        return retStr;
    }

    private boolean checkValidUID(String headerAuthorization) {
        boolean isValid = false;
        List<String> validUIDs = getFromDB();
        if (validUIDs.size() == 0) {
            FileSystemWorker.readFileToList("uid.txt");
        }
        for (String validUID : validUIDs) {
            if (headerAuthorization.equals(validUID)) {
                messageToUser.info(getClass().getSimpleName(), "checkValidUID", validUID);
                isValid = true;
            }
            else {
                messageToUser.warn(getClass().getSimpleName(), "checkValidUID", validUID + " != " + headerAuthorization);
            }
        }
        return isValid;
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

    /**
     @see RestCTRLTest#addDomainRESTTest()
     */
    @PostMapping(ConstantsFor.SSHADD)
    public String sshCommandAddDomain(@NotNull HttpServletRequest request, HttpServletResponse response) {
        String retStr = "";
        if (checkValidUID(request.getHeader(ConstantsFor.AUTHORIZATION))) {
            if (request.getContentType().equals(ConstantsFor.JSON)) {
                retStr = RestApiHelper.getInstance(RestApiHelper.DOMAIN).getResult(getJSON(readRequestBytes(request)));
                messageToUser.info(getClass().getSimpleName(), ConstantsFor.SSHADD, retStr);
            }

        }
        return retStr + "\n" + getAllowDomains();
    }

    @GetMapping("/getsshlists")
    public String sshRest() {
        ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext();
        PfListsSrv pfService = (PfListsSrv) context.getBean(ConstantsFor.BEANNAME_PFLISTSSRV);
        PfLists pfLists = (PfLists) context.getBean(ConstantsFor.BEANNAME_PFLISTS);
        messageToUser.warn(getClass().getSimpleName(), "sshRest", new Date(pfLists.getTimeStampToNextUpdLong()).toString());
        pfService.makeListRunner();
        return pfLists.toString();
    }
    
    @GetMapping("/sshgetdomains")
    public String getAllowDomains() {
        PfListsSrv bean = (PfListsSrv) IntoApplication.getConfigurableApplicationContext().getBean(ConstantsFor.BEANNAME_PFLISTSSRV);
        bean.setCommandForNatStr(ConstantsFor.SSHCOM_GETALLOWDOMAINS);
        return bean.runCom();
    }
    
    @GetMapping("/getoldfiles")
    public String collectOldFiles() {
        OldBigFilesInfoCollector oldBigFilesInfoCollector = (OldBigFilesInfoCollector) IntoApplication.getConfigurableApplicationContext()
                .getBean(OldBigFilesInfoCollector.class.getSimpleName());
        AppConfigurationLocal.getInstance().execute(oldBigFilesInfoCollector, (int) TimeUnit.HOURS.toSeconds(9));
        return oldBigFilesInfoCollector.getFromDatabase();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", RestCTRL.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
}