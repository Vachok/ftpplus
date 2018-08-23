package ru.vachok.money.logic;



import org.slf4j.Logger;
import ru.vachok.money.ApplicationConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**<h1>Парсер курсов валют</h1>
 * @since 21.08.2018 (11:11)
 */
public class ParseCurrency {


   public String getTodayUSD() {
      URL url;
      File kursFile=new File("cbr.html");
      String todayUSD;
      try{
         url = new URL("https://www.cbr.ru/currency_base/daily/");
         todayUSD = url.toURI().toString();
         return todayUSD;
      }
      catch(IOException|URISyntaxException e){
         Logger logger = new ApplicationConfiguration().getLogger();
         logger.error(e.getMessage() , e);
      }
      todayUSD = new String(kursFile.getAbsoluteFile().toString().getBytes(), StandardCharsets.UTF_8);
      return todayUSD;
   }
}
