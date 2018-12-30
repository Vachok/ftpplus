package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.net.NetScanCtr;
import ru.vachok.networker.net.NetScannerSvc;

import java.io.*;
import java.net.InetAddress;
import java.sql.*;
import java.util.Date;
import java.util.*;


/**
 @since 25.09.2018 (15:10) */
@Service("adsrv")
public class ADSrv implements Runnable {

    /**
     {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ADSrv.class.getName());

    /**
     {@link ADUser}
     */
    private ADUser adUser;

    /**
     {@link ADComputer}
     */
    private ADComputer adComputer;

    /**
     Строка из формы на сайте.
     */
    private String userInputRaw = null;

    /**
     Проверяет по-базе, какие папки есть у юзера.

     @param users Active Dir Username <i>(Example: ikudryashov)</i>
     @return информация о правах юзера, взятая из БД.
     */
    public String checkCommonRightsForUserName(String users) {
        String owner;
        List<String> ownerRights = adUser.getOwnerRights();
        StringBuilder stringBuilder = new StringBuilder();
        String sql = "select * from common where users like ? LIMIT 0, 300";
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom")) {
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                preparedStatement.setString(1, "%" + users + "%");
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        owner = "<details><summary><b>" +
                            resultSet.getString("dir") +
                            " </b>***Владелец: " +
                            resultSet.getString("user") +
                            " Время проверки: " +
                            resultSet.getString("timerec") +
                            "</summary><small>" +
                            resultSet.getString(ConstantsFor.USERS) +
                            "</small></details>";
                        ownerRights.add(owner);
                    }
                }
            }
            stringBuilder.append("<font color=\"yellow\">")
                .append(sql.replaceAll("\\Q?\\E", users))
                .append("</font><br>Пользователь отмечен в правах на папки:<br>");
            stringBuilder.append(new TForms().fromArray(ownerRights, true));
            adUser.setOwnerRights(ownerRights);
            return stringBuilder.toString();
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     @return {@link #adComputer}
     */
    public ADComputer getAdComputer() {
        return adComputer;
    }

    /**
     @return {@link #userInputRaw}
     */
    public String getUserInputRaw() {
        return userInputRaw;
    }

    /**
     @param userInputRaw пользовательский ввод в строку
     @see NetScanCtr#pcNameForInfo(NetScannerSvc, BindingResult, Model)
     */
    public void setUserInputRaw(String userInputRaw) {
        this.userInputRaw = userInputRaw;
    }

    /**
     @return {@link #adUser}
     */
    ADUser getAdUser() {
        return adUser;
    }

    /**
     Thread name = ADSrv

     @param adUser     {@link ADUser}
     @param adComputer {@link ADComputer}
     */
    @Autowired
    public ADSrv(ADUser adUser, ADComputer adComputer) {
        this.adUser = adUser;
        this.adComputer = adComputer;
        Thread.currentThread().setName(getClass().getSimpleName());
    }

    /**
     Запуск.
     */
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
                        adU.setSid(s.split(": ")[1]);
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
        if(InetAddress.getByName(queryString + ConstantsFor.EATMEAT_RU).isReachable(500)){
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
                new PCUserResolver().recToDB(queryString + ConstantsFor.EATMEAT_RU, s1.split(" ")[1]);
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

}
