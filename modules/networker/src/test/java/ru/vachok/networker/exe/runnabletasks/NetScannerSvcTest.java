package ru.vachok.networker.exe.runnabletasks;


import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;


/**
 @see NetScannerSvc
 @since 24.06.2019 (11:11) */
public class NetScannerSvcTest {
    
    
    /**
     @see NetScannerSvc#toString()
     */
    @Test
    public void testToString1() {
        throw new IllegalInvokeEx("24.06.2019 (11:13)");
    }
    
    /**
     @see NetScannerSvc#theSETOfPcNames()
     */
    @Test
    public void testTheSETOfPcNames() {
        throw new IllegalInvokeEx("24.06.2019 (11:19)");
    }
    
    /**
     @see NetScannerSvc#theSETOfPCNamesPref(String)
     */
    @Test
    public void testTheSETOfPCNamesPref() {
        throw new IllegalInvokeEx("24.06.2019 (11:20)");
    }
    
    /**
     @see NetScannerSvc#theInfoFromDBGetter()
     */
    @Test
    public void testTheInfoFromDBGetter() {
        throw new IllegalInvokeEx("24.06.2019 (11:17)");
    }
}