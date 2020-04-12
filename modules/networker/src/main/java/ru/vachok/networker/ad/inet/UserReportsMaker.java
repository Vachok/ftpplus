package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


/**
 @see UserReportsMakerTest
 @since 14.10.2019 (11:40) */
public class UserReportsMaker extends InternetUse {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserReportsMaker.class.getSimpleName());

    private String userCred;

    UserReportsMaker(String type) {
        this.userCred = type;
    }

    /**
     @param fileName имя csv-файла
     @return {@link java.net.URI}

     @see UserReportsMakerTest#testGetInfoAbout()
     */
    @Override
    public String getInfoAbout(String fileName) {
        Map<Date, String> dateStringMap = getMapUsage();
        File outFile = new File(fileName);
        delOldFile(outFile);
        Set<String> uniqSites = new TreeSet<>();
        List<String> sitesAll = new ArrayList<>();
        for (Map.Entry<Date, String> entry : dateStringMap.entrySet()) {
            JsonObject asJson = toJSON(entry);
            String site = String.valueOf(asJson.get("site"));
            if (!site.contains(".")) {
                site = "";
            }
            uniqSites.add(site);
            sitesAll.add(site);
        }
        for (String site : uniqSites) {
            FileSystemWorker.appendObjectToFile(new File(fileName), site + "," + Collections.frequency(sitesAll, site));
        }

        try {
            return outFile.toPath().toAbsolutePath().toUri().toURL().toString();
        }
        catch (MalformedURLException e) {
            return e.getMessage();
        }
    }

    private @NotNull Map<Date, String> getMapUsage() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        Map<Date, String> timeSite = new TreeMap<>();
        this.userCred = resolveTableName();
        final String sql = createDBQuery();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_INETSTATS + userCred);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.first()) {
                timeSite.put(new Date(resultSet.getLong(ConstantsFor.DBCOL_STAMP)), "Start.log");

            }
            while (resultSet.next()) {
                timeSite.put(new Date(resultSet.getLong(ConstantsFor.DBCOL_STAMP)), MessageFormat
                        .format("{0} bytes: {1}", resultSet.getString("site"), resultSet.getInt(ConstantsFor.DBCOL_BYTES)));
            }
            if (resultSet.last()) {
                timeSite.put(new Date(resultSet.getLong(ConstantsFor.DBCOL_STAMP)), "Stop.log");
            }
        }
        catch (SQLException | RuntimeException e) {
            messageToUser.error("UserReportsMaker", "getMapUsage", e.getMessage() + " see line: 62");
            timeSite.put(new Date(), e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        messageToUser.info(this.getClass().getSimpleName(), "Returning MAP: ", timeSite.size() + " records");
        return timeSite;
    }

    private void delOldFile(@NotNull File outFile) {
        outFile.deleteOnExit();
        try {
            Files.deleteIfExists(outFile.toPath());
        }
        catch (IOException e) {
            messageToUser.error("UserReportsMaker", "delOldFile", e.getMessage() + " see line: 79");
        }
    }

    private String resolveTableName() {
        if (userCred.contains(".")) {
            return userCred.replaceAll("\\Q.\\E", "_");
        }
        else {
            return userCred;
        }
    }

    private @NotNull JsonObject toJSON(@NotNull Map.Entry<Date, String> entry) {
        JsonObject inetUse = new JsonObject();
        String localDateStr = String.valueOf(LocalDateTime.ofEpochSecond(entry.getKey().getTime() / 1000, 0, ZoneOffset.ofHours(3)).toLocalDate());
        inetUse.add(ConstantsFor.DBCOL_STAMP, localDateStr);
        String[] valSplit = new String[2];
        try {
            valSplit = entry.getValue().split(" bytes: ");
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //15.10.2019 (11:32)
        }
        inetUse.add("site", parseDomainName(valSplit[0]));
        try {
            inetUse.add(ConstantsFor.DBCOL_BYTES, valSplit[1]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            inetUse.add(ConstantsFor.DBCOL_BYTES, "42");
            inetUse.add("site", MessageFormat.format("!{0}.{1}", localDateStr, valSplit[0]));
        }
        return inetUse;
    }

    @Contract(pure = true)
    private @NotNull String createDBQuery() {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_INETSTATS + userCred);
             PreparedStatement preparedStatement = connection.prepareStatement(String.format("DELETE FROM %s WHERE stamp = 1", userCred))) {
            preparedStatement.executeUpdate();
        }
        catch (SQLException | RuntimeException e) {
            messageToUser.error("UserReportsMaker", "createDBQuery", e.getMessage() + " see line: 156");
        }
        return "SELECT * FROM inetstats." + userCred + " WHERE squidans NOT IN ('TCP_DENIED/403') ORDER BY stamp;";
    }

    private @NotNull String parseDomainName(@NotNull String unparsedDomain) {
        unparsedDomain = unparsedDomain.replace(ConstantsFor.HTTPS, "http://").replace("http://", "");
        unparsedDomain = unparsedDomain.split("/")[0];
        if (unparsedDomain.contains(":")) {
            unparsedDomain = unparsedDomain.split(":")[0];
        }
        return unparsedDomain;
    }

    @Override
    public void setClassOption(@NotNull Object option) {
        this.userCred = (String) option;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserReportsMaker{");
        sb.append("userCred='").append(userCred).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getInfo() {
        return AbstractForms.fromArray(getMapUsage());
    }

    private String parseJSONObj(@NotNull List<JsonObject> jsonS) {
        return String.valueOf(jsonS.get(0).get(ConstantsFor.DBCOL_STAMP));
    }
}
