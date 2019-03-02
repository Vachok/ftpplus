package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.springframework.ui.Model;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.fileworks.FileOut;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;


@SuppressWarnings ("DuplicateStringLiteralInspection")
public class CommonRightsChecker extends SimpleFileVisitor<Path> {

    /**
     {@link AppComponents#getLogger(String)}
     */
    private static final Logger LOGGER = AppComponents.getLogger(CommonRightsChecker.class.getSimpleName());

    /**
     @throws IOException deleteIfExists старые файлы.
     */
    public CommonRightsChecker() throws IOException {
        AppComponents.threadConfig().thrNameSet("com.rgh");
        boolean b1 = Files.deleteIfExists(new File("common.own").toPath());
        boolean b = Files.deleteIfExists(new File("common.rgh").toPath());
        String msg = new StringBuilder()
            .append("Starting a new instance of ")
            .append(getClass().getSimpleName())
            .append(" at ").append(new Date())
            .append("\ncommon.rgh and common.own deleted : ")
            .append(b)
            .append(" ")
            .append(b1).toString();
        LOGGER.warn(msg);
    }

    public static String getCommonAccessRights(String workPos, Model model) {
        ADSrv adSrv = AppComponents.adSrv();
        try {
            String users = workPos.split(": ")[1];
            String commonRights = adSrv.checkCommonRightsForUserName(users);
            model.addAttribute(ConstantsFor.ATT_WHOIS, commonRights);
            model.addAttribute(ConstantsFor.ATT_TITLE, workPos);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        } catch (ArrayIndexOutOfBoundsException e) {
            new MessageLocal().errorAlert("CommonRightsChecker", "getCommonAccessRights", e.getMessage());
            FileSystemWorker.error("CommonRightsChecker.getCommonAccessRights", e);
        }
        return ConstantsFor.BEANNAME_MATRIX;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if(attrs.isDirectory()){
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
            List<AclEntry> acl = fileAttributeView.getAcl();
            writeFile("common.own", (dir.toString() + " owner is: " + Files.getOwner(dir).getName() + "\nUsers:Rights\n" + new TForms().fromArray(acl, false) + "\n\n").getBytes());
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(attrs.isRegularFile()){
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(file, AclFileAttributeView.class);
            writeFile("common.rgh", (file.toString() + "\nUsers:Rights\n" + new TForms().fromArray(fileAttributeView.getAcl(), false) + "\n\n").getBytes());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    private void writeFile(String fileName, byte[] appendToFileBytes) {
        new Thread(new FileOut(fileName, appendToFileBytes)).start();
    }
}
