// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import ru.vachok.networker.abstr.Keeper;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 23.07.2019 (11:35) */
public abstract class UsersKeeper implements Keeper {
    
    
    private static final Map<String, String> TMP_INET_MAP = new ConcurrentHashMap<>();
    
    private static final Map<String, String> INET_UNIQ = new ConcurrentHashMap<>();
    
    public static Map<String, String> get24hrsTempInetList() {
        return TMP_INET_MAP;
    }
    
    public static Map<String, String> getInetUniqMap() {
        return INET_UNIQ;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UsersKeeper.class.getSimpleName() + "[\n", "\n]")
            .add("uniqUserInetAccess = " + TMP_INET_MAP.size())
            .toString();
    }
}