package ru.vachok.networker;


import com.eclipsesource.json.JsonObject;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;

import java.util.List;


/**
 Class ru.vachok.networker.JsonMaker
 <p>

 @since 13.07.2020 (22:21) */
public class JsonMaker {


    @Test
    public void makeJSON() {
        List<String> strings = FileSystemWorker.readFileToList("d:\\poznay_sebya.txt");
        JsonObject jsonObject = new JsonObject();
        for (String string : strings) {
            String[] split = string.split("/");
            jsonObject.add(split[split.length - 1], string);
        }
        FileSystemWorker.writeFile("d:\\poznay_sebya.json", jsonObject.toString());
    }

}