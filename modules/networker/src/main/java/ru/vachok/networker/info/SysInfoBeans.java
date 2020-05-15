package ru.vachok.networker.info;


import com.eclipsesource.json.JsonObject;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


/**
 Class ru.vachok.networker.info.SysInfoBeans
 <p>

 @since 13.05.2020 (19:45) */
public class SysInfoBeans implements InformationFactory {


    private Object option;

    @Override
    public String getInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        ThreadMXBean thrMXBean = ManagementFactory.getThreadMXBean();
        stringBuilder.append(thrMXBean.getObjectName()).append(" object name, \n");
        stringBuilder.append(thrMXBean.getTotalStartedThreadCount()).append(" total threads started, \n");
        stringBuilder.append(thrMXBean.getThreadCount()).append(" current threads live, \n");
        stringBuilder.append(thrMXBean.getPeakThreadCount()).append(" peak live, ");
        stringBuilder.append(thrMXBean.getDaemonThreadCount()).append(" Daemon Thread Count, \n");
        return stringBuilder.toString();
    }

    @Override
    public void setClassOption(Object option) {
        throw new TODOException("ru.vachok.networker.info.SysInfoBeans.setClassOption( void ) at 13.05.2020 - (19:45)");
    }

    @Override
    public String getInfoAbout(String aboutWhat) {
        throw new TODOException("ru.vachok.networker.info.SysInfoBeans.getInfoAbout( String ) at 13.05.2020 - (19:45)");
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, getClass().getSimpleName());
        jsonObject.add(ConstantsFor.OPTION, String.valueOf(option));
        return jsonObject.toString();
    }
}