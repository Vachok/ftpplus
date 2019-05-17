// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;


public class RNDFileCopyTest {
    
    
    @Test
    public void copyTest() {
        String fileName = "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst";
        MakeConvert makeConvert = new ConverterImpl(fileName);
        String copyierWithSave = makeConvert.copyierWithSave("n");
        System.out.println("copyierWithSave = " + copyierWithSave);
    }
}