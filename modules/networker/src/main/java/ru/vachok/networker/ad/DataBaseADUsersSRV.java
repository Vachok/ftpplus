package ru.vachok.networker.ad;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.user.ADUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;


/**
 @since 09.10.2018 (10:35) */
@Service
public class DataBaseADUsersSRV {

    @Autowired
    public DataBaseADUsersSRV(ADUser adUser) {
        dbUploader();
    }

    private boolean dbUploader() {
        DataConnectTo dataConnectTo = new RegRuMysql();
        Connection defaultConnection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + ConstantsFor.STR_VELKOM);
        try(InputStream resourceAsStream = getClass().getResourceAsStream(ConstantsFor.USERS_TXT)){
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

    private ConcurrentMap<String, String> fileRead(BufferedReader bufferedReader) {
        Stream<String> lines = bufferedReader.lines();
        ConcurrentMap<String, String> paramNameValue = new ConcurrentHashMap<>();
        lines.forEach(s -> {
            StringBuilder stringBuilderSQL = new StringBuilder();
            String distinguishedName = "";
            String enabled = "";
            String givenName = "";
            String name = "";
            String objectClass = "";
            String objectGUID = "";
            String samAccountName = "";
            String SID = "";
            String surname = "";
            String userPrincipalName = "";
            try {
                if (s.toLowerCase().contains("distinguishedName")) {
                    distinguishedName = s.split(" : ")[1];
                    paramNameValue.put("distinguishedName", distinguishedName);
                }
                if (s.toLowerCase().contains("enabled")) {
                    enabled = s.split(" : ")[1];
                    paramNameValue.put("enabled", enabled);
                }
                if (s.toLowerCase().contains("givenName")) {
                    givenName = s.split(" : ")[1];
                    paramNameValue.put("givenName", givenName);
                }
                if (s.toLowerCase().contains("name")) {
                    name = s.split(" : ")[1];
                    paramNameValue.put("name", name);
                }
                if (s.toLowerCase().contains("objectClass")) {
                    objectClass = s.split(" : ")[1];
                    paramNameValue.put("objectClass", objectClass);
                }
                if (s.toLowerCase().contains("objectGUID")) {
                    objectGUID = s.split(" : ")[1];
                    paramNameValue.put("objectGUID", objectGUID);
                }
                if (s.toLowerCase().contains("samAccountName")) {
                    samAccountName = s.split(" : ")[1];
                    paramNameValue.put("samAccountName", samAccountName);
                }
                if (s.toLowerCase().contains("sid")) {
                    SID = s.split(" : ")[1];
                    paramNameValue.put("sid", SID);
                }
                if (s.toLowerCase().contains("surname")) {
                    surname = s.split(" : ")[1];
                    paramNameValue.put("surname", surname);
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
        });
        return paramNameValue;
    }

}
