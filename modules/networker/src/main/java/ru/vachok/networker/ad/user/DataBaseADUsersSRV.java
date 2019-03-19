package ru.vachok.networker.ad.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.MessageLocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 @since 09.10.2018 (10:35) */
@Service
public class DataBaseADUsersSRV {

    private static MessageToUser messageToUser = new MessageLocal();

    private ADUser adUser;

    @Autowired
    public DataBaseADUsersSRV(ADUser adUser) {
        this.adUser = adUser;
    }

    public DataBaseADUsersSRV() {
    }

    Map<String, String> fileParser(List<String> strings) {
        Map<String, String> paramNameValue = new HashMap<>();
        StringBuilder stringBuilderSQL = new StringBuilder();
        String distinguishedName = "";
        String enabled = "";
        String givenName;
        String name = "";
        String objectClass = "";
        String objectGUID = "";
        String samAccountName = "";
        String SID = "";
        String surname = "";
        String userPrincipalName = "";
        for (String s : strings) {
            try {
                if (s.toLowerCase().contains("distinguishedName")) {
                    distinguishedName = s.split(" : ")[1];
                    paramNameValue.put("distinguishedName", distinguishedName);
                    adUser.setDistinguishedName(distinguishedName);
                }
                if (s.toLowerCase().contains("enabled")) {
                    enabled = s.split(" : ")[1];
                    paramNameValue.put("enabled", enabled);
                    adUser.setEnabled(enabled);
                }
                if (s.toLowerCase().contains("givenName")) {
                    givenName = s.split(" : ")[1];
                    paramNameValue.put("givenName", givenName);
                    adUser.setGivenName(givenName);
                }
                if (s.toLowerCase().contains("name")) {
                    name = s.split(" : ")[1];
                    paramNameValue.put("name", name);
                    adUser.setName(name);
                }
                if (s.toLowerCase().contains("objectClass")) {
                    objectClass = s.split(" : ")[1];
                    paramNameValue.put("objectClass", objectClass);
                    adUser.setObjectClass(objectClass);
                }
                if (s.toLowerCase().contains("objectGUID")) {
                    objectGUID = s.split(" : ")[1];
                    paramNameValue.put("objectGUID", objectGUID);
                    adUser.setObjectGUID(objectGUID);
                }
                if (s.toLowerCase().contains("samAccountName")) {
                    samAccountName = s.split(" : ")[1];
                    paramNameValue.put("samAccountName", samAccountName);
                    adUser.setSamAccountName(samAccountName);
                }
                if (s.toLowerCase().contains("sid")) {
                    SID = s.split(" : ")[1];
                    paramNameValue.put("sid", SID);
                    adUser.setSid(SID);
                }
                if (s.toLowerCase().contains("surname")) {
                    surname = s.split(" : ")[1];
                    paramNameValue.put("surname", surname);
                    adUser.setSurname(surname);
                }
                if (s.toLowerCase().contains("userPrincipalName")) {
                    userPrincipalName = s.split(" : ")[1];
                    paramNameValue.put("userPrincipalName", userPrincipalName);
                }
                stringBuilderSQL
                    .append("insert into u0466446_velkom.adusers")
                    .append("(userDomain, userName, userSurname, distinguishedName, userPrincipalName,")
                    .append(" SID, samAccountName, objectClass, objectGUID, enabled)")
                    .append(" values ")
                    .append("(")
                    .append("eatmeat.ru, ")
                    .append(name)
                    .append(", ")
                    .append(surname)
                    .append(", ")
                    .append(distinguishedName)
                    .append(", ")
                    .append(userPrincipalName)
                    .append(", ")
                    .append(SID)
                    .append(", ")
                    .append(samAccountName)
                    .append(", ")
                    .append(objectClass)
                    .append(", ")
                    .append(objectGUID)
                    .append(", ")
                    .append(enabled)
                    .append(")");
            } catch (ArrayIndexOutOfBoundsException ignore) {
                //
            }
        }
        messageToUser.infoNoTitles(stringBuilderSQL.toString());
        return paramNameValue;
    }

    ConcurrentMap<String, String> fileRead(BufferedReader bufferedReader) {
        throw new UnsupportedOperationException();
    }

    private boolean dbUploader() {

        try (Connection defaultConnection = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM);
             InputStream resourceAsStream = getClass().getResourceAsStream(ConstantsFor.FILEPATHSTR_USERSTXT)) {
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ConcurrentMap<String, String> paramNameValueMap = fileRead(bufferedReader);
            try (PreparedStatement p = defaultConnection.prepareStatement("")) {
                p.executeUpdate();
            }
        } catch (SQLException | IOException e) {
            return false;
        }
        return true;
    }

}
