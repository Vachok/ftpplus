// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 @see ru.vachok.networker.ad.usermanagement.ACLParserTest
 @since 04.07.2019 (9:48) */
class ACLParser extends UserACLManagerImpl {
    
    
    private int linesLimit = Integer.MAX_VALUE;
    
    private int countTotalLines;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
    
    private Map<Path, List<String>> mapRights = new ConcurrentSkipListMap<>();
    
    private List<String> searchPatterns = new ArrayList<>();
    
    private List<String> rightsListFromFile = new ArrayList<>();
    
    public ACLParser() {
        super(Paths.get("."));
    }
    
    public void setLinesLimit(int linesLimit) {
        this.linesLimit = linesLimit;
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
            return new TForms().fromArray(mapRights.keySet());
        }
        else {
            return getParsedResult();
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ACLParser.class.getSimpleName() + "[\n", "\n]")
            .add("linesLimit = " + linesLimit)
            .add("countTotalLines = " + countTotalLines)
            .add("searchPatterns = " + new TForms().fromArray(searchPatterns))
            .toString();
    }
    
    private int foundPatternMap() {
        if (searchPatterns.size() <= 0) {
            throw new InvokeIllegalException("Nothing to search! Set List of patterns via setInfo()");
        }
        if (!readAllACLWithSearchPatternFromDB()) {
            readAllACLWithSearchPatternFromFile();
        }
        return rightsListFromFile.size();
    }
    
    /**
     @return map with path and ACLs
     
     @see ACLParserTest#testReadAllACLWithSearchPatternFromDB()
     */
    protected boolean readAllACLWithSearchPatternFromDB() {
        String sql;
        ExecutorService stealingPool = Executors.newWorkStealingPool(6);
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection(ConstantsFor.STR_VELKOM)) {
            if (searchPatterns.size() == 0 || searchPatterns.get(0).equals("*")) {
                dbSearch(connection, new StringBuilder().append("select * from common limit ").append(linesLimit).toString());
            }
            else {
                for (String pattern : searchPatterns) {
                    sql = String.format("select * from common where user like '%%%s%%'", pattern);
                    sql = String.format("%s limit %d", sql, linesLimit);
                    String finalSql = sql;
                    stealingPool.execute(()->parseResult(finalSql, pattern));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return mapRights.size() > 0;
    }
    
    private void readAllACLWithSearchPatternFromFile() {
        try (InputStream inputStream = new FileInputStream(new File(ConstantsFor.COMMON_DIR + "\\14_ИТ_служба\\Внутренняя\\common.rgh"));
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, ConstantsFor.CP_WINDOWS_1251);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            Queue<String> tempQueue = new LinkedList<>();
            if (searchPatterns.get(0).equals("*")) {
                bufferedReader.lines().limit(linesLimit).forEach(rightsListFromFile::add);
                mapFoldersRights();
            }
            else {
                bufferedReader.lines().limit(linesLimit).forEach(tempQueue::add);
                searchPatterns.forEach(searchPattern->{
                    if (searchPattern.toLowerCase().contains("srv-fs")) {
                        readRightsFromConcreteFolder(searchPattern);
                    }
                    else {
                        searchInQueue(searchPattern, tempQueue);
                    }
                });
            }
            this.countTotalLines = tempQueue.size();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    private void dbSearch(@NotNull Connection connection, String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            mapRights.put(Paths.get("."), Collections.singletonList(sql));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    rsNext(resultSet);
                }
            }
        }
    }
    
    private void parseResult(@NotNull String sql, @NotNull String searchPattern) {
        if (searchPattern.toLowerCase().contains("srv-fs")) {
            readRightsFromConcreteFolder(searchPattern);
        }
        else {
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection(ConstantsFor.STR_VELKOM)) {
                messageToUser.info(this.getClass().getSimpleName(), "parseResult->dbSearch: ", sql);
                dbSearch(connection, sql);
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage() + " see line: 168 ***");
            }
        }
    }
    
    private void readRightsFromConcreteFolder(String searchPattern) {
        messageToUser.info(this.getClass().getSimpleName(), "readRightsFromConcreteFolder", searchPattern);
        Path path = Paths.get(searchPattern).toAbsolutePath().normalize();
        mapRights.put(path, Collections.singletonList("searching by pattern : " + searchPattern));
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        try {
            List<String> collect = aclFileAttributeView.getAcl().stream().map(AclEntry::toString).collect(Collectors.toList());
            mapRights.put(path, collect);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 185 ***");
        }
    }
    
    private @NotNull String getParsedResult() {
        int patternMapSize = foundPatternMap();
        String patternsToSearch = MessageFormat
            .format("{0}. Lines = {1}/{2}", new TForms().fromArray(this.searchPatterns).replaceAll("\n", " | "), patternMapSize, this.countTotalLines);
        String retMap = new TForms().fromArray(mapRights).replaceAll("\\Q : \\E", "\n");
        String retStr = patternsToSearch + "\n" + retMap;
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".txt", retStr.replaceAll(", ", "\n").replaceAll("\\Q]]\\E", "\n"));
    }
    
    private void searchInQueue(String searchPattern, @NotNull Queue<String> queue) {
        queue.parallelStream().forEach(acl->{
            if (acl.toLowerCase().contains(searchPattern.toLowerCase())) {
                rightsListFromFile.add(acl);
            }
        });
    }
    
    private void mapFoldersRights() {
        rightsListFromFile.forEach(this::parseLine);
    }
    
    private void parseLine(@NotNull String line) {
        try {
            String[] splitRights = line.split("\\Q | ACL: \\E");
            mapRights.put(Paths.get(splitRights[0]), Arrays.asList(splitRights[1].replaceFirst("\\Q:\\E", " ").split("\\Q, \\E")));
        }
        catch (IndexOutOfBoundsException | InvalidPathException ignore) {
            alterParsing(line);
        }
    }
    
    private void alterParsing(@NotNull String line) {
        try {
            String[] splitRights = line.split("\\Q\\\\E");
            mapRights.put(Paths.get(splitRights[0]), Arrays.asList(splitRights[1].replaceFirst("\\Q:\\E", " ").split("\\Q, \\E")));
        }
        catch (IndexOutOfBoundsException | InvalidPathException ignore) {
            //13.09.2019 (14:38)
        }
    }
    
    private void rsNext(@NotNull ResultSet resultSet) throws SQLException {
        Path path = Paths.get(resultSet.getString("dir"));
        String owner = resultSet.getString("user");
        String acl = resultSet.getString(ModelAttributeNames.USERS).replaceAll("\\Q[\\E", "").replaceAll("\\Q]\\E", "");
        List<String> value = new ArrayList<>();
        value.add(owner);
        value.addAll(Arrays.asList(acl.replaceFirst("\\Q:\\E", " ").split("\\Q, \\E")));
        mapRights.put(path, value);
    }
}
