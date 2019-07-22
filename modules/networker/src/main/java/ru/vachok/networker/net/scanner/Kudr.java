// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.net.NetPingerServiceFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @since 12.07.2019 (0:46) */
public class Kudr extends NetFactory {
    
    
    private Map<String, Object> mapOfConditionsTypeNameTypeCondition = new ConcurrentHashMap<>();
    
    private int monitoringCycleDelayInSeconds = ConstantsFor.ONE_DAY_HOURS;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public Kudr() {
    }
    
    @Contract(pure = true)
    protected Kudr(Map<String, Object> mapOfConditionsTypeNameTypeCondition) {
        this.mapOfConditionsTypeNameTypeCondition = mapOfConditionsTypeNameTypeCondition;
    }
    
    public Map<String, Object> getMapOfConditionsTypeNameTypeCondition() {
        return Collections.unmodifiableMap(mapOfConditionsTypeNameTypeCondition);
    }
    
    public boolean pingOneDevice(@NotNull InetAddress devAddress) {
        try {
            return devAddress.isReachable((int) TimeUnit.SECONDS.toMillis(monitoringCycleDelayInSeconds));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("Kudr.pingOneDevice threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    @Override
    public List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        List<String> retList = new ArrayList<>();
        for (Map.Entry<InetAddress, String> addressNameEntry : ipAddressAndDeviceNameToShow.entrySet()) {
            boolean isDeviceOn = pingOneDevice(addressNameEntry.getKey());
            retList.add(MessageFormat.format("Pinging {1}, with timeout {2} seconds - {0}", isDeviceOn, addressNameEntry.getValue(), monitoringCycleDelayInSeconds));
        }
        mapOfConditionsTypeNameTypeCondition.put("pingDevList", retList);
        return retList;
    }
    
    @Override
    public boolean isReach(InetAddress inetAddrStr) {
        return new NetPingerServiceFactory().isReach(inetAddrStr);
    }
    
    @Override
    public void run() {
        AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay(getMonitoringRunnable(), monitoringCycleDelayInSeconds);
    }
    
    @Override
    public String getExecution() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    @Override
    public String getPingResultStr() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    public boolean isReach(String inetAddrStr) {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    @Override
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("12.07.2019 (16:41)");
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        NetFactory netMonFactory = this;
        String statisticsKudr = netMonFactory.getStatistics();
        mapOfConditionsTypeNameTypeCondition.put("Delay", monitoringCycleDelayInSeconds);
        mapOfConditionsTypeNameTypeCondition.put("Runnable created", ConstantsFor.getAtomicTime());
        return this;
    }
    
    @Override
    public String getStatistics() {
        return new TForms().fromArray(mapOfConditionsTypeNameTypeCondition);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Kudr{");
        sb.append("mapOfConditionsTypeNameTypeCondition=").append(new TForms().fromArray(mapOfConditionsTypeNameTypeCondition));
        sb.append(", monitoringCycleDelayInSeconds=").append(monitoringCycleDelayInSeconds);
        sb.append('}');
        return sb.toString();
    }
    
    private enum ConstantConditions {
        MOBILE_PHONE_IP,
        WORK_PC_IP;
    }
}