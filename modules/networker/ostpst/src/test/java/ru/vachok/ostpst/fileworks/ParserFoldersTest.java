// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.Properties;


public class ParserFoldersTest {
    
    
    @Test
    public void testFolders() {
        Properties properties = new Properties();
        String fileName = ConstantsOst.FILENAME_TESTPST;
        try {
            properties.load(new FileInputStream(ConstantsOst.FILENAME_PROPERTIES));
            fileName = properties.getProperty("file");
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        MakeConvertOrCopy makeConvertOrCopy = new ConverterImpl(fileName);
        String itemsString = makeConvertOrCopy.showListFolders();
        Deque<String> deqFolderNames = null;
        try {
            deqFolderNames = makeConvertOrCopy.getDequeFolderNamesAndWriteToDisk();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TFormsOST().fromArray(e));
        }
        Assert.assertNotNull(deqFolderNames.getFirst());
    }
}