package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;


/**
 @see SyncInDBStatisticsTest
 @since 08.09.2019 (14:55) */
@SuppressWarnings("InstanceVariableOfConcreteClass")
class SyncInDBStatistics extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncInDBStatistics.class.getSimpleName());
    
    static final String LIMDEQ_STR = " fillLimitDequeFromDBWithFile";
    
    private String ipAddress;
    
    private DBStatsUploader dbStatsUploader;
    
    public SyncInDBStatistics(String ipAddress) {
        this.ipAddress = ipAddress;
        this.dbStatsUploader = new DBStatsUploader(ipAddress);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncInetStatistics{");
    
        sb.append(", dbStatsUploader=").append(dbStatsUploader);
        sb.append(", aboutWhat='").append(ipAddress).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String syncData() {
        StringBuilder result = new StringBuilder();
        Path rootPath = Paths.get(".");
        if (ipAddress.equalsIgnoreCase(ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC)) {
            throw new UnsupportedOperationException(MessageFormat.format("Try getInstance({0})", SyncData.PC));
        }
        else {
            result.append(inetStatSync(rootPath));
        }
        return result.toString();
    }
    
    private @NotNull String inetStatSync(Path rootPath) {
        if (ipAddress.isEmpty() || !ipAddress.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            throw new IllegalArgumentException(ipAddress + " only internet stats sync available!");
        }
    
        String statFile = ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddress
            .replaceAll("_", ".") + ".csv";
        String inetStatsPath = rootPath.toAbsolutePath().normalize().toString() + statFile;
        messageToUser.info(fillLimitDequeueFromDBWithFile(Paths.get(inetStatsPath), ipAddress) + LIMDEQ_STR);
        return convertToJSON();
    }
    
    private @NotNull String convertToJSON() {
        Iterator<String> deqIterator = getFromFileToJSON().iterator();
        
        while (deqIterator.hasNext()) {
            String entryStat = getFromFileToJSON().removeLast();
            if (entryStat.isEmpty() || !entryStat.contains(",")) {
                continue;
            }
    
            getFromFileToJSON().addLast(makeJSONString(entryStat));
            
            if (entryStat.contains("[") & entryStat.contains("]")) {
                break;
            }
            
        }
        return new TForms().fromArray(getFromFileToJSON());
    }
    
    private @NotNull String makeJSONString(@NotNull String entryStat) {
        String[] dbFields = entryStat.split(",");
        try {
            dbFields[0] = "\"stamp\":\"" + dbFields[0].replaceAll(">", "") + "\"";
            dbFields[1] = "\"squidans\":\"" + dbFields[1].replaceAll(">", "") + "\"";
            dbFields[2] = "\"bytes\":\"" + dbFields[2].replaceAll(">", "") + "\"";
    
            dbFields[3] = "\"timespend\":\"" + 0 + "\"";
            dbFields[4] = "\"site\":\"" + dbFields[4].replaceAll("<br>", "") + "\"";
        }
        catch (IndexOutOfBoundsException ignore) {
            //10.09.2019 (19:37)
        }
        return Arrays.toString(dbFields);
    }
    
    @Override
    public void setOption(Object option) {
        if (option instanceof Deque) {
            setFromFileToJSON((Deque<String>) option);
        }
        else {
            this.ipAddress = (String) option;
        }
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncInDBStatistics.uploadCollection( int ) at 14.09.2019 - (9:11)");
    }
    
    @Override
    String getDbToSync() {
        return ipAddress;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.ipAddress = dbToSync;
    }
    
    @Override
    Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put(ConstantsFor.DBCOL_SQUIDANS, ConstantsFor.VARCHAR_20);
        colMap.put("site", ConstantsFor.VARCHAR_190);
        colMap.put(ConstantsFor.DBCOL_BYTES, "int(11)");
        colMap.put(ConstantsFor.DBCOL_STAMP, ConstantsFor.BIGINT_13);
        return colMap;
    }
    
    @Override
    public void superRun() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncInDBStatistics.superRun( void ) at 15.09.2019 - (10:18)");
    }
    
    private void getTableName(@NotNull Path rootPath) {
        File[] inetFiles = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS).toFile()
            .listFiles();
        for (File statCsv : inetFiles) {
            String tableName = statCsv.getName().replaceAll("\\Q.\\E", "_").replace("_csv", "");
            dbStatsUploader.setOption(tableName);
        }
    }
    
}
