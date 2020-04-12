package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;


/**
 Class ru.vachok.networker.restapi.RestError
 <p>

 @since 12.04.2020 (13:08) */
public class RestError implements RestApiHelper {


    @Override
    public String getResult(@NotNull JsonObject jsonObject) {
        return "Error, your version of mobile app is old.\nPlease - visit \n**\nhttps://vf.vachok.ru/\n**\nfor more info. ThankU! ";
    }
}