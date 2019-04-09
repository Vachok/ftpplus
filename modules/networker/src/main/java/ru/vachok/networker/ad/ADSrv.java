package ru.vachok.networker.ad;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.ad.user.PCUserResolver;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 @since 25.09.2018 (15:10) */
@Service("adSrv")
public class ADSrv implements Runnable {
    
    
    private static final String PROP_SAMACCOUNTNAME = "SamAccountName";
    
    private static final MessageToUser messageToUser = new MessageLocal(ADSrv.class.getSimpleName());
    
    /**
     {@link ADUser}
     */
    private ADUser adUser;
    
    /**
     Строка из формы на сайте.
     */
    private String userInputRaw;
    
    /**
     {@link ADComputer}
     */
    private ADComputer adComputer;
    
    /**
     Thread name = ADSrv
     
     @param adUser {@link ADUser}
     @param adComputer {@link ADComputer}
     */
    @Autowired
    public ADSrv(ADUser adUser, ADComputer adComputer) {
        this.adUser = adUser;
        this.adComputer = adComputer;
        Thread.currentThread().setName(getClass().getSimpleName());
    }
    
    public ADSrv(ADUser adUser) {
        this.userInputRaw = adUser.getInputName();
        this.adUser = adUser;
        try {
            parseFile();
        }
        catch (IndexOutOfBoundsException ignored) {
            //
        }
    }
    
    
    protected ADSrv() {
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
    @SuppressWarnings("unused")
    public String getUserInputRaw() {
        return userInputRaw;
    }
    
    
    /**
     @param userInputRaw пользовательский ввод в строку
     */
    public void setUserInputRaw(String userInputRaw) {
        this.userInputRaw = userInputRaw;
    }
    
    
    /**
     @return {@link #adUser}
     */
    public ADUser getAdUser() {
        return adUser;
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
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM)) {
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
        }
        catch (SQLException | IOException e) {
            return e.getMessage();
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ADSrv{");
        sb.append("CLASS_NAME_PCUSERRESOLVER='").append(ConstantsFor.CLASS_NAME_PCUSERRESOLVER).append('\'');
        sb.append(", adUser=").append(adUser);
        sb.append(", userInputRaw='").append(userInputRaw).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    
    /**
     Запуск.
     */
    @Override
    public void run() {
        new MessageCons().errorAlert("ADSrv.run");
    }
    
    
    /**
     Читает /static/texts/users.txt
     
     @return {@link ADUser} как {@link List}
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    List<ADUser> userSetter() {
        List<String> fileAsList = new ArrayList<>();
        List<ADUser> adUserList = new ArrayList<>();
        try (InputStream usrInputStream = getClass().getResourceAsStream(ConstantsFor.FILEPATHSTR_USERSTXT);
             InputStreamReader inputStreamReader = new InputStreamReader(usrInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            while (bufferedReader.ready()) {
                fileAsList.add(bufferedReader.readLine());
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + "\n" + new TForms().fromArray(e, false));
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
                    if (s.contains(PROP_SAMACCOUNTNAME)) {
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
                    }
                    else {
                        if (s.equals("")) {
                            adUserList.add(adU);
                            adU = new ADUser();
                        }
                    }
                }
            }
            catch (IndexOutOfBoundsException | IllegalArgumentException ignore) {
                //
            }
            h += 10;
        }
        String msg = indexUser + " users read";
        messageToUser.warn(msg);
        psComm();
        return adUserList;
    }
    
    /**
     Резолвит онлайн пользователя ПК.
     <p>
     
     @param queryString запрос из браузера
     @return {@link #getUserName(String)} или {@link ADSrv#offNowGetU(CharSequence)}
     
     @throws IOException {@link InetAddress}.getByName(queryString + ".eatmeat.ru").isReachable(650))
     @see ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)
     */
    String getDetails(String queryString) throws IOException {
        InternetUse internetUse = new InetUserPCName();
        String internetUseUsage = internetUse.getUsage(queryString + ConstantsFor.DOMAIN_EATMEATRU);
        internetUseUsage = internetUseUsage.replace("юзер", "компьютер");
        if (InetAddress.getByName(queryString + ConstantsFor.DOMAIN_EATMEATRU).isReachable(ConstantsFor.TIMEOUT_650)) {
            return getUserName(queryString) + "<p><center>" + internetUseUsage + "</center>";
        }
        else {
            return offNowGetU(queryString) + "<p><center>" + internetUseUsage + "</center>";
        }
    }
    
    /**
     Читает БД на предмет наличия юзера для <b>offline</b> компьютера.<br>
     
     @param pcName имя ПК
     @return имя юзера, время записи.
     
     @see ADSrv#getDetails(String)
     */
    private static String offNowGetU(CharSequence pcName) {
        StringBuilder v = new StringBuilder();
        try (Connection c = new AppComponents().connection(ConstantsFor.DBDASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement("select * from pcuser");
                 PreparedStatement pAuto = c.prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by pcName asc limit 203");
                 ResultSet resultSet = p.executeQuery();
                 ResultSet resultSetA = pAuto.executeQuery()
            ) {
                while (resultSet.next()) {
                    if (resultSet.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v.append("<b>").append(resultSet.getString(ConstantsFor.DB_FIELD_USER)).append("</b> <br>At ").append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
                while (resultSetA.next()) {
                    if (resultSetA.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v.append("<p>").append(resultSet.getString(ConstantsFor.DB_FIELD_USER)).append(" auto QUERY at: ").append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
            }
        }
        catch (SQLException | IOException e) {
            new MessageCons().errorAlert(ConstantsFor.CLASS_NAME_PCUSERRESOLVER, "offNowGetU", e.getMessage());
            FileSystemWorker.error("PCUserResolver.offNowGetU", e);
        }
        return v.toString();
    }
    
    /**
     Запись в БД <b>pcuser</b><br> Запись по-запросу от браузера.
     <p>
     pcName - уникальный (таблица не переписывается или не дополняется, при наличиизаписи по-компу)
     <p>
     Лог - <b>PCUserResolver.recToDB</b> в папке запуска.
     <p>
     
     @param userName имя юзера
     @param pcName имя ПК
     @see ADSrv#getDetails(String)
     */
    private static void recToDB(String userName, String pcName) {
        String sql = "insert into pcuser (pcName, userName) values(?,?)";
        String msg = userName + " on pc " + pcName + " is set.";
        try (Connection connection = new AppComponents().connection(ConstantsNet.DB_NAME);
             PreparedStatement p = connection.prepareStatement(sql)
        ) {
            p.setString(1, userName);
            p.setString(2, pcName);
            p.executeUpdate();
            messageToUser.info(msg);
            ConstantsNet.getPcUMap().put(pcName, msg);
        }
        catch (SQLException ignore) {
            //nah
        }
        catch (IOException e) {
            messageToUser.errorAlert(ADSrv.class.getSimpleName(), "recToDB", e.getMessage());
            FileSystemWorker.error("ADSrv.recToDB", e);
        }
    }
    
    private void parseFile() {
        List<String> stringList;
        List<ADUser> adUsers = new ArrayList<>();
        if (adUser.getUsersAD() != null) {
            List<Integer> indexEmptyStrings = new ArrayList<>();
            stringList = adUsrFromFile();
            for (int i = 0; i < stringList.size(); i++) {
                String s = stringList.get(i);
                if (s.equals("")) {
                    indexEmptyStrings.add(i);
                }
            }
            for (int i = 0; i < indexEmptyStrings.size(); i++) {
                List<String> oneUser = stringList.subList(indexEmptyStrings.get(i), indexEmptyStrings.get(i + 1));
                adUsers.add(setUserFromInput(oneUser));
            }
            messageToUser.infoNoTitles(adUsers.size() + "");
        }
        else {
            PCUserResolver.getPcUserResolver().searchForUser(adUser.getInputName());
        }
        for (ADUser adUser1 : adUsers) {
            messageToUser.infoNoTitles(adUser1.toString());
        }
    }
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private ADUser setUserFromInput(List<String> uList) {
        ADUser adU = new ADUser();
        for (String s : uList) {
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
            if (s.contains(PROP_SAMACCOUNTNAME)) {
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
            }
        }
        return adU;
    }
    
    private List<String> adUsrFromFile() {
        List<String> retList = new ArrayList<>();
        try (InputStream inputStream = adUser.getUsersAD().getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(retList::add);
        }
        catch (IOException e) {
            messageToUser.errorAlert(ADSrv.class.getSimpleName(), "adUsrFromFile", e.getMessage());
            FileSystemWorker.error("ADSrv.adUsrFromFile", e);
        }
        return retList;
    }
    
    
    /**
     Информация о пользователе ПК.
     <p>
     {@link List} со строками {@code file.lastModified() file.getName }, для папок из директории Users , компьютера из запроса. <br> {@link String} timesUserLast - последняя строка из
     сортированого списка <br> Цикл резолвит время файла в папке. <br>
     
     @param queryString запрос (имя ПК) {@code http://localhost:8880/ad?queryString}
     @return html Более подробно о ПК: из http://localhost:8880/ad?
     */
    private String getUserName(String queryString) {
        List<String> timeName = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        File filesAsFile = new File("\\\\" + queryString + ".eatmeat.ru\\c$\\Users\\");
        File[] usersDirectory = filesAsFile.listFiles();
    
        stringBuilder.append("<p>   Более подробно про ПК:<br>");
    
        for (File file : Objects.requireNonNull(usersDirectory)) {
            if (!file.getName().toLowerCase().contains("temp") &&
                !file.getName().toLowerCase().contains("default") &&
                !file.getName().toLowerCase().contains("public") &&
                !file.getName().toLowerCase().contains("all") &&
                !file.getName().toLowerCase().contains("все") &&
                !file.getName().toLowerCase().contains("desktop")) {
                timeName.add(file.lastModified() + " " + file.getName());
            }
        }
        Collections.sort(timeName);
        String timesUserLast = timeName.get(timeName.size() - 1);
        for (String s : timeName) {
            String[] strings = s.split(" ");
            stringBuilder.append(strings[1])
                .append(" ")
                .append(new Date(Long.parseLong(strings[0])))
                .append("<br>");
        }
        ConstantsNet.getPCnameUsersMap().put(timesUserLast, filesAsFile);
        try {
            recToDB(queryString + ConstantsFor.DOMAIN_EATMEATRU, timesUserLast.split(" ")[1]);
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        stringBuilder.append("\n\n<p><b>").append(timesUserLast).append("<br>\n").append(ConstantsNet.getPCnameUsersMap().size())
            .append(ConstantsNet.STR_COMPNAME_USERS_MAP_SIZE)
            .append("</p></b>");
        return stringBuilder.toString();
    }
    
    /**
     <b>Запрос на конвертацию фото</b>
     
     @see PhotoConverterSRV
     */
    private void psComm() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        photoConverterSRV.psCommands();
    }
    
}
