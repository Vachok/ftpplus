package ru.vachok.networker.net;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 Пингует заданные адреса.
 <p>
 Список адресов загружается как текстовый файл, который читаем построчно.

 @since 08.02.2019 (9:34) */
@SuppressWarnings("unused")
@Service("netPinger")
public class NetPinger implements Runnable, Pinger {

    /**
     NetPinger
     */
    private static final String STR_CLASSNAME = "NetPinger";

    /**
     NetPinger.pingSW
     */
    private static final String STR_METH_PINGSW = "NetPinger.pingSW";

    /**
     Таймаут метода {@link #pingSW()}.
     <p>
     Берётся из {@link AppComponents#getOrSetProps()}. В <b>миллисекундах</b>. По-умолчанию 20 мсек.

     @see ConstantsNet#PROP_PINGSLEEP
     */
    private long pingSleepMsec = Long.parseLong(AppComponents.getOrSetProps().getProperty(ConstantsNet.PROP_PINGSLEEP, "20"));

    /**
     Лист {@link InetAddress}.
     <p>
     Адреса, для пингера из {@link #multipartFile}
     */
    private final List<InetAddress> ipAsList = new ArrayList<>();

    /**
     {@link MessageLocal}. Вывод сообщений
     */
    private MessageToUser messageToUser = new MessageLocal(NetPinger.class.getSimpleName());

    /**
     Ввод минут из браузера. По-умолчанию 3.

     @see NetScanCtr#pingPost(org.springframework.ui.Model, javax.servlet.http.HttpServletRequest, ru.vachok.networker.net.NetPinger, javax.servlet.http.HttpServletResponse)
     */
    private String timeToScanStr = "3";

    /**
     Результат работы, как {@link String}
     */
    private String pingResultStr = "No result yet";

    /**
     Лист результатов.
     <p>
     {@link String} - 1 результат.
     */
    private final List<String> resList = new ArrayList<>();

    /**
     Время до конца работы.
     */
    private String timeToEndStr = "0";

    /**
     Файл, загружаемый из браузера.

     @see NetScanCtr#pingPost(org.springframework.ui.Model, javax.servlet.http.HttpServletRequest, ru.vachok.networker.net.NetPinger, javax.servlet.http.HttpServletResponse)
     */
    private MultipartFile multipartFile;

    /**
     @return {@link #timeToScanStr}
     */
    @SuppressWarnings("WeakerAccess")
    public String getTimeToScanStr() {
        return timeToScanStr;
    }

    /**
     @param timeToScanStr {@link #timeToScanStr}
     */
    public void setTimeToScanStr(String timeToScanStr) {
        this.timeToScanStr = timeToScanStr;
    }

    /**
     @return {@link #multipartFile}
     */
    @SuppressWarnings("WeakerAccess")
    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    /**
     @param multipartFile {@link #multipartFile}
     */
    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    /**
     Пингер.
     <p>
     После обработки {@link #multipartFile} и заполнения {@link #ipAsList}, пингуем адреса. Таймаут - {@link ConstantsFor#TIMEOUT_650}<br> Результат
     ({@link InetAddress#toString()} is
     {@link InetAddress#isReachable(int)}) добавляется в {@link #resList}.
     */
    private void pingSW() {
        Properties properties = AppComponents.getOrSetProps();
        this.pingSleepMsec = Long.parseLong(properties.getProperty(ConstantsNet.PROP_PINGSLEEP, String.valueOf(pingSleepMsec)));
        for (InetAddress inetAddress : ipAsList) {
            try {
                resList.add(inetAddress + " is " + inetAddress.isReachable(ConstantsFor.TIMEOUT_650));
                Thread.sleep(pingSleepMsec);
            } catch (IOException | InterruptedException e) {
                FileSystemWorker.error(getClass().getSimpleName() + ".pingSW", e);
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
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                bufferedReader.lines().forEach(this::parseAddr);
            }
        } catch (IOException e) {
            messageToUser.errorAlert(STR_CLASSNAME, "parseFile", e.getMessage());
            FileSystemWorker.error("NetPinger.parseFile", e);
        }
    }

