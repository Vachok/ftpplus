// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import org.testng.annotations.Test;

import java.awt.*;
import java.net.UnknownHostException;

import static org.testng.Assert.assertNull;


/**
 @since 10.06.2019 (9:33) */
public class NameOrIPCheckerTest {
    
    
    @Test
    public void testCheckPat() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void testResolveIP() {
        try {
            new NameOrIPChecker("1.1.1.1").resolveIP();
        }
        catch (UnknownHostException e) {
            assertNull(e, e.getMessage());
        }
    }
}