// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import java.util.StringJoiner;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.InetIPUserTest
 @since 02.04.2019 (10:25) */
public class InetIPUser extends InternetUse {
    
    
    public String getUsage(String userCred) {
        InternetUse.aboutWhat = userCred;
        return getUsage0(userCred);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", InetIPUser.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
