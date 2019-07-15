// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.NetNetworkerFactory;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;


/**
 Class ru.vachok.networker.net.scanner.KudrNetworker
 <p>
 @see
 @since 12.07.2019 (0:46) */
public class KudrNetworker extends NetNetworkerFactory {
    
    
    private Map<String, Object> monitoringConditionsTypeNameTypeCondition = new ConcurrentHashMap<>();
    
    private String ipAddr = "10.200.214.80";
    
    private int monitoringCycleDelay = ConstantsFor.ONE_DAY_HOURS;
    
    public KudrNetworker() {
    }
    
    @Contract(pure = true)
    protected KudrNetworker(Map<String, Object> monitoringConditionsTypeNameTypeCondition) {
        this.monitoringConditionsTypeNameTypeCondition = monitoringConditionsTypeNameTypeCondition;
    }
    
    public Map<String, Object> getMonitoringConditionsTypeNameTypeCondition() {
        return Collections.unmodifiableMap(monitoringConditionsTypeNameTypeCondition);
    }
    
    @Override
    public void run() {
    
    }
    
    @Override
    public String getExecution() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    @Override
    public String getPingResultStr() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    @Override
    public boolean isReach(String inetAddrStr) {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    @Override
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("12.07.2019 (16:41)");
    }
    
    @Override
    public void setLaunchTimeOut(int monitoringCycleDelay) {
        this.monitoringCycleDelay = monitoringCycleDelay;
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        NetNetworkerFactory netMonFactory = this;
        netMonFactory.setLaunchTimeOut((int) ConstantsFor.DELAY);
        String statisticsKudr = netMonFactory.getStatistics();
        monitoringConditionsTypeNameTypeCondition.put("Delay", monitoringCycleDelay);
        monitoringConditionsTypeNameTypeCondition.put("Runnable created", ConstantsFor.getAtomicTime());
        return this;
    }
    
    @Override
    public String getStatistics() {
        return new TForms().fromArray(monitoringConditionsTypeNameTypeCondition);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", KudrNetworker.class.getSimpleName() + "[\n", "\n]")
            .add("ipAddr = '" + ipAddr + "'")
            .add("monitoringCycleDelay = " + monitoringCycleDelay)
            .toString();
    }
    
    private enum ConstantConditions {
        MOBILE_PHONE_IP,
        WORK_PC_IP;
    }
}