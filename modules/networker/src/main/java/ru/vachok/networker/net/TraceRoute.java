package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.concurrent.Callable;

/**
 Trace Route
 <p>

 @since 05.02.2019 (13:10) */
public class TraceRoute implements Callable<String> {

    private String traceRt() {
        Thread.currentThread().setName(getClass().getSimpleName());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InetAddress yaRu = InetAddress.getByName("ya.ru");
            stringBuilder.append(yaRu.toString());
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

    private String winTrace(InetAddress yaRu) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("tracert " + yaRu.getHostName());
            InputStream inputStream = process.getInputStream();
            byte[] bytes = new byte[ConstantsFor.KBYTE];
            while (inputStream.read() > 0) {
                String str = new String(bytes);
                new MessageCons().infoNoTitles(str);
                stringBuilder.append(str);
            }
            inputStream.close();
        } catch (IOException e) {
            new MessageCons().errorAlert("TraceRoute", "winTrace", TForms.from(e));
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }

    @Override
    public String call() throws Exception {
        throw new UnsupportedOperationException();
    }
}
