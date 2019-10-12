// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.ad.inet.InternetUseTest
 @since 02.04.2019 (10:24) */
public abstract class InternetUse implements InformationFactory {
    
    private static final Map<String, String> TMP_INET_MAP = new ConcurrentHashMap<>();
    
    private static final Map<String, String> INET_UNIQ = new ConcurrentHashMap<>();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, InternetUse.class.getSimpleName());
    
    private static int cleanedRows = 0;
    
    @Contract(value = " -> new")
    public static @NotNull InternetUse getInstance(@NotNull String type) {
        if (type.equals(InformationFactory.ACCESS_LOG_HTMLMAKER) || type.equals(INET_USAGE)) {
            return new AccessLogHTMLMaker();
        }
        else {
            return new AccessLogUSER();
        }
    }
    
    @Contract(pure = true)
    public static Map<String, String> get24hrsTempInetList() {
        return TMP_INET_MAP;
    }
    
    @Contract(pure = true)
    public static Map<String, String> getInetUniqMap() {
        return INET_UNIQ;
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(@NotNull Object option);
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InternetUse{");
        sb.append("cleanedRows=").append(cleanedRows);
        sb.append('}');
        return sb.toString();
    }
    
    static int getCleanedRows() {
        AppComponents.threadConfig().getTaskScheduler().scheduleAtFixedRate(InternetUse::cleanTrash, TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        return cleanedRows;
    }
    
    private static void cleanTrash() {
        int retInt = -1;
        for (String sqlLocal : UsefulUtilities.getDeleteTrashInternetLogPatterns()) {
            try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlLocal)
            ) {
                int retQuery = preparedStatement.executeUpdate();
                retInt += retQuery;
                InternetUse.cleanedRows = retInt;
            }
            catch (SQLException e) {
                retInt = e.getErrorCode();
            }
        }
        messageToUser.info(InternetUse.class.getSimpleName(), String.valueOf(retInt), "rows deleted.");
        if (retInt != 1065) {
            InternetUse.cleanedRows = retInt;
        }
    }
    
}
