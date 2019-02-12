package ru.vachok.networker.net;


import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
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
public class NetPinger implements Runnable {

    /**
     NetPinger
     */
    private static final String CLASS_NAME = "NetPinger";

    /**
     NetPinger.pingSW
     */
    private static final String METH_PINGSW = "NetPinger.pingSW";

    /**
     * Лист {@link InetAddress}.
     <p>
     Адреса, для пингера из {@link #multipartFile}
     */
    private List<InetAddress> ipAsList = new ArrayList<>();

    /**
     * {@link MessageLocal}. Вывод сообщений
     */
    private MessageToUser messageToUser = new MessageLocal();

    /**
     * Ввод минут из браузера. По-умолчанию 3.
     *
     @see NetScanCtr#pingPost(org.springframework.ui.Model, javax.servlet.http.HttpServletRequest, ru.vachok.networker.net.NetPinger)
     */
    private String timeToScan = "3";

    /**
     * Результат работы, как {@link String}
     */
    private String pingResult = "No result yet";

    /**
     * Лист результатов.
     <p>
     {@link String} - 1 результат.
     */
    private List<String> resList = new ArrayList<>();

    /**
     * Файл, загружаемый из браузера.
     * @see NetScanCtr#pingPost(org.springframework.ui.Model, javax.servlet.http.HttpServletRequest, ru.vachok.networker.net.NetPinger)
     */
    private MultipartFile multipartFile = null;

    /**
     @return {@link #pingResult}
     */
    public String getPingResult() {
        return pingResult;
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
     * Парсинг {@link #multipartFile}.
     <p>
     Читаем через {@link BufferedReader#lines()}.{@link Stream#forEach(java.util.function.Consumer)} {@link #multipartFile}, и преобразуем в {@link InetAddress}, через {@link #parseAddr(String)}. <br>
     <b>{@link IOException}:</b><br>
     1. {@link MessageLocal#errorAlert(java.lang.String, java.lang.String, java.lang.String)}
     2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
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

    /**
     * Пингер.
     <p>
     После обработки {@link #multipartFile} и заполнения {@link #ipAsList}, пингуем адреса. Таймаут - {@link ConstantsFor#TIMEOUT_650}<br>
     Результат ({@link InetAddress#toString()} is {@link InetAddress#isReachable(int)}) добавляется в {@link #resList}.
     */
    private void pingSW() {
        for (InetAddress inetAddress : ipAsList) {
            try {
                resList.add(inetAddress.toString() + " is " + inetAddress.isReachable(ConstantsFor.TIMEOUT_650));
            } catch (IOException e) {
                messageToUser.errorAlert(CLASS_NAME, "pingSW", e.getMessage());
                FileSystemWorker.error(METH_PINGSW, e);
            }
        }
    }

    /**
     Парсинг результатов.
     <p>
     {@link Collection#stream()}.{@link Stream#distinct()}.{@link Stream#forEach(java.util.function.Consumer)}:
     Посчитаем кол-во уникальных элементов коллекции {@link #resList} через {@link Collections#frequency(java.util.Collection, java.lang.Object)} ({@code int frequency}) <br>
     Добавим в new {@link ArrayList}, результат - {@code int frequency} times {@code x} (уникальный элемент из {@link #resList}).
     <p>
     Записать результат в файл {@link FileSystemWorker#recFile(java.lang.String, java.util.List)}. Файл - {@link ConstantsNet#PINGRESULT_LOG}. <br>
     Если пингер работал 3 и более минут, отправить отчёт на почту {@link ConstantsFor#GMAIL_COM} ({@link ESender#sendM(java.util.List, java.lang.String, java.lang.String)}) <br>
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
        pingsList.add("Total - " + ((float) TimeUnit.MILLISECONDS.toMinutes(userIn) / ConstantsFor.ONE_HOUR_IN_MIN) + " hours spend");
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
     try: {@link InetAddress#getByName(java.lang.String)} <br>
     catch {@link UnknownHostException}: {@link #ipIsIP(String)}.
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
     Если в {@link #parseAddr(java.lang.String)}, возникло исключение, пробуем преобразовать строку из <i>х.х.х.х</i>. <br>
     {@link InetAddress#getAddress()} - делаем байты из строки. <br>
     {@link InetAddress#getByAddress(byte[])} пробуем преобразовать байты в {@link InetAddress}
     <p>
     <b>{@link UnknownHostException}:</b><br>
     1. {@link MessageToUser#errorAlert(java.lang.String, java.lang.String, java.lang.String)} <br>
     2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} <br>
     throw new {@link IllegalStateException}.

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
     * Старт.
     <p>
     Если {@link #multipartFile} не null, 1. {@link #parseFile()}. <br>
     2. {@link #getTimeToScan()}. Парсинг строки в {@link Long}. <br>
     3. Пока {@link System#currentTimeMillis()} меньше чем время старта ({@code final long startSt}), запускать {@link #pingSW()}.
     Устанавливаем {@link Thread#setName(java.lang.String)} - {@link ConstantsFor#getUpTime()}.<br>
     4. {@link TForms#fromArray(java.util.List, boolean)}. Устанавливаем {@link #pingResult}, после сканирования. <br>
     5. {@link #parseResult(long)}. Парсим результат пингера.
     <p>
     {@link #messageToUser}, выводит после каждого запуска {@link #pingSW()}, {@link MessageToUser#infoNoTitles(java.lang.String)}, остаток времени на пинг в минутах. br
     {@link #messageToUser}, после окончания пинга, вывести в консоль {@link #pingResult}
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
            messageToUser.infoNoTitles(getClass().getSimpleName() + " left " +
                (float) TimeUnit.MILLISECONDS.toSeconds(totalMillis - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN);
        }
        this.pingResult = new TForms().fromArray(resList, true);
        messageToUser.infoNoTitles(pingResult);
        parseResult(userIn);
    }
}
