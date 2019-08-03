// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.componentsrepo.exceptions.ScanFilesException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

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
import java.util.stream.Stream;


/**
 Пингует заданные адреса.
 <p>
 Список адресов загружается как текстовый файл, который читаем построчно.
 
 @since 08.02.2019 (9:34) */
@SuppressWarnings("unused")
@Service(ConstantsFor.ATT_NETPINGER)
public class LongNetScanServiceFactory extends AbstractNetworkerFactory implements NetScanService {
    
    
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
     Берётся из {@link AppComponents#getProps()}. В <b>миллисекундах</b>. По-умолчанию 20 мсек.
     
     @see ConstantsFor#PR_PINGSLEEP
     */
    private long pingSleepMsec = Long.parseLong(AppComponents.getProps().getProperty(ConstantsFor.PR_PINGSLEEP, "20"));
    
    /**
     {@link MessageLocal}. Вывод сообщений
     */
    private MessageToUser messageToUser = new MessageLocal(LongNetScanServiceFactory.class.getSimpleName());
    
    private String timeForScanStr = String.valueOf(TimeUnit.SECONDS.toMinutes(Math.abs(LocalTime.parse("08:30").toSecondOfDay() - LocalTime.now().toSecondOfDay())));
    
    /**
     Результат работы, как {@link String}
     */
    private String pingResultStr = "No result yet";
    
    public String getTimeForScanStr() {
        return timeForScanStr;
    }
    
    /**
     Время до конца работы.
     */
    private String timeToEndStr = "0";
    
    private long timeStartLong = System.currentTimeMillis();
    
    private MultipartFile multipartFile;
    
    public List<String> getResultsList() {
        return Collections.unmodifiableList(resultsList);
    }
    
    /**
     @param timeForScanStr {@link #timeForScanStr}
     */
    public void setTimeForScanStr(String timeForScanStr) {
        this.timeForScanStr = timeForScanStr;
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
        stringBuilder.append(AbstractNetworkerFactory.getApplicationRunInformation().replace("\n", "<br>")).append("<p>");
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
    public String getPingResultStr() {
        return pingResultStr;
    }
    
    @Override
    public List<String> pingDevices(@NotNull Map<InetAddress, String> ipAddressAndDeviceNameToPing) {
        ipAddressAndDeviceNameToPing.forEach((key, value)->{
            boolean ipIsReach = this.isReach(key);
            String toListAdd = ipIsReach ? MessageFormat.format("{0} {1} is online.", key.toString(), value) : MessageFormat
                .format("{0} {1} is offline.", key.toString(), value);
            resultsList.add(toListAdd);
        });
        return resultsList;
    }
    
    @Override
    public boolean isReach(@NotNull InetAddress inetAddrStr) {
        try {
            return inetAddrStr.isReachable(ConstantsFor.TIMEOUT_650);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("NetPingerService.isReach: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    @Override
    public void run() {
        final long startSt = System.currentTimeMillis();
        if (multipartFile != null) {
            parseFile();
        }
        else {
            throw new ScanFilesException("File not set...\n" + new TForms().fromArray(Thread.currentThread().getStackTrace()));
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
    
    /**
     Пингер.
     <p>
     После обработки {@link #multipartFile} и заполнения {@link #ipAsList}, пингуем адреса. Таймаут - {@link ConstantsFor#TIMEOUT_650}<br> Результат
     ({@link InetAddress#toString()} is
     {@link InetAddress#isReachable(int)}) добавляется в {@link #resultsList}.
     */
    private void pingSW() {
        Properties properties = AppComponents.getProps();
        this.pingSleepMsec = Long.parseLong(properties.getProperty(ConstantsFor.PR_PINGSLEEP, String.valueOf(pingSleepMsec)));
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
    
    /**
     Парсинг {@link #multipartFile}.
     <p>
     Читаем через {@link BufferedReader#lines()}.{@link Stream#forEach(java.util.function.Consumer)} {@link #multipartFile}, и преобразуем в {@link InetAddress}, через {@link
    #parseAddr(String)}. <br>
     <b>{@link IOException}:</b><br>
     1. {@link MessageLocal#errorAlert(java.lang.String, java.lang.String, java.lang.String)} 2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
     */
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
    
    /**
     Парсинг результатов.
     <p>
     {@link Collection#stream()}.{@link Stream#distinct()}.{@link Stream#forEach(java.util.function.Consumer)}: Посчитаем кол-во уникальных элементов коллекции {@link #resultsList} через
     {@link Collections#frequency(java.util.Collection, java.lang.Object)} ({@code int frequency}) <br> Добавим в new {@link ArrayList}, результат - {@code int frequency} times {@code x}
     (уникальный элемент из {@link #resultsList}).
     <p>
     Записать результат в файл {@link FileSystemWorker#writeFile(java.lang.String, java.util.List)}. Файл - {@link ConstantsNet#PINGRESULT_LOG}. <br> Если пингер работал 3 и более минут,
     отправить отчёт на почту {@link ConstantsFor#MAILADDR_143500GMAILCOM} ({@link ESender#sendM(java.util.List, java.lang.String, java.lang.String)}) <br>
     
     @param userIn кол-во минут в мсек, которые пингер работал.
     */
    private void parseResult(long userIn) {
        Set<String> pingsList = new HashSet<>();
        pingsList.add("Pinger is start at " + new Date(System.currentTimeMillis() - userIn));
        resultsList.stream().distinct().forEach(x->{
            int frequency = Collections.frequency(resultsList, x);
            pingsList.add(frequency + " times " + x + "\n");
        });
        pingsList.add(((float) TimeUnit.MILLISECONDS.toMinutes(userIn) / ConstantsFor.ONE_HOUR_IN_MIN) + " hours spend");
        FileSystemWorker.writeFile(ConstantsNet.PINGRESULT_LOG, pingsList.stream());
    }
    
    /**
     Заполнение {@link #ipAsList}.
     <p>
     try: {@link InetAddress#getByName(java.lang.String)} <br> catch {@link UnknownHostException}: {@link #ipIsIP(String)}.
     <p>
     Добавляет адрес в {@link #ipAsList}.
     
     @param readLine строка из {@link #multipartFile}
     @see #parseFile()
     */
    private void parseAddr(String readLine) {
        try {
            ipAsList.add(InetAddress.getByName(readLine));
        }
        catch (UnknownHostException | RuntimeException e) {
            ipAsList.add(ipIsIP(readLine));
        }
    }
    
    /**
     Разбор IP-адреса, если строке не hostname.
     <p>
     Если в {@link #parseAddr(java.lang.String)}, возникло исключение, пробуем преобразовать строку из <i>х.х.х.х</i>. <br> {@link InetAddress#getAddress()} - делаем байты из строки.
     <br> {@link InetAddress#getByAddress(byte[])} пробуем преобразовать байты в {@link InetAddress}
     <p>
     <b>{@link UnknownHostException}:</b><br>
     1. {@link MessageToUser#errorAlert(java.lang.String, java.lang.String, java.lang.String)} <br> 2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} <br> throw
     new {@link IllegalStateException}.
     
     @param readLine строка из {@link #multipartFile}
     @return {@link InetAddress#getByAddress(byte[])}
     */
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
}
