// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.InternetUse;


/**
 @since 09.06.2019 (21:30) */
public class InetUserPCNameTest {
    
    
    @Test
    public void testGetUsage() {
    }
    
    @Test
    public void testShowLog() {
        InternetUse internetUse = new InetUserPCName();
        internetUse.showLog();
    }
}