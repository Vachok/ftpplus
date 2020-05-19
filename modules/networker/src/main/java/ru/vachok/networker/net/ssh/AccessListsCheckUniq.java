// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.ad.inet.InternetUse;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.SwitchesWiFi;
import ru.vachok.networker.restapi.RestApiHelper;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;


/**
 @see ru.vachok.networker.net.ssh.AccessListsCheckUniqTest
 @since 17.04.2019 (11:30) */
public class AccessListsCheckUniq implements Callable<String> {


    private static final Pattern FILENAME_COMPILE = Pattern.compile("/pf/");

    private static final Pattern FILENAME_PATTERN = Pattern.compile(" && ");

    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
        .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, AccessListsCheckUniq.class.getSimpleName());

    private final Collection<String> fileNames = new ArrayList<>();

    protected static final String SQUIDLIMITED = "squidlimited";

    protected static final String TEMPFULL = "tempfull";

    private void parseListFiles() {
        Map<String, String> usersIPFromPFLists = getInetUniqMap();
        for (String fileName : fileNames) {
            Queue<String> stringDeque = FileSystemWorker.readFileToQueue(new File(fileName).toPath());
            while (!stringDeque.isEmpty()) {
                String key = stringDeque.poll();
                String put = usersIPFromPFLists.putIfAbsent(key, fileName);
                if (put != null) {
                    usersIPFromPFLists.put(key + " " + fileName, "NOT UNIQUE"); //пометка адресов, которые присутствуют в более чем одном списке.
                }
            }
        }
        StringBuilder fromArray = new StringBuilder();
        for (Map.Entry<String, String> ipEntries : usersIPFromPFLists.entrySet()) {
            if (ipEntries.getValue().equals("NOT UNIQUE")) {
                fromArray.append(ipEntries.getKey()).append(" ").append(ipEntries.getValue()).append("\n");
            }
        }
        messageToUser.info(getClass().getSimpleName(), ".parseListFiles", " = \n" + fromArray);
        FileSystemWorker.writeFile(FileNames.INET_UNIQ, fromArray.toString());
    }

    @Override
    public String call() {
        if (new File(ConstantsFor.AUTHORIZATION).exists()) {
            compareWithRest();
        }
        return connectTo();
    }

    private void compareWithRest() {
        Set<String> resSet = new HashSet<>();
        for (JsonValue jsonValue : genArray().values()) {
            RestApiHelper instance = RestApiHelper.getInstance(RestApiHelper.SSH);
            JsonObject result = (JsonObject) Json.parse(instance.getResult(jsonValue.asObject()));
            System.out.println("result = " + result);
        }
    }

    private JsonArray genArray() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonValues = new JsonArray();
        String authString = FileSystemWorker.readRawFile(ConstantsFor.AUTHORIZATION);
        jsonObject.add(ConstantsFor.AUTHORIZATION, authString.substring(0, (authString.length() - 2)));
        jsonObject.add(ConstantsFor.PARAM_NAME_CODE, Integer.MAX_VALUE);
        jsonObject.add(ConstantsFor.PARAM_NAME_SERVER, getSRVNeed());
        String[] listNames = {TEMPFULL, PfListsCtr.ATT_VIPNET, SQUIDLIMITED, ConstantsFor.JSON_OBJECT_SQUID};
        for (String listName : listNames) {
            jsonObject.add(ConstantsFor.PARM_NAME_COMMAND, String.format("sudo cat /etc/pf/%s & exit", listName));
            jsonValues.add(jsonObject);
        }
        return jsonValues;
    }

    private @NotNull String connectTo() {
        StringBuilder stringBuilder = new StringBuilder();
        SSHFactory.Builder builder = new SSHFactory.Builder(getSRVNeed(), ConstantsFor.SSH_UNAMEA, getClass().getSimpleName());
        SSHFactory sshFactory = builder.build();
        String[] commandsToGetList = {ConstantsFor.SSH_CAT24HRSLIST, "sudo cat /etc/pf/vipnet && exit", ConstantsFor.SSH_SHOW_PFSQUID, ConstantsFor.SSH_SHOW_SQUIDLIMITED, ConstantsFor.SSH_SHOW_PROXYFULL};
        for (String getList : commandsToGetList) {
            makePfListFiles(getList, sshFactory, stringBuilder);
        }
        parseListFiles();
        return stringBuilder.toString();
    }

    private static String getSRVNeed() {
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            return SwitchesWiFi.RUPSGATE;
        }
        else {
            return SwitchesWiFi.IPADDR_SRVGIT;
        }
    }

    private void makePfListFiles(String getList, @NotNull SSHFactory sshFactory, @NotNull StringBuilder stringBuilder) {
        sshFactory.setCommandSSH(getList);
        stringBuilder.append(AppConfigurationLocal.getInstance().submitAsString(sshFactory, 4)).append("\n");
        Set<String> stringSet = FileSystemWorker.readFileToSet(sshFactory.getTempFile());
        String fileName = FILENAME_PATTERN.split(FILENAME_COMPILE.split(getList)[1])[0] + ".list";
        fileNames.add(fileName);
        FileSystemWorker.writeFile(fileName, stringSet.stream());
    }

    @Contract(pure = true)
    private static Map<String, String> getInetUniqMap() {
        return InternetUse.getInetUniqMap();
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", AccessListsCheckUniq.class.getSimpleName() + "[\n", "\n]")
                .add("fileNames = " + fileNames.size())
                .toString();
    }
}
