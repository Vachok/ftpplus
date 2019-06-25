// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.List;
import java.util.Queue;


/**
 @see DataBaseADUsersSRV
 @since 21.06.2019 (13:08) */
public class DataBaseADUsersSRVTest {
    
    
    /**
     @see DataBaseADUsersSRV#fileParser(Queue)
     */
    @Test
    public void testFileParser() {
        DataBaseADUsersSRV dataBaseADUsersSRV = new DataBaseADUsersSRV();
        Queue<String> fileAsList = FileSystemWorker.readFileToQueue(new File("users.csv").toPath());
        dataBaseADUsersSRV.fileParser(fileAsList);
        List<ADUser> adUsers = dataBaseADUsersSRV.getAdUsers();
        String outString = "new TForms().fromArray(adUsers, false) = " + new TForms().fromArray(adUsers, false);
        Assert.assertTrue(outString.contains("kudr"), outString);
    }
    
    @Test
    public void testFileRead() {
        DataBaseADUsersSRV dataBaseADUsersSRV = new DataBaseADUsersSRV();
        try {
            dataBaseADUsersSRV.fileRead(null);
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }
    }
}