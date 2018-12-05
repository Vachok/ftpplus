package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

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

    public RestoreFromArchives(String pathToRestoreAsStr) {
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestoreFromArchives{");
        sb.append("ARCHIVE_DIR=").append(ARCHIVE_DIR);
        sb.append(", COMMON_DIR=").append(COMMON_DIR);
        sb.append(", pathToRestoreAsStr='").append(pathToRestoreAsStr).append('\'');
        sb.append(", resultStr=").append(resultStr);
        sb.append('}');
        if (resultStr.toString().isEmpty()) return "NO RESULT...";
        else return sb.toString();
    }

    private FileVisitResult directoryMismatch() {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String filePathStr = COMMON_DIR.toString() + pathToRestoreAsStr;
        if (attrs.isRegularFile() && filePathStr.contains(".")) return fileChecker(filePathStr, file);
        return FileVisitResult.CONTINUE;
    }

    private FileVisitResult fileChecker(String filePathStr, Path file) throws IOException {
        if (!file.toFile().getName().equalsIgnoreCase(pathToRestoreAsStr)) return FileVisitResult.CONTINUE;
        else {
            String msg = file.toFile().getCanonicalPath().replace(ARCHIVE_DIR.toString(), COMMON_DIR.toString()) +
                " canonical \n" + filePathStr;
            resultStr.append(msg);
            LOGGER.info(msg);
            return FileVisitResult.TERMINATE;
        }
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
        int result = resultStr != null ? resultStr.hashCode() : 0;
        result = 31 * result + pathToRestoreAsStr.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestoreFromArchives)) return false;

        RestoreFromArchives that = (RestoreFromArchives) o;

        if (resultStr != null ? !resultStr.equals(that.resultStr) : that.resultStr != null) return false;
        return pathToRestoreAsStr.equals(that.pathToRestoreAsStr);
    }

    private FileVisitResult directoryMatch(String pathInArch) {
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
                .append("</font> это пропащая папка<br><font color=\"green\">")
                .append(restInArch)
                .append("</font> это папка из архива.")
                .toString();
            LOGGER.warn(msg);
            resultStr.append(msg);
        }
        return FileVisitResult.TERMINATE;
    }

    Path getArchiveDir() {
        return ARCHIVE_DIR;
    }

    Path getCommonDir() {
        return COMMON_DIR;
    }
}
