package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.MessageFormat;
import java.util.*;


/**
 @see UserACLDeleterTest
 @since 26.07.2019 (11:03) */
class UserACLDeleter extends UserACLManagerImpl {


    private String userName;

    private UserPrincipal oldUser;

    private final List<AclEntry> needACL = new ArrayList<>();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserACLDeleter.class.getSimpleName());

    UserACLDeleter(@NotNull UserPrincipal oldUser) {
        super(Paths.get(ModelAttributeNames.COMMON));
        this.oldUser = oldUser;
        this.userName = oldUser.toString().split(" ")[0];
    }

    UserACLDeleter(Path startPath) {
        super(startPath);
        try {
            this.oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format(ConstantsFor.ACLADDER_0_1, e.getMessage(), e.getClass().getName()));
        }

    }

    private UserACLDeleter(String userName) {
        super(Paths.get(ModelAttributeNames.COMMON));
        this.userName = userName;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        replaceOwner(dir);
        removeOldACL(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("delete = " + dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public void setClassOption(Object classOption) {
        this.oldUser = (UserPrincipal) classOption;
    }

    @Override
    public String getResult() {
        return new TForms().fromArray(needACL);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserACLCommonDeleter{");
        sb.append("userName='").append(userName).append('\'');
        sb.append(", oldUser=").append(oldUser);
        sb.append(", needACL=").append(needACL.size());
        sb.append('}');
        return sb.toString();
    }

    private void replaceOwner(Path dir) throws IOException {
        UserPrincipal userPrincipal = Files.getOwner(dir);
        if (userPrincipal.equals(oldUser)) {
            Files.setOwner(dir, Files.getOwner(dir.getRoot()));
        }
    }

    private void removeOldACL(Path path) {
        AclFileAttributeView attributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        Set<AclEntry> tmpSet = new HashSet<>();
        try {
            for (AclEntry aclEntry : attributeView.getAcl()) {
                if (!aclEntry.principal().equals(oldUser)) {
                    tmpSet.add(aclEntry);
                }
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonDeleter.removeOldACL: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        needACL.addAll(tmpSet);
        try {
            attributeView.setAcl(needACL);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format(path.toString(), e.getMessage(), e.getClass().getName()));
        }
    }
}
