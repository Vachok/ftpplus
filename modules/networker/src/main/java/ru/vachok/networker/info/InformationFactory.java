// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.ad.inet.InternetUse;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


/**
 @since 09.04.2019 (13:16) */
public interface InformationFactory {
    
    
    ThreadMXBean MX_BEAN_THREAD = ManagementFactory.getThreadMXBean();
    
    String INET_USAGE = "inetusage";
    
    String ACCESS_LOG_HTMLMAKER = "AccessLogHTMLMaker";
    
    String INET_USAGE_HTML = ACCESS_LOG_HTMLMAKER;
    
    String REGULAR_LOGS_SAVER = "ru.vachok.stats.SaveLogsToDB";
    
    String STATS_SUDNAY_PC_SORT = "ComputerUserResolvedStats";
    
    String USER = ModelAttributeNames.ADUSER;
    
    String TV = "TvPcInformation";
    
    String STATS_WEEKLY_INTERNET = "WeeklyInternetStats";
    
    String getInfoAbout(String aboutWhat);
    
    /**
     @param option объект, вспомогательный для класса.
     */
    void setClassOption(Object option);
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static @NotNull InformationFactory getInstance(@NotNull String type) {
        switch (type) {
            case INET_USAGE:
            case INET_USAGE_HTML:
                return InternetUse.getInstance(type);
            case STATS_SUDNAY_PC_SORT:
                return Stats.getInstance(STATS_SUDNAY_PC_SORT);
            case STATS_WEEKLY_INTERNET:
                return Stats.getInstance(STATS_WEEKLY_INTERNET);
            case USER:
                return UserInfo.getInstance(type);
            case TV:
                return PCInfo.getInstance(TV);
            case REGULAR_LOGS_SAVER:
                return new SaveLogsToDB();
            default:
                return PCInfo.getInstance(type);
        }
    }
    
    default String writeObj(String logName, Object information) {
        try (OutputStream outputStream = new FileOutputStream(logName)) {
            new ExitApp(information).writeExternal(new ObjectOutputStream(outputStream));
        }
        catch (IOException e) {
            MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, InformationFactory.class.getSimpleName()).error(e.getMessage() + " see line: 138 ***");
        }
        return new File(logName).getAbsolutePath();
    }
    
    String getInfo();
}
