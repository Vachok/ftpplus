package ru.vachok.networker.logic;


import ru.vachok.networker.beans.AppComponents;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;


/**
 @since 21.08.2018 (15:57) */
public class PhotoConverter {

    private static final String SOURCE_CLASS = PhotoConverter.class.getSimpleName();

    private static Properties p = new Properties();

    private BiConsumer<String, BufferedImage> imageBiConsumer = (x, y) -> {
        File outFile = new File(x + ".jpg");
        try{
            ImageIO.write(y, "jpg", outFile);
        } catch(IOException e){
            AppComponents.getLogger().error(e.getMessage(), e);
        }
    };

    public Map<String, BufferedImage> convertFoto() {
        File photosDirectory = new File(p.getProperty("dirwithfoto"));
        File[] fotoFiles = photosDirectory.listFiles();
        Map<String, BufferedImage> filesList = new HashMap<>();
        try{
            for(File f : fotoFiles){
                filesList.put(f.getName().split("\\Q.\\E")[0], ImageIO.read(f));
            }
        } catch(IOException e){
            AppComponents.getLogger().error(e.getMessage(), e);
        }
        filesList.forEach(imageBiConsumer);
        return filesList;
    }
}
