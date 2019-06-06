package ru.vachok.networker.exe.schedule;


import org.testng.annotations.Test;


public class DiapazonScanTest {
    
    
    @Test
    public void testRun() {
        DiapazonScan.getInstance().run();
    }
}