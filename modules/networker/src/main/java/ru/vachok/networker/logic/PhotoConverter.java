package ru.vachok.networker.logic;


import ru.vachok.networker.config.AppComponents;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


/**
 @since 21.08.2018 (15:57) */
public class PhotoConverter {

    private File photosDirectory = new File("c:\\Users\\ikudryashov\\Documents\\ShareX\\Screenshots\\2018-08\\pers\\"); //fixme refactor 05.09.2018 (22:18)

    private BiConsumer<String, BufferedImage> imageBiConsumer = (x, y) -> {
        File outFile = new File(x + ".jpg");
        try{
            ImageIO.write(y, "jpg", outFile);
        } catch(IOException e){
            AppComponents.logger().error(e.getMessage(), e);
        }
    };

    public Map<String, BufferedImage> convertFoto() {
        File[] fotoFiles = photosDirectory.listFiles();
        Map<String, BufferedImage> filesList = new HashMap<>();
        try{
            for(File f : fotoFiles){
                filesList.put(f.getName().split("\\Q.\\E")[0], ImageIO.read(f));
            }
        } catch(IOException e){
            AppComponents.logger().error(e.getMessage(), e);
        }
        filesList.forEach(imageBiConsumer);
        return filesList;
    }

}
