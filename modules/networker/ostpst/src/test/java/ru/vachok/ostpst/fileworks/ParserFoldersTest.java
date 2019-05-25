package ru.vachok.ostpst.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
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
        try {
            properties.load(new FileInputStream("app.properties"));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        String fileName = properties.getProperty("file");
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