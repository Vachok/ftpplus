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
import ru.vachok.networker.net.enums.ConstantsNet;

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
     Проверяет по-базе, какие папки есть у юзера.

     @param users Active Dir Username <i>(Example: ikudryashov)</i>
     @return информация о правах юзера, взятая из БД.
     */
    public String checkCommonRightsForUserName(String users) {
        String owner;
        List<String> ownerRights = adUser.getOwnerRights();
        StringBuilder stringBuilder = new StringBuilder();
        String sql = "select * from common where users like ? LIMIT 0, 300";
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + ConstantsFor.STR_VELKOM)) {
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
                            resultSet.getString(ConstantsFor.ATT_USERS) +
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
     Читает /static/texts/users.txt

     @return {@link ADUser} как {@link List}
     */
    List<ADUser> userSetter() {
        List<String> fileAsList = new ArrayList<>();
        List<ADUser> adUserList = new ArrayList<>();
        try (InputStream usrInputStream = getClass().getResourceAsStream(ConstantsFor.USERS_TXT);
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
     Резолвит онлайн пользователя ПК.
     <p>
     @param queryString запрос из браузера
     @return {@link #getUserName(String, PCUserResolver)} или {@link PCUserResolver#offNowGetU(CharSequence)}
     @throws IOException {@link InetAddress}.getByName(queryString + ".eatmeat.ru").isReachable(650))
     @see ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)
     */
    String getDetails(String queryString) throws IOException {
        PCUserResolver pcUserResolver = PCUserResolver.getPcUserResolver();
        if (InetAddress.getByName(queryString + ConstantsFor.EATMEAT_RU).isReachable(ConstantsFor.TIMEOUT_650)) {
            return getUserName(queryString, pcUserResolver);
        } else {
            return pcUserResolver.offNowGetU(queryString);
        }
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
     Информация о пользователе ПК.
     <p>
     {@link List} со строками {@code file.lastModified() file.getName }, для папок из директории Users , компьютера из запроса. <br>
     {@link String} timestUserLast - последняя строка из сортированого списка <br>
     Цикл резолвит время файла в папке. <br>

     @param queryString запрос (имя ПК) {@code http://localhost:8880/ad?queryString}
     @param pcUserResolver {@link PCUserResolver}
     @return html Более подробно о ПК: из http://localhost:8880/ad?
     */
    private String getUserName(String queryString, PCUserResolver pcUserResolver) {
        List<String> timeName = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        File filesAsFile = new File("\\\\" + queryString + ".eatmeat.ru\\c$\\Users\\");
        File[] usersDirectory = filesAsFile.listFiles();

        stringBuilder.append("<p>   Более подробно про ПК:<br>");
        for (File file : Objects.requireNonNull(usersDirectory)) {
            timeName.add(file.lastModified() + " " + file.getName());
        }
        Collections.sort(timeName);
        String timestUserLast = timeName.get(timeName.size() - 1);
        for (String s : timeName) {
            String[] strings = s.split(" ");
            stringBuilder.append(strings[1])
                .append(" ")
                .append(new Date(Long.parseLong(strings[0])))
                .append("<br>");
        }
        ConstantsNet.COMPNAME_USERS_MAP.put(timestUserLast, filesAsFile);
        try {
            pcUserResolver.recToDB(queryString + ConstantsFor.EATMEAT_RU, timestUserLast.split(" ")[1]);
        } catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        stringBuilder.append("\n\n<p><b>")
            .append(timestUserLast)
            .append("<br>\n")
            .append(ConstantsNet.COMPNAME_USERS_MAP.size())
            .append(ConstantsNet.STR_COMPNAME_USERS_MAP_SIZE)
            .append("</p></b>");
        return stringBuilder.toString();
    }

    /**
     Запуск.
     */
    @Override
    public void run() {
        List<ADUser> adUsers = userSetter();
        adUser.setAdUsers(adUsers);
    }

}
