package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;


/**
 @see UserACLCommonDeleter
 @since 26.07.2019 (11:15) */
public class UserACLCommonDeleterTest {
    
    
    @Test
    public void testDeleter() {
        UserPrincipal oldUser = null;
        try {
            oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        UserACLCommonManager userACLCommonManager = new UserACLCommonManagerImpl(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));
        String removeAccess = userACLCommonManager.removeAccess(oldUser);
    }
}