package ru.vachok.networker.net;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
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
@Service
public class NetPinger implements Runnable, Pinger {

    /**
     NetPinger
     */
    private static final String CLASS_NAME = "NetPinger";

    /**
     NetPinger.pingSW
     */
    private static final String METH_PINGSW = "NetPinger.pingSW";

    /**
     Таймаут метода {@link #pingSW()}.
     <p>
     Берётся из {@link ConstantsFor#getProps()}. В <b>миллисекундах</b>. По-умолчанию 20 мсек.

     @see ConstantsNet#PROP_PINGSLEEP
     */
    private long pingSleep = Long.parseLong(AppComponents.getProps().getProperty(ConstantsNet.PROP_PINGSLEEP, "20"));

    /**
     Лист {@link InetAddress}.
     <p>
     Адреса, для пингера из {@link #multipartFile}
     */
    private List<InetAddress> ipAsList = new ArrayList<>();

    /**
     {@link MessageLocal}. Вывод сообщений
     */
    private MessageToUser messageToUser = new MessageLocal();

    /**
     Ввод минут из браузера. По-умолчанию 3.

     @see NetScanCtr#pingPost(org.springframework.ui.Model, javax.servlet.http.HttpServletRequest, ru.vachok.networker.net.NetPinger, javax.servlet.http.HttpServletResponse)
     */
    private String timeToScan = "3";

    /**
     Результат работы, как {@link String}
     */
    private String pingResult = "No result yet";

    /**
     Лист результатов.
     <p>
     {@link String} - 1 результат.
     */
    private List<String> resList = new ArrayList<>();

    /**
     Время до конца работы.
     */
    private String timeToEnd = "0";

    /**
     Файл, загружаемый из браузера.

     @see NetScanCtr#pingPost(org.springframework.ui.Model, javax.servlet.http.HttpServletRequest, ru.vachok.networker.net.NetPinger, javax.servlet.http.HttpServletResponse)
     */
    private MultipartFile multipartFile = null;

    /**
     @return {@link #pingResult}
     */
    @SuppressWarnings("WeakerAccess")
    public String getPingResult() {
        return pingResult;
    }

    /**
     Пингер.
     <p>
     После обработки {@link #multipartFile} и заполнения {@link #ipAsList}, пингуем адреса. Таймаут - {@link ConstantsFor#TIMEOUT_650}<br> Результат
     ({@link InetAddress#toString()} is
     {@link InetAddress#isReachable(int)}) добавляется в {@link #resList}.
     */
    private void pingSW() {
        final Properties properties = AppComponents.getProps();
        properties.setProperty(ConstantsNet.PROP_PINGSLEEP, pingSleep + "");
        for(InetAddress inetAddress : ipAsList){
            try{
                resList.add(inetAddress.toString() + " is " + inetAddress.isReachable(ConstantsFor.TIMEOUT_650));
                Thread.sleep(pingSleep);
            }
            catch(IOException | InterruptedException e){
                messageToUser.errorAlert(CLASS_NAME, "pingSW", e.getMessage());
                FileSystemWorker.error(METH_PINGSW, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     @return {@link #timeToScan}
     */
    @SuppressWarnings("WeakerAccess")
    public String getTimeToScan() {
        return timeToScan;
    }

    /**
     @param timeToScan {@link #timeToScan}
     */
    public void setTimeToScan(String timeToScan) {
        this.timeToScan = timeToScan;
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
     @return {@link #timeToEnd}
     */
    @Override
    public String getTimeToEnd() {
        return timeToEnd;
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
            messageToUser.errorAlert(CLASS_NAME, "parseFile", e.getMessage());
            FileSystemWorker.error("NetPinger.parseFile", e);
        }
    }

    @Override
    public boolean isReach(String inetAddrStr) {
        try{
            byte[] bytesAddr = InetAddress.getByName(inetAddrStr).getAddress();
            return InetAddress.getByAddress(bytesAddr).isReachable(ConstantsFor.TIMEOUT_650);
        }
        catch(IOException e){
            FileSystemWorker.error("NetPinger.isReach", e);
            return false;
        }
    }

    /**
     Парсинг результатов.
     <p>
     {@link Collection#stream()}.{@link Stream#distinct()}.{@link Stream#forEach(java.util.function.Consumer)}: Посчитаем кол-во уникальных элементов коллекции {@link #resList} через
     {@link Collections#frequency(java.util.Collection, java.lang.Object)} ({@code int frequency}) <br> Добавим в new {@link ArrayList}, результат - {@code int frequency} times {@code x}
     (уникальный элемент из {@link #resList}).
     <p>
     Записать результат в файл {@link FileSystemWorker#recFile(java.lang.String, java.util.List)}. Файл - {@link ConstantsNet#PINGRESULT_LOG}. <br> Если пингер работал 3 и более минут,
     отправить отчёт на почту {@link ConstantsFor#GMAIL_COM} ({@link ESender#sendM(java.util.List, java.lang.String, java.lang.String)}) <br>

     @param userIn кол-во минут в мсек, которые пингер работал.
     */
    private void parseResult(long userIn) {
        List<String> pingsList = new ArrayList<>();
        pingsList.add("Pinger is start at " + new Date(System.currentTimeMillis() - userIn));
        resList.stream().distinct().forEach(x -> {
            int frequency = Collections.frequency(resList, x);
            pingsList.add(frequency + " times " + x + "\n");
        });
        FileSystemWorker.recFile(ConstantsNet.PINGRESULT_LOG, pingsList);
        messageToUser = new MessageFile();
        pingsList.add(((float) TimeUnit.MILLISECONDS.toMinutes(userIn) / ConstantsFor.ONE_HOUR_IN_MIN) + " hours spend");
        if (userIn >= TimeUnit.MINUTES.toMillis(3)) {
            try {
                ESender.sendM(Collections.singletonList(ConstantsFor.GMAIL_COM), getClass().getSimpleName(), new TForms().fromArray(pingsList, false));
            } catch (Exception e) {
                FileSystemWorker.error("NetPinger.parseResult", e);
            }
        }
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
        InetAddress byName;
        try {
            byName = InetAddress.getByName(readLine);
        } catch (UnknownHostException e) {
            byName = ipIsIP(readLine);
        }
        ipAsList.add(byName);
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
        try {
            byte[] address = InetAddress.getByName(readLine).getAddress();
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            messageToUser.errorAlert(CLASS_NAME, "ipIsIP", e.getMessage());
            FileSystemWorker.error("NetPinger.ipIsIP", e);
            throw new IllegalStateException();
        }
    }

    /**
     Старт.
     <p>
     Если {@link #multipartFile} не null, 1. {@link #parseFile()}. <br> 2. {@link #getTimeToScan()}. Парсинг строки в {@link Long}. <br> 3. Пока {@link System#currentTimeMillis()} меньше
     чем время старта ({@code final long startSt}), запускать {@link #pingSW()}. Устанавливаем {@link Thread#setName(java.lang.String)} - {@link ConstantsFor#getUpTime()}.<br> 4. {@link
    TForms#fromArray(java.util.List, boolean)}. Устанавливаем {@link #pingResult}, после сканирования. <br> 5. {@link #parseResult(long)}. Парсим результат пингера.
     <p>
     {@link #messageToUser}, выводит после каждого запуска {@link #pingSW()}, {@link MessageToUser#infoNoTitles(java.lang.String)}, остаток времени на пинг в минутах. <br> {@link
    #messageToUser}, после окончания пинга, вывести в консоль {@link #pingResult} <br> {@link #timeToEnd} - переписываем значение.
     */
    @Override
    public void run() {
        final long startSt = System.currentTimeMillis();
        if (multipartFile != null) {
            parseFile();
        }
        long userIn = TimeUnit.MINUTES.toMillis(Long.parseLong(getTimeToScan()));
        long totalMillis = startSt + userIn;
        while (System.currentTimeMillis() < totalMillis) {
            pingSW();
            Thread.currentThread().setName(ConstantsFor.getUpTime());
            this.timeToEnd = getClass().getSimpleName() + " left " + (float) TimeUnit.MILLISECONDS
                .toSeconds(totalMillis - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN;
            messageToUser.infoNoTitles(timeToEnd);
        }
        this.pingResult = new TForms().fromArray(resList, true);
        messageToUser.infoNoTitles(pingResult);
        parseResult(userIn);
    }

    @Override
    public int hashCode() {
        int result = (int) (pingSleep ^ (pingSleep >>> 32));
        result = 31 * result + ipAsList.hashCode();
        result = 31 * result + messageToUser.hashCode();
        result = 31 * result + getTimeToScan().hashCode();
        result = 31 * result + getPingResult().hashCode();
        result = 31 * result + resList.hashCode();
        result = 31 * result + getTimeToEnd().hashCode();
        result = 31 * result + (getMultipartFile() != null ? getMultipartFile().hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NetPinger netPinger = (NetPinger) o;

        return pingSleep == netPinger.pingSleep &&
            ipAsList.equals(netPinger.ipAsList) &&
            messageToUser.equals(netPinger.messageToUser) &&
            getTimeToScan().equals(netPinger.getTimeToScan()) &&
            getPingResult().equals(netPinger.getPingResult()) &&
            resList.equals(netPinger.resList) &&
            getTimeToEnd().equals(netPinger.getTimeToEnd()) &&
            (getMultipartFile() != null ? getMultipartFile().equals(netPinger.getMultipartFile()) : netPinger.getMultipartFile() == null);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetPinger{");
        char c = '\'';
        sb.append("CLASS_NAME='").append(CLASS_NAME).append(c);
        sb.append(", ipAsList=").append(ipAsList.size());
        sb.append(ConstantsFor.TOSTRING_MESSAGE_TO_USER).append(messageToUser.toString());
        sb.append(", METH_PINGSW='").append(METH_PINGSW).append(c);
        sb.append(", multipartFile=").append(multipartFile.getName());
        sb.append(", pingResult='").append(pingResult).append(c);
        sb.append(", pingSleep=").append(pingSleep);
        sb.append(", PROP_PINGSLEEP='").append(ConstantsNet.PROP_PINGSLEEP).append(c);
        sb.append(", resList=").append(resList.size());
        sb.append(", timeToEnd='").append(timeToEnd).append(c);
        sb.append(", timeToScan='").append(timeToScan).append(c);
        sb.append('}');
        return sb.toString();
    }
}
