package ru.vachok.networker.ad.usermanagement;


import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 @see UserACLAdderTest
 @since 25.07.2019 (13:27) */
class UserACLAdder extends UserACLManagerImpl {
    
    
    private UserPrincipal newUser;
    
    private int foldersCounter;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserACLAdder.class.getSimpleName());
    
    private int filesCounter;
    
    private List<AclEntry> neededACLs;
    
    UserACLAdder(Path startPath) {
        super(startPath);
        try {
            this.newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLAdder.UserACLAdder: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private UserACLAdder(UserPrincipal newUser) {
        super(Paths.get(ModelAttributeNames.COMMON).toAbsolutePath().normalize());
        this.newUser = newUser;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        foldersCounter++;
        try {
            createACLs(dir);
            List<AclEntry> tipEnters = new ArrayList<>(neededACLs);
            Files.getFileAttributeView(dir, AclFileAttributeView.class).setAcl(tipEnters);
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonAdder.preVisitDirectory: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        filesCounter++;
        try {
            createACLs(file);
            List<AclEntry> tipEnters = new ArrayList<>(neededACLs);
            Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(tipEnters); //todo 13.11.2019 (21:52) too long
    
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonAdder.visitFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserACLCommonAdder{");
        sb.append(", newUser=").append(newUser);
        sb.append(", foldersCounter=").append(foldersCounter);
        sb.append(", filesCounter=").append(filesCounter);
        sb.append(", neededACLs=").append(new TForms().fromArray(neededACLs));
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.newUser = (UserPrincipal) classOption;
    }
    
    @Override
    public String getResult() {
        return new TForms().fromArray(neededACLs);
    }
    
    private void createACLs(Path dir) throws IOException {
        Map<UserPrincipal, AclEntry> principalAclEntry = new HashMap<>();
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
        List<AclEntry> currentACLs = aclFileAttributeView.getAcl(); //todo 13.11.2019 (21:52) too long
        AclEntry addAcl = createACLFor(newUser, "rw");
        currentACLs.add(addAcl);
        principalAclEntry.put(addAcl.principal(), addAcl);
        this.neededACLs = currentACLs;
    }
}
