// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 Пингует заданные адреса.
 <p>
 Список адресов загружается как текстовый файл, который читаем построчно.
 
 @since 08.02.2019 (9:34) */
@SuppressWarnings("unused")
@Service(ModelAttributeNames.ATT_NETPINGER)
public class PingerFromFile implements NetScanService {
    
    
    private static final String STR_METH_PINGSW = "NetPinger.pingSW";
    
    /**
     Лист {@link InetAddress}.
     <p>
     Адреса, для пингера из {@link #multipartFile}
     */
    private final List<InetAddress> ipAsList = new ArrayList<>();
    
    /**
     Лист результатов.
     <p>
     {@link String} - 1 результат.
     */
    private final List<String> resultsList = new ArrayList<>();
    
    /**
     Таймаут метода {@link #pingSW()}.
     <p>
     Берётся из {@link InitProperties#getTheProps()}. В <b>миллисекундах</b>. По-умолчанию 20 мсек.
 
     @see PropertiesNames#PINGSLEEP
     */
    private long pingSleepMsec = Long.parseLong(InitProperties.getTheProps().getProperty(PropertiesNames.PINGSLEEP, "20"));
    
    /**
     {@link MessageLocal}. Вывод сообщений
     */
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PingerFromFile.class.getSimpleName());
    
    private String timeForScanStr = String.valueOf(TimeUnit.SECONDS.toMinutes(Math.abs(LocalTime.parse("08:30").toSecondOfDay() - LocalTime.now().toSecondOfDay())));
    
    /**
     Результат работы, как {@link String}
     */
    private String pingResultStr = "No result yet";
    
    /**
     Время до конца работы.
     */
    private String timeToEndStr = "0";
    
    private long timeStartLong = System.currentTimeMillis();
    
    private MultipartFile multipartFile;
    
    public String getTimeForScanStr() {
        return timeForScanStr;
    }
    
    /**
     @param timeForScanStr {@link #timeForScanStr}
     */
    public void setTimeForScanStr(String timeForScanStr) {
        this.timeForScanStr = timeForScanStr;
    }
    
    public List<String> getResultsList() {
        return Collections.unmodifiableList(resultsList);
    }
    
    /**
     @return {@link #multipartFile}
     */
    public MultipartFile getMultipartFile() {
        return multipartFile;
    }
    
    /**
     @param multipartFile {@link #multipartFile}
     */
    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(resultsList.size()).append(" size of result list<br>");
        stringBuilder.append(timeToEndStr).append(" time to end.<p>");
        stringBuilder.append(UsefulUtilities.getRunningInformation().replace("\n", "<br>")).append("<p>");
        return stringBuilder.toString();
    }
    
    @Override
    public String writeLog() {
        throw new TODOException("Make NetPingerServiceFactory.writeLogToFile 21.07.2019 (13:30)");
    }
    
    /**
     @return {@link #timeToEndStr}
     */
    @Override
    public String getExecution() {
        return timeToEndStr;
    }
    
    /**
     @return {@link #pingResultStr}
     */
    @Override
    public String getPingResultStr() {
        return pingResultStr;
    }
    
    @Override
    public List<String> pingDevices(@NotNull Map<InetAddress, String> ipAddressAndDeviceNameToPing) {
        ipAddressAndDeviceNameToPing.forEach((key, value)->{
            boolean ipIsReach = NetScanService.isReach(key.getHostAddress());
            String toListAdd = ipIsReach ? MessageFormat.format("{0} {1} is online.", key.toString(), value) : MessageFormat
                .format("{0} {1} is offline.", key.toString(), value);
            resultsList.add(toListAdd);
        });
        return resultsList;
    }
    
    @Override
    public void run() {
        final long startSt = System.currentTimeMillis();
        if (multipartFile != null) {
            parseFile();
        }
        else {
            throw new InvokeIllegalException(MessageFormat.format("{0} - multipartFile is not set", this.getClass().getSimpleName()));
        }
        long userIn;
        try {
            userIn = TimeUnit.MINUTES.toMillis(Long.parseLong(timeForScanStr));
        }
        catch (NumberFormatException e) {
            userIn = 2000;
        }
        long totalMillis = startSt + userIn;
        while (System.currentTimeMillis() < totalMillis) {
            pingSW();
            this.timeToEndStr = getClass().getSimpleName() + " left " + (float) TimeUnit.MILLISECONDS
                .toSeconds(totalMillis - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN;
            messageToUser.infoNoTitles(timeToEndStr);
        }
        this.pingResultStr = new TForms().fromArray(resultsList, true);
        messageToUser.infoNoTitles(pingResultStr);
        parseResult(userIn);
    }
    
    private void parseFile() {
        try (InputStream inputStream = multipartFile.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            while (bufferedReader.ready()) {
                bufferedReader.lines().forEach(this::parseAddr);
            }
            if (ipAsList.size() == 0) {
                ipAsList.add(InetAddress.getLoopbackAddress());
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".parseFile", e));
        }
    }
    
    private void pingSW() {
        Properties properties = InitProperties.getTheProps();
        this.pingSleepMsec = Long.parseLong(properties.getProperty(PropertiesNames.PINGSLEEP, String.valueOf(pingSleepMsec)));
        for (InetAddress inetAddress : ipAsList) {
            try {
                resultsList.add(inetAddress + " is " + inetAddress.isReachable((int) pingSleepMsec));
                Thread.sleep(pingSleepMsec);
            }
            catch (IOException | InterruptedException e) {
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void parseResult(long userIn) {
        Set<String> pingsList = new HashSet<>();
        pingsList.add("Pinger is start at " + new Date(System.currentTimeMillis() - userIn));
        resultsList.stream().distinct().forEach(x->{
            int frequency = Collections.frequency(resultsList, x);
            pingsList.add(frequency + " times " + x + "\n");
        });
        pingsList.add(((float) TimeUnit.MILLISECONDS.toMinutes(userIn) / ConstantsFor.ONE_HOUR_IN_MIN) + " hours spend");
        FileSystemWorker.writeFile(FileNames.PINGRESULT, pingsList.stream());
        MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName()).info(AbstractForms.fromArray(pingsList));
    }
    
    private void parseAddr(String readLine) {
        try {
            ipAsList.add(InetAddress.getByName(readLine));
        }
        catch (UnknownHostException | RuntimeException e) {
            ipAsList.add(ipIsIP(readLine));
        }
    }
    
    private InetAddress ipIsIP(String readLine) {
        
        InetAddress resolvedAddress = InetAddress.getLoopbackAddress();
        try {
            byte[] addressBytes = InetAddress.getByName(readLine).getAddress();
            resolvedAddress = InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            messageToUser.error(e.getMessage());
        }
        return resolvedAddress;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetPinger{");
        sb.append("pingResultStr='").append(pingResultStr).append('\'');
        sb.append(", pingSleepMsec=").append(pingSleepMsec);
        sb.append(", timeToEndStr='").append(timeToEndStr).append('\'');
        sb.append(TimeUnit.SECONDS.toMinutes(LocalTime.now().toSecondOfDay())).append("-")
            .append(TimeUnit.SECONDS.toMinutes(LocalTime.parse("08:30").toSecondOfDay())).append(" (08:30)")
            .append(String.valueOf((LocalTime.now().toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay())))
            .append(" = ").append(timeForScanStr);
        sb.append('}');
        return sb.toString();
    }
}
