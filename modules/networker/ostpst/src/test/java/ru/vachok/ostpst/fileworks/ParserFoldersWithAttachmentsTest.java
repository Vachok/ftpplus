package ru.vachok.ostpst.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.utils.TForms;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.Properties;


public class ParserFoldersWithAttachmentsTest {
    
    
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
        MakeConvert makeConvert = new ConverterImpl(fileName);
        String itemsString = makeConvert.showListFolders();
        Deque<String> deqFolderNames = null;
        try {
            deqFolderNames = makeConvert.getDequeFolderNamesAndWriteToDisk();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertNotNull(deqFolderNames.getFirst());
    }
}