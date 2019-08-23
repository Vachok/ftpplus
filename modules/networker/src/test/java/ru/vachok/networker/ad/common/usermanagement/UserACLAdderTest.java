package ru.vachok.networker.ad.common.usermanagement;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.usermanagement.UserACLAdder;
import ru.vachok.networker.ad.usermanagement.UserACLManagerImpl;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;


/**
 @see UserACLAdder
 @since 02.08.2019 (14:03) */
public class UserACLAdderTest {
    
    
    private UserACLManagerImpl commonAdder;
    
    @Test
    private void booleanAddTest() {
        try {
            UserPrincipal owner = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            Path startPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\");
            this.commonAdder = new UserACLAdder(startPath);
            Files.walkFileTree(startPath, commonAdder);
            AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(ConstantsFor.COMMON_DIR, AclFileAttributeView.class);
            AclEntry acl;
            for (AclEntry aclEntry : aclFileAttributeView.getAcl()) {
                boolean notOwner = !aclEntry.principal().equals(owner);
                boolean notDeny = !aclEntry.type().name().equalsIgnoreCase("deny");
                boolean contains = aclEntry.principal().toString().contains("BUILTIN\\Администраторы");
                
                boolean isAdd = notOwner & notDeny & contains;
                if (isAdd) {
                    System.out.println("isAdd = " + true);
                }
                
            }
            System.out.println("new TForms().fromArray(commonAdder.getNeededACLs()) = " + commonAdder.getResult());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}