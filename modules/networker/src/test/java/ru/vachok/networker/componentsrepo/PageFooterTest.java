// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.info.PageFooter;


public class PageFooterTest {
    
    
    @Test
    public void testTestToString() {
        String toStr = new PageFooter().toString();
        Assert.assertTrue(toStr.contains("/images/icons8-плохие-поросята-100g.png"), toStr);
    }
}