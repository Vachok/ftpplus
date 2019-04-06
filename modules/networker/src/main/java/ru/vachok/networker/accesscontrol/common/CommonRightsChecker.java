package ru.vachok.networker.accesscontrol.common;



import org.slf4j.Logger;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;


@SuppressWarnings ("DuplicateStringLiteralInspection")
@Deprecated
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


    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if(attrs.isDirectory()){
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
            List<AclEntry> acl = fileAttributeView.getAcl();
            writeFile("common.own", (dir + " owner is: " + Files.getOwner(dir).getName() + "\nUsers:Rights\n" + new TForms().fromArray(acl, false) + "\n\n").getBytes());
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(attrs.isRegularFile()){
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(file, AclFileAttributeView.class);
            writeFile("common.rgh", (file + "\nUsers:Rights\n" + new TForms().fromArray(fileAttributeView.getAcl(), false) + "\n\n").getBytes());
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
        throw new IllegalComponentStateException("Deprecated");
    }
}
