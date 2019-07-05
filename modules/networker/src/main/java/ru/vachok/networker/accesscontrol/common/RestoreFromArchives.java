// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 Восстановление папки из архива
 <p>
 @deprecated 05.07.2019 (8:48)
 @see ru.vachok.networker.accesscontrol.common.RestoreFromArchivesTest
 @since 05.12.2018 (8:59) */
@Deprecated
@SuppressWarnings({"SameReturnValue", "RedundantThrows", "MethodWithMultipleReturnPoints"})
public class RestoreFromArchives extends SimpleFileVisitor<Path> {
    
    
    /**
     {@link AppComponents#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreFromArchives.class.getSimpleName());
    
    /**
     Important!
     */
    private static final Pattern COMPILE = Pattern.compile("\\Qcommon_new\\\\E", Pattern.CASE_INSENSITIVE);
    
    /**
     {@link StringBuilder} результата работы.
     */
    private final StringBuilder resultStr = new StringBuilder();
    
    /**
     Папка первого уровня.
     <p>
     Для того, чтобы не перебирать каждый раз всё.
     */
    private String firstLeverSTR;
    
    private String pathToRestoreAsStr;
    
    private int perionDays;
    
    /**
     @param pathToRestoreAsStr путь для восстановления
     @param pDays период (дни)
     @see CommonSRV#reStoreDir()
     */
    RestoreFromArchives(@NonNull String pathToRestoreAsStr, String pDays) throws InvocationTargetException {
        try {
            this.perionDays = Integer.parseInt(pDays);
        }
        catch (NumberFormatException e) {
            this.perionDays = 0;
        }
        if (pathToRestoreAsStr == null) {
            pathToRestoreAsStr = "";
            this.pathToRestoreAsStr = pathToRestoreAsStr;
        }
    
        if (pathToRestoreAsStr.toLowerCase().contains(ConstantsFor.FOLDERNAME_COMMONNEW)) {
            pathToRestoreAsStr = COMPILE.split(pathToRestoreAsStr)[1];
        }
        else if (pathToRestoreAsStr.toLowerCase().contains("archives")) {
            pathToRestoreAsStr = pathToRestoreAsStr.split("\\Qrchives\\\\E")[1];
        }
        else if (pathToRestoreAsStr.toLowerCase().contains(":\\")) {
            pathToRestoreAsStr = pathToRestoreAsStr.split("\\Q:\\\\E")[1];
        }
        else if (pathToRestoreAsStr.isEmpty()) {
            pathToRestoreAsStr = Paths.get("").toString();
        }
        else {
            pathToRestoreAsStr = "";
        }
        char[] chars = pathToRestoreAsStr.toCharArray();
        try {
            Character lastChar = chars[chars.length - 1];
            if (lastChar.equals('\\')) {
                chars[chars.length - 1] = ' ';
                this.pathToRestoreAsStr = new String(chars).trim();
            }
            else {
                this.pathToRestoreAsStr = new String(pathToRestoreAsStr.getBytes(), Charset.defaultCharset());
            }
            getFirstLevelDirs();
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
    }
    
    public String getPathToRestoreAsStr() {
        return pathToRestoreAsStr;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestoreFromArchives <br>");
        sb.append("\n ").append(resultStr);
        sb.append("\n<p> <font color=\"grey\">ARCHIVE_DIR=").append(ConstantsFor.ARCHIVE_DIR);
        sb.append("\n<br> COMMON_DIR=").append(ConstantsFor.COMMON_DIR);
        sb.append("\n<br> firstLeverSTR='").append(firstLeverSTR).append('\'');
        sb.append("\n<br> pathToRestoreAsStr='").append(pathToRestoreAsStr).append('\'');
        sb.append("\n<br> perionDays='").append(perionDays).append('\'');
        sb.append("</font><p>");
        return sb.toString();
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        boolean isArchivesPathEqCommonPath = false;
        String inArchivesPathRaw = pathToRestoreAsStr;
        String inCommonTargetPathRaw = pathToRestoreAsStr;
        try {
            if (attrs.isDirectory() && dir.toString().equals(ConstantsFor.ARCHIVE_DIR.toString())) {
                inArchivesPathRaw = dir.toString();
            }
            else {
                if (attrs.isDirectory() && dir.toString().contains(firstLeverSTR)) {
                    inArchivesPathRaw = dir.toString().split("\\QArchives\\\\E")[1];
                    isArchivesPathEqCommonPath = inArchivesPathRaw.equalsIgnoreCase(inCommonTargetPathRaw);
                }
                else {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            isArchivesPathEqCommonPath = inArchivesPathRaw.equalsIgnoreCase(inCommonTargetPathRaw);
        }
        catch (NullPointerException e1) {
            resultStr.append(FileVisitResult.TERMINATE);
            return FileVisitResult.TERMINATE;
        }
        if (attrs.isDirectory() && isArchivesPathEqCommonPath) {
            return directoryMatch(inArchivesPathRaw);
        }
        else {
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        resultStr
            .append(exc.getMessage())
            .append(" err");
        return FileVisitResult.SKIP_SIBLINGS;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String filePathStr = ConstantsFor.COMMON_DIR + pathToRestoreAsStr;
        if (attrs.isRegularFile() && filePathStr.contains(".")) {
            return fileChecker(filePathStr, file, attrs.lastModifiedTime());
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        String msg = dir + " is visit";
        LOGGER.info(msg);
        return FileVisitResult.CONTINUE;
    }
    
    Path getArchiveDir() {
        return Paths.get(checkCommonAndArchiveDirsLastChar().get("archives"));
    }
    
    Path getCommonDir() {
        return Paths.get(checkCommonAndArchiveDirsLastChar().get("common"));
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
    
    private FileVisitResult fileChecker(String filePathStr, Path file, FileTime fileTime) throws IOException {
        List<String> fileNamesList = new ArrayList<>();
        File newCommonFile = new File(filePathStr);
        String archFileName = file.toFile().getName().toLowerCase();
        String newCommonFileName = newCommonFile.getName().toLowerCase();
        if (fileTime.toMillis() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(perionDays))
            && archFileName.contains(newCommonFileName)) {
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
        for (String s : fileNamesList) {
            copyFile(s);
        }
        return FileVisitResult.CONTINUE;
    }
    
    private FileVisitResult directoryMatch(String inArchivesPathRaw) throws IOException {
        Map<String, String> commonAndArchiveDirs = checkCommonAndArchiveDirsLastChar();
        
        Path pathToRestore = Paths.get(commonAndArchiveDirs.get("common") + inArchivesPathRaw);
        Path pathInArchives = Paths.get(commonAndArchiveDirs.get("archives") + inArchivesPathRaw);
        Path archivesParent = pathInArchives.getParent();
        
        File[] filesFromArchive = archivesParent.toFile().listFiles();
        
        if (pathToRestore.toAbsolutePath().toFile().isDirectory()) {
            resultStr
                .append("Похоже что вы ищете папку - ")
                .append(pathToRestore.toAbsolutePath())
                .append(" ")
                .append(pathToRestore.toFile().isDirectory())
                .append("<p>");
            String msg = new StringBuilder()
                .append(inArchivesPathRaw)
                .append("<p><font color=\"yellow\">")
                .append(pathToRestore)
                .append("</font> это пропащая папка:<br>")
                .append("<textarea readonly rows=9>")
                .append(Arrays.toString(pathToRestore.toFile().listFiles())
                    .replaceAll(", ", "\n")
                    .replaceAll("\\Q]\\E", "")
                    .replaceAll("\\Q[\\E", ""))
                .append("</textarea><font color=\"green\">")
                .append(pathInArchives)
                .append("</font> это папка из архива:<br><textarea readonly rows=9>")
                .append(Arrays.toString(filesFromArchive)
                    .replaceAll(", ", "\n")
                    .replaceAll("\\Q]\\E", "")
                    .replaceAll("\\Q[\\E", ""))
                .append("</textarea>")
                .toString();
            LOGGER.warn(msg);
            resultStr.append(msg);
        }
        else if (!pathToRestore.toFile().exists()) {
                resultStr
                    .append(!pathToRestore.toFile().exists()).append(" ").append(pathToRestore.toFile().getAbsolutePath())
                    .append(" не существует!!! Восстанавливаю всю папку в common_new<br>");
            File tFilePath = new File(pathToRestore.toString());
                boolean mkdirs = tFilePath.mkdirs();
            if (mkdirs && Objects.requireNonNull(filesFromArchive).length > 0) {
                    resultStr
                        .append("<font color=\"red\">DEADLINE IS        ")
                        .append(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(perionDays)))
                        .append("</font><br>");
                for (File f : filesFromArchive) {
                        if (f.exists() && f.lastModified() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(perionDays))) {
                            Path copy = Files.copy(f.toPath(), Paths.get(tFilePath.toPath() + "\\" + f.getName()));
                            resultStr
                                .append(" ")
                                .append(copy.toFile().getName());
                        }
                        else {
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
        else {
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.TERMINATE;
    }
    
    private Map<String, String> checkCommonAndArchiveDirsLastChar() {
        Map<String, String> commonAndArchives = new HashMap<>();
        char[] charArrayCommonDir = ConstantsFor.COMMON_DIR.toString().toCharArray();
        char lastCharInPath = charArrayCommonDir[charArrayCommonDir.length - 1];
        if (lastCharInPath == '\\') {
            commonAndArchives.put("common", new String(charArrayCommonDir));
        }
        else {
            commonAndArchives.put("archives", new String(charArrayCommonDir) + "\\");
        }
        char[] charArrayArchiveDir = ConstantsFor.ARCHIVE_DIR.toString().toCharArray();
        lastCharInPath = charArrayArchiveDir[charArrayArchiveDir.length - 1];
        if (lastCharInPath == '\\') {
            commonAndArchives.put("archives", new String(charArrayArchiveDir));
        }
        else {
            commonAndArchives.put("archives", new String(charArrayArchiveDir) + "\\");
        }
        return commonAndArchives;
    }
    
    /**
     Копирование из архива.
     <p>
     Usages: {@link #fileChecker(String, Path, FileTime)}
 
     @param fileABSPath путь до файла в архиве
     @throws IOException {@link Files#createDirectories(Path, FileAttribute[])}, {@link Files#copy(Path, OutputStream)}
     */
    private void copyFile(String fileABSPath) throws IOException {
        String targetFileName = fileABSPath.replace("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\",
            "\\\\srv-fs.eatmeat.ru\\common_new\\");
        File targetFile = new File(targetFileName);
        String targetFilePath = targetFile.getAbsolutePath().replace(targetFile.getName(), "");
        Files.createDirectories(Paths.get(targetFilePath));
        Files.copy(Paths.get(fileABSPath), targetFile.toPath());
        resultStr
            .append("Cкопирован сюда: <font color=\"green\">")
            .append(targetFileName)
            .append("</font><br>");
    }
}
