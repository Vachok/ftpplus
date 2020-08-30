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
        List<String> strings = FileSystemWorker.readFileToList("f:\\OneDrive\\Загрузки\\Kniga_vseobschih_zabluzhdeniy.txt");
        JsonObject jsonObject = new JsonObject();
        for (String val : strings) {
            String[] split = val.split("/");
            String name = split[split.length - 1];
            if (name.contentEquals(val)) {
                val = "https://vaplayer.vachok.ru/mp3/Kniga_vseobschih_zabluzhdeniy/" + val;
            }
            jsonObject.add(name, val);
        }
        FileSystemWorker.writeFile("G:\\My_Proj\\VaBookPlayer\\app\\src\\main\\assets\\Kniga_vseobschih_zabluzhdeniy.json", jsonObject.toString());
    }

}