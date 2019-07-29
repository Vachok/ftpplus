// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
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
    
    private long linesLimit = Long.MAX_VALUE;
    
    private int countDirectories;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    /**
     Паттерн имени папки.
     */
    private @NotNull String folderNamePattern;
    
    private Map<Path, List<String>> mapRights = new ConcurrentHashMap<>();
    
    public RightsParsing(@NotNull String folderNamePattern, long linesLimit) {
        this.linesLimit = linesLimit;
        this.folderNamePattern = folderNamePattern;
    }
    
    public RightsParsing(@NotNull String folderNamePattern) {
        this.folderNamePattern = folderNamePattern;
    }
    
    public RightsParsing(@NotNull Path absPath) {
        this.folderNamePattern = absPath.toAbsolutePath().normalize().toString();
    }
    
    public RightsParsing(@NotNull Path toCheckPath, File fileRGHToRead) {
        this.fileWithRights = fileRGHToRead;
        this.folderNamePattern = toCheckPath.toAbsolutePath().normalize().toString();
    }
    
    public Map<Path, List<String>> rightsWriterToFolderACL() {
        if (folderNamePattern.contains("srv-fs.eatmeat.ru")) {
            readRightsFromConcreteFolder();
        }
        List<String> fileRights = readRights();
        return mapFoldersRights(fileRights);
    }
    
    private void readRightsFromConcreteFolder() {
        Path path = Paths.get(folderNamePattern).toAbsolutePath().normalize();
        if (path.toFile().isDirectory()) {
            for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
                if (file.getName().equals(ConstantsFor.FILENAME_OWNER)) {
                    mapFoldersRights(FileSystemWorker.readFileToList(file.getAbsolutePath()));
                }
            }
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonRightsParsing{");
        sb.append("linesLimit=").append(linesLimit);
        sb.append(", countDirectories=").append(countDirectories);
        sb.append(", folderNamePattern='").append(folderNamePattern).append('\'');
        sb.append('}');
        return sb.toString();
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
        List<String> rightList = FileSystemWorker
            .readFileToList(Paths.get(folderNamePattern) + ConstantsFor.FILESYSTEM_SEPARATOR + new File(ConstantsFor.FILENAME_OWNER));
        this.mapRights.put(Paths.get(folderNamePattern), rightList);
    }
    
    private void pathIsDirMapping(@NotNull String[] splitRights, Path folderPath) throws IndexOutOfBoundsException {
        String acls = splitRights[1];
        String[] aclsArray = acls.split(", ");
        this.mapRights.put(folderPath, Arrays.asList(aclsArray));
    }
    
    private @NotNull List<String> readRights() {
        List<String> rightsListFromFile = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(fileWithRights);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, ConstantsFor.CP_WINDOWS_1251);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            if (folderNamePattern.equals("*") || folderNamePattern.isEmpty()) {
                bufferedReader.lines().limit(linesLimit).forEach(rightsListFromFile::add);
            }
            else {
                System.out.println("folderNamePattern = " + folderNamePattern);
                bufferedReader.lines().limit(linesLimit).forEach(line->{
                    if (line.toLowerCase().contains(folderNamePattern.toLowerCase())) {
                        rightsListFromFile.add(line);
                    }
                });
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return rightsListFromFile;
    }
}
