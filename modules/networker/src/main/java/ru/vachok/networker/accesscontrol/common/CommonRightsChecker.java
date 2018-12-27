package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileOut;

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

public class CommonRightsChecker extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = AppComponents.getLogger();


    private static final ThreadPoolTaskExecutor TASK_EXECUTOR = new ThreadConfig().threadPoolTaskExecutor();

    public CommonRightsChecker() throws IOException {
        Thread.currentThread().setName(getClass().getSimpleName());

        boolean b1 = Files.deleteIfExists(new File("common.own").toPath());
        boolean b = Files.deleteIfExists(new File("common.rgh").toPath());

        String msg = "Starting a new instance of " + getClass().getSimpleName() + " at " + new Date() + "\n" + b + " " + b1 + " deleted";
        LOGGER.warn(msg);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(dir, AclFileAttributeView.class);
            List<AclEntry> acl = fileAttributeView.getAcl();
            writeFile("common.own", (dir.toString() + " owner is: " + Files.getOwner(dir).getName() + "\nUsers:Rights\n" + new TForms().fromArray(acl, false) + "\n\n").getBytes());
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            AclFileAttributeView fileAttributeView = Files.getFileAttributeView(file, AclFileAttributeView.class);
            writeFile("common.rgh", (file.toString() + "\nUsers:Rights\n" + new TForms().fromArray(fileAttributeView.getAcl(), false) + "\n\n").getBytes());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    private void writeFile(String fileName, byte[] appendToFileBytes) {
        TASK_EXECUTOR.execute(new FileOut(fileName, appendToFileBytes));
    }
}
