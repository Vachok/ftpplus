package ru.vachok.networker.logic;


import org.slf4j.Logger;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.SetADUser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;


/**
 * @since 21.08.2018 (15:57)
 */
public class PhotoConverter {

    private static final Logger LOGGER = AppComponents.getLogger();

    private BiConsumer<String, BufferedImage> imageBiConsumer = (x, y) -> {
        File outFile = new File(x + ".JPEG");
        try {
            BufferedImage bufferedImage = new BufferedImage(y.getWidth(), y.getHeight(), BufferedImage.TYPE_INT_RGB);
            bufferedImage.createGraphics().drawImage(y, 0, 0, Color.WHITE, null);
            ImageIO.write(bufferedImage, "jpg", outFile);
            String msg = outFile.getAbsolutePath() + " written";
            LOGGER.info(msg);
        } catch (IOException e) {
            AppComponents.getLogger().error(e.getMessage(), e);
        }
    };
    public Map<String, BufferedImage> convertFoto() {
        String adFotoPrep = "C:\\Users\\ikudryashov\\Documents\\ShareX\\Screenshots\\2018-08\\pers";
        File photosDirectory = new File(adFotoPrep);
        File[] fotoFiles = photosDirectory.listFiles();
        Map<String, BufferedImage> filesList = new HashMap<>();
        try {
            for (File f : fotoFiles) {
                filesList.put(f.getName().split("\\Q.\\E")[0], ImageIO.read(f));
            }
        } catch (IOException e) {
            AppComponents.getLogger().error(e.getMessage(), e);
        }
        filesList.forEach(imageBiConsumer);
        return filesList;
    }

    public List<String> psCommands() {
        List<String> commandsAD = new ArrayList<>();
        Map<String, BufferedImage> stringBufferedImageMap = convertFoto();
        Set<String> fileNames = stringBufferedImageMap.keySet();
        for (String fileName : fileNames) {
            List<String> stringStream = new SetADUser().adFileReader();
            stringStream.forEach(x -> {
                if (x.toLowerCase().contains("samacc")) {
                    LOGGER.info(x);
                    x = "Import-RecipientDataProperty -Identity " +
                        x.split("\\Q:\\E") + //fixme 17.09.2018 (17:30)
                        " -Picture -FileData ([Byte[]] $(Get-Content -Path “C:\\PS\\jkuznetsov_photo.jpg” -Encoding Byte -ReadCount 0))";
                    commandsAD.add(x);
                }
            });
            LOGGER.info(new TForms().fromArray(commandsAD));
        }
        return commandsAD;
    }
}
