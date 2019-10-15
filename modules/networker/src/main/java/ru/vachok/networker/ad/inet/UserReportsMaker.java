package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
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
    
    @Override
    public String getInfoAbout(String fileName) {
        Map<Date, String> dateStringMap = getMapUsage();
        File outFile = new File(fileName);
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
        
        return outFile.toPath().toAbsolutePath().normalize().toString();
    }
    
    private @NotNull Map<Date, String> getMapUsage() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        Map<Date, String> timeSite = new TreeMap<>();
        this.userCred = resolveTableName();
        final String sql = createDBQuery();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_INETSTATS + userCred);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                timeSite.put(new Date(resultSet.getLong(ConstantsFor.DBCOL_STAMP)), MessageFormat
                        .format("{0} bytes: {1}", resultSet.getString("site"), resultSet.getInt(ConstantsFor.DBCOL_BYTES)));
            }
        }
        catch (SQLException e) {
            messageToUser.error("UserReportsMaker", "getMapUsage", e.getMessage() + " see line: 62");
        }
        messageToUser.info(this.getClass().getSimpleName(), "Returning MAP: ", timeSite.size() + " records");
        return timeSite;
    }
    
    private @NotNull JsonObject toJSON(@NotNull Map.Entry<Date, String> entry) {
        JsonObject inetUse = new JsonObject();
        inetUse.add(ConstantsFor.DBCOL_STAMP, String.valueOf(LocalDateTime.ofEpochSecond(entry.getKey().getTime() / 1000, 0, ZoneOffset.ofHours(3)).toLocalDate()));
        String[] valSplit = new String[2];
        try {
            valSplit = entry.getValue().split(" bytes: ");
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //15.10.2019 (11:32)
        }
        inetUse.add("site", parseDomainName(valSplit[0]));
        inetUse.add(ConstantsFor.DBCOL_BYTES, valSplit[1]);
        return inetUse;
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
    public String getInfo() {
        return AbstractForms.fromArray(getMapUsage());
    }
    
    private String parseJSONObj(@NotNull List<JsonObject> jsonS) {
        return String.valueOf(jsonS.get(0).get(ConstantsFor.DBCOL_STAMP));
    }
    
    private String resolveTableName() {
        if (userCred.contains(".")) {
            return userCred.replaceAll("\\Q.\\E", "_");
        }
        else {
            return userCred;
        }
    }
    
    private String createDBQuery() {
        return "SELECT * FROM inetstats." + userCred + " WHERE squidans NOT IN ('TCP_DENIED/403');";
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserReportsMaker{");
        sb.append("userCred='").append(userCred).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
