// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 @see ru.vachok.networker.accesscontrol.common.RightsParsingTest
 @since 04.07.2019 (9:48) */
public class RightsParsing {
    
    
    private File fileWithRights = new File(ConstantsFor.COMMON_DIR + "\\14_ИТ_служба\\Внутренняя\\common.rgh");
    
    private int linesLimit = Integer.MAX_VALUE;
    
    private int countDirectories;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private Map<Path, List<String>> mapRights = new ConcurrentHashMap<>();
    
    public RightsParsing(List<String> searchPatterns, int linesLimit) {
        this.searchPatterns.addAll(searchPatterns);
        this.linesLimit = linesLimit;
    }
    
    public RightsParsing(@NotNull String searchPattern, int linesLimit) {
        this.linesLimit = linesLimit;
        searchPatterns.add(searchPattern);
    }
    
    private List<String> searchPatterns = new ArrayList<>();
    
    public List<String> getSearchPatterns() {
        return searchPatterns;
    }
    
    public RightsParsing(@NotNull String searchPattern) {
        searchPatterns.add(searchPattern);
    }
    
    public RightsParsing(@NotNull Path absPath) {
        searchPatterns.add(absPath.toAbsolutePath().normalize().toString());
    }
    
    public RightsParsing(@NotNull Path toCheckPath, File fileRGHToRead) {
        this.fileWithRights = fileRGHToRead;
        searchPatterns.add(toCheckPath.toAbsolutePath().normalize().toString());
    }
    
    public RightsParsing(List<String> searchPatterns) {
        this.searchPatterns = searchPatterns;
    }
    
    public Map<Path, List<String>> foundPatternMap() {
        if (searchPatterns.size() <= 0) {
            throw new InvokeIllegalException("Nothing to search!");
        }
        if (searchPatterns.contains("srv-fs.eatmeat.ru")) {
            for (String searchPattern : searchPatterns) {
                readRightsFromConcreteFolder(searchPattern);
            }
        }
        List<String> fileRights = readAllACLWithSearchPattern();
        return mapFoldersRights(fileRights);
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
    
    private void readRightsFromConcreteFolder(String searchPattern) {
        Path path = Paths.get(searchPattern).toAbsolutePath().normalize();
        if (path.toFile().isDirectory()) {
            for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
                if (file.getName().equals(FileNames.FILENAME_OWNER)) {
                    mapFoldersRights(FileSystemWorker.readFileToList(file.getAbsolutePath()));
                }
            }
        }
    }
    
    private @NotNull Map<Path, List<String>> mapFoldersRights(@NotNull List<String> rights) {
        rights.forEach(this::parseLine);
        return mapRights;
    }
    
    private void parseLine(@NotNull String line) {
        try {
            String[] splitRights = line.split("\\Q | ACL: \\E");
            Path folderPath = Paths.get(splitRights[0]);
            splitRights[1] = splitRights[1].replaceFirst("\\Q[\\E", "").replaceFirst("\\Q]\\E", "");
            if (Files.isDirectory(folderPath)) {
                pathIsDirMapping(splitRights, folderPath);
            }
        }
        catch (IndexOutOfBoundsException | InvalidPathException e) {
            messageToUser.error(e.getMessage());
            alterParsing(line);
        }
    }
    
    private void alterParsing(@NotNull String lineToParse) {
        for (String searchPattern : searchPatterns) {
            List<String> rightList = FileSystemWorker
                .readFileToList(Paths.get(searchPattern) + ConstantsFor.FILESYSTEM_SEPARATOR + new File(FileNames.FILENAME_OWNER));
            this.mapRights.put(Paths.get(searchPattern), rightList);
        }
    }
    
    private void pathIsDirMapping(@NotNull String[] splitRights, Path folderPath) throws IndexOutOfBoundsException {
        String acls = splitRights[1];
        String[] aclsArray = acls.split(", ");
        this.mapRights.put(folderPath, Arrays.asList(aclsArray));
    }
    
    private @NotNull List<String> readAllACLWithSearchPattern() {
        List<String> rightsListFromFile = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(fileWithRights);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, ConstantsFor.CP_WINDOWS_1251);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            Queue<String> rightsQFromFileTmp = new LinkedList<>();
            if (searchPatterns.get(0).equals("*")) {
                bufferedReader.lines().limit(linesLimit).forEach(rightsListFromFile::add);
                return rightsListFromFile;
            }
            else {
                bufferedReader.lines().limit(linesLimit).forEach(rightsQFromFileTmp::add);
                while (!rightsQFromFileTmp.isEmpty()) {
                    String acl = rightsQFromFileTmp.poll();
                    searchPatterns.forEach(searchPattern->{
                        if (acl.toLowerCase().contains(searchPattern.toLowerCase())) {
                            rightsListFromFile.add(acl);
                        }
                    });
                }
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
        return rightsListFromFile;
    }
}
