// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;

import java.io.File;


public class ParserContactsTest {
    
    
    @Test
    public void runNoParam() {
        MakeConvertOrCopy makeConvertOrCopy = new ConverterImpl(FileSystemWorkerOST.getTestPST());
        System.out.println(" = " + makeConvertOrCopy.saveContacts("save.csv"));
        Assert.assertTrue(new File("save.csv").exists());
    }
}