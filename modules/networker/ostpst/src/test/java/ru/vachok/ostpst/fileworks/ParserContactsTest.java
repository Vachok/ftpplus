package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;


public class ParserContactsTest {
    
    
    @Test(enabled = false)
    public void runNoParam() {
        MakeConvert makeConvert = new ConverterImpl("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost");
        System.out.println(" = " + makeConvert.saveContacts("save.csv"));
    }
}