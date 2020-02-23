package ru.vachok.networker.firebase;


import com.eclipsesource.json.JsonObject;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.perf.plugin.FirebasePerfExtension;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.vachok.networker.data.enums.ConstantsFor.FIREBASE;


/**
 @see FBAdminTest */
public class FBAdmin {


    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, FBAdmin.class.getSimpleName());

    public FBAdmin() {
        initSDK();
    }

    public void initSDK() {
        try (FileInputStream inputStream = new FileInputStream(getCred())) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .setDatabaseUrl("https://converter-2f70e.firebaseio.com/")
                .build();
            FirebaseApp.initializeApp(options);
            new FirebasePerfExtension().setInstrumentationEnabled(true);
        }

        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        catch (Exception e) {
            messageToUser.error("FBAdmin.initSDK", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }

    @NotNull
    private String getCred() {
        File file = new File(FIREBASE);
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.general");
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM velkom.general");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.getString(ConstantsFor.DBCOL_PROPERTY).equalsIgnoreCase(FIREBASE)) {
                    byte[] bins = resultSet.getBytes("bin");
                    try (FileOutputStream fileOutputStream = new FileOutputStream(FIREBASE)) {
                        fileOutputStream.write(bins);
                    }
                    catch (IOException e) {
                        messageToUser.error("FBAdmin.getCred", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("FBAdmin.getCred", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        file.deleteOnExit();
        return file.getAbsolutePath();
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("className", getClass().getSimpleName());
        jsonObject.add("messageToUser", messageToUser.toString());
        return jsonObject.toString();
    }
}
