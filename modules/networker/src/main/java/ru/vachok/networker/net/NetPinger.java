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

    private String timeToScan = "3";

    private String pingResult = "No result yet";

    private List<String> resList = new ArrayList<>();

    private MultipartFile multipartFile = null;

    public String getPingResult() {
        return pingResult;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    @Override
    public void run() {
        final long startSt = System.currentTimeMillis();
        if(multipartFile!=null){
            parseFile();
        }
        long userIn = TimeUnit.MINUTES.toMillis(Long.parseLong(getTimeToScan()));
        long totalMillis = startSt + userIn;
        while(System.currentTimeMillis() < totalMillis){
            pingSW();
            messageToUser.infoNoTitles(getClass().getSimpleName() + " left " +
                ( float ) TimeUnit.MILLISECONDS.toSeconds(totalMillis - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN);
        }
        this.pingResult = new TForms().fromArray(resList, true);
        messageToUser.infoNoTitles(pingResult);
        parseResult(userIn);
    }

    private void parseFile() {
        try(InputStream inputStream = multipartFile.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)){
            while(bufferedReader.ready()){
                bufferedReader.lines().forEach(this::parseAddr);
            }
        }
        catch(IOException e){
            new MessageLocal().errorAlert(CLASS_NAME, "parseFile", e.getMessage());
            FileSystemWorker.error("NetPinger.parseFile", e);
        }
    }

    @SuppressWarnings ("WeakerAccess")
    public String getTimeToScan() {
        return timeToScan;
    }

    public void setTimeToScan(String timeToScan) {
        this.timeToScan = timeToScan;
    }

    private void pingSW() {
        for(InetAddress inetAddress : ipAsList){
            try{
                resList.add(inetAddress.toString() + " is " + inetAddress.isReachable(ConstantsFor.TIMEOUT_650));
            }
            catch(IOException e){
                messageToUser.errorAlert(CLASS_NAME, "pingSW", e.getMessage());
                FileSystemWorker.error(METH_PINGSW, e);
            }
        }
    }

    private void parseResult(long userIn) {
        List<String> pingsList = new ArrayList<>();
        resList.stream().distinct().forEach(x -> {
            int frequency = Collections.frequency(resList, x);
            pingsList.add(frequency + " times " + x + "\n");
        });
        messageToUser.infoNoTitles(pingResult);
        FileSystemWorker.recFile(ConstantsNet.PINGRESULT_LOG, pingsList);
        messageToUser = new MessageFile();
        messageToUser.infoNoTitles(pingResult);
        if(userIn >= TimeUnit.MINUTES.toMillis(3)){
            try{
                ESender.sendM(Collections.singletonList(ConstantsFor.GMAIL_COM), getClass().getSimpleName(), new TForms().fromArray(pingsList, false));
            }
            catch(Exception e){
                FileSystemWorker.error("NetPinger.parseResult", e);
            }
        }
    }

    private void parseAddr(String readLine) {
        InetAddress byName;
        try{
            byName = InetAddress.getByName(readLine);
        }
        catch(UnknownHostException e){
            byName = ipIsIP(readLine);
        }
        ipAsList.add(byName);
    }

    private InetAddress ipIsIP(String readLine) {
        try{
            byte[] address = InetAddress.getByName(readLine).getAddress();
            return InetAddress.getByAddress(address);
        }
        catch(UnknownHostException e){
            new MessageLocal().errorAlert(CLASS_NAME, "ipIsIP", e.getMessage());
            FileSystemWorker.error("NetPinger.ipIsIP", e);
            throw new IllegalStateException();
        }
    }
}
