package ru.vachok.networker.beans;


import java.util.List;


/**
 @since 27.08.2018 (14:38) */
public class ToStringFrom {

   public void fromArr() {
      throw new UnsupportedOperationException();
   }

   public String fromArr(List<String> list) {
      return list.toString().replaceAll(", ", "<br>")
            .replace("\\Q:\\E", "")
            .replace("\\Q[\\E", "");
   }
}
