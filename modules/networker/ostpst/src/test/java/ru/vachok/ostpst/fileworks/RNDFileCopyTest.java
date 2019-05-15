package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;


public class RNDFileCopyTest {
    
    
    @Test
    public void copyTest() {
        String fileName = "c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost";
        MakeConvert makeConvert = new ConverterImpl(fileName);
        String copyierWithSave = makeConvert.copyierWithSave();
        System.out.println("copyierWithSave = " + copyierWithSave);
    }
}