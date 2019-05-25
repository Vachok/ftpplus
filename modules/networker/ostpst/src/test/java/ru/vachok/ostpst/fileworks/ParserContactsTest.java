package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvertOrCopy;


public class ParserContactsTest {
    
    
    @Test(enabled = false)
    public void runNoParam() {
        MakeConvertOrCopy makeConvertOrCopy = new ConverterImpl("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost");
        System.out.println(" = " + makeConvertOrCopy.saveContacts("save.csv"));
    }
}