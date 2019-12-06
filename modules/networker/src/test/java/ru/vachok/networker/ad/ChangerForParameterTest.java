package ru.vachok.networker.ad;


import com.eclipsesource.json.JsonObject;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class ChangerForParameterTest {
    
    
    private Set<JsonObject> jsonObjectSet = new HashSet<>();
    
    private Path filePath = Paths.get("telephones.csv");
    
    @Test
    @Ignore
    public void changeAttr() {
        Set<String> readFile = FileSystemWorker.readFileToEncodedSet(filePath, "Windows-1251");
        for (String phone : readFile) {
            String[] splitBySemicolon = phone.split(";");
            JsonObject jsonObject = new JsonObject();
            if (splitBySemicolon.length == 4) {
                jsonObject.add("name", splitBySemicolon[1]);
                jsonObject.add("number", splitBySemicolon[3]);
            }
            jsonObjectSet.add(jsonObject);
        }
        makeCommands();
    }
    
    private void makeCommands() {
        List<String> psCommands = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjectSet) {
            try {
                if (!jsonObject.get("name").asString().isEmpty()) {
                    try {
                        int num = Integer.parseInt(jsonObject.get("number").asString());
                        String uName = jsonObject.get("name").toString().split(" ")[0].replace("\"", "");
                        String psCommand = "Get-ADUser -Filter 'name -like \"" + uName + "*\"' | Set-ADUser -OfficePhone '" + num + "'";
                        psCommands.add(psCommand);
                    }
                    catch (NumberFormatException ignore) {
                        //
                    }
                }
            }
            catch (RuntimeException ignore) {
                //
            }
        }
        System.out.println("psCommands = " + AbstractForms.fromArray(psCommands));
    }
}
