package ru.vachok.networker.ad.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;

/**
 Работа с правами пользователя.

 @since 13.02.2019 (15:43) */
@Service
public class UserRightsOnCommon extends SimpleFileVisitor<Path> {

    private ADUser adUser;

    private MessageToUser messageToUser = new MessageLocal(UserRightsOnCommon.class.getSimpleName());

    @Autowired
    public UserRightsOnCommon(ADUser adUser) {
        this.adUser = adUser;
    }

    protected UserRightsOnCommon() {
        super();
    }
    
    public static String getCommonAccessRights(String workPos, Model model) {
        ADSrv adSrv = AppComponents.adSrv();
        try {
            String users = workPos.split(": ")[1];
            String commonRights = adSrv.checkCommonRightsForUserName(users);
            model.addAttribute(ConstantsFor.ATT_WHOIS, commonRights);
            model.addAttribute(ConstantsFor.ATT_TITLE, workPos);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            FileSystemWorker.error("CommonRightsChecker.getCommonAccessRights", e);
        }
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        AclFileAttributeView fileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
        for (AclEntry aclEntry : fileAttributeView.getAcl()) {
            UserPrincipal userPrincipal = aclEntry.principal();
            userPrincipal.getName();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return super.visitFile(file, attrs);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return super.visitFileFailed(file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return super.postVisitDirectory(dir, exc);
    }
}
