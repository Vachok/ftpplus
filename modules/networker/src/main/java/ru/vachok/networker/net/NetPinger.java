package ru.vachok.networker.net;


import org.springframework.web.multipart.MultipartFile;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 Пингует заданные адреса.
 <p>

 @since 08.02.2019 (9:34) */
public class NetPinger implements Runnable {

    private static final String CLASS_NAME = "NetPinger";

    private static final String METH_PINGSW = "NetPinger.pingSW";

    private List<InetAddress> ipAsList = new ArrayList<>();

    private MessageToUser messageToUser = new MessageLocal();

    private String timeToScan;

    private String pingResult = "No result yet";

    private List<String> resList = new ArrayList<>();

    private MultipartFile multipartFile = null;

    public String getPingResult() {
        return pingResult;
    }

    public String getTimeToScan() {
        return timeToScan;
    }

    public void setTimeToScan(String timeToScan) {
        this.timeToScan = timeToScan;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    private void parseFile() {
        try (InputStream inputStream = multipartFile.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                bufferedReader.lines().forEach(this::parseAddr);
            }
        } catch (IOException e) {
            new MessageLocal().errorAlert(CLASS_NAME, "parseFile", e.getMessage());
            FileSystemWorker.error("NetPinger.parseFile", e);
        }
    }

    private void parseAddr(String readLine) {
        InetAddress byName;
        try {
            byName = InetAddress.getByName(readLine);
        } catch (UnknownHostException e) {
            byName = ipIsIP(readLine);
        }
        ipAsList.add(byName);
    }

    private InetAddress ipIsIP(String readLine) {
        try {
            byte[] address = InetAddress.getByName(readLine).getAddress();
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            new MessageLocal().errorAlert(CLASS_NAME, "ipIsIP", e.getMessage());
            FileSystemWorker.error("NetPinger.ipIsIP", e);
            throw new IllegalStateException();
        }
    }

    @Override
    public void run() {
        final long startSt = System.currentTimeMillis();
        if (multipartFile != null) {
            parseFile();
        }
        while (System.currentTimeMillis() < startSt + TimeUnit.MINUTES.toMillis(Long.parseLong(getTimeToScan()))) {
            pingSW();
        }
        this.pingResult = new TForms().fromArray(resList, true);
        messageToUser.infoNoTitles(pingResult);
        parseResult();
    }

    private void pingSW() {
        new MessageCons().errorAlert(METH_PINGSW);
        for(InetAddress inetAddress : ipAsList){
            try{
                resList.add(inetAddress.toString() + " is " + inetAddress.isReachable(ConstantsFor.TIMEOUT_650));
            }
            catch(IOException e){
                new MessageLocal().errorAlert(CLASS_NAME, "pingSW", e.getMessage());
                FileSystemWorker.error(METH_PINGSW, e);
            }
        }
    }

    private void parseResult() {
        List<String> pingsList = new ArrayList<>();
        resList.stream().distinct().forEach(x -> {
            int frequency = Collections.frequency(resList, x);
            pingsList.add(frequency + " times " + x + "\n");
        });
        messageToUser.infoNoTitles(pingResult);
        FileSystemWorker.recFile("pingresult", pingsList);
        messageToUser = new MessageFile();
        messageToUser.infoNoTitles(pingResult);
    }
}
