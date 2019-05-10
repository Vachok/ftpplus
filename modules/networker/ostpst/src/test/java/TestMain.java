// Copyright (c) all rights. http://networker.vachok.ru 2019.

import org.testng.annotations.Test;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;

import java.io.File;
import java.nio.charset.Charset;


public class TestMain {
    
    
    @Test(enabled = false)
    public void launchProg() {
        File file = new File(new String("c:\\\\Users\\\\ikudryashov\\\\OneDrive\\\\Документы\\\\Файлы Outlook\\\\ksamarchenko@velkomfood.ru.ost".getBytes(), Charset.forName("UTF-8")));
        MakeConvert makeConvert = new OstToPst(file.getAbsolutePath());
        makeConvert.saveContacts();
        String itemsString = makeConvert.folderContentItemsString();
        MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
        messageToUser.info(getClass().getSimpleName() + ".launchProg", "itemsString", " = " + itemsString);
    }
}
