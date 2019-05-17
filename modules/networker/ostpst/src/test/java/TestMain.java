// Copyright (c) all rights. http://networker.vachok.ru 2019.

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.TForms;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Paths;


public class TestMain {
    
    
    @Test(enabled = true)
    public void launchProg() {
        File file = new File(new String("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost".getBytes(), Charset.forName("UTF-8")));
        OstToPst ostToPst = new OstToPst();
        try {
            ostToPst.main(new String[]{"-t"});
        }
        catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void complexTest() {
        String fileName = "c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost";
        MakeConvert makeConvert = new ConverterImpl(fileName);
        String listFolders = makeConvert.showListFolders();
        Assert.assertNotNull(listFolders);
        boolean totalC = listFolders.contains("totalCounter =");
        Assert.assertTrue(totalC, "List folders incorrect");
        
        String csvFileName = Paths.get(fileName).getParent().toAbsolutePath() + "\\cnt.csv";
        String saveContacts = makeConvert.saveContacts(csvFileName);
        Assert.assertNotNull(saveContacts);
        Assert.assertFalse(saveContacts.contains(" is "));
        
        String saveFolders = null;
        try {
            saveFolders = makeConvert.saveFolders();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        boolean foldersTxt = saveFolders.contains("folders.txt");
        Assert.assertTrue(foldersTxt);
        
        String showContacts = makeConvert.showContacts();
        Assert.assertNotNull(showContacts);
        
        String cleanPreviousCopy = makeConvert.cleanPreviousCopy();
        Assert.assertNotNull(cleanPreviousCopy);
        
        System.out.println(listFolders);
        System.out.println(saveContacts);
        System.out.println(showContacts);
        System.out.println(cleanPreviousCopy);
    }
    
    @Test(enabled = false)
    public void showBytes() {
        try {
            String str8 = new String("И".getBytes(), "UTF-8");
            str8 = new CharsetEncoding("UTF-8").getStrInAnotherCharset(str8);
            byte[] bytes = str8.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                System.out.println("bytes = " + bytes[i]);
            }
            System.out.println(str8);
            
            String str1251 = new String("И".getBytes(), "windows-1251");
            str1251 = new CharsetEncoding("windows-1251").getStrInAnotherCharset(str1251);
            byte[] str1251Bytes = str1251.getBytes();
            for (int i = 0; i < str1251Bytes.length; i++) {
                System.out.println("bytes1251 = " + str1251Bytes[i]);
            }
            System.out.println(str1251);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        byte[] inUnicode = new CharsetEncoding().getInUnicode("И");
        for (int i = 0; i < inUnicode.length; i++) {
            System.out.println("inUnicode = " + inUnicode[i]);
        }
        try {
            System.out.println("UNICODE = " + new String(inUnicode, "unicode"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
