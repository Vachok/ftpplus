package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;


/**
 @see UserACLCommonAdder
 @since 02.08.2019 (14:03) */
public class UserACLCommonAdderTest {
    
    
    private UserACLCommonAdder commonAdder;
    
    @Test
    private void booleanAddTest() {
        try {
            UserPrincipal owner = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            this.commonAdder = new UserACLCommonAdder(owner);
            commonAdder.createACLs(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));
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
            System.out.println("new TForms().fromArray(commonAdder.getNeededACLs()) = " + new TForms().fromArray(commonAdder.getNeededACLs()));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}