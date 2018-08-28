package ru.vachok.networker.logic;



import ru.vachok.networker.ApplicationConfiguration;
import ru.vachok.networker.web.ConstantsFor;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.IIOByteBuffer;
import java.io.File;
import java.io.IOException;


/**
 @since 21.08.2018 (15:57) */
public class PhotoConverter {

   File photosDirectory = new File("c:\\Users\\ikudryashov\\Documents\\ShareX\\Screenshots\\2018-08\\pers\\");

   public static void main(String[] args) {

   }

   private void convertFoto() {
      for(File f : photosDirectory.listFiles()){
         try(FileImageInputStream imageInputStream = new FileImageInputStream(f)){
            while(imageInputStream.read() > 0){
               byte[] iBytes;
               imageInputStream.readBytes(new IIOByteBuffer(new byte[ConstantsFor.MBYTE], 0, ConstantsFor.MBYTE), imageInputStream.read());
            }
         }
         catch(IOException e){
            ApplicationConfiguration.logger().error(e.getMessage(), e);
         }
      }
   }
}
