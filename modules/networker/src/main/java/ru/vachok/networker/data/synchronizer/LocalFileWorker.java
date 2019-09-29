package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @since 17.09.2019 (16:21) */
class LocalFileWorker {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, LocalFileWorker.class.getSimpleName());
    
    private Deque<String> fromFileToJSON;
    
    private Path absNormalGeneralFile;
    
    LocalFileWorker() {
        String originDirectory = "." + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS;
        this.fromFileToJSON = new ConcurrentLinkedDeque<>();
        this.setAbsNormalGeneralFile(Paths.get(originDirectory).toAbsolutePath().normalize());
    }
    
    void setAbsNormalGeneralFile(Path absNormalGeneralFile) {
        this.absNormalGeneralFile = absNormalGeneralFile;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LocalFileWorker{");
        sb.append("fromFileToJSON=").append(getFromFileToJSON());
        sb.append(", absNormalGeneralFile=").append(absNormalGeneralFile);
        sb.append('}');
        return sb.toString();
    }
    
    @Contract(pure = true)
    private Deque<String> getFromFileToJSON() {
        return fromFileToJSON;
    }
    
    void setFromFileToJSON(Deque<String> fromFileToJSON) {
        this.fromFileToJSON = fromFileToJSON;
    }
    
    void renewCopyFile(Deque<String> fromFileToJSON) {
        this.fromFileToJSON = fromFileToJSON;
        List<String> fromFileJSONList = new ArrayList<>(getFromFileToJSON());
        Collections.sort(fromFileJSONList);
        try {
            FileSystemWorker.writeFile(absNormalGeneralFile.toAbsolutePath().normalize().toString().replace(FileNames.DIR_INETSTATS, FileNames.DIR_INETSTATSZIP),
                fromFileJSONList
                    .stream());
        }
        catch (RuntimeException e) {
            messageToUser.error(e.getMessage() + " see line: 59");
        }
    }
    
    void renewOriginFile(Deque<String> fromFileToJSON) {
        this.fromFileToJSON = fromFileToJSON;
        Path originPath = absNormalGeneralFile.toAbsolutePath().normalize();
        Path copyPath = Paths.get(originPath.toString().replace(FileNames.DIR_INETSTATS, FileNames.DIR_INETSTATSZIP));
        FileSystemWorker.copyOrDelFileWithPath(originPath.toFile(), copyPath, true);
        FileSystemWorker.writeFile(originPath.toString(), getFromFileToJSON().stream().sorted());
    }
    
    @NotNull String convertToJSON(@NotNull String stringFromUserIPInetStatisticsFile) {
        try {
            return Json.parse(stringFromUserIPInetStatisticsFile).asObject().toString();
        }
        catch (com.eclipsesource.json.ParseException e) {
            return parseFromCSV(stringFromUserIPInetStatisticsFile);
        }
        
    }
    
    private String parseFromCSV(@NotNull String stringFromUserIPInetStatisticsFile) {
        JsonObject jsonObject = new JsonObject();
        String[] splittedString = stringFromUserIPInetStatisticsFile.split(",");
        if (splittedString.length < 4) {
            return stringFromUserIPInetStatisticsFile;
        }
        long timeStamp = parseStamp(splittedString[0]);
        try {
            jsonObject.add(ConstantsFor.DBCOL_STAMP, String.valueOf(timeStamp));
            jsonObject.add(ConstantsFor.DBCOL_SQUIDANS, splittedString[1]);
            jsonObject.add(ConstantsFor.DBCOL_BYTES, splittedString[2]);
            jsonObject.add("site", splittedString[4]);
        }
        catch (IndexOutOfBoundsException ignore) {
            //16.09.2019 (20:28)
        }
        return jsonObject.toString();
    }
    
    private long parseStamp(@NotNull String strToParse) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        Date parsedDate;
        try {
            parsedDate = format.parse(strToParse);
        }
        catch (ParseException e) {
            return System.currentTimeMillis();
        }
        return parsedDate.getTime();
    }
    
    JsonObject parseJSONObj(@NotNull String jsStr) {
        JsonObject object = new JsonObject();
        try {
            object = Json.parse(jsStr.replace("},", "}")).asObject();
        }
        catch (com.eclipsesource.json.ParseException ignore) {
            //16.09.2019 (11:02)
        }
        return object;
    }
}
