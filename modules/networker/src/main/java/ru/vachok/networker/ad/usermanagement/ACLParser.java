// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


/**
 @see ru.vachok.networker.ad.usermanagement.ACLParserTest
 @since 04.07.2019 (9:48) */
class ACLParser extends UserACLManagerImpl {
    
    
    private Map<Path, List<String>> mapRights = new ConcurrentSkipListMap<>();
    
    private List<String> rightsListFromFile = new ArrayList<>();
    
    private int countTotalLines;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ACLParser.class.getSimpleName());
    
    private String searchPattern;
    
    private List<String> searchPatterns;
    
    private int linesLimit = Integer.MAX_VALUE;
    
    List<String> getRightsListFromFile() {
        return rightsListFromFile;
    }
    
    @Contract(pure = true)
    Map<Path, List<String>> getMapRights() {
        return mapRights;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        if (classOption instanceof Integer) {
            this.linesLimit = (int) classOption;
        }
        else if (classOption instanceof String) {
            this.searchPattern = (String) classOption;
        }
        else {
            this.searchPatterns = (List<String>) classOption;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ACLParser{");
        sb.append("searchPattern='").append(searchPattern).append('\'');
        sb.append(", messageToUser=").append(messageToUser);
        sb.append(", linesLimit=").append(linesLimit);
        sb.append(", countTotalLines=").append(countTotalLines);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getResult() {
        ACLDatabaseSearcher searcher = new ACLDatabaseSearcher();
        searcher.setClassOption(searchPatterns);
        try {
            return searcher.getResult();
        }
        catch (RuntimeException e) {
            messageToUser.error(MessageFormat.format("ACLParser.getResult", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            return localRead();
        }
    }
    
    private String localRead() {
        for (String pat : searchPatterns) {
            this.searchPattern = pat;
            readRightsFromConcreteFolder();
        }
        return AbstractForms.fromArray(mapRights.keySet());
    }
    
    void readAllACLWithSearchPatternFromFile() {
        try (InputStream inputStream = new FileInputStream(new File(ConstantsFor.COMMON_DIR + "\\14_ИТ_служба\\Внутренняя\\common.rgh"));
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, ConstantsFor.CP_WINDOWS_1251);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            Queue<String> tempQueue = new LinkedList<>();
            if (searchPatterns.get(0).equals("*")) {
                bufferedReader.lines().limit(linesLimit).forEach(getRightsListFromFile()::add);
                mapFoldersRights();
            }
            else {
                bufferedReader.lines().limit(linesLimit).forEach(tempQueue::add);
                for (String srchPat : searchPatterns) {
                    if (srchPat.toLowerCase().contains("srv-fs")) {
                        readRightsFromConcreteFolder();
                    }
                    else {
                        searchInQueue(tempQueue);
                    }
                }
            }
            this.countTotalLines = tempQueue.size();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    private void mapFoldersRights() {
        getRightsListFromFile().forEach(this::parseLine);
    }
    
    private void searchInQueue(@NotNull Queue<String> queue) {
        queue.parallelStream().forEach(acl->{
            if (acl.toLowerCase().contains(searchPattern.toLowerCase())) {
                getRightsListFromFile().add(acl);
            }
        });
    }
    
    void readRightsFromConcreteFolder() {
        messageToUser.info(this.getClass().getSimpleName(), "readRightsFromConcreteFolder", searchPattern);
        Path path = Paths.get(searchPattern).toAbsolutePath().normalize();
        mapRights.put(path, Collections.singletonList("searching by pattern : " + searchPattern));
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        try {
            List<String> collect = aclFileAttributeView.getAcl().stream().map(AclEntry::toString).collect(Collectors.toList());
            mapRights.put(path, collect);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("ACLParser.readRightsFromConcreteFolder", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
    }
    
    private void parseLine(@NotNull String line) {
        try {
            String[] splitRights = line.split("\\Q | ACL: \\E");
            getMapRights().put(Paths.get(splitRights[0]), Arrays.asList(splitRights[1].replaceFirst("\\Q:\\E", " ").split("\\Q, \\E")));
        }
        catch (IndexOutOfBoundsException | InvalidPathException ignore) {
            alterParsing(line);
        }
    }
    
    public ACLParser() {
        super(Paths.get("."));
    }
    
    private void alterParsing(@NotNull String line) {
        try {
            String[] splitRights = line.split("\\Q\\\\E");
            getMapRights().put(Paths.get(splitRights[0]), Arrays.asList(splitRights[1].replaceFirst("\\Q:\\E", " ").split("\\Q, \\E")));
        }
        catch (IndexOutOfBoundsException | InvalidPathException ignore) {
            //13.09.2019 (14:38)
        }
    }
}