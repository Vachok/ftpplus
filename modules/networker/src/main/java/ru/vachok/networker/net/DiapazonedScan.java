package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
public class DiapazonedScan implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static volatile DiapazonedScan ourInstance;


    private DiapazonedScan() {
    }

    public static DiapazonedScan getInstance() {
        ConstantsFor.showMem();
        if (ourInstance == null) {
            synchronized (DiapazonedScan.class) {
                if (ourInstance == null) {
                    LOGGER.info("DiapazonedScan.getInstance");
                    ourInstance = new DiapazonedScan();
                    ExecutorService e = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
                    Future<?> submit = e.submit(ourInstance);
                    new Thread(() ->
                    {
                        String msg1 = "START " + ourInstance.toString();
                        LOGGER.warn(msg1);

                        Object o;
                        try {
                            o = submit.get();
                            String msg = o.toString() + " obj returned by " + ourInstance.getClass().getSimpleName();
                            LOGGER.warn(msg);
                        } catch (InterruptedException | ExecutionException e1) {
                            e1.getMessage();
                            Thread.currentThread().interrupt();
                        }

                    });
                }
            }
        }
        return ourInstance;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonedScan{");
        sb.append("ourInstance=").append(ourInstance.getClass().getTypeName());
        sb.append(" Скан диапазона адресов since 19.12.2018 (11:35)").append("\n");
        sb.append(ConstantsFor.showMem());
        sb.append(Arrays.toString(KassaSBank.values()));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            scanAll();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     Добавляет в {@link #swAddr} адреса 10.200.200-217.254
     <p>

     @throws IOException если адрес недоступен.
     */
    private void scanAll() throws IOException {
        long stArt = System.currentTimeMillis();
        LOGGER.info("DiapazonedScan.scanAll");
        OutputStream outputStream = new FileOutputStream("avalible.txt");
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        ConcurrentMap<String, InetAddress> avaliablePCs = new ConcurrentHashMap<>();
        List<String> notAval = new ArrayList<>();
        for (int i = 200; i < 218; i++) {
            for (int j = 0; j < 255; j++) {
                byte[] aBytes = InetAddress.getByName("10.200." + i + "." + j).getAddress();
                InetAddress byAddress = InetAddress.getByAddress(aBytes);
                if (byAddress.isReachable(250)) {
                    avaliablePCs.put(byAddress.getHostName(), byAddress);
                    printWriter.println(byAddress.toString());
                } else notAval.add(byAddress.toString());
            }
        }
        printWriter.close();
        outputStream.close();
        String msg = avaliablePCs.size() +
            " avaliablePCs\n" + notAval.size() +
            " notAval\nTotal scanned: " +
            avaliablePCs.size() + notAval.size() +
            "\nTime spend: " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt);
        LOGGER.warn(msg);
    }

    List<String> pingSwitch() throws IllegalAccessException {
        LOGGER.info("DiapazonedScan.pingSwitch");
        StringBuilder stringBuilder = new StringBuilder();
        Field[] swFields = Switches.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            swList.add("\n" + swF.get(swF).toString());
        }
        swList.forEach(stringBuilder::append);
        String retMe = stringBuilder.toString();
        LOGGER.warn(retMe);
        return swList;
    }

}
