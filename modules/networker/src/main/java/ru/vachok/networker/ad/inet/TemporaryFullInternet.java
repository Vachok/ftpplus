// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 @see TemporaryFullInternetTest
 @since 28.02.2019 (11:52) */
public class TemporaryFullInternet implements Runnable, Callable<String> {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, TemporaryFullInternet.class.getSimpleName());

    private static final Queue<String> MINI_LOGGER = new ArrayDeque<>();

    private static final Map<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("StaticVariableOfConcreteClass") private static final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class
            .getSimpleName()).build();

    private static final Pattern PAT_FILEEXT_LIST = Pattern.compile(".list", Pattern.LITERAL);

    private static final Pattern PAT_BR_N = Pattern.compile("<br>\n");

    private static final Pattern PAT_SHARP = Pattern.compile(" #");

    @SuppressWarnings("CanBeFinal")
    private String userInputIpOrHostName;

    private long delStamp;

    private String optionToDo;

    private String whoCalls = UsefulUtilities.thisPC();

    private long initStamp = System.currentTimeMillis();

    private static final String NEEDED_SRV = new AppComponents().sshActs().whatSrvNeed();

    public TemporaryFullInternet(String input, long hoursToOpenInet, @NotNull String option, String whoCalls) {
        this.userInputIpOrHostName = input;
        this.delStamp = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hoursToOpenInet);
        this.optionToDo = option;
        this.whoCalls = whoCalls;
        MINI_LOGGER.add(getClass().getSimpleName() + "() starting... " + option.toUpperCase() + " " + input + " full internet access before: " + new Date(delStamp));
    }

    public TemporaryFullInternet() {
        this.userInputIpOrHostName = "10.200.213.254";
        this.delStamp = System.currentTimeMillis();
        this.optionToDo = "check";

        MINI_LOGGER.add(getClass().getSimpleName() + "() starting... " + optionToDo
            .toUpperCase() + " " + userInputIpOrHostName + " full internet access before: " + new Date(delStamp));
    }

    @Override
    public void run() {
        SSH_FACTORY.setConnectToSrv(new AppComponents().sshActs().whatSrvNeed());
        if (optionToDo != null && optionToDo.equals("add")) {
            messageToUser.info(this.getClass().getSimpleName(), "RUN", doAdd());
        }
        execOldMeth();
    }

    public TemporaryFullInternet(long timeStampOff) {
        this.userInputIpOrHostName = "10.200.213.85";
        this.delStamp = timeStampOff;
    }

    @Override
    public String call() {
        return doAdd();
    }

    @SuppressWarnings("FeatureEnvy")
    private @NotNull String doAdd() {
        SSH_FACTORY.setConnectToSrv(NEEDED_SRV);
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(userInputIpOrHostName);
        StringBuilder retBuilder = new StringBuilder();
        String sshIP = String.valueOf(nameOrIPChecker.resolveInetAddress()).split("/")[1];
        String tempString24HRSFile = sshCall();
        Map<String, String> inetUniqMap = get24hrsTempInetList();
        if (tempString24HRSFile.contains(sshIP)) {
            retBuilder.append("<h2>")
                .append(getClass().getSimpleName())
                .append(" doAdd: ")
                .append(sshIP)
                .append(" is exist!</h2><br>")
                    .append(AbstractForms.fromArray(SSH_CHECKER_MAP).replace("<br>", "\n"));
        }
        else {
            if (inetUniqMap.containsKey(sshIP) && !inetUniqMap.get(sshIP).equalsIgnoreCase("10.200.213.85")) {
                String listWhere = inetUniqMap.get(PAT_FILEEXT_LIST.matcher(sshIP).replaceAll(Matcher.quoteReplacement("")));

                retBuilder.append("<h2>").append(sshIP).append(" in regular list: ").append(listWhere).append("</h2>");
                retBuilder.append(addFromExistList(sshIP, listWhere));
            }
            else {
                String sshCommand = new StringBuilder()
                        .append(ConstantsFor.SSH_SUDO_ECHO)
                    .append("\"").append(sshIP).append(" #")
                        .append(delStamp).append("\"").append(ConstantsFor.SSHCOM_24HRS).append(ConstantsFor.SSH_INITPF).toString();
                SSH_FACTORY.setCommandSSH(sshCommand);
                retBuilder.append(SSH_FACTORY.call());
            }
        }
        MINI_LOGGER.add("doAdd(): " + retBuilder);
        return retBuilder.toString();
    }

    private @NotNull String sshCall() {
        StringBuilder tempString24HRSBuilder = new StringBuilder();
        try {
            SSH_FACTORY.setCommandSSH(ConstantsFor.COM_CAT24HRSLIST);
            tempString24HRSBuilder.append(SSH_FACTORY.call());
        }
        catch (ArrayIndexOutOfBoundsException | UnknownFormatConversionException e) {
            tempString24HRSBuilder.append(AbstractForms.fromArray(e).replace("<br>", "\n"));
        }
        finally {
            AppConfigurationLocal.getInstance().execute(this::writeLog, 21);
        }
        return tempString24HRSBuilder.toString();
    }

    @Contract(pure = true)
    private static Map<String, String> get24hrsTempInetList() {
        return InternetUse.get24hrsTempInetList();
    }

    private @NotNull StringBuilder getSSHCommandBuider(String listWhere) {
        StringBuilder comSSHBuilder = new StringBuilder();
        comSSHBuilder.append("sudo cp /etc/pf/");
        comSSHBuilder.append(listWhere);
        comSSHBuilder.append("_tmp /etc/pf/").append(listWhere).append(";");
        return comSSHBuilder;
    }

    @SuppressWarnings("FeatureEnvy")
    private String addFromExistList(String sshIP, String listWhere) {

        listWhere = PAT_FILEEXT_LIST.matcher(listWhere).replaceAll(Matcher.quoteReplacement(""));

        StringBuilder comSSHBuilder = new StringBuilder();
        comSSHBuilder.append(ConstantsFor.SSH_SUDO_GREP_V);
        comSSHBuilder.append(sshIP).append("'");
        comSSHBuilder.append(ConstantsFor.SSH_ETCPF).append(listWhere).append(" >").append(ConstantsFor.SSH_ETCPF).append(listWhere).append("_tmp;");

        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        String copyPermanentToTmp = SSH_FACTORY.call();
        messageToUser.info(copyPermanentToTmp);

        comSSHBuilder = getSSHCommandBuider(listWhere);

        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        String copyTmpToPermanent = SSH_FACTORY.call();
        MINI_LOGGER.add(copyPermanentToTmp);
        MINI_LOGGER.add(copyTmpToPermanent);
        comSSHBuilder = new StringBuilder();
        comSSHBuilder.append(ConstantsFor.SSH_SUDO_ECHO).append("\"");
        comSSHBuilder.append(sshIP).append(" #").append(delStamp).append(" #");
        comSSHBuilder.append(listWhere).append("\"").append(ConstantsFor.SSHCOM_24HRS).append(ConstantsFor.SSH_INITPF);

        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        String initNewConfig = SSH_FACTORY.call();
        messageToUser.info(initNewConfig);

        return initNewConfig;
    }

    private void sshChecker() {
        SSH_FACTORY.setCommandSSH(ConstantsFor.COM_CAT24HRSLIST);
        String fromSSH24HrsList = SSH_FACTORY.call();
        MINI_LOGGER.add(fromSSH24HrsList);

        if (fromSSH24HrsList.isEmpty()) {
            MINI_LOGGER.add("fromSSH24HrsList.isEmpty()");
            AppConfigurationLocal.getInstance().execute(this::writeLog, 21);
            throw new InvokeIllegalException(getClass().getSimpleName() + " fromSSH24HrsList.isEmpty()");
        }
        else {
            String[] strings = PAT_BR_N.split(fromSSH24HrsList);
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(this::parseString);
        }
        long atomicTimeLong = UsefulUtilities.getAtomicTime();
        for (Map.Entry<String, Long> entry : SSH_CHECKER_MAP.entrySet()) {
            String x = entry.getKey();
            Long y = entry.getValue();
            mapEntryParse(x, y, atomicTimeLong);
        }
        ConstantsNet.setSshMapStr(AbstractForms.sshCheckerMapWithDates(SSH_CHECKER_MAP, true));
        messageToUser.info(getClass().getSimpleName() + ".sshChecker", "ConstantsNet.getSshMapStr()", " = " + ConstantsNet.getSshMapStr()
            .replaceAll(ConstantsFor.STR_BR, ConstantsFor.STR_N));

    }

    private void execOldMeth() {
        AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(this::sshChecker);
        Date nextStart = new Date(UsefulUtilities.getAtomicTime() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        String fromArray = AbstractForms.fromArray(SSH_CHECKER_MAP);
        MINI_LOGGER.add(fromArray);
        MINI_LOGGER.add(nextStart.toString());
        AppConfigurationLocal.getInstance().execute(this::writeLog, 21);
    }

    private void writeLog() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        messageToUser.info(this.getClass().getSimpleName(), "creating table ", String
            .valueOf(dataConnectTo.createTable(ConstantsFor.DBTABLE_LOGTEMPINET, Collections.emptyList())));
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, this.getClass().getSimpleName());
        jsonObject.add("called", whoCalls);
        jsonObject.add("log", AbstractForms.fromArray(MINI_LOGGER));
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DBTABLE_LOGTEMPINET)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `log`.`tempinet` (`upstring`, `json`) VALUES (?, ?)")) {
                preparedStatement.setQueryTimeout(20);
                preparedStatement.setString(1, MessageFormat.format("{0} called: {1}", optionToDo, whoCalls));
                preparedStatement.setString(2, jsonObject.toString());
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error("TemporaryFullInternet.writeLog", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }

    private void parseString(String x) {
        if (PAT_SHARP.split(x).length > 2) {
            chkWithList(PAT_SHARP.split(x));
        }
        try {
            Long ifAbsent = SSH_CHECKER_MAP.putIfAbsent(PAT_SHARP.split(x)[0].trim(), Long.valueOf(PAT_SHARP.split(x)[1]));
            MINI_LOGGER.add("Added to map = " + x + " " + ifAbsent);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            messageToUser.errorAlert("TemporaryFullInternet", "sshChecker", e.getMessage());
            MINI_LOGGER.add(e.getMessage());
        }
    }

    private void mapEntryParse(String x, Long y, long atomicTimeLong) {
        String willBeDel = x + " will be deleted at " + LocalDateTime.ofEpochSecond(delStamp / 1000, 0, ZoneOffset.ofHours(3));
        MINI_LOGGER.add(willBeDel);
        this.delStamp = y;
        if (delStamp < atomicTimeLong) {
            boolean isDelete = doDelete(x);
            MINI_LOGGER.add("sshChecker(SSH_CHECKER_MAP.forEach): time is " + true + "\n" + x + " is delete = " + isDelete);
            MINI_LOGGER.add("delStamp = " + delStamp);
            MINI_LOGGER.add("ConstantsFor.getAtomicTime()-delStamp = " + (atomicTimeLong - delStamp));
        }
        else {
            MINI_LOGGER.add("IP" + " = " + x + " time: " + y + " (" + new Date(y) + ")");
        }
    }

    private void chkWithList(@NotNull String[] x) {
        this.delStamp = Long.parseLong(x[1]);
        if (delStamp < UsefulUtilities.getAtomicTime()) {
            doDelete(x[0]);
            System.out.println(addBackToList(x[0], x[2]));
        }
    }

    private static boolean doDelete(String delDomainName) {
        String sshC = new StringBuilder()
                .append(ConstantsFor.SSH_SUDO_GREP_V).append(delDomainName)
            .append("' /etc/pf/24hrs > /etc/pf/24hrs_tmp;").append("sudo cp /etc/pf/24hrs_tmp /etc/pf/24hrs;")
                .append(ConstantsFor.SSH_INITPF).toString();
        SSH_FACTORY.setCommandSSH(sshC);
        String sshCommand = SSH_FACTORY.call();
        Long aLong = SSH_CHECKER_MAP.remove(delDomainName);
        if (!(aLong == null)) {
            MINI_LOGGER.add(new Date(aLong) + ", doDelete: " + sshCommand);
        }
        return SSH_CHECKER_MAP.containsKey(delDomainName);
    }

    private static String addBackToList(String ip, String accList) {
        StringBuilder sshBuilder = new StringBuilder();
        sshBuilder.append(ConstantsFor.SSH_SUDO_ECHO).append("\"").append(ip).append(" #").append(new Date()).append("\"")
                .append(" >> /etc/pf/").append(accList).append(";").append(ConstantsFor.SSH_INITPF);
        SSH_FACTORY.setCommandSSH(sshBuilder.toString());
        return SSH_FACTORY.call();
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", TemporaryFullInternet.class.getSimpleName() + "[\n", "\n]")
            .add("userInputIpOrHostName = '" + userInputIpOrHostName + "'")
            .add("delStamp = " + delStamp)
            .add("optionToDo = '" + optionToDo + "'")
            .add("initStamp = " + initStamp)
            .toString();
    }
}
