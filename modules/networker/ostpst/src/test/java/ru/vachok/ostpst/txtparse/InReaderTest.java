package ru.vachok.ostpst.txtparse;


import org.testng.annotations.Test;
import ru.vachok.ostpst.fileworks.txtparse.InReader;


/**
 @since 31.05.2019 (14:56) */
public class InReaderTest {
    
    
    @Test
    public void testInReader() {
        InReader inReader = new InReader(0);
        inReader.dozenReadFile();
    }
    
}
