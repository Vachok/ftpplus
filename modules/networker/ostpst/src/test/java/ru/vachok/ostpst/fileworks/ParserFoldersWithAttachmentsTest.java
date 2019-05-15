package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.TForms;

import java.util.Deque;


public class ParserFoldersWithAttachmentsTest {
    
    
    @Test
    public void testFolders() {
        String fileName = "c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost";
        fileName = new CharsetEncoding("windows-1251").getStrInAnotherCharset(fileName);
        MakeConvert makeConvert = new ConverterImpl(fileName);
        String itemsString = makeConvert.showListFolders();
        Deque<String> deqFolderNames = makeConvert.getDequeFolderNames();
        System.out.println(new TForms().fromArray(deqFolderNames));
    }
}