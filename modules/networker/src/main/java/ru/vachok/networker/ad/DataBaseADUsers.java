package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Stream;


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
            List<String> sql = fileRead(bufferedReader);
            while(sql.iterator().hasNext()){
                String sqlString = sql.iterator().next();
                try(PreparedStatement p = defaultConnection.prepareStatement(sqlString)){
            p.executeUpdate();
                }
            }
        } catch (SQLException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    private List<String> fileRead(BufferedReader bufferedReader) {
        Stream<String> lines = bufferedReader.lines();
        List<String> list = new ArrayList<>();
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
            try{
                if(s.toLowerCase().contains("distinguishedName")){
                    distinguishedName = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (distinguishedName) values (\'" + distinguishedName + "\')");
                }
                if(s.toLowerCase().contains("enabled")){
                    enabled = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (enabled) values (\'" + enabled + "\')");
                }
                if(s.toLowerCase().contains("givenName")){
                    givenName = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (givenName) values (\'" + givenName + "\')");
                }
                if(s.toLowerCase().contains("name")){
                    name = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (name) values (\'" + name + "\')");
                }
                if(s.toLowerCase().contains("objectClass")){
                    objectClass = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (objectClass) values (\'" + objectClass + "\')");
                }
                if(s.toLowerCase().contains("objectGUID")){
                    objectGUID = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (objectGUID) values (\'" + objectGUID + "\')");
                }
                if(s.toLowerCase().contains("samAccountName")){
                    samAccountName = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (samAccountName) values (\'" + samAccountName + "\')");
                }
                if(s.toLowerCase().contains("sid")){
                    SID = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (sid) values (\'" + SID + "\')");
                }
                if(s.toLowerCase().contains("surname")){
                    surname = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (surname) values (\'" + surname + "\')");
                }
                if(s.toLowerCase().contains("userPrincipalName")){
                    userPrincipalName = s.split(" : ")[1];
                    list.add("insert into u0466446_velkom.adusers (userPrincipalName) values (\'" + userPrincipalName + "\')");
                }
            stringBuilderSQL
                .append("insert into u0466446_velkom.adusers")
                .append("(userDomain, userName, userSurname, distinguishedName, userPrincipalName,")
                .append(" SID, samAccountName, objectClass, objectGUID, enabled)")
                .append(" values ")
                .append("(")
                .append("eatmeat.ru, ")
                .append(name + ", ")
                .append(surname + ", ")
                .append(distinguishedName + ", ")
                .append(userPrincipalName + ", ")
                .append(SID + ", ")
                .append(samAccountName + ", ")
                .append(objectClass + ", ")
                .append(objectGUID + ", ")
                .append(enabled)
                .append(")");
            }
            catch(ArrayIndexOutOfBoundsException ignore){
                //
            }
        });
        return list;
    }
}
