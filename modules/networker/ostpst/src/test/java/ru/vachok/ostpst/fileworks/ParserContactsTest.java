package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.utils.CharsetEncoding;


public class ParserContactsTest {
    
    
    @Test
    public void runNoParam() {
        MakeConvert makeConvert = new ConverterImpl(new CharsetEncoding("windows-1251")
            .getStrInAnotherCharset("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost"));
        System.out.println(" = " + makeConvert.saveContacts(null));
    }
}