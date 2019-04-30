// Copyright (c) all rights. http://networker.vachok.ru 2019.

import org.testng.annotations.Test;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class TestMain {
    
    
    @Test(enabled = false)
    public void launchProg() {
        MakeConvert makeConvert = new OstToPst();
        MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
        makeConvert.setFileName("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\yu.gukov.pst");
        makeConvert.copyierWithSave();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("app.properties"));
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        properties.setProperty("file", new File("test.pst").getAbsolutePath());
        makeConvert.showFileContent();
    }
}
