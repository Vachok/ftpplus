package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import ru.vachok.networker.AppComponents;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 Восстановление папки из архива
 <p>

 @since 05.12.2018 (8:59) */
@SuppressWarnings ({"SameReturnValue", "RedundantThrows", "MethodWithMultipleReturnPoints"})
public class RestoreFromArchives extends SimpleFileVisitor<Path> {

    /**
     Путь к архиву
     */
    private static final Path ARCHIVE_DIR = Paths.get("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\");

    /**
     Путь к продуктиву
     */
    private static final Path COMMON_DIR = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");

    /**
     {@link AppComponents#getLogger(String)}
     */
    private static final Logger LOGGER = AppComponents.getLogger(RestoreFromArchives.class.getSimpleName());

    /**
     Папка первого уровня.
     <p>
     Для того, чтобы не перебирать каждый раз всё.
     */
    private String firstLeverSTR;
    
    private String pathToRestoreAsStr;

    /**
     {@link StringBuilder} результата работы.
     */
    private final StringBuilder resultStr = new StringBuilder();

    private final int perionDays;

    Path getArchiveDir() {
        return ARCHIVE_DIR;
    }

    Path getCommonDir() {
        return COMMON_DIR;
    }


    /**
     @param pathToRestoreAsStr путь для восстановления
     @param pDays              период (дни)
     @see CommonSRV#reStoreDir()
     */
    RestoreFromArchives(String pathToRestoreAsStr, String pDays) {
        this.perionDays = Integer.parseInt(pDays);
        if(pathToRestoreAsStr.toLowerCase().contains("common_new")){
            pathToRestoreAsStr = pathToRestoreAsStr.split("\\Qcommon_new\\\\E")[1];
        }
        else{
            if(pathToRestoreAsStr.toLowerCase().contains("archives")){
                pathToRestoreAsStr = pathToRestoreAsStr.split("\\Qrchives\\\\E")[1];
            }
            else{
                if(pathToRestoreAsStr.toLowerCase().contains(":\\")){
                    pathToRestoreAsStr = pathToRestoreAsStr.split("\\Q:\\\\E")[1];
                }
                else{
                    if(pathToRestoreAsStr.isEmpty()){
                        pathToRestoreAsStr = Paths.get("").toString();
                    }
                }
            }
        }
        char[] chars = pathToRestoreAsStr.toCharArray();
        try{
            Character lastChar = chars[chars.length - 1];
            if(lastChar.equals('\\')){
                chars[chars.length - 1] = ' ';
                this.pathToRestoreAsStr = new String(chars).trim();
            }
            else{
                this.pathToRestoreAsStr = new String(pathToRestoreAsStr.getBytes(), Charset.defaultCharset());
            }

            getFirstLevelDirs();
        }
        catch(ArrayIndexOutOfBoundsException ignore){
            //
        }
    }

    /**
     Возвращает папку первого уровня.
     <p>
     Чтобы не перебирать весь архив, метод отдаёт только верхнюю папку из {@link #pathToRestoreAsStr}
     */
    private void getFirstLevelDirs() {
        String[] dirs = pathToRestoreAsStr.split("\\Q\\\\E");
        this.firstLeverSTR = dirs[0];
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathToRestoreAsStr, perionDays);
    }

