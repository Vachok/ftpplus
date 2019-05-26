// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvertOrCopy;

import java.io.File;


public class ParserContactsTest {
    
    
    @Test
    public void runNoParam() {
        MakeConvertOrCopy makeConvertOrCopy = new ConverterImpl("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\ostpst\\tmp_t.p.magdich.pst");
        System.out.println(" = " + makeConvertOrCopy.saveContacts("save.csv"));
        Assert.assertTrue(new File("save.csv").exists());
    }
}