package ru.vachok.networker.accesscontrol.common;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonOwnerParsingTest
 @since 26.06.2019 (17:07) */
public class CommonOwnerParsing {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private String ownerToSearchPattern;
    
    private long linesLimit = Long.MAX_VALUE;
    
    public CommonOwnerParsing(String ownerToSearchPattern, long linesLimit) {
        this.ownerToSearchPattern = ownerToSearchPattern;
        this.linesLimit = linesLimit;
    }
    
    public CommonOwnerParsing(String ownerToSearchPattern) {
        this.ownerToSearchPattern = ownerToSearchPattern;
    }
    
    public List<String> userOwnedFilesGetter() {
        Path pathToRead = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Внутренняя\\common.own");
        List<String> noBuiltinAdministrators = null;
        try (InputStream inputStream = new FileInputStream(pathToRead.toAbsolutePath().normalize().toString());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "windows-1251");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            noBuiltinAdministrators = readOwners(bufferedReader);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".userOwnedFilesGetter", e));
        }
        
        Map<String, List<String>> mapOwners = mapOwners(noBuiltinAdministrators);
        noBuiltinAdministrators.clear();
        noBuiltinAdministrators.add(toString());
        noBuiltinAdministrators.add("\n");
        if (ownerToSearchPattern.equals("*")) {
            for (Map.Entry<String, List<String>> entry : mapOwners.entrySet()) {
                noBuiltinAdministrators.add(entry.getKey() + " owns " + entry.getValue().size() + " files:\n" + new TForms().fromArray(entry.getValue(), false));
            }
        }
        else
            for (String key : mapOwners.keySet()) {
            if (key.toLowerCase().contains(ownerToSearchPattern)) {
                noBuiltinAdministrators.add("For user - " + key + " found " + mapOwners.get(key).size() + " files:");
                noBuiltinAdministrators.addAll(mapOwners.get(key));
            }
        }
        return noBuiltinAdministrators;
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonOwnerParsing{");
        sb.append("ownerToSearchPattern='").append(ownerToSearchPattern).append('\'');
        sb.append(", linesLimit=").append(linesLimit);
        sb.append('}');
        return sb.toString();
    }
    
    private Map<String, List<String>> mapOwners(List<String> administrators) {
        Map<String, List<String>> fileUserMap = new ConcurrentHashMap<>();
        administrators.forEach(fileUser->{
            try {
                String[] splitFileUser = fileUser.split(" owned by: ");
                if (!fileUserMap.containsKey(splitFileUser[1])) {
                    List<String> stringList = new ArrayList<>();
                    stringList.add(splitFileUser[0]);
                    fileUserMap.put(splitFileUser[1], stringList);
                }
                else {
                    fileUserMap.get(splitFileUser[1]).add(splitFileUser[0]);
                }
            }
            catch (IndexOutOfBoundsException ignore) {
                //
            }
        });
        return fileUserMap;
    }
    
    private List<String> readOwners(BufferedReader bufferedReader) {
        List<String> ownersList = new ArrayList<>();
        bufferedReader.lines().limit(linesLimit).distinct().forEach(line->{
            if (!line.contains("BUILTIN\\Admin")) {
                ownersList.add(line);
            }
        });
        return ownersList;
    }
    
}
