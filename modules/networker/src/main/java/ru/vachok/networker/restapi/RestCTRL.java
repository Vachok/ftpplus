package ru.vachok.networker.restapi;


import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


/**
 @see RestCTRLTest
 @since 15.12.2019 (19:42) */
@RestController
public class RestCTRL {


    @GetMapping("/status")
    public String appStatus() {
        return UsefulUtilities.getRunningInformation();
    }

    @GetMapping("/pc")
    public String uniqPC(HttpServletRequest request) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.REST_PC_UNIQ);
        if (request.getQueryString() != null) {
            return informationFactory.getInfo();
        }
        else {
            informationFactory.setClassOption(true);
            return informationFactory.getInfoAbout("");
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
        if (uAgent.contains(ConstantsFor.OKHTTP)) {
            MessageToUser.getInstance(MessageToUser.EMAIL, "Get /file from " + UsefulUtilities.thisPC()).info(filesShow);
        }
        return filesShow;
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
                if (uAgent.contains(ConstantsFor.OKHTTP)) {
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

}