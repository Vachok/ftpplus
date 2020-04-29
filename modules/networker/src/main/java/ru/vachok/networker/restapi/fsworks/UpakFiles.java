// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.Keeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 @see ru.vachok.networker.restapi.fsworks.UpakFilesTest
 @since 06.07.2019 (7:32) */
public class UpakFiles implements Keeper {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UpakFiles.class.getSimpleName());

    private List<File> filesToPack = new ArrayList<>();

    private String zipName = "null";

    private int compressionLevelFrom0To9 = 9;

    public void createZip(File[] files) throws InvokeIllegalException {
        if (files.length <= 0) {
            throw new InvokeIllegalException(AbstractForms.fromArray(files));
        }
        else {
            this.zipName = files[0].toPath().getParent().getFileName() + ".zip";
            try {
                Files.deleteIfExists(new File(zipName).toPath().toAbsolutePath().normalize());
            }
            catch (IOException e) {
                messageToUser.error("UpakFiles", "createZip", e.getMessage() + " see line: 61");
            }
            filesToPack.addAll(Arrays.asList(files));
            createZip(filesToPack, zipName, compressionLevelFrom0To9);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpakFiles{");
        sb.append("zipName='").append(zipName).append('\'');

        sb.append(", filesToPack=").append(filesToPack);
        sb.append(", compressionLevelFrom0To9=").append(compressionLevelFrom0To9);
        sb.append('}');
        return sb.toString();
    }

    public String createZip(List<File> filesToZip, String zipName, int compressionLevelFrom0To9) {
        this.filesToPack = filesToZip;
        this.zipName = zipName;
        this.compressionLevelFrom0To9 = compressionLevelFrom0To9;
        makeZip();
        return new File(zipName).getAbsolutePath();
    }

    private void makeZip() {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipName))) {
            for (File toZipFile : filesToPack) {
                zipOutputStream.setLevel(compressionLevelFrom0To9);
                packFile(toZipFile, zipOutputStream);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".makeZip");
        }
        File zipCreatedFile = new File(zipName);
        System.out.println(zipCreatedFile.getAbsolutePath() + " zip. Size = " + (zipCreatedFile.length() / ConstantsFor.MBYTE) + " compress level: " + compressionLevelFrom0To9);
    }

    private void packFile(@NotNull File toZipFile, @NotNull ZipOutputStream zipOutputStream) {
        try (InputStream inputStream = new FileInputStream(toZipFile)) {
            ZipEntry zipEntry = new ZipEntry(toZipFile.getName());
            zipOutputStream.putNextEntry(zipEntry);

            zipEntry.setCreationTime(FileTime.fromMillis(System.currentTimeMillis()));
            byte[] bytesBuff = new byte[ConstantsFor.KBYTE];
            while (inputStream.read(bytesBuff) > 0) {
                zipOutputStream.write(bytesBuff);
            }
            zipOutputStream.closeEntry();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 82");
        }
    }
}