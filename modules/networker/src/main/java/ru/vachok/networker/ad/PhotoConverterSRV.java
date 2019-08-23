// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;


/**
 * @see ru.vachok.networker.ad.PhotoConverterSRVTest
 */
@Service(ModelAttributeNames.PHOTO_CONVERTER)
public class PhotoConverterSRV {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(PhotoConverterSRV.class.getSimpleName());
    
    private final Collection<String> psCommands = new ArrayList<>();
    
    private File adFotoFile;
    
    private Properties properties = AppComponents.getProps();
    
    private File rawPhotoFile;
    
    private String adPhotosPath;
    
    private @NotNull Map<String, BufferedImage> filesList = new ConcurrentHashMap<>();
    
    public File getAdFotoFile() {
        return adFotoFile;
    }
    
    public void setAdFotoFile(File adFotoFile) {
        this.adFotoFile = adFotoFile;
    }
    
    /**
     Создание списка PoShe комманд для добавления фото
     <p>
     1. {@link #convertFoto()} запуск конверсии. <br>
     2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} - запишем исключение.
     
     @return Комманды Exchange PowerShell
     */
    public @NotNull String psCommands() {
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        try {
            convertFoto();
        }
        catch (@NotNull IOException | NullPointerException e) {
            stringBuilder.append(e.getMessage()).append("<p>").append(new TForms().fromArray(e, true));
        }
        stringBuilder.append(ConstantsFor.PS_IMPORTSYSMODULES).append("<br>");
        for (String s : psCommands) {
            stringBuilder.append(s);
            stringBuilder.append("<br>");
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PhotoConverterSRV.class.getSimpleName() + "[\n", "\n]")
            .add("psCommands = " + new TForms().fromArray(psCommands))
            .add("adFotoFile = " + adFotoFile)
            .toString();
    }
    
    private void convertFoto() throws IOException {
        this.adPhotosPath = properties.getProperty(PropertiesNames.PR_ADPHOTOPATH, "\\\\srv-mail3.eatmeat.ru\\c$\\newmailboxes\\fotoraw\\");
        BiConsumer<String, BufferedImage> imageBiConsumer = this::imgWorker;
        @Nullable File[] fotoFiles = new File(adPhotosPath).listFiles();
        if ((!(fotoFiles == null) & Objects.requireNonNull(fotoFiles).length > 0) && !adPhotosPath.isEmpty()) {
            for (File rawPhotoFile : fotoFiles) {
                this.rawPhotoFile = rawPhotoFile;
                resizeRawFoto();
            }
        }
        else {
            filesList.put("No files. requireNonNull adPhotosPath is: " + adPhotosPath, null);
        }
        try {
            filesList.forEach(imageBiConsumer);
        }
        catch (NullPointerException e) {
            filesList.put(ConstantsFor.STR_ERROR, null);
        }
    }
    
    private void resizeRawFoto() throws IOException {
        for (@NotNull String format : ImageIO.getWriterFormatNames()) {
            @NotNull String key = rawPhotoFile.getName();
            if (key.contains(format)) {
                if (rawPhotoFile != null) {
                    filesList.put(key.replaceFirst("\\Q.\\E" + format, ""), ImageIO.read(rawPhotoFile));
                }
                else {
                    throw new InvokeIllegalException(MessageFormat.format("No Files with Foto {0}!", adPhotosPath));
                }
            }
        }
    }
    
    @SuppressWarnings("MagicNumber")
    private @NotNull BufferedImage scaledImage(@NotNull BufferedImage bufferedImage) {
        int newW = 113;
        int newH = 154;
    
        newH = (newW * bufferedImage.getHeight()) / bufferedImage.getWidth();
        
        Image scaledImageTMP = bufferedImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        @NotNull BufferedImage scaledImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        boolean drawImage = g2d.drawImage(scaledImageTMP, 0, 0, Color.WHITE, null);
        g2d.dispose();
        return scaledImage;
    }
    
    private void imgWorker(String rawFileName, @NotNull BufferedImage rawImage) {
        String pathName = properties.getOrDefault("pathName", "\\\\srv-mail3.eatmeat.ru\\c$\\newmailboxes\\foto\\").toString();
        @NotNull File outFile = new File(pathName + rawFileName + ".jpg");
        @NotNull String fName = "jpg";
        try {
            boolean write = ImageIO.write(scaledImage(rawImage), fName, outFile);
            if (write) {
                @NotNull String msg = outFile.getAbsolutePath() + ConstantsFor.STR_WRITTEN;
                messageToUser.info(msg);
                msg = "Import-RecipientDataProperty -Identity " +
                    rawFileName + " -Picture -FileData ([Byte[]] $(Get-Content -Path “C:\\newmailboxes\\foto\\" +
                    outFile.getName() +
                    "\" -Encoding Byte -ReadCount 0))";
                psCommands.add(msg);
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        if (outFile.exists()) {
            delRawFile(outFile);
        }
    }
    
    private void delRawFile(@NotNull File outFile) {
        String rawFilesDirName = properties.getProperty(PropertiesNames.PR_ADPHOTOPATH, "\\\\srv-mail3.eatmeat.ru\\c$\\newmailboxes\\fotoraw\\");
        @Nullable File[] rawFilesArray = new File(rawFilesDirName).listFiles();
        File[] filesList = Objects.requireNonNull(rawFilesArray);
        if (outFile.exists() & outFile.isFile()) {
            for (File file : filesList) {
                String outFileName = outFile.getName().split("\\Q.\\E")[0];
                if (file.getName().contains(outFileName)) {
                    try {
                        Files.deleteIfExists(file.toPath());
                    }
                    catch (IOException e) {
                        file.delete();
                    }
                }
            }
        }
    }
}
