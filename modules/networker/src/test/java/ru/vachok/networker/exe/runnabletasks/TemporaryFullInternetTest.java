package ru.vachok.networker.exe.runnabletasks;


import org.testng.annotations.Test;


public class TemporaryFullInternetTest {
    
    
    @Test
    public void testRunCheck() {
        new TemporaryFullInternet().run();
    }
    
    @Test
    public void testRunAdd() {
        new TemporaryFullInternet("8.8.8.8", System.currentTimeMillis(), "add").run();
    }
    
}