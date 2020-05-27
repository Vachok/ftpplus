// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import com.eclipsesource.json.JsonObject;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Сбор информации о файла, в которые не заходили более 2 лет, и которые имеют размер более 25 мб.
 <p>
 Список папок-исключений: {@link ConstantsFor#EXCLUDED_FOLDERS_FOR_CLEANER}

 @see ru.vachok.networker.ad.common.OldBigFilesInfoCollectorTest
 @since 22.11.2018 (14:53) */
@Service("OldBigFilesInfoCollector")
public class OldBigFilesInfoCollector implements Callable<String> {


    private static final String DOS_ARCHIVE = "dos:archive";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, OldBigFilesInfoCollector.class.getSimpleName());

    private String reportUser = ConstantsFor.RUNNING;

    @NotNull private String startPath = "\\\\srv-fs.eatmeat.ru\\common_new";

    private long dirsCounter;

    private long filesCounter;

    private long totalFilesSize;

    private long filesMatched;

    @NotNull
    public String getStartPath() {
        return startPath;
    }

    public void setStartPath(@NotNull String startPath) {
        this.startPath = startPath;
    }

    public String getFromDatabase() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_COMMONOLDFILES);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM common.oldfiles");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            float totalSizeMB = 0;
            int totalFiles = 0;
            while (resultSet.next()) {
                totalSizeMB += resultSet.getFloat("size");
                totalFiles++;
            }
            stringBuilder.append("Total file size in DB now: ").append(totalSizeMB).append(" megabytes\n");
            stringBuilder.append(ConstantsFor.FILES).append(totalFiles);
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }

    @Override
    public String call() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            OldBigFilesInfoCollector.WalkerCommon walkerCommon = getWalker();
            stringBuilder.append(Files.walkFileTree(Paths.get(startPath), walkerCommon));
            new File(FileNames.WALKER_LCK).delete();
        }
        catch (IOException | InvokeIllegalException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        finally {
            this.reportUser = stringBuilder.toString();
            InitProperties.getTheProps().setProperty(OldBigFilesInfoCollector.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = reportUser != null ? reportUser.hashCode() : 0;
        result = 31 * result + startPath.hashCode();
        result = 31 * result + (int) (dirsCounter ^ (dirsCounter >>> 32));
        result = 31 * result + (int) (filesCounter ^ (filesCounter >>> 32));
        result = 31 * result + (int) (totalFilesSize ^ (totalFilesSize >>> 32));
        result = 31 * result + (int) (filesMatched ^ (filesMatched >>> 32));
        return result;
    }

    private OldBigFilesInfoCollector.WalkerCommon getWalker() throws InvokeIllegalException {
        File walkFile = new File(FileNames.WALKER_LCK);
        if (walkFile.exists()) {
            throw new InvokeIllegalException(walkFile.getAbsolutePath() + " : " + new Date(walkFile.lastModified()));
        }
        else {
            try {
                Files.createFile(walkFile.toPath());
                walkFile.deleteOnExit();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
            return new OldBigFilesInfoCollector.WalkerCommon();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OldBigFilesInfoCollector)) {
            return false;
        }

        OldBigFilesInfoCollector collector = (OldBigFilesInfoCollector) o;

        if (dirsCounter != collector.dirsCounter) {
            return false;
        }
        if (filesCounter != collector.filesCounter) {
            return false;
        }
        if (totalFilesSize != collector.totalFilesSize) {
            return false;
        }
        if (filesMatched != collector.filesMatched) {
            return false;
        }
        if (reportUser != null ? !reportUser.equals(collector.reportUser) : collector.reportUser != null) {
            return false;
        }
        return startPath.equals(collector.startPath);
    }

    private void writeToDB(@NotNull Path file, float mByteSize, String attrArray) throws SQLException {
        DataConnectTo localDCT = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        String[] cleanStop = ConstantsFor.getExcludedFoldersForCleaner();
        for (String stop : cleanStop) {
            if (file.toString().contains(stop)) {
                file = Paths.get("blackhole");
            }
            else {
                try (Connection connection = localDCT.getDefaultConnection(ConstantsFor.DB_COMMONOLDFILES)) {
                    try (PreparedStatement preparedStatement = connection
                        .prepareStatement("INSERT INTO oldfiles (AbsolutePath, size, Attributes) VALUES (?, ?, ?)")) {
                        preparedStatement.setString(1, file.toAbsolutePath().normalize().toString());
                        preparedStatement.setFloat(2, mByteSize);
                        preparedStatement.setString(3, attrArray);
                        preparedStatement.executeUpdate();
                    }
                }
            }
        }
    }

    @NotNull
    private String askUser() {
        String msg = MessageFormat.format("{0} total dirs, {1} total files scanned. Matched: {2} ({3} mb)",
            dirsCounter, filesCounter, filesMatched, totalFilesSize / ConstantsFor.MBYTE);
        messageToUser.warn(msg);
        String confirm = AppComponents.getMessageSwing(this.getClass().getSimpleName()).confirm(this.getClass().getSimpleName(), "Do you want to clean?", msg);
        if (confirm.equals("ok")) {
            new Cleaner(1);
        }
        else {
            writeToLog();
        }
        return msg;
    }

    private void writeToLog() {
        String logName = this.getClass().getSimpleName() + ".log";
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(logName))) {
            outputStream.write(reportUser.getBytes());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 128");
        }
    }

    /**
     @param attrs {@link BasicFileAttributes}
     @return более 15 мб и старше 2х лет.
     */
    private boolean more2MBOld(@NotNull BasicFileAttributes attrs) {
        int oldfileminimumsizemb = Integer.parseInt(InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps().getProperty("oldfilessize", "10"));
        return attrs.lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(Long
            .parseLong(InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps().getProperty("oldfilesperiod", "730"))) && attrs
            .size() > ConstantsFor.MBYTE * oldfileminimumsizemb;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("reportUser", reportUser + "'");
        jsonObject.add(ConstantsFor.JSON_PARAM_NAME_STARTPATH, startPath + "'");
        jsonObject.add("dirsCounter", dirsCounter);
        jsonObject.add("filesCounter", filesCounter);
        jsonObject.add("totalFilesSize", totalFilesSize);
        jsonObject.add("filesMatched", filesMatched);
        return jsonObject.toString();
    }

    private class WalkerCommon extends SimpleFileVisitor<Path> {


        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            dirsCounter += 1;
            if (Arrays.stream(ConstantsFor.getExcludedFoldersForCleaner()).anyMatch(tabooDir->dir.toAbsolutePath().normalize().toString().contains(tabooDir))) {
                messageToUser.info(getClass().getSimpleName(), "Skipped", dir.toString());
                return FileVisitResult.SKIP_SUBTREE;
            }
            else {
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            filesCounter += 1;
            if (more2MBOld(attrs)) {
                Files.setAttribute(file, DOS_ARCHIVE, true);
                String attrArray = AbstractForms.fromArray(Files.readAttributes(file, "*"));
                float mByteSize = (float) attrs.size() / ConstantsFor.MBYTE;
                try {
                    writeToDB(file, mByteSize, attrArray);
                }
                catch (SQLException | RuntimeException e) {
                    return FileVisitResult.CONTINUE;
                }
                finally {
                    filesMatched += 1;
                    totalFilesSize += attrs.size();
                    FirebaseDatabase.getInstance().getReference(OldBigFilesInfoCollector.class.getSimpleName())
                        .setValue(System.currentTimeMillis(), (error, ref)->messageToUser
                            .error("Cleaner.onComplete", error.getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace())));

                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            String toString = MessageFormat.format("Dirs: {0}, files: {2}/{3}. Size {4} MB. Current dir: {1}", dirsCounter, dir.toAbsolutePath()
                .normalize(), filesMatched, filesCounter, totalFilesSize / ConstantsFor.MBYTE);
            messageToUser.info(toString);
            return FileVisitResult.CONTINUE;
        }
    }
}

