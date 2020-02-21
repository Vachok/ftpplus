package ru.vachok.networker.firebase;


import com.eclipsesource.json.JsonObject;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.FileInputStream;
import java.io.IOException;


public class FBAdmin {
    
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, FBAdmin.class.getSimpleName());
    
    public void initSDK() {
        try (FileInputStream inputStream = new FileInputStream("firebase-adminsdk.json")) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .setDatabaseUrl("https://converter-2f70e.firebaseio.com/")
                    .build();
            FirebaseApp initializeApp = FirebaseApp.initializeApp(options);
        }
        catch (IOException e) {
            messageToUser.error("FBAdmin.initSDK", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
    
    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("className", getClass().getSimpleName());
        jsonObject.add("messageToUser", messageToUser.toString());
        return jsonObject.asString();
    }
}