    @Override
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(o==null || getClass()!=o.getClass()){
            return false;
        }
        RestoreFromArchives that = ( RestoreFromArchives ) o;
        return perionDays==that.perionDays &&
            Objects.equals(pathToRestoreAsStr, that.pathToRestoreAsStr);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestoreFromArchives <br>");
        sb.append("\n ").append(resultStr);
        sb.append("\n<p> <font color=\"grey\">ARCHIVE_DIR=").append(ARCHIVE_DIR);
        sb.append("\n<br> COMMON_DIR=").append(COMMON_DIR);
        sb.append("\n<br> firstLeverSTR='").append(firstLeverSTR).append('\'');
        sb.append("\n<br> pathToRestoreAsStr='").append(pathToRestoreAsStr).append('\'');
        sb.append("\n<br> perionDays='").append(perionDays).append('\'');
        sb.append("</font><p>");
        return sb.toString();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        boolean aCase = false;
        String pathInArch = pathToRestoreAsStr;
        String pathInCommon = pathToRestoreAsStr;
        try{
            if(attrs.isDirectory() && dir.toString().equals(ARCHIVE_DIR.toString())){
                pathInArch = dir.toString();
            }
            else{
                if(attrs.isDirectory() && dir.toString().contains(firstLeverSTR)){
                    pathInArch = dir.toString().split("\\QArchives\\\\E")[1];
                    aCase = pathInArch.equalsIgnoreCase(pathInCommon);
                }
                else{
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }
        }
        catch(ArrayIndexOutOfBoundsException e){
            aCase = pathInArch.equalsIgnoreCase(pathInCommon);
        }
        catch(NullPointerException e1){
            resultStr.append(FileVisitResult.TERMINATE);
            return FileVisitResult.TERMINATE;
        }
        if(attrs.isDirectory() && aCase){
            return directoryMatch(pathInArch);
        }
        else{
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     Проверка имени.
     Usages: {@link #visitFile(Path, BasicFileAttributes)}

     @param filePathStr {@link #COMMON_DIR} + {@link #pathToRestoreAsStr}
     @param file        путь до файла из {@link #visitFile(Path, BasicFileAttributes)}
     @param fileTime    {@link BasicFileAttributes#lastModifiedTime()}
     @return {@link FileVisitResult#CONTINUE}
     @throws IOException {@link Files#size(Path)}, {@link #copyFile(String)}
     */
    private FileVisitResult fileChecker(String filePathStr, Path file, FileTime fileTime) throws IOException {
        List<String> fileNamesList = new ArrayList<>();
        File newCommonFile = new File(filePathStr);
        String archFileName = file.toFile().getName().toLowerCase();
        String newCommonFileName = newCommonFile.getName().toLowerCase();
        if(fileTime.toMillis() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(perionDays))
            && archFileName.contains(newCommonFileName)){
            resultStr
                .append("<br>Модифицировано: ").append(fileTime)
                .append("<br>")
                .append(file.toFile().getName())
                .append("<br>");
            String msg = "Файл из архива - <font color=\"orange\">" + file;
            LOGGER.info(msg);
            resultStr.append(msg)
                .append("</font> <font color=\"red\"> размер в байтах: ")
                .append(Files.size(file))
                .append("</font>.<br>");
            fileNamesList.add(file.toFile().getAbsolutePath());
        }
        for(String s : fileNamesList){
            copyFile(s);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        resultStr
            .append(exc.getMessage())
            .append(" err");
        return FileVisitResult.SKIP_SIBLINGS;
    }

    private FileVisitResult directoryMatch(String pathInArch) throws IOException {
        String commonNeed = COMMON_DIR + pathInArch;
        Path restInCommon = Paths.get(commonNeed);
        commonNeed = ARCHIVE_DIR + "\\" + pathInArch;
        Path restInArch = Paths.get(commonNeed);
        File[] files = restInArch.toFile().listFiles();
        if(restInCommon.toFile().isDirectory()){
            resultStr
                .append("Похоже что вы ищете папку - ")
                .append(restInCommon.toAbsolutePath())
                .append(" ")
                .append(restInCommon.toFile().isDirectory())
                .append("<p>");
            String msg = new StringBuilder()
                .append(commonNeed)
                .append("<p><font color=\"yellow\">")
                .append(restInCommon)
                .append("</font> это пропащая папка:<br>")
                .append("<textarea readonly rows=9>")
                .append(Arrays.toString(restInCommon.toFile().listFiles())
                    .replaceAll(", ", "\n")
                    .replaceAll("\\Q]\\E", "")
                    .replaceAll("\\Q[\\E", ""))
                .append("</textarea><font color=\"green\">")
                .append(restInArch)
                .append("</font> это папка из архива:<br><textarea readonly rows=9>")
                .append(Arrays.toString(files)
                    .replaceAll(", ", "\n")
                    .replaceAll("\\Q]\\E", "")
                    .replaceAll("\\Q[\\E", ""))
                .append("</textarea>")
                .toString();
            LOGGER.warn(msg);
            resultStr.append(msg);
        }
        else{
            if(!restInCommon.toFile().exists()){
                resultStr
                    .append(!restInCommon.toFile().exists()).append(" ").append(restInCommon.toFile().getAbsolutePath())
                    .append(" не существует!!! Восстанавливаю всю папку в common_new<br>");
                File tFilePath = new File(restInCommon.toString());
                boolean mkdirs = tFilePath.mkdirs();
                if(mkdirs && Objects.requireNonNull(files).length > 0){
                    resultStr
                        .append("<font color=\"red\">DEADLINE IS        ")
                        .append(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(perionDays)))
                        .append("</font><br>");
                    for(File f : files){
                        if(f.exists() && f.lastModified() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(perionDays))){
                            Path copy = Files.copy(f.toPath(), Paths.get(tFilePath.toPath() + "\\" + f.getName()));
                            resultStr
                                .append(" ")
                                .append(copy.toFile().getName());
                        }
                        else{
                            resultStr
                                .append(f.getName())
                                .append(" <font color=\"yellow\">")
                                .append(new Date(f.lastModified()))
                                .append("</font> period ")
                                .append(false)
                                .append("<br>");
                        }
                    }
                }
            }
        }
        return FileVisitResult.TERMINATE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String filePathStr = COMMON_DIR + pathToRestoreAsStr;
        if (attrs.isRegularFile() && filePathStr.contains(".")) {
            return fileChecker(filePathStr, file, attrs.lastModifiedTime());
        }
        return FileVisitResult.CONTINUE;
    }
    
    /**
     Копирование из архива.
     <p>
     Usages: {@link #fileChecker(String, Path, FileTime)}
     
     @param s путь до файла в архиве
     @throws IOException {@link Files#createDirectories(Path, FileAttribute[])}, {@link Files#copy(Path, OutputStream)}
     */
    private void copyFile(String s) throws IOException {
        String target = s.replace("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\",
            "\\\\srv-fs.eatmeat.ru\\common_new\\");
        File tFile = new File(target);
        String tFPath = tFile.getAbsolutePath().replace(tFile.getName(), "");
        Files.createDirectories(Paths.get(tFPath));
        Files.copy(Paths.get(s), tFile.toPath());
        resultStr
            .append("Cкопирован сюда: <font color=\"green\">")
            .append(target)
            .append("</font><br>");
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        String msg = dir + " is visit";
        LOGGER.info(msg);
        return FileVisitResult.CONTINUE;
    }
}
