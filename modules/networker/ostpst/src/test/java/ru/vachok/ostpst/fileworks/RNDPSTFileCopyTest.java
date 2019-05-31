// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvertOrCopy;


public class RNDPSTFileCopyTest {
    
    
    @Test(enabled = false)
    public void copyTest() {
        String fileName = "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst";
        MakeConvertOrCopy makeConvertOrCopy = new ConverterImpl(fileName);
        makeConvertOrCopy.copyierWithSave("c");
    }
}