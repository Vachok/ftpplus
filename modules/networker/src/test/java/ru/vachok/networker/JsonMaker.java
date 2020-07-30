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
        List<String> strings = FileSystemWorker.readFileToList("d:\\Александр Носов Любовная зависимость.txt");
        JsonObject jsonObject = new JsonObject();
        for (String val : strings) {
            String[] split = val.split("/");
            String name = split[split.length - 1];
            if (name.contentEquals(val)) {
                val = "https://vaplayer.vachok.ru/mp3/Александр Носов Любовная зависимость/" + val;
            }
            jsonObject.add(name, val);
        }
        FileSystemWorker.writeFile("d:\\Александр Носов Любовная зависимость.json", jsonObject.toString());
    }

}