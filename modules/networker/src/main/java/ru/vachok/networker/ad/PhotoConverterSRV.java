package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;


/**
 <h1>Создаёт команды для MS Power Shell, чтобы добавить фото пользователей</h1>

 @since 21.08.2018 (15:57) */
@Service (ConstantsFor.ATT_PHOTO_CONVERTER)
public class PhotoConverterSRV {

    /**
     {@link Logger}
     */
    private static final MessageToUser messageToUser = new MessageLocal(PhotoConverterSRV.class.getSimpleName());

    private final Properties properties = AppComponents.getOrSetProps();

    /**
     Путь до папки с фото.
     */
    private String adPhotosPath = properties.getProperty("adphotopath", "\\\\srv-mail3.eatmeat.ru\\c$\\newmailboxes\\fotoraw\\");

    /**
     Файл-фото
     */
    private File adFotoFile;
    
    private final Collection<String> psCommands = new ArrayList<>();

    /**
     <b>Преобразование в JPG</b>
     Подготавливает фотографии для импорта в ActiveDirectory. Преобразует любой понимаемый {@link BufferedImage} формат в jpg.
     */
    @SuppressWarnings("OverlyLongLambda")
    private final BiConsumer<String, BufferedImage> imageBiConsumer = (String x, BufferedImage y) -> {
        String pathName = properties.getOrDefault("pathName", "\\\\srv-mail3.eatmeat.ru\\c$\\newmailboxes\\foto\\").toString();
        File outFile = new File(pathName + x + ".jpg");
        String fName = "jpg";
        Set<String> samAccountNames = samAccFromDB();
        for (String sam : samAccountNames) {
            if (sam.toLowerCase().contains(x)) x = sam;
        }
        try {
            BufferedImage bufferedImage = new BufferedImage(y.getWidth(), y.getHeight(), BufferedImage.TYPE_INT_RGB);
            bufferedImage.createGraphics().drawImage(y, 0, 0, Color.WHITE, null);
            ImageIO.write(bufferedImage, fName, outFile);
            String msg = outFile.getAbsolutePath() + " written"; messageToUser.info(msg);
            msg = "Import-RecipientDataProperty -Identity " +
                x + " -Picture -FileData ([Byte[]] $(Get-Content -Path “C:\\newmailboxes\\foto\\" +
                outFile.getName() +
                "\" -Encoding Byte -ReadCount 0))"; messageToUser.warn(msg);
            psCommands.add(msg);
        } catch (Exception e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "", e.getMessage()); FileSystemWorker.error(getClass().getSimpleName() + ".", e);
        }
    };

    @SuppressWarnings("unused")
    public File getAdFotoFile() {
        return adFotoFile;
    }

    public void setAdFotoFile(File adFotoFile) {
        this.adFotoFile = adFotoFile;
    }

    public String getAdPhotosPath() {
        return adPhotosPath;
    }

    public void setAdPhotosPath(String adPhotosPath) {
        this.adPhotosPath = adPhotosPath;
    }

    /**
     Создание списка PoShe комманд для добавления фото
     <p>
     1. {@link #convertFoto()} запуск конверсии. <br>
     2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} - запишем исключение.
     @return Комманды Exchange PowerShell
     */
    String psCommands() {
        try {
            convertFoto();
        } catch (IOException | NullPointerException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "psCommands", e.getMessage());
            FileSystemWorker.error("PhotoConverterSRV.psCommands", e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : psCommands) {
            stringBuilder.append(s);
            stringBuilder.append("<br>");
        }
        return stringBuilder.toString();
    }

    private void convertFoto() throws NullPointerException, IOException {
        Map<String, BufferedImage> filesList = new HashMap<>();
        File[] fotoFiles = new File(this.adPhotosPath).listFiles();
        if (fotoFiles != null && !adPhotosPath.isEmpty()) {
            for (File f : fotoFiles) {
                for (String format : ImageIO.getWriterFormatNames()) {
                    String key = f.getName();
                    if (key.contains(format)) filesList.put(key.replaceFirst("\\Q.\\E" + format, ""), ImageIO.read(f));
                }
            }
        } else {
            filesList.put("No files. requireNonNull adPhotosPath is: " + adPhotosPath, null);
        }
        try {
            filesList.forEach(imageBiConsumer);
        } catch (NullPointerException e) {
            filesList.put("ERROR", null);
        }
    }

    private Set<String> samAccFromDB() {

        Set<String> samAccounts = new HashSet<>();

        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM);
             PreparedStatement p = c.prepareStatement("select * from u0466446_velkom.adusers");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                samAccounts.add(r.getString("samAccountName"));
            }
        }
        catch(SQLException | IOException e){
            FileSystemWorker.error("PhotoConverterSRV.samAccFromDB", e);
        }
        return samAccounts;
    }
}
