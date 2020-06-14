package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.net.ssh.SshActs.whatSrvNeed;


/**
 @see VpnHelperTest
 @since 21.03.2020 (12:34) */
public class VpnHelper implements Runnable {


    private static final String GET_STATUS_COMMAND = "cat openvpn-status;exit";

    private static final String URL_WITH_KEYS = ConstantsFor.GIT_SERVER + "/?p=.git;a=tree;f=vpn/keys/keys;h=e2ff30b915f6277e541cc9415b7ce025fc0b11f4;hb=HEAD";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, VpnHelper.class.getSimpleName());

    private int connectCounter = 0;

    private String keyName;

    public String getStatus() {
        String result;
        try {
            InetAddress byName = InetAddress.getByName(ConstantsFor.SRV_VPN);
            if (byName.isReachable(200)) {
                result = execSSHCommand(byName.getHostAddress(), GET_STATUS_COMMAND);
            }
            else {
                result = MessageFormat.format("No connection to {0}. Tried {1} times.\nCommand: {2}", whatSrvNeed(), connectCounter, GET_STATUS_COMMAND);
                FileSystemWorker.writeFile(FileNames.OPENVPN_STATUS, result);
            }
        }
        else {
            this.connectCounter = 0;
            messageToUser.info(getClass().getSimpleName(), "file written", FileSystemWorker.writeFile(FileNames.OPENVPN_STATUS, result));
        }
        if (result.isEmpty() || !result.contains("OpenVPN CLIENT LIST")) {
            result = result + "\n" + whatSrvNeed() + " openvpn-status: \n" + execSSHCommand(GET_STATUS_COMMAND);
        }
        return result;
    }

    public String getConfig(String keyName) {
        String result = "";
        this.keyName = keyName;
        OkHttpClient okClient = buildClient();
        Request request = buildRequest();
        Call keyCall = okClient.newCall(request);
        try (Response response = keyCall.execute();
             ResponseBody responseBody = response.body()) {
            if (responseBody != null) {
                result = parseURLs(responseBody.string());
            }
        }
        catch (IOException e) {
            messageToUser.error("VpnHelper.getConfig", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            result = e.getMessage();
        }
        return result;
    }

    @NotNull
    private OkHttpClient buildClient() {
        OkHttpClient.Builder okBuild = new OkHttpClient.Builder();
        okBuild.connectTimeout(5, TimeUnit.SECONDS);
        okBuild.readTimeout(30, TimeUnit.SECONDS);
        okBuild.callTimeout(40, TimeUnit.SECONDS);
        return okBuild.build();
    }

    @NotNull
    @Contract(pure = true)
    private Request buildRequest() {
        Request.Builder builder = new Request.Builder().url(URL_WITH_KEYS);
        builder.get();
        return builder.build();
    }

    private String parseURLs(String responseBodyString) {
        StringBuilder stringBuilder = new StringBuilder();
        Parser documentParser = Parser.htmlParser();
        Document documentHTML = documentParser.parseInput(responseBodyString, URL_WITH_KEYS);
        for (Element element : documentHTML.getElementsByTag("td")) {
            String txtNodes = AbstractForms.fromArray(element.getAllElements());
            if (txtNodes.contains(keyName) && (txtNodes.contains(".crt") | txtNodes.contains(".key"))) {
                Elements elements = element.getAllElements();
                Elements linkElements = elements.tagName("a");
                if (linkElements.text().contains(keyName)) {
                    Elements html = linkElements.tagName("a");
                    stringBuilder.append(ConstantsFor.GIT_SERVER).append(html.get(1).attr("href")).append("\n<br>");
                }
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void run() {
        getStatus();
    }

    @NotNull
    private OkHttpClient buildClient() {
        OkHttpClient.Builder okBuild = new OkHttpClient.Builder();
        okBuild.connectTimeout(2, TimeUnit.SECONDS);
        okBuild.readTimeout(19, TimeUnit.SECONDS);
        okBuild.eventListener(new VpnHelper.CallFAILListener());
        return okBuild.build();
    }

    private static class CallFAILListener extends EventListener {


        @Override
        public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
            messageToUser.error("VpnHelper.callFailed", ioe.getMessage(), AbstractForms.fromArray(ioe));
        }
    }
}