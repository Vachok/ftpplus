package ru.vachok.money;


import org.slf4j.*;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

import static ru.vachok.money.ConstantsFor.APP_NAME;


/**
 <h1>Конфигуратор</h1>

 @since 21.08.2018 (11:24) */
public class ApplicationConfiguration {

   /**
    <b>Пытается выяснить имя локального ПК</b>

    @return hostname
    */
   public static String pcName() {
      try{
         InetAddress inetAddress = InetAddress.getLocalHost();
         return inetAddress.getCanonicalHostName();
      }
      catch(UnknownHostException e){
         getLogger().error(ApplicationConfiguration.class.getSimpleName(), e.getMessage(), e);
      }
      throw new UnsupportedOperationException("Method completed, BUT : <b>No hostname resolved... Sorry</b>");
   }
   public static Logger getLogger(){
      return LoggerFactory.getLogger(pcName()+" "+APP_NAME);
   }


   /**<b>{@link Properties} из БД</b>
    @param classSimpleName имя класса - ID для БД
    @return {@link Properties}
    */
   public Properties getBaseProperties(String classSimpleName) {
      InitProperties initProperties = new DBRegProperties(classSimpleName);
      Properties props = initProperties.getProps();
      if(props!=null){
         return props;
      }
      else{
         Properties properties = new Properties();
         try(FileInputStream fileInputStream = new FileInputStream("application.properties")){
            properties.load(fileInputStream);
         }
         catch(IOException e){
            getLogger().error(e.getMessage(), e);
         }
         new DBRegProperties(classSimpleName).createNewTableInDB();
         initProperties.setProps(properties);
         return properties;
      }
   }

   static Marker marker() {
      return MarkerFactory.getMarker(APP_NAME + "-" + pcName());
   }

    public static BiConsumer<String, String> sendMeEmail = (x, y) -> {
        List<String> RCPT = new ArrayList<>();
        RCPT.add("143500@gmail.com");
        MessageToUser messageToUser = new ESender(RCPT);
        messageToUser.info(ConstantsFor.APP_NAME, x, y);
    };
}
