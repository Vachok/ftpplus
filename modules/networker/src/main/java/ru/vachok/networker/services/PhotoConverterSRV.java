package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.AppComponents;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;


/**
 * <h1>Создаёт команды для MS Power Shell, чтобы добавить фото пользователей</h1>
 *
 * @since 21.08.2018 (15:57)
 */
@Service("photoConverter")
public class PhotoConverterSRV {

    /**
     * {@link Logger}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Путь до папки с фото. По-умолчанию c:\Users\ikudryashov\Documents\ShareX\Screenshots\adfoto
     */
    private String adPhotosPath = "c:\\Users\\ikudryashov\\Documents\\ShareX\\Screenshots\\adfoto";

    /**
     Файл-фото
     */
    private File adFotoFile;

    /**
     <b>Преобразование в JPG</b>
     Подготавливает фотографии для импорта в ActiveDirectory. Преобразует любой понимаемый {@link BufferedImage} формат в jpg.
     */
    private BiConsumer<String, BufferedImage> imageBiConsumer = (x, y) -> {
        File outFile = new File("\\\\srv-mail3\\c$\\newmailboxes\\foto\\" + x + ".jpg");
        String fName = "jpg";
        try {
            BufferedImage bufferedImage = new BufferedImage(y.getWidth(), y.getHeight(), BufferedImage.TYPE_INT_RGB);
            bufferedImage.createGraphics().drawImage(y, 0, 0, Color.WHITE, null);
            ImageIO.write(bufferedImage, fName, outFile);
            String msg = outFile.getAbsolutePath() + " written";
            LOGGER.info(msg);
        } catch (IOException e) {
            AppComponents.getLogger().error(e.getMessage(), e);
        }
    };

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
     * @return {@link Map}, где {@link String} - это имя файла, и
     */
    private Map<String, BufferedImage> convertFoto() throws IOException, NullPointerException {
        File photosDirectory = new File(this.adPhotosPath);
        File[] fotoFiles = photosDirectory.listFiles();
        Map<String, BufferedImage> filesList = new HashMap<>();
        for (File f : fotoFiles) {
            String key = f.getName().split("\\Q.\\E")[0];
            for (String format : ImageIO.getWriterFormatNames()) {
                if (f.getName().toLowerCase().contains(format)) filesList.put(key, ImageIO.read(f));
            }
        }
        filesList.forEach(imageBiConsumer);
        return filesList;
    }

    private void fileAsFile() {
        try {
            BufferedImage read = ImageIO.read(adFotoFile);
            LOGGER.info(read.getHeight() + " h/w " + read.getWidth());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Формирование листа комманд, на основе анализа фотографий.
     *
     * @return {@link List} команд для применения в среде PS
     * @throws IOException
     * @throws NullPointerException
     */
    public List<String> psCommands() throws IOException, NullPointerException {
        List<String> commandsAD = new ArrayList<>();
        Map<String, BufferedImage> stringBufferedImageMap = convertFoto();
        Set<String> fileNames = stringBufferedImageMap.keySet();
        List<String> stringStream = new ADUserSRV().adFileReader();
        for (String fileName : fileNames) {
            stringStream.forEach(x -> {
                if (x.toLowerCase().contains("samacc") && x.toLowerCase().contains(fileName)) {
                    x = x.split("\\Q: \\E")[1];
                    LOGGER.info(x);
                    x = "Import-RecipientDataProperty -Identity " +
                        x +
                        " -Picture -FileData ([Byte[]] $(Get-Content -Path “C:\\newmailboxes\\foto\\" + fileName + ".jpg” -Encoding Byte -ReadCount 0))";
                    commandsAD.add(x);
                }
            });
        }
        return commandsAD;
    }
}
