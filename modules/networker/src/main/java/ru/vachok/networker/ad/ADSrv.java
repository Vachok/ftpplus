package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.ad.user.PCUserResolver;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanCtr;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 @since 25.09.2018 (15:10) */
@Service("adsrv")
public class ADSrv implements Runnable {

    private static final String PROP_SAMACCOUNTNAME = "SamAccountName";

    private static final String PC_USER_RESOLVER_CLASS_NAME = "PCUserResolver";

    /**
     {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ADSrv.class.getName());

    private static final MessageToUser MESSAGE_TO_USER = new MessageLocal();

    /**
     {@link RegRuMysql#getDefaultConnection(String)} - u0466446_velkom ({@link ConstantsNet#DB_NAME})
     */
    private static Connection connection = new RegRuMysql().getDefaultConnection(ConstantsNet.DB_NAME);

    /**
     {@link ADUser}
     */
    private ADUser adUser = null;

    /**
     Строка из формы на сайте.
     */
    private String userInputRaw = null;

    /**
     {@link ADComputer}
     */
    private ADComputer adComputer = null;

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
    public ADUser getAdUser() {
        return adUser;
    }

    /**
     {@link ADUser}, как {@link ConcurrentHashMap}
     <p>
     {@link ADSrv#userSetter()}.{@link Optional#ifPresent(java.util.function.Consumer)}. <br>
     Поиск соответсвия {@link #adUser} и {@link #userInputRaw} <br>

     @return new {@link ConcurrentHashMap} key: <b>AD field name</b>, value: <b>x.get...</b>
     */
    public ConcurrentMap<String, String> getUserCommonRights() {
        ConcurrentMap<String, String> retMap = new ConcurrentHashMap<>();
        //noinspection OverlyLongLambda
        userSetter().stream().findAny().ifPresent((ADUser x) -> {
            boolean contains = x.getSamAccountName().toLowerCase().contains(adUser.getInputName().toLowerCase());
            if (contains) {
                ADSrv.this.adUser = x;
                retMap.put("InputName", x.getInputName());
                retMap.put(PROP_SAMACCOUNTNAME, x.getSamAccountName());
                retMap.put("Sid", x.getSid());
            }
        });
        return retMap;
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
     Читает /static/texts/users.txt

     @return {@link ADUser} как {@link List}
     */
    List<ADUser> userSetter() {
        List<String> fileAsList = new ArrayList<>();
        List<ADUser> adUserList = new ArrayList<>();
        try(InputStream usrInputStream = getClass().getResourceAsStream(ConstantsFor.USERS_TXT);
            InputStreamReader inputStreamReader = new InputStreamReader(usrInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            while(bufferedReader.ready()){
                fileAsList.add(bufferedReader.readLine());
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        int indexUser = 0;
        int h = 10;
        ADUser adU = new ADUser();
        for(int i = 0; i < fileAsList.size(); i += 10){
            indexUser++;
            try{
                List<String> list = fileAsList.subList(i, h);
                for(String s : list){
                    if(s.contains("DistinguishedName")){
                        adU.setDistinguishedName(s.split(": ")[1]);
                    }
                    if(s.contains("Enabled")){
                        adU.setEnabled(s.split(": ")[1]);
                    }
                    if(s.contains("GivenName")){
                        adU.setGivenName(s.split(": ")[1]);
                    }
                    if(s.contains("Name")){
                        adU.setName(s.split(": ")[1]);
                    }
                    if(s.contains("ObjectClass")){
                        adU.setObjectClass(s.split(": ")[1]);
                    }
                    if(s.contains("ObjectGUID")){
                        adU.setObjectGUID(s.split(": ")[1]);
                    }
                    if(s.contains(PROP_SAMACCOUNTNAME)){
                        adU.setSamAccountName(s.split(": ")[1]);
                    }
                    if(s.contains("SID")){
                        adU.setSid(s.split(": ")[1]);
                    }
                    if(s.contains("Surname")){
                        adU.setSurname(s.split(": ")[1]);
                    }
                    if(s.contains("UserPrincipalName")){
                        adU.setUserPrincipalName(s.split(": ")[1]);
                    }
                    else{
                        if(s.equals("")){
                            adUserList.add(adU);
                            adU = new ADUser();
                        }
                    }
                }
            }
            catch(IndexOutOfBoundsException | IllegalArgumentException ignore){
                //
            }
            h += 10;
        }
        String msg = indexUser + " users read";
        LOGGER.warn(msg);
        psComm();
        return adUserList;
    }

    protected ADSrv() {
    }

    public ADSrv(ADUser adUser) {
        this.userInputRaw = adUser.getInputName();
        this.adUser = adUser;
        try {
            parseFile();
        }
        catch(IndexOutOfBoundsException ignored){
            //
        }
    }

    /**
     Читает БД на предмет наличия юзера для <b>offline</b> компьютера.<br>

     @param pcName имя ПК
     @return имя юзера, время записи.
     @see ADSrv#getDetails(String)
     */
    private static synchronized String offNowGetU(CharSequence pcName) {
        StringBuilder v = new StringBuilder();
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement("select * from pcuser");
                 PreparedStatement pAuto = c.prepareStatement("select * from pcuserauto where pcName in (select pcName from pcuser) order by pcName asc limit 203");
                 ResultSet resultSet = p.executeQuery();
                 ResultSet resultSetA = pAuto.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getString(ConstantsFor.DB_FIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v
                            .append("<b>")
                            .append(resultSet.getString(ConstantsFor.DB_FIELD_USER))
                            .append("</b> <br>At ")
                            .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
                while (resultSetA.next()) {
                    if (resultSetA.getString(ConstantsFor.DB_FIELD_PCNAME).toLowerCase().contains(pcName)) {
                        v
                            .append("<p>")
                            .append(resultSet.getString(ConstantsFor.DB_FIELD_USER))
                            .append(" auto QUERY at: ")
                            .append(resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED));
                    }
                }
            }
        } catch (SQLException e) {
            new MessageCons().errorAlert(PC_USER_RESOLVER_CLASS_NAME, "offNowGetU", e.getMessage());
            FileSystemWorker.error("PCUserResolver.offNowGetU", e);
        }
        return v.toString();
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
     Резолвит онлайн пользователя ПК.
     <p>

     @param queryString запрос из браузера
     @return {@link #getUserName(String)} или {@link ADSrv#offNowGetU(CharSequence)}
     @throws IOException {@link InetAddress}.getByName(queryString + ".eatmeat.ru").isReachable(650))
     @see ActDirectoryCTRL#queryStringExists(java.lang.String, org.springframework.ui.Model)
     */
    String getDetails(String queryString) throws IOException {
        if (InetAddress.getByName(queryString + ConstantsFor.EATMEAT_RU).isReachable(ConstantsFor.TIMEOUT_650)) {
            return getUserName(queryString);
        } else {
            return offNowGetU(queryString);
        }
    }

    private void parseFile() {
        List<String> stringList;
        List<ADUser> adUsers = new ArrayList<>();
        if(adUser.getUsersAD()!=null){
            List<Integer> indexEmptyStrs = new ArrayList<>();
            stringList = adUsrFromFile();
            for(int i = 0; i < stringList.size(); i++){
                String s = stringList.get(i);
                if(s.equals("")){
                    indexEmptyStrs.add(i);
                }
            }
            for(int i = 0; i < indexEmptyStrs.size(); i++){
                List<String> oneUser = stringList.subList(indexEmptyStrs.get(i), indexEmptyStrs.get(i + 1));
                adUsers.add(setUserFromInput(oneUser));
            }
            MESSAGE_TO_USER.infoNoTitles(adUsers.size() + "");
        }
        else{
            PCUserResolver.getPcUserResolver().searchForUser(adUser.getInputName());
        }
        for(ADUser adUserth : adUsers){
            MESSAGE_TO_USER.infoNoTitles(adUserth.toString());
        }
    }

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
            if(s.contains(PROP_SAMACCOUNTNAME)){
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

    /**
     Запись в БД <b>pcuser</b><br> Запись по-запросу от браузера.
     <p>
     pcName - уникальный (таблица не переписывается или не дополняется, при наличиизаписи по-компу)
     <p>
     Лог - <b>PCUserResolver.recToDB</b> в папке запуска.
     <p>

     @param userName имя юзера
     @param pcName   имя ПК
     @see ADSrv#getDetails(String)
     */
    private static synchronized void recToDB(String userName, String pcName) {
        String sql = "insert into pcuser (pcName, userName) values(?,?)";
        String msg = userName + " on pc " + pcName + " is set.";
        try(PreparedStatement p = connection.prepareStatement(sql)){
            p.setString(1, userName);
            p.setString(2, pcName);
            p.executeUpdate();
            LOGGER.info(msg);
            ConstantsNet.PC_U_MAP.put(pcName, msg);
        }
        catch(SQLException e){
            FileSystemWorker.error("PCUserResolver.recToDB", e);
        }
    }

    private List<String> adUsrFromFile() {
        List<String> retList = new ArrayList<>();
        try (InputStream inputStream = adUser.getUsersAD().getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(retList::add);
        } catch (IOException e) {
            new MessageLocal().errorAlert("ADSrv", "adUsrFromFile", e.getMessage());
            FileSystemWorker.error("ADSrv.adUsrFromFile", e);
        }
        return retList;
    }

    /**
     Информация о пользователе ПК.
     <p>
     {@link List} со строками {@code file.lastModified() file.getName }, для папок из директории Users , компьютера из запроса. <br> {@link String} timestUserLast - последняя строка из
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
            recToDB(queryString + ConstantsFor.EATMEAT_RU, timestUserLast.split(" ")[1]);
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
     <b>Запрос на конвертацию фото</b>

     @see PhotoConverterSRV
     */
    private void psComm() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        photoConverterSRV.psCommands();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ADSrv{");
        sb.append("PC_USER_RESOLVER_CLASS_NAME='").append(PC_USER_RESOLVER_CLASS_NAME).append('\'');
        sb.append(", adUser=").append(adUser.toString());
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

}
