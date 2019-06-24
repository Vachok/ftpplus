// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;
import ru.vachok.networker.net.InfoWorker;


/**
 @see ConditionChecker
 @since 23.06.2019 (15:11) */
public class ConditionCheckerTest {
    
    
    @Test
    public void testGetInfoAbout() {
        
        InfoWorker infoWorker = new ConditionChecker("select * from velkompc where NamePP like ?", "do0213.eatmeat.ru:false");
        String infoWorkerString = infoWorker.getInfoAbout();
        System.out.println(infoWorkerString);
        infoWorker = new ConditionChecker("select * from pcuser where pcName like ?", "do0004.eatmeat.ru:true");
        System.out.println(infoWorker.getInfoAbout());
    }
    
    @Test
    public void testSetInfo() {
        throw new IllegalInvokeEx("23.06.2019 (15:11)");
    }
    
    @Test
    public void testToString1() {
        throw new IllegalInvokeEx("23.06.2019 (15:11)");
    }
}