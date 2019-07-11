// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.AbstractMonitorFactory;
import ru.vachok.networker.abstr.monitors.NetMonitorFactory;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;


/**
 Class ru.vachok.networker.exe.KudrMonitor
 <p>
 ru.vachok.networker.exe.KudrMonitorTest
 
 @since 12.07.2019 (0:46) */
public class KudrMonitor extends NetMonitorFactory implements Runnable {
    
    
    private Map<String, Object> monitoringConditionsTypeNameTypeCondition = new ConcurrentHashMap<>();
    
    private String ipAddr = "10.200.214.80";
    
    private int monitoringCycleDelay = ConstantsFor.ONE_DAY_HOURS;
    
    public KudrMonitor() {
    }
    
    @Contract(pure = true)
    protected KudrMonitor(Map<String, Object> monitoringConditionsTypeNameTypeCondition) {
        this.monitoringConditionsTypeNameTypeCondition = monitoringConditionsTypeNameTypeCondition;
    }
    
    public Map<String, Object> getMonitoringConditionsTypeNameTypeCondition() {
        return Collections.unmodifiableMap(monitoringConditionsTypeNameTypeCondition);
    }
    
    @Override
    public String getTimeToEndStr() {
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
    public void setLaunchTimeOut(int monitoringCycleDelay) {
        this.monitoringCycleDelay = monitoringCycleDelay;
    }
    
    @Override
    public void run() {
        Long startTime = ConstantsFor.getAtomicTime();
        monitoringConditionsTypeNameTypeCondition.put(String.class.getTypeName(), startTime);
        Object constCond = KudrMonitor.ConstantConditions.values();
        monitoringConditionsTypeNameTypeCondition.put(KudrMonitor.ConstantConditions.class.getTypeName(), constCond);
    }
    
    @Override
    public Runnable launchMonitoring() {
        NetMonitorFactory netMonFactory = AbstractMonitorFactory.createNetMonitorFactory("kudr");
        netMonFactory.setLaunchTimeOut((int) ConstantsFor.DELAY);
        String statisticsKudr = netMonFactory.getStatistics();
        System.out.println("statisticsKudr = " + statisticsKudr);
        return this::launchMonitoring;
    }
    
    @Override
    public String getStatistics() {
        return new TForms().fromArray(monitoringConditionsTypeNameTypeCondition);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", KudrMonitor.class.getSimpleName() + "[\n", "\n]")
            .add("ipAddr = '" + ipAddr + "'")
            .add("monitoringCycleDelay = " + monitoringCycleDelay)
            .toString();
    }
    
    private enum ConstantConditions {
        MOBILE_PHONE_IP,
        WORK_PC_IP;
    }
}