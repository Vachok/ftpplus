package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 @since 09.10.2018 (10:35) */
@Service
public class DataBaseADUsers implements Callable<Boolean> {

    private static final Properties PROPERTIES = ConstantsFor.PROPS;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseADUsers.class.getSimpleName());

    @Override
    public Boolean call() {
        return dbUploader();
    }

    private boolean dbUploader() {
        DataConnectTo dataConnectTo = new RegRuMysql();
        Connection defaultConnection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/static/texts/users.txt")) {
            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String sql = fileRead(bufferedReader);
        try (PreparedStatement p = defaultConnection.prepareStatement(sql)) {
            p.executeUpdate();
        }
        } catch (SQLException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private String fileRead(BufferedReader bufferedReader) {
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
        String s = null;
        try {
            s = bufferedReader.readLine();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (s.toLowerCase().contains("distinguishedName")) distinguishedName = s.split(" : ")[1];
            if (s.toLowerCase().contains("enabled")) enabled = s.split(" : ")[1];
            if (s.toLowerCase().contains("givenName")) givenName = s.split(" : ")[1];
            if (s.toLowerCase().contains("name")) name = s.split(" : ")[1];
            if (s.toLowerCase().contains("objectClass")) objectClass = s.split(" : ")[1];
            if (s.toLowerCase().contains("objectGUID")) objectGUID = s.split(" : ")[1];
            if (s.toLowerCase().contains("samAccountName")) samAccountName = s.split(" : ")[1];
            if (s.toLowerCase().contains("sid")) SID = s.split(" : ")[1];
            if (s.toLowerCase().contains("surname")) surname = s.split(" : ")[1];
            if (s.toLowerCase().contains("userPrincipalName")) userPrincipalName = s.split(" : ")[1];
            stringBuilderSQL
                .append("insert into u0466446_velkom.adusers")
                .append("(userDomain, userName, userSurname, distinguishedName, userPrincipalName,")
                .append(" SID, samAccountName, objectClass, objectGUID, enabled)")
                .append(" values ")
                .append("(")
                .append("eatmeat.ru")
                .append(name)
                .append(surname)
                .append(distinguishedName)
                .append(userPrincipalName)
                .append(SID)
                .append(samAccountName)
                .append(objectClass)
                .append(objectGUID)
                .append(enabled)
                .append(")");
        return stringBuilderSQL.toString();
    }
}
