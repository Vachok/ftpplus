// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.Pinger;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.services.TimeChecker;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import static java.net.InetAddress.getByAddress;


/**
 Проверка свичей в локальной сети.
 <p>
 
 @since 04.12.2018 (9:23) */
class SwitchesAvailability implements Runnable, Pinger {
    
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final InvokeEmptyMethodException EMPTY_METHOD_EXCEPTION = new InvokeEmptyMethodException(SwitchesAvailability.class.getTypeName());
    
    @SuppressWarnings("InstanceVariableOfConcreteClass")
    private final ThreadConfig thrCfg = AppComponents.threadConfig();
    
    private final Set<String> okIP = new HashSet<>();
    
    /**
     {@link InetAddress} свчичей.
     */
    private List<String> swAddr = new ArrayList<>();
    
    private String okStr = "null";
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private String badStr = "It's OK!";
    
    public Set<String> getOkIP() {
        return okIP;
    }
    
    @Override
    public String getExecution() {
        throw EMPTY_METHOD_EXCEPTION;
    }
    
    @Override
    public String getPingResultStr() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(diapazonPingSwitches()).append(" size of DiapazonScan.pingSwitch()\n");
    
        try {
            stringBuilder.append(makeAddrQ());
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append(" ").append(getClass().getSimpleName()).append(".run");
        }
        return stringBuilder.toString();
    }
    
    @Override
    public boolean isReach(String inetAddrStr) {
        throw EMPTY_METHOD_EXCEPTION;
    }
    
    @Override
    public String writeLogToFile() {
        return writeToLogFile(okStr, badStr);
    }
    
    @Override
    public void run() {
        System.out.println(getPingResultStr());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwitchesAvailability{");
        sb.append("badStr='").append(badStr).append('\'');
        sb.append(", okIP=").append(okIP);
        sb.append(", okStr='").append(okStr).append('\'');
        sb.append(", swAddr=").append(swAddr);
        sb.append('}');
        return sb.toString();
    }
    
    private int diapazonPingSwitches() {
        List<String> stringList = new ArrayList<>();
        try {
            swAddr.addAll(pingSwitch());
        }
        catch (IllegalAccessException e) {
            messageToUser.error(MessageFormat
                .format("SwitchesAvailability.diapazonPingSwitches says: {0}. Parameters: \n[no]:-\nSTACK:\n {1}", e.getMessage(), new TForms().fromArray(e)));
        }
        Collections.sort(swAddr);
        
        return swAddr.size();
    }
    
    /**
     Проверяет пинги
     <p>
     1. {@link TForms#fromArray(java.util.Set, boolean)} - лист онлайн ИП в строку. {@link #okIP}. <br>
     2. {@link TForms#fromArray(java.util.List, boolean)} - - лист онлайн ИП в строку. <br>
     3. {@link SwitchesAvailability#writeToLogFile(java.lang.String, java.lang.String)}
     <p>
 
     @param inetAddressQueue преобразованный лист строк в ИП. {@link #makeAddrQ()}
     @return File, with results
 
     @throws IOException если адрес недоступен.
     */
    private String pingAddrAndReturnLogFileName(@NotNull Queue<InetAddress> inetAddressQueue) throws IOException {
        List<String> badIP = new ArrayList<>();
        Logger javaLogger = Logger.getLogger(this.getClass().getSimpleName());
        
        while (inetAddressQueue.iterator().hasNext()) {
            int delay = (int) (ConstantsFor.DELAY) * 2;
            InetAddress poll = inetAddressQueue.poll();
            String ipStr = poll != null ? poll.toString() : null;
            javaLogger.fine(MessageFormat.format("Pinging {0} with delay {1} milliseconds... ", ipStr, delay));
            thrCfg.thrNameSet(ipStr);
            if (poll != null && poll.isReachable(delay)) {
                okIP.add(poll.toString());
                javaLogger.fine(String.valueOf(true));
            }
            else {
                if (poll != null) {
                    badIP.add(ipStr + " " + poll.getCanonicalHostName());
                }
                else {
                    badIP.add(ipStr);
                }
                javaLogger.fine(String.valueOf(false));
            }
        }
    
        okStr = new TForms().fromArray(okIP, false).replaceAll("/", "");
        badStr = new TForms().fromArray(badIP, false).replaceAll("/", "");
        return writeToLogFile(okStr, badStr);
    }
    
    /**
     Преобразователь строк в адреса.
     <p>
     1. {@link SwitchesAvailability#pingAddrAndReturnLogFileName(java.util.Queue)} - тестируем онлайность.
     <p>
     
     @return File, with results
     
     @throws IOException если хост unknown.
     */
    private String makeAddrQ() throws IOException {
        Queue<InetAddress> inetAddressesQ = new ConcurrentLinkedQueue<>();
        for (String s : swAddr) {
            byte[] addressBytes = InetAddress.getByName(s).getAddress();
            @SuppressWarnings("ObjectAllocationInLoop") InetAddress byAddress = getByAddress(addressBytes);
            inetAddressesQ.add(byAddress);
        }
        return pingAddrAndReturnLogFileName(inetAddressesQ);
    }
    
    /**
     Запись в файл информации
     <p>
     1. {@link TimeChecker#call()}
     <p>
     
     @param okIP лист он-лайн адресов
     @param badIP лист офлайн адресов
     */
    private String writeToLogFile(String okIP, String badIP) {
        File file = new File("sw.list.log");
    
        String toWrite = new StringBuilder()
            .append(new TimeChecker().call().getMessage())
            .append("\n\n")
            .append("Online SwitchesWiFi: \n")
            .append(okIP)
            .append("\nOffline SwitchesWiFi: \n")
            .append(badIP).toString();
        return FileSystemWorker.writeFile(file.getAbsolutePath(), toWrite);
    }
    
    /**
     Контролер пинг-экзекуторов
     <p>
     Свичи начала сегментов. Вкл. в оптическое ядро.
     
     @return лист важного оборудования
     
     @throws IllegalAccessException swF.get(swF).toString()
     */
    private static List<String> pingSwitch() throws IllegalAccessException {
        Field[] swFields = SwitchesWiFi.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            String ipAddrStr = swF.get(swF).toString();
            swList.add(ipAddrStr);
        }
        return swList;
    }
}
