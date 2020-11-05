// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.info.InformationFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @see ru.vachok.networker.ad.inet.InternetUseTest
 @since 02.04.2019 (10:24) */
@SuppressWarnings("MethodWithMultipleReturnPoints")
public abstract class InternetUse implements InformationFactory {


    private static final Map<String, String> TMP_INET_MAP = new ConcurrentHashMap<>();

    private static final Map<String, String> INET_UNIQ = new ConcurrentHashMap<>();

    @Contract(pure = true)
    public static Map<String, String> get24hrsTempInetList() {
        return TMP_INET_MAP;
    }

    @Contract(pure = true)
    public static Map<String, String> getInetUniqMap() {
        return INET_UNIQ;
    }
    
    public static @NotNull InternetUse getInstance(@NotNull String type) {
        if (type.equals(InformationFactory.ACCESS_LOG_HTMLMAKER) || type.equals(INET_USAGE)) {
            return new AccessLogHTMLMaker();
        }
        else if (new NameOrIPChecker(type).isLocalAddress()) {
            return new UserReportsMaker(type);
        }
        else {
            return new AccessLogUSER();
        }
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
        int cleanedRows = 0;
        sb.append("cleanedRows=").append(cleanedRows);
        sb.append('}');
        return sb.toString();
    }

}
