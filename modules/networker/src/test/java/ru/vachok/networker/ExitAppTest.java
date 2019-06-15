// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.annotations.Test;

import java.awt.*;


/**
 @since 09.06.2019 (21:10) */
public class ExitAppTest {
    
    
    @Test(enabled = false)
    public void testRun() {
        new ExitApp("test").run();
    }
    
    @Test
    public void testWriteOwnObject() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testGetVisitsMap() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testScanFiles() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
}