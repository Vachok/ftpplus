// Copyright (c) all rights. http://networker.vachok.ru 2019.

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.OstToPst;

import java.io.File;
import java.nio.charset.Charset;


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
}
