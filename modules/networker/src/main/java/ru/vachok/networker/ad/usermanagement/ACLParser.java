// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
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
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
    
    private String searchPattern;
    
    private List<String> searchPatterns;
    
    private int linesLimit = Integer.MAX_VALUE;
    
    public ACLParser() {
        super(Paths.get("."));
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.searchPatterns = (List<String>) classOption;
    }
    
    @Override
    public String getResult() {
        ACLDatabaseSearcher searcher = new ACLDatabaseSearcher();
        searcher.setClassOption(searchPatterns);
        return searcher.getResult();
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
    
    List<String> getRightsListFromFile() {
        return rightsListFromFile;
    }
    
    private void searchInQueue(@NotNull Queue<String> queue) {
        queue.parallelStream().forEach(acl->{
            if (acl.toLowerCase().contains(searchPattern.toLowerCase())) {
                getRightsListFromFile().add(acl);
            }
        });
    }
    
    @Contract(pure = true)
    Map<Path, List<String>> getMapRights() {
        return mapRights;
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
            messageToUser.error(e.getMessage() + " see line: 185 ***");
        }
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