package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.sql.*;


/**
 @see UserACLAdder
 @since 02.08.2019 (14:03) */
public class UserACLAdderTest {
    
    
    private UserACLManagerImpl commonAdder;
    
    private AclFileAttributeView attributeView;
    
    @Test
    private void booleanAddTest() {
        try {
            UserPrincipal owner = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            Path startPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\");
//            Path startPath = Paths.get("\\\\srv-fs\\Common_new\\Проекты\\Проекты_Строительство\\Проект_KFC");
            this.commonAdder = new UserACLAdder(startPath, owner, "rw");
            Files.walkFileTree(startPath, commonAdder);
            this.attributeView = Files.getFileAttributeView(ConstantsFor.COMMON_DIR, AclFileAttributeView.class);
            AclEntry acl;
            for (AclEntry aclEntry : attributeView.getAcl()) {
                boolean notOwner = !aclEntry.principal().equals(owner);
                boolean notDeny = !aclEntry.type().name().equalsIgnoreCase("deny");
                boolean contains = aclEntry.principal().toString().contains("BUILTIN\\Администраторы");
                boolean isAdd = notOwner & notDeny & contains;
                if (isAdd) {
                    System.out.println("isAdd = " + true);
                }
            }
            System.out.println("AbstractForms.fromArray(commonAdder.getNeededACLs()) = " + commonAdder.getResult());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    @Ignore
    private void aclToDB() {
        final String sql = "INSERT INTO common.concreteacl (userName, fileName, acl) VALUES (?, ?, ?)";
        Path startPath = Paths.get("\\\\srv-fs\\Common_new\\Z01.ПАПКИ_ОБМЕНА\\Коммерция-Маркетинг_Отчеты\\аналитика ТиФ\\_ЗП\\");
        this.attributeView = Files.getFileAttributeView(startPath, AclFileAttributeView.class);
        String fileName = this.getClass().getSimpleName() + ".attributeView";
        try (ObjectOutput objectOutput = new ObjectOutputStream(new FileOutputStream(fileName))) {
            new ExitApp().writeExternal(objectOutput);
            Files.getFileAttributeView(new File(fileName).toPath(), AclFileAttributeView.class).setAcl(attributeView.getAcl());
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("common.concreteacl");
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, "test");
                preparedStatement.setString(2, startPath.toAbsolutePath().normalize().toString());
                preparedStatement.setBinaryStream(3, new FileInputStream(fileName));
                preparedStatement.executeUpdate();
            }
        }
        catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}