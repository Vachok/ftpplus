// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.monitor.ExecScan;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 Сканирование только тех, что он-лайн
 <p>

 @see ScanOnlineTest
 @since 26.01.2019 (11:18) */
@Service
@Scope(ConstantsFor.SINGLETON)
public class ScanOnline implements NetScanService {


    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ScanOnline.class.getSimpleName());

    private List<String> maxOnList = new ArrayList<>();

    @NotNull private File fileMAXOnlines = new File(FileNames.ONLINES_MAX);

    private File onlinesFile = new File(FileNames.ONSCAN);

    private final InformationFactory tvInfo = InformationFactory.getInstance(InformationFactory.TV);

    private String replaceFileNamePattern;

    @SuppressWarnings("InstanceVariableOfConcreteClass") private final TForms tForms = AbstractForms.getI();

    @NotNull
    protected File getFileMAXOnlines() {
        return fileMAXOnlines;
    }

    protected File getOnlinesFile() {
        return onlinesFile;
    }

    /**
     @return {@link #replaceFileNamePattern}

     @see ru.vachok.networker.net.scanner.ScanOnlineTest#fileOnToLastCopyTest()
     @since 12.07.2019 (23:08)
     */
    protected String getReplaceFileNamePattern() {
        return replaceFileNamePattern;
    }

    public ScanOnline() {
        try {
            initialMeth();
        }
        catch (InvokeIllegalException e) {
            messageToUser.warn(ScanOnline.class.getSimpleName(), e.getMessage(), " see line: 91 ***");
        }
    }

    @Override
    public void run() {
        AppConfigurationLocal.getInstance().execute(SwitchesAvailability::new);

        setMaxOnlineListFromFile();

        if (onlinesFile.exists()) {
            try {
                onListFileCopyToLastAndMax();
            }
            catch (InvokeIllegalException e) {
                messageToUser.error("ScanOnline.run", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            }
        }
        messageToUser.info(String.valueOf(writeOnLineFile()), "writeOnLineFile: ", " = " + onlinesFile.getAbsolutePath());
    }

    private void setMaxOnlineListFromFile() {
        try {
            this.maxOnList = setMaxOnlineFromDatabase();
        }
        catch (RuntimeException e) {
            this.maxOnList = FileSystemWorker.readFileToList(fileMAXOnlines.getAbsolutePath());
        }
    }

    private void onListFileCopyToLastAndMax() throws InvokeIllegalException {
        File scanOnlineLast = new File(replaceFileNamePattern);
        if (!scanOnlineLast.exists()) {
            FileSystemWorker.copyOrDelFile(onlinesFile, Paths.get(replaceFileNamePattern).toAbsolutePath().normalize(), false);
        }
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);

        if (onLastAsTreeSet.size() < NetKeeper.getDequeOfOnlineDev().size()) {
            FileSystemWorker.copyOrDelFile(onlinesFile, Paths.get(replaceFileNamePattern).toAbsolutePath().normalize(), false);
        }
        if (scanOnlineLast.length() > fileMAXOnlines.length()) {
            messageToUser.warn(onlinesFile.getName(), scanOnlineLast.getName() + " size difference", " = " + (scanOnlineLast.length() - scanOnlineLast.length()));
            scanOnlineLastBigger();
            boolean isCopyOk = FileSystemWorker.copyOrDelFile(scanOnlineLast, Paths.get(fileMAXOnlines.getAbsolutePath()).toAbsolutePath().normalize(), false);
        }
        scanOnlineLast.deleteOnExit();
    }

    private boolean writeOnLineFile() {
        boolean retBool;
        try {
            Files.deleteIfExists(onlinesFile.toPath());
        }
        catch (IOException e) {
            onlinesFile.deleteOnExit();
        }
        try (OutputStream outputStream = new FileOutputStream(onlinesFile);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            Deque<InetAddress> onDeq = NetKeeper.getDequeOfOnlineDev();
            printStream.println("Checked: " + new Date());
            while (!onDeq.isEmpty()) {
                InetAddress inetAddrPool = onDeq.poll();
                printStream.println(inetAddrPool.toString() + " " + NetScanService.isReach(inetAddrPool.getHostAddress()));
            }
            retBool = true;
        }
        catch (IOException e) {
            retBool = false;
        }
        return retBool;
    }

    @Override
    public String writeLog() {
        String s = String.valueOf(writeOnLineFile());
        MessageToUser.getInstance(MessageToUser.DB, getClass().getSimpleName()).info(getClass().getSimpleName(), "scan complete", s);
        return s;
    }

    /**
     когда размер в байтах файла ScanOnline.last, больше чем \lan\max.online, добавить содержание max.online в список maxOnList

     @since 12.07.2019 (22:56)
     */
    protected List<String> scanOnlineLastBigger() {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT pcName FROM lan.online");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                maxOnList.add(resultSet.getString(ConstantsFor.DBFIELD_PCNAME));
            }
        }
        catch (SQLException e) {
            messageToUser.warn(ScanOnline.class.getSimpleName(), e.getMessage(), " see line: 178 ***");
        }
        Collections.sort(maxOnList);
        return maxOnList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>Since ");
        sb.append("<i>");
        sb.append(new Date(InitProperties.getUserPref().getLong(ExecScan.class.getSimpleName(), UsefulUtilities.getMyTime())));
        sb.append(" last ExecScan: ");
        sb.append("</i>");
        sb.append(tvInfo.getInfoAbout("tv"));
        sb.append("</b><br><br>");
        sb.append("<details><summary>Максимальное кол-во онлайн адресов: ").append(maxOnList.size()).append("</summary>")
            .append(tForms.fromArray(maxOnList, true))
            .append(ConstantsFor.HTMLTAG_DETAILSCLOSE);
        sb.append("<b>ipconfig /flushdns = </b>").append(new String(UsefulUtilities.ipFlushDNS().getBytes(), Charset.forName(ConstantsFor.CHARSET_IBM866)))
            .append("<br>");
        return sb.toString();
    }

    @Override
    public List<String> pingDevices(@NotNull Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        List<String> pingedDevices = new ArrayList<>();
        for (Map.Entry<InetAddress, String> addressStringEntry : ipAddressAndDeviceNameToShow.entrySet()) {
            String entryValue = addressStringEntry.getValue();
            InetAddress entryKey = addressStringEntry.getKey();
            boolean reach = NetScanService.isReach(entryKey.getHostAddress());
            pingedDevices.add(entryValue + " " + reach);
        }
        Collections.sort(pingedDevices);
        return pingedDevices;
    }

    @Override
    public String getExecution() {
        return FileSystemWorker.readFile(onlinesFile.getAbsolutePath());
    }

    @Override
    public String getPingResultStr() {
        Deque<InetAddress> address = NetKeeper.getDequeOfOnlineDev();
        return tForms.fromArray(address, true);
    }

    @Contract(pure = true)
    @NotNull
    private List<String> setMaxOnlineFromDatabase() {
        List<String> retList = new ArrayList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT ip FROM online");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                retList.add(resultSet.getString("ip"));
            }
        }
        catch (SQLException e) {
            messageToUser.warn(ScanOnline.class.getSimpleName(), e.getMessage(), " see line: 121 ***");
        }
        return retList;
    }

    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }

    @Override
    public String getStatistics() {
        Set<String> filesOnLineRead = new TreeSet<>(NetKeeper.getCurrentScanLists());
        return tForms.fromArray(filesOnLineRead, true);
    }

    private void initialMeth() throws InvokeIllegalException {
        if (ConstantsFor.onRunOn(ConstantsFor.REGRUHOSTING_PC)) {
            throw new InvokeIllegalException(UsefulUtilities.thisPC());
        }
        this.onlinesFile = new File(FileNames.ONSCAN);
        this.replaceFileNamePattern = onlinesFile.getName().toLowerCase().replace(".onlist", ".last");
        String fileMaxName = FileNames.ONLINES_MAX;
        this.fileMAXOnlines = new File(fileMaxName);

        maxOnList = FileSystemWorker.readFileToList(fileMAXOnlines.getAbsolutePath());
    }

}
