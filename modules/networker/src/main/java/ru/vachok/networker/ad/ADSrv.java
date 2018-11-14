package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


/**
 <h1></h1>

 @since 25.09.2018 (15:10) */
@Service("adsrv")
public class ADSrv implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ADSrv.class.getName());

    private ADUser adUser;

    private ADComputer adComputer;

    private String userInputRaw;

    ADUser getAdUser() {
        return adUser;
    }

    public ADComputer getAdComputer() {
        return adComputer;
    }

    public String getUserInputRaw() {
        return userInputRaw;
    }

    public void setUserInputRaw(String userInputRaw) {
        this.userInputRaw = userInputRaw;
    }

    /*Instances*/
    @Autowired
    public ADSrv(ADUser adUser, ADComputer adComputer) {
        this.adUser = adUser;
        this.adComputer = adComputer;
    }

    @Override
    public void run() {
        List<ADUser> adUsers = userSetter();
        adUser.setAdUsers(adUsers);
    }

    /**
     Читает /static/texts/users.txt

     @return {@link ADUser} как {@link List}
     */
    List<ADUser> userSetter() {
        List<String> fileAsList = new ArrayList<>();
        List<ADUser> adUserList = new ArrayList<>();
        try (InputStream usrInputStream = getClass().getResourceAsStream("/static/texts/users.txt");
             InputStreamReader inputStreamReader = new InputStreamReader(usrInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            while (bufferedReader.ready()) {
                fileAsList.add(bufferedReader.readLine());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        int indexUser = 0;
        int h = 10;
        ADUser adU = new ADUser();
        for (int i = 0; i < fileAsList.size(); i += 10) {
            indexUser++;
            try {
                List<String> list = fileAsList.subList(i, h);
                for (String s : list) {
                    if (s.contains("DistinguishedName")) {
                        adU.setDistinguishedName(s.split(": ")[1]);
                    }
                    if (s.contains("Enabled")) {
                        adU.setEnabled(s.split(": ")[1]);
                    }
                    if (s.contains("GivenName")) {
                        adU.setGivenName(s.split(": ")[1]);
                    }
                    if (s.contains("Name")) {
                        adU.setName(s.split(": ")[1]);
                    }
                    if (s.contains("ObjectClass")) {
                        adU.setObjectClass(s.split(": ")[1]);
                    }
                    if (s.contains("ObjectGUID")) {
                        adU.setObjectGUID(s.split(": ")[1]);
                    }
                    if (s.contains("SamAccountName")) {
                        adU.setSamAccountName(s.split(": ")[1]);
                    }
                    if (s.contains("SID")) {
                        adU.setSID(s.split(": ")[1]);
                    }
                    if (s.contains("Surname")) {
                        adU.setSurname(s.split(": ")[1]);
                    }
                    if (s.contains("UserPrincipalName")) {
                        adU.setUserPrincipalName(s.split(": ")[1]);
                    } else {
                        if (s.equals("")) {
                            adUserList.add(adU);
                            adU = new ADUser();
                        }
                    }
                }
            } catch (IndexOutOfBoundsException | IllegalArgumentException ignore) {
                //
            }
            h += 10;
        }
        String msg = indexUser + " users read";
        LOGGER.warn(msg);
        psComm();
        return adUserList;
    }

    /**
     <b>Запрос на конвертацию фото</b>

     @see PhotoConverterSRV
     */
    private void psComm() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        photoConverterSRV.psCommands();
    }

    /**
     @param queryString запрос из браузера
     @return имя компьютера и пользователя
     @throws IOException {@link InetAddress}.getByName(queryString + ".eatmeat.ru").isReachable(500))
     */
    String getDetails(String queryString) throws IOException {
        if (InetAddress.getByName(queryString + ".eatmeat.ru").isReachable(500)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<p>   Более подробно про ПК:<br>");
            File filesAsFile = new File(new StringBuilder()
                .append("\\\\")
                .append(queryString)
                .append(".eatmeat.ru\\c$\\Users\\")
                .toString());
            File[] files = filesAsFile.listFiles();
            List<String> timeName = new ArrayList<>();
            for (File file : Objects.requireNonNull(files)) {
                timeName.add(file.lastModified() + " " + file.getName());
            }
            Collections.sort(timeName);
            String s1 = timeName.get(timeName.size() - 1);
            for (String s : timeName) {
                String[] strings = s.split(" ");
                stringBuilder.append(strings[1])
                    .append(" ")
                    .append(new Date(Long.parseLong(strings[0])))
                    .append("<br>");
            }
            ConstantsFor.COMPNAME_USERS_MAP.put(s1, filesAsFile);
            try {
                new PCUserResolver().recToDB(queryString + ".eatmeat.ru", s1.split(" ")[1]);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                //
            }
            stringBuilder.append("<p><b>")
                .append(s1)
                .append("<br>")
                .append(ConstantsFor.COMPNAME_USERS_MAP.size())
                .append(" COMPNAME_USERS_MAP size")
                .append("</p></b>");
            return stringBuilder.toString();
        } else {
            return new PCUserResolver().offNowGetU(queryString);
        }
    }

    /**
     <b>allmailbox.txt</b>

     @return файл построчно
     */
    private List<String> adFileReader() {
        List<String> strings = new ArrayList<>();
        File adUsers = new File("allmailbox.txt");
        try (FileReader fileReader = new FileReader(adUsers);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            while (bufferedReader.ready()) {
                strings.add(bufferedReader.readLine());
            }
        } catch (IOException | InputMismatchException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info(adUser.toString());
        return strings;
    }

    /**
     <b>Обновление БД velkom.adusers</b>

     @param adU {@link ADUser}
     */
    private void sendToDB(ADUser adU) {
        DataConnectTo dataConnectTo = new RegRuMysql();
        StringBuilder sql = new StringBuilder();
        String str = "\', \'";
        sql
            .append("insert into adusers (distinguishedName, enabled, givenName, name, objectClass, objectGUID, ")
            .append("samAccountName, ")
            .append("SID, Surname, UserPrincipalName) values (\'")
            .append(adU.getDistinguishedName()).append(str)
            .append(adU.getEnabled()).append(str)
            .append(adU.getGivenName()).append(str)
            .append(adU.getName()).append(str)
            .append(adU.getObjectClass()).append(str)
            .append(adU.getObjectGUID()).append(str)
            .append(adU.getSamAccountName()).append(str)
            .append(adU.getSID()).append(str)
            .append(adU.getSurname()).append(str)
            .append(adU.getUserPrincipalName())
            .append("\');");
        try (Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom")) {
            try (PreparedStatement p = c.prepareStatement(sql.toString())) {
                p.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
