package ru.vachok.money.logic;


import ru.vachok.money.ctrls.ErrCtrl;

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
      String todayUSD = "no currency";
      try{
         url = new URL("https://www.cbr.ru/currency_base/daily/");
         todayUSD = url.toURI().toString();
         return todayUSD;
      }
      catch(IOException|URISyntaxException e){
         ErrCtrl.stackErr(e);
      }
      todayUSD = new String(kursFile.getAbsoluteFile().toString().getBytes(), StandardCharsets.UTF_8);
      return todayUSD;
   }
}
