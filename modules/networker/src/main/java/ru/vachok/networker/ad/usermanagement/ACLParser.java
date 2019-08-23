// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


/**
 @see ru.vachok.networker.ad.common.usermanagement.ACLParserTest
 @since 04.07.2019 (9:48) */
class ACLParser extends UserACLManagerImpl {
    
    
    private int linesLimit = Integer.MAX_VALUE;
    
    private int countTotalLines;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private Map<Path, List<String>> mapRights = new ConcurrentSkipListMap<>();
    
    private List<String> searchPatterns = new ArrayList<>();
    
    private List<String> rightsListFromFile = new ArrayList<>();
    
    public ACLParser() {
        super(Paths.get("."));
    }
    
    public void setLinesLimit(int linesLimit) {
        this.linesLimit = linesLimit;
    }
    
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
        return getParsedResult();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ACLParser.class.getSimpleName() + "[\n", "\n]")
            .add("linesLimit = " + linesLimit)
            .add("countTotalLines = " + countTotalLines)
            .add("searchPatterns = " + new TForms().fromArray(searchPatterns))
            .toString();
    }
    
    private @NotNull String getParsedResult() {
        int patternMapSize = foundPatternMap();
        String patternsToSearch = MessageFormat
            .format("{0}. Lines = {1}/{2}", new TForms().fromArray(this.searchPatterns).replaceAll("\n", " | "), patternMapSize, this.countTotalLines);
        String retMap = new TForms().fromArray(mapRights).replaceAll("\\Q : \\E", "\n");
        String retStr = patternsToSearch + "\n" + retMap;
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".txt", retStr.replaceAll(", ", "\n").replaceAll("\\Q]]\\E", "\n"));
    }
    
    private @NotNull List<String> readAllACLWithSearchPattern() {
        try (InputStream inputStream = new FileInputStream(new File(ConstantsFor.COMMON_DIR + "\\14_ИТ_служба\\Внутренняя\\common.rgh"));
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
            this.countTotalLines = tempQueue.size();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
        return rightsListFromFile;
    }
    
    private int foundPatternMap() {
        if (searchPatterns.size() <= 0) {
            throw new InvokeIllegalException("Nothing to search! Set List of patterns via setInfo()");
        }
        List<String> fileRights = readAllACLWithSearchPattern();
        mapFoldersRights(fileRights);
        return rightsListFromFile.size();
    }
    
    private void mapFoldersRights(@NotNull List<String> rights) {
        rights.forEach(this::parseLine);
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
    
    private void searchInQueue(String searchPattern, @NotNull Queue<String> queue) {
        queue.parallelStream().forEach(acl->{
            if (acl.toLowerCase().contains(searchPattern.toLowerCase())) {
                rightsListFromFile.add(acl);
            }
        });
    }
    
    private void pathIsDirMapping(@NotNull String[] splitRights, Path folderPath) throws IndexOutOfBoundsException {
        String acls = splitRights[1];
        String[] aclsArray = acls.split(", ");
        this.mapRights.put(folderPath, Arrays.asList(aclsArray));
    }
}
