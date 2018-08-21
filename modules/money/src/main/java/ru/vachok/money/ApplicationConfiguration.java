package ru.vachok.money;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.money.ctrls.ErrCtrl;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;


/**
 <h1>Конфигуратор</h1>

 @since 21.08.2018 (11:24) */
public class ApplicationConfiguration {

   public Logger logger = LoggerFactory.getLogger(ConstantsFor.APP_NAME);

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
         ErrCtrl.stackErr(e);
      }
      throw new UnsupportedOperationException("Method completed, BUT : <b>No hostname resolved... Sorry</b>");
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
            logger.error(e.getMessage(), e);
         }
         new DBRegProperties(classSimpleName).createNewTableInDB();
         initProperties.setProps(properties);
         return properties;
      }
   }
}
