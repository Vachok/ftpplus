// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ExitApp;

import java.io.File;


/**
 @see NetKeeper
 @since 02.08.2019 (0:23) */
public class NetKeeperTest {
    
    
    private Class<? extends Class> aClass = NetKeeper.class.getClass();
    
    @Test
    public void testWriteExternal() {
        boolean isWrite = new ExitApp("test", aClass).isWriteOwnObject();
        Assert.assertTrue(isWrite);
        Assert.assertTrue(new File(NetKeeper.class.getSimpleName() + ".obj").exists());
    }
    
    @Test
    public void testReadExternal() {
        Assert.assertEquals(NetKeeper.class.getClass(), aClass);
    }
}