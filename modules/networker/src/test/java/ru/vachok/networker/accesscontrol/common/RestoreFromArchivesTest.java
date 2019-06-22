// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Paths;


/**
 @see RestoreFromArchives
 @since 22.06.2019 (22:32) */
public class RestoreFromArchivesTest {
    
    
    @Test
    public void testToString1() {
        try {
            String rawPathToRestoreStr = "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\";
            RestoreFromArchives restoreFromArchives = new RestoreFromArchives(rawPathToRestoreStr, "100");
            String pathToRestoreAsStrFrom = restoreFromArchives.getPathToRestoreAsStr();
            pathsWorking(rawPathToRestoreStr);
        }
        catch (InvocationTargetException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    //todo разбор uri 22.06.2019 23:06
    private void pathsWorking(String pathToRestoreAsStrFrom) {
        URI uriRestore = Paths.get(pathToRestoreAsStrFrom).toAbsolutePath().normalize().toUri();
        Assert.assertNotNull(uriRestore);
        Assert.assertEquals(uriRestore.getHost(), "srv-fs.eatmeat.ru");
        throw new IllegalStateException("22.06.2019 (22:50)");
    }
}
