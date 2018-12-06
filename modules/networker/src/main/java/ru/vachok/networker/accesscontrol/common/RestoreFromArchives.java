package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 Восстановление папки из архива
 <p>

 @since 05.12.2018 (8:59) */
public class RestoreFromArchives extends SimpleFileVisitor<Path> {

    /**
     Путь к архиву
     */
    private static final Path ARCHIVE_DIR = Paths.get("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\");

    /**
     Путь к продуктиву
     */
    private static final Path COMMON_DIR = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");

    private String firstLeverSTR;

    private static final Logger LOGGER = AppComponents.getLogger();

    private StringBuilder resultStr = new StringBuilder();

    private String pathToRestoreAsStr;

    RestoreFromArchives(String pathToRestoreAsStr) {
        if (pathToRestoreAsStr.toLowerCase().contains("common_new")) pathToRestoreAsStr = pathToRestoreAsStr.split("\\Qcommon_new\\\\E")[1];
        else if (pathToRestoreAsStr.toLowerCase().contains("archives")) pathToRestoreAsStr = pathToRestoreAsStr.split("\\Qrchives\\\\E")[1];
        else if (pathToRestoreAsStr.toLowerCase().contains(":\\")) pathToRestoreAsStr = pathToRestoreAsStr.split("\\Q:\\\\E")[1];
        char[] chars = pathToRestoreAsStr.toCharArray();
        Character lastChar = chars[chars.length - 1];
        if (lastChar.equals('\\')) {
            chars[chars.length - 1] = ' ';
            this.pathToRestoreAsStr = new String(chars).trim();
        } else this.pathToRestoreAsStr = new String(pathToRestoreAsStr.getBytes(), Charset.defaultCharset());
        getFirstLevelDirs();
    }

    private void getFirstLevelDirs() {
        String[] dirs = pathToRestoreAsStr.split("\\Q\\\\E");
        this.firstLeverSTR = dirs[0];
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        boolean aCase = false;
        String pathInArch = pathToRestoreAsStr;
        String pathInCommon = pathToRestoreAsStr;
        try {
            if (attrs.isDirectory() && dir.toString().equals(ARCHIVE_DIR.toString())) pathInArch = dir.toString();
            else if (attrs.isDirectory() && dir.toString().contains(firstLeverSTR)) {
                pathInArch = dir.toString().split("\\QArchives\\\\E")[1];
                aCase = pathInArch.equalsIgnoreCase(pathInCommon);
            } else return FileVisitResult.SKIP_SUBTREE;
        } catch (ArrayIndexOutOfBoundsException e) {
            aCase = pathInArch.equalsIgnoreCase(pathInCommon);
        }
        if (attrs.isDirectory() && aCase) return directoryMatch(pathInArch);
        else return directoryMismatch();
    }

    private FileVisitResult directoryMismatch() {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String filePathStr = COMMON_DIR.toString() + pathToRestoreAsStr;
        if (attrs.isRegularFile() && filePathStr.contains(".")) {
            return fileChecker(filePathStr, file, attrs.lastModifiedTime());
        }
        return FileVisitResult.CONTINUE;
    }

    private FileVisitResult fileChecker(String filePathStr, Path file, FileTime fileTime) throws IOException {
        List<String> fileNamesList = new ArrayList<>();
        File newCommonFile = new File(filePathStr);
        String archFileName = file.toFile().getName().toLowerCase();
        String newCommonFileName = newCommonFile.getName().toLowerCase();
        if (fileTime.toMillis() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5))
            && archFileName.contains(newCommonFileName)) {
            resultStr
                .append("<br>Модифицировано: ")
                .append(fileTime)
                .append("<br>")
                .append(file.toFile().getName())
                .append("<br>");
            String msg = "Файл из архива - <font color=\"orange\">" + file.toString();
            LOGGER.info(msg);
            resultStr.append(msg)
                .append("</font> <font color=\"red\"> размер в байтах: ")
                .append(Files.size(file))
                .append("</font>.<br>");
            fileNamesList.add(file.toFile().getAbsolutePath());
        }
        for (String s : fileNamesList) {
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
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.SKIP_SIBLINGS;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        String msg = dir.toString() + " is visit";
        LOGGER.info(msg);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstLeverSTR, pathToRestoreAsStr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestoreFromArchives)) return false;
        RestoreFromArchives that = (RestoreFromArchives) o;
        return Objects.equals(firstLeverSTR, that.firstLeverSTR) &&
            pathToRestoreAsStr.equals(that.pathToRestoreAsStr);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestoreFromArchives{");
        sb.append("ARCHIVE_DIR=").append(ARCHIVE_DIR);
        sb.append(", COMMON_DIR=").append(COMMON_DIR);
        sb.append(", firstLeverSTR='").append(firstLeverSTR).append('\'');
        sb.append(", pathToRestoreAsStr='").append(pathToRestoreAsStr).append('\'');
        sb.append(", resultStr=").append(resultStr);
        sb.append('}');
        return sb.toString();
    }

    Path getArchiveDir() {
        return ARCHIVE_DIR;
    }

    Path getCommonDir() {
        return COMMON_DIR;
    }

    private FileVisitResult directoryMatch(String pathInArch) throws IOException {
        LOGGER.info(pathInArch);
        String commonNeed = COMMON_DIR.toString() + pathInArch;
        Path restInCommon = Paths.get(commonNeed);
        commonNeed = ARCHIVE_DIR.toString() + "\\" + pathInArch;
        Path restInArch = Paths.get(commonNeed);
        if (restInCommon.toFile().isDirectory() && restInCommon.toFile().exists()) {
            String msg = new StringBuilder()
                .append(commonNeed)
                .append("<br><font color=\"yellow\">")
                .append(restInCommon)
                .append("</font> это пропащая папка<br>")
                .append("<textarea>")
                .append(Arrays.toString(restInCommon.toFile().listFiles())
                    .replaceAll(", ", "\n")
                    .replaceAll("\\Q]\\E", "")
                    .replaceAll("\\Q[\\E", ""))
                .append("</textarea><font color=\"green\">")
                .append(restInArch)
                .append("</font> это папка из архива.<p><textarea>")
                .append(Arrays.toString(restInArch.toFile().listFiles())
                    .replaceAll(", ", "\n")
                    .replaceAll("\\Q]\\E", "")
                    .replaceAll("\\Q[\\E", ""))
                .append("</textarea>")
                .toString();
            LOGGER.warn(msg);
            resultStr.append(msg);
        } else {
            File tFilePath = new File(restInCommon.toString());
            boolean mkdirs = tFilePath.mkdirs();
            if (mkdirs) {
                for (File f : restInArch.toFile().listFiles()) {
                    if (f.exists() && f.lastModified() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5))) {
                        Path copy = Files.copy(f.toPath(), Paths.get(tFilePath.toPath() + "\\" + f.getName()));
                        resultStr.append(copy.toUri());
                    }
                }
            }
        }
        return FileVisitResult.TERMINATE;
    }
}
