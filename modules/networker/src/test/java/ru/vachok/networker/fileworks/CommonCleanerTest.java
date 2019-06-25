package ru.vachok.networker.fileworks;


import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;


/**
 @see CommonCleaner */
public class CommonCleanerTest {
    
    
    @Test
    public void testCall() {
    
    }
    
    @Test
    public void testPreVisitDirectory() {
        throw new IllegalInvokeEx();
    }
    
    @Test
    public void testVisitFile() {
        throw new IllegalInvokeEx();
    }
    
    @Test
    public void testVisitFileFailed() {
        throw new IllegalInvokeEx();
    }
    
    @Test
    public void testPostVisitDirectory() {
        throw new IllegalInvokeEx();
    }
}