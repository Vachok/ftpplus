// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;


/**
 <h1>Создаёт команды для MS Power Shell, чтобы добавить фото пользователей</h1>
 
 @since 21.08.2018 (15:57) */
@Service(ConstantsFor.ATT_PHOTO_CONVERTER)
public class PhotoConverterSRV {
    
    
    /**
     {@link Logger}
     */
    private static final MessageToUser messageToUser = new MessageLocal(PhotoConverterSRV.class.getSimpleName());
    
    private final Collection<String> psCommands = new ArrayList<>();
    
    /**
     Файл-фото
     */
    private File adFotoFile;
    
    private Properties properties = AppComponents.getProps();
    
    @SuppressWarnings("unused")
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
    public String psCommands() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            convertFoto();
        }
        catch (IOException | NullPointerException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "psCommands", e.getMessage());
            stringBuilder.append(e.getMessage()).append("<p>").append(new TForms().fromArray(e, true));
        }
        stringBuilder.append("ImportSystemModules").append("<br>");
        for (String s : psCommands) {
            stringBuilder.append(s);
            stringBuilder.append("<br>");
        }
        return stringBuilder.toString();
    }
    
    private void convertFoto() throws NullPointerException, IOException {
        String adPhotosPath = properties.getProperty("adphotopath", "\\\\srv-mail3.eatmeat.ru\\c$\\newmailboxes\\fotoraw\\");
        Map<String, BufferedImage> filesList = new HashMap<>();
        File[] fotoFiles = new File(adPhotosPath).listFiles();
        BiConsumer<String, BufferedImage> imageBiConsumer = this::imgWorker;
        if ((fotoFiles != null & fotoFiles.length > 0) && !adPhotosPath.isEmpty()) {
            for (File f : fotoFiles) {
                for (String format : ImageIO.getWriterFormatNames()) {
                    String key = f.getName();
                    if (key.contains(format)) {
                        filesList.put(key.replaceFirst("\\Q.\\E" + format, ""), ImageIO.read(f));
                    }
                }
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
    
    private BufferedImage resizedImage(BufferedImage bufferedImage) {
        int newW = 113;
        int newH = 154;
    
        newH = (newW * bufferedImage.getHeight()) / bufferedImage.getWidth();
        
        Image scaledImageTMP = bufferedImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        boolean drawImage = g2d.drawImage(scaledImageTMP, 0, 0, Color.WHITE, null);
        g2d.dispose();
        return scaledImage;
    }
    
    private void imgWorker(String rawFileName, BufferedImage rawImage) {
        String pathName = properties.getOrDefault("pathName", "\\\\srv-mail3.eatmeat.ru\\c$\\newmailboxes\\foto\\").toString();
        File outFile = new File(pathName + rawFileName + ".jpg");
        String fName = "jpg";
        try {
            boolean write = ImageIO.write(resizedImage(rawImage), fName, outFile);
            if (write) {
                String msg = outFile.getAbsolutePath() + ConstantsFor.STR_WRITTEN;
                messageToUser.info(msg);
                msg = "Import-RecipientDataProperty -Identity " +
                    rawFileName + " -Picture -FileData ([Byte[]] $(Get-Content -Path “C:\\newmailboxes\\foto\\" +
                    outFile.getName() +
                    "\" -Encoding Byte -ReadCount 0))";
                messageToUser.warn(msg);
                psCommands.add(msg);
            }
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(outFile.getName().replace(".jpg", ".err"), e));
        }
    }
}
