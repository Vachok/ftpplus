package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;


/**
 @since 20.09.2019 (12:50) */
class ACLDatabaseSearcher extends ACLParser {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ACLParser.class.getSimpleName());
    
    private int linesLimit = Integer.MAX_VALUE;
    
    private List<String> searchPatterns = new ArrayList<>();
    
    private String sql;
    
    private String searchPattern;
    
    private int countTotalLines;
    
    private @NotNull String getParsedResult() {
        int patternMapSize = foundPatternMap();
        String patternsToSearch = MessageFormat
                .format("{0}. Lines = {1}/{2}", new TForms().fromArray(this.searchPatterns).replaceAll("\n", " | "), patternMapSize, countTotalLines);
        String retMap = new TForms().fromArray(ACLParser.getMapRights()).replaceAll("\\Q : \\E", "\n");
        String retStr = patternsToSearch + "\n" + retMap;
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".txt", retStr.replaceAll(", ", "\n").replaceAll("\\Q]]\\E", "\n"));
    }
    
    List<String> getSearchPatterns() {
        return searchPatterns;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        if (classOption instanceof List) {
            this.searchPatterns = (List<String>) classOption;
        }
        else if (classOption instanceof Integer) {
            this.linesLimit = Integer.parseInt(classOption.toString());
        }
    }
    
    @Override
    public String getResult() {
        if (readAllACLWithSearchPatternFromDB()) {
            return new TForms().fromArray(ACLParser.getMapRights().keySet()).replaceFirst("\\Q.\\E", MessageFormat.format("{0} folders found\nQuery: {1}", ACLParser.getMapRights()
                    .size(), ACLParser.getMapRights().get(Paths.get(".").getFileName())));
        }
        else {
            return getParsedResult();
        }
        
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ACLDatabaseSearcher{");
        sb.append("sql='").append(sql).append('\'');
        sb.append(", searchPatterns=").append(searchPatterns);
        sb.append(", searchPattern='").append(searchPattern).append('\'');
        sb.append(", linesLimit=").append(linesLimit);
        sb.append(", countTotalLines=").append(countTotalLines);
        sb.append('}');
        return sb.toString();
    }
    
    private void rsNext(@NotNull ResultSet resultSet) throws SQLException {
        Path path = Paths.get(resultSet.getString("dir"));
        String owner = resultSet.getString("user");
        String acl = resultSet.getString(ModelAttributeNames.USERS).replaceAll("\\Q[\\E", "").replaceAll("\\Q]\\E", "");
        List<String> value = new ArrayList<>();
        value.add(owner);
        value.addAll(Arrays.asList(acl.replaceFirst("\\Q:\\E", " ").split("\\Q, \\E")));
        ACLParser.getMapRights().put(path, value);
    }
    
    private void parseResult() {
        if (searchPattern.toLowerCase().contains("srv-fs")) {
            readRightsFromConcreteFolder();
        }
        else {
            try {
                messageToUser.info(this.getClass().getSimpleName(), "parseResult->dbSearch: ", sql);
                dbSearch();
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage() + " see line: 168 ***");
            }
        }
    }
    
    private int foundPatternMap() {
        
        if (searchPatterns.size() <= 0) {
            throw new InvokeIllegalException("Nothing to search! Set List of patterns via setInfo()");
        }
        if (!readAllACLWithSearchPatternFromDB()) {
            readAllACLWithSearchPatternFromFile();
        }
        return getRightsListFromFile().size();
    }
    
    private void dbSearch() throws SQLException {
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(ConstantsFor.STR_VELKOM + ConstantsFor.SQLTABLE_POINTCOMMON);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ACLParser.getMapRights().put(Paths.get(searchPattern).getFileName(), Collections.singletonList(sql));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    rsNext(resultSet);
                }
            }
        }
    }
    
    /**
     @return map with path and ACLs
     
     @see ACLParserTest#testReadAllACLWithSearchPatternFromDB()
     */
    private boolean readAllACLWithSearchPatternFromDB() {
        for (String pattern : searchPatterns) {
            try {
                if (searchPatterns.size() == 0 || searchPatterns.get(0).equals("*")) {
                    this.sql = "select * from common limit ";
                    this.searchPattern = pattern;
                    dbSearch();
                }
                else {
                    this.sql = String.format("select * from common where user like '%%%s%%'", pattern);
                    this.searchPattern = pattern;
                    sql = String.format("%s limit %d", sql, linesLimit);
                    parseResult();
                }
            }
            catch (SQLException e) {
                messageToUser.error("ACLParser", "readAllACLWithSearchPatternFromDB", e.getMessage() + " see line: 148");
            }
        }
        return ACLParser.getMapRights().size() > 0;
    }
}
