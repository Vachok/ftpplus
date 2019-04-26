package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.concurrent.Callable;

/**
 Trace Route
 <p>

 @since 05.02.2019 (13:10) */
public class TraceRoute implements Callable<String> {

    private static MessageToUser messageToUser = new MessageLocal(TraceRoute.class.getSimpleName());

    private String traceRt() {
        Thread.currentThread().setName(getClass().getSimpleName());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InetAddress yaRu = InetAddress.getByName("ya.ru");
            stringBuilder.append(yaRu);
            String valStr = "yaRu = " + yaRu.isReachable(500);
            stringBuilder.append(valStr);
            if (System.getProperty("os.name").toLowerCase().contains("window")) {
                stringBuilder.append(winTrace(yaRu));
            } else {
                throw new UnsupportedOperationException("Not ready yet");
            }
        } catch (IOException e) {
            stringBuilder.append(e.getMessage());
            FileSystemWorker.error("TraceRoute.traceRt", e);
        }
        return stringBuilder.toString();
    }

    @SuppressWarnings("resource")
    private String winTrace(InetAddress yaRu) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("tracert " + yaRu.getHostName());
            InputStream inputStream = process.getInputStream();
            byte[] bytes = new byte[ConstantsFor.KBYTE];
            while (inputStream.read() > 0) {
                String str = new String(bytes);
                new MessageCons(getClass().getSimpleName()).infoNoTitles(str);
                stringBuilder.append(str);
            }
            inputStream.close();
        } catch (IOException e) {
            messageToUser.errorAlert("TraceRoute", "winTrace", e.getMessage());
            FileSystemWorker.error("TraceRoute.winTrace", e);
        }
        return stringBuilder.toString();
    }

    @Override
    public String call() {
        throw new UnsupportedOperationException();
    }
}
