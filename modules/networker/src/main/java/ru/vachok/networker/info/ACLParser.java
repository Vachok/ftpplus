// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


/**
 @see ru.vachok.networker.info.ACLParserTest
 @since 04.07.2019 (9:48) */
public class ACLParser implements InformationFactory {
    
    private File fileWithRights = new File(ConstantsFor.COMMON_DIR + "\\14_ИТ_служба\\Внутренняя\\common.rgh");
    
    private int linesLimit = Integer.MAX_VALUE;
    
    private int countDirectories;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private Map<Path, List<String>> mapRights = new ConcurrentSkipListMap<>();
    
    private List<String> searchPatterns = new ArrayList<>();
    
    private List<String> rightsListFromFile = new ArrayList<>();
    
    public ACLParser() {
    }
    
    private ACLParser(@NotNull Path absPath) {
        searchPatterns.add(absPath.toAbsolutePath().normalize().toString());
    }
    
    private ACLParser(List<String> searchPatterns, int linesLimit) {
        this.searchPatterns.addAll(searchPatterns);
        this.linesLimit = linesLimit;
    }
    
    private ACLParser(@NotNull String searchPattern, int linesLimit) {
        this.linesLimit = linesLimit;
        searchPatterns.add(searchPattern);
    }
    
    private ACLParser(List<String> searchPatterns) {
        this.searchPatterns = searchPatterns;
    }
    
    @Override
    public String getInfoAbout(String linesLimit) {
        try {
            this.linesLimit = Integer.parseInt(linesLimit);
        }
        catch (NumberFormatException e) {
            this.linesLimit = Integer.MAX_VALUE;
        }
        
        String patternsToSearch = MessageFormat.format("{0}. Lines limit = {1}", new TForms().fromArray(this.searchPatterns).replaceAll("\n", " | "), this.linesLimit);
        String retMap = new TForms().fromArray(foundPatternMap()).replaceAll("\\Q : \\E", "\n");
        return patternsToSearch + "\n" + retMap;
    }
    
    @Override
    public void setInfo(Object info) {
        if (info instanceof List) {
            this.searchPatterns = (List<String>) info;
        }
        else {
            throw new InvokeIllegalException(MessageFormat.format("Please, set {0} of {1} via this method!", List.class.getSimpleName(), String.class.getSimpleName()));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RightsParsing{");
        sb.append("fileWithRights=").append(fileWithRights);
        sb.append(", linesLimit=").append(linesLimit);
        sb.append(", countDirectories=").append(countDirectories);
        
        sb.append(", mapRights=").append(mapRights.size());
        sb.append(", searchPatterns=").append(searchPatterns.size());
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull Map<Path, List<String>> foundPatternMap() {
        if (searchPatterns.size() <= 0) {
            throw new InvokeIllegalException("Nothing to search! Set List of patterns via setInfo()");
        }
        List<String> fileRights = readAllACLWithSearchPattern();
        
        return mapFoldersRights(fileRights);
    }
    
    private void readRightsFromConcreteFolder(String searchPattern) {
        Path path = Paths.get(searchPattern).toAbsolutePath().normalize();
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        try {
            List<String> collect = aclFileAttributeView.getAcl().stream().map(AclEntry::toString).collect(Collectors.toList());
            mapRights.put(path, collect);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("RightsParsing.readRightsFromConcreteFolder: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private @NotNull Map<Path, List<String>> mapFoldersRights(@NotNull List<String> rights) {
        rights.forEach(this::parseLine);
        return mapRights;
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
            //
        }
    }
    
    private void pathIsDirMapping(@NotNull String[] splitRights, Path folderPath) throws IndexOutOfBoundsException {
        String acls = splitRights[1];
        String[] aclsArray = acls.split(", ");
        this.mapRights.put(folderPath, Arrays.asList(aclsArray));
    }
    
    private @NotNull List<String> readAllACLWithSearchPattern() {
        try (InputStream inputStream = new FileInputStream(fileWithRights);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, ConstantsFor.CP_WINDOWS_1251);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            Queue<String> tempQueue = new LinkedList<>();
            if (searchPatterns.get(0).equals("*")) {
                bufferedReader.lines().limit(linesLimit).forEach(rightsListFromFile::add);
                return rightsListFromFile;
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
                    ;
                });
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
        return rightsListFromFile;
    }
    
    private void searchInQueue(String searchPattern, @NotNull Queue<String> queue) {
        queue.parallelStream().forEach(acl->{
            if (acl.toLowerCase().contains(searchPattern.toLowerCase())) {
                rightsListFromFile.add(acl);
            }
        });
    }
}
