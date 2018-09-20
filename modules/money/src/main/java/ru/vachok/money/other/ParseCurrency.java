package ru.vachok.money.other;


import org.slf4j.Logger;
import ru.vachok.money.ConstantsFor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**<h1>Парсер курсов валют</h1>
 * @since 21.08.2018 (11:11)
 */
@Deprecated
public class ParseCurrency {


    private String downloadCbrHTML() {
      URL url;
      File kursFile=new File("cbr.html");
        String todayAllCurrency;
      try{
         url = new URL("https://www.cbr.ru/currency_base/daily/");
          todayAllCurrency = url.toURI().toString();
          return todayAllCurrency;
      }
      catch(IOException|URISyntaxException e){
          Logger logger = ConstantsFor.getLogger();
         logger.error(e.getMessage() , e);
      }
        todayAllCurrency = new String(kursFile.getAbsoluteFile().toString().getBytes(), StandardCharsets.UTF_8);
        return todayAllCurrency;
   }

}