    /**
     Парсинг результатов.
     <p>
     {@link Collection#stream()}.{@link Stream#distinct()}.{@link Stream#forEach(java.util.function.Consumer)}: Посчитаем кол-во уникальных элементов коллекции {@link #resList} через
     {@link Collections#frequency(java.util.Collection, java.lang.Object)} ({@code int frequency}) <br> Добавим в new {@link ArrayList}, результат - {@code int frequency} times {@code x}
     (уникальный элемент из {@link #resList}).
     <p>
     Записать результат в файл {@link FileSystemWorker#writeFile(java.lang.String, java.util.List)}. Файл - {@link ConstantsNet#PINGRESULT_LOG}. <br> Если пингер работал 3 и более минут,
     отправить отчёт на почту {@link ConstantsFor#MAILADDR_143500GMAILCOM} ({@link ESender#sendM(java.util.List, java.lang.String, java.lang.String)}) <br>

     @param userIn кол-во минут в мсек, которые пингер работал.
     */
    private void parseResult(long userIn) {
        Collection<String> pingsList = new ArrayList<>();
        pingsList.add("Pinger is start at " + new Date(System.currentTimeMillis() - userIn));
        resList.stream().distinct().forEach(x -> {
            int frequency = Collections.frequency(resList, x);
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
        } catch (UnknownHostException|RuntimeException e) {
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
    private InetAddress ipIsIP(String readLine)  {
        InetAddress resolvedAddress = InetAddress.getLoopbackAddress();
        try {
            byte[] addressBytes = InetAddress.getByName(readLine).getAddress();
            resolvedAddress=InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), ".ipIsIP", e.getMessage());
            FileSystemWorker.error(getClass().getSimpleName() + ".ipIsIP", e);
        }
        return resolvedAddress;
    }
    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetPinger pinger = (NetPinger) o;
        return pingSleepMsec == pinger.pingSleepMsec && Objects.equals(timeToScanStr, pinger.timeToScanStr) && Objects.equals(timeToEndStr, pinger.timeToEndStr) && Objects
            .equals(multipartFile, pinger.multipartFile);
    }
    
    @Override public int hashCode() {
        return Objects.hash(pingSleepMsec, timeToScanStr, timeToEndStr, multipartFile);
    }
    
    /**
     @return {@link #timeToEndStr}
     */
    @Override
    public String getTimeToEndStr() {
        return timeToEndStr;
    }

    /**
     @return {@link #pingResultStr}
     */
    public String getPingResultStr() {
        return pingResultStr;
    }

    @Override
    public boolean isReach(String inetAddrStr) {
        try {
            byte[] bytesAddr = InetAddress.getByName(inetAddrStr).getAddress();
            return InetAddress.getByAddress(bytesAddr).isReachable(ConstantsFor.TIMEOUT_650);
        } catch (IOException e) {
            FileSystemWorker.error("NetPinger.isReach", e);
            return false;
        }
    }

    /**
     Старт.
     <p>
     Если {@link #multipartFile} не null, 1. {@link #parseFile()}. <br> 2. {@link #getTimeToScanStr()}. Парсинг строки в {@link Long}. <br> 3. Пока {@link System#currentTimeMillis()} меньше
     чем время старта ({@code final long startSt}), запускать {@link #pingSW()}. Устанавливаем {@link ThreadConfig#thrNameSet(String)} -
     {@link ConstantsFor#getUpTime()}.<br> 4. {@link
    TForms#fromArray(java.util.List, boolean)}. Устанавливаем {@link #pingResultStr}, после сканирования. <br> 5. {@link #parseResult(long)}. Парсим результат пингера.
     <p>
     {@link #messageToUser}, выводит после каждого запуска {@link #pingSW()}, {@link MessageToUser#infoNoTitles(java.lang.String)}, остаток времени на пинг в минутах. <br> {@link
    #messageToUser}, после окончания пинга, вывести в консоль {@link #pingResultStr} <br> {@link #timeToEndStr} - переписываем значение.
     */
    @Override
    public void run() {
        final long startSt = System.currentTimeMillis();
        if (multipartFile != null) {
            parseFile();
        }
        long userIn = TimeUnit.MINUTES.toMillis(Long.parseLong(getTimeToScanStr()));
        long totalMillis = startSt + userIn;
        while (System.currentTimeMillis() < totalMillis) {
            pingSW();
            this.timeToEndStr = getClass().getSimpleName() + " left " + (float) TimeUnit.MILLISECONDS
                .toSeconds(totalMillis - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN;
            messageToUser.infoNoTitles(timeToEndStr);
        }
        this.pingResultStr = new TForms().fromArray(resList, true);
        messageToUser.infoNoTitles(pingResultStr);
        parseResult(userIn);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetPinger{");
        sb.append("pingResultStr='").append(pingResultStr).append('\'');
        sb.append(", pingSleepMsec=").append(pingSleepMsec);
        sb.append(", timeToEndStr='").append(timeToEndStr).append('\'');
        sb.append(", timeToScanStr='").append(timeToScanStr).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
