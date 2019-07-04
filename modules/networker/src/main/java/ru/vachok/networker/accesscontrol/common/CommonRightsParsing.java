package ru.vachok.networker.accesscontrol.common;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonRightsParsingTest
 @since 04.07.2019 (9:48) */
public class CommonRightsParsing {
    
    
    private long linesLimit = Long.MAX_VALUE;
    
    private int countDirectories;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private String folderNamePattern;
    
    public CommonRightsParsing(String folderNamePattern, long linesLimit) {
        this.linesLimit = linesLimit;
        this.folderNamePattern = folderNamePattern;
    }
    
    public CommonRightsParsing(String folderNamePattern) {
        this.folderNamePattern = folderNamePattern;
    }
    
    public Map<Path, List<String>> rightsWriterToFolderACL() {
        List<String> fileRights = readRights();
        return mapFoldersRights(fileRights);
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonRightsParsing{");
        sb.append("linesLimit=").append(linesLimit);
        sb.append(", countDirectories=").append(countDirectories);
        sb.append(", folderNamePattern='").append(folderNamePattern).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private Map<Path, List<String>> mapFoldersRights(List<String> rights) {
        Map<Path, List<String>> mapRights = new ConcurrentHashMap<>();
        rights.stream().parallel().forEach(line->parseLine(line, mapRights));
        return mapRights;
    }
    
    private void parseLine(String line, Map<Path, List<String>> mapRights) {
        try {
            String[] splitRights = line.split("\\Q | ACL: \\E");
            Path folderPath = Paths.get(splitRights[0]);
            splitRights[1] = splitRights[1].replaceFirst("\\Q[\\E", "").replaceFirst("\\Q]\\E", "");
            if (Files.isDirectory(folderPath)) {
                pathIsDirMapping(splitRights, mapRights, folderPath);
            }
        }
        catch (IndexOutOfBoundsException | InvalidPathException | IOException ignore) {
            //
        }
    }
    
    private void pathIsDirMapping(String[] splitRights, Map<Path, List<String>> mapRights, Path folderPath) throws IndexOutOfBoundsException, IOException {
        String acls = splitRights[1];
        String[] aclsArray = acls.split(", ");
        mapRights.put(folderPath, Arrays.asList(aclsArray));
        if (folderNamePattern.equals("*") || folderNamePattern.isEmpty()) {
            writeACLToFile(folderPath, aclsArray);
        }
    }
    
    private void writeACLToFile(Path folderPath, String[] aclsArray) throws IOException {
        String fileFullPath = folderPath + "\\" + "folder_acl.txt";
        Files.deleteIfExists(Paths.get(fileFullPath));
        FileSystemWorker.writeFile(fileFullPath, Arrays.stream(aclsArray));
        Path setAttribute = Files.setAttribute(Paths.get(fileFullPath), "dos:hidden", true);
        System.out.println("dos:hidden set " + setAttribute + ".\n total dirs = " + this.countDirectories++);
    }
    
    private List<String> readRights() {
        List<String> rightsListFromFile = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Внутренняя\\common.rgh");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "windows-1251");
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
