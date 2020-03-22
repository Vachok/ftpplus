package ru.vachok.networker.net.ssh;


import okhttp3.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.net.InetAddress;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 @since 21.03.2020 (12:34) */
public class VpnHelper extends SshActs {


    private static final String GET_STATUS_COMMAND = "cat openvpn-status && exit";

    private static final String URL_WITH_KEYS = ConstantsFor.GIT_SERVER + "/?p=.git;a=tree;f=vpn/keys/keys;h=630f3a1a66209d3569bae93df371200d24400f12;hb=refs/heads/rem";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, VpnHelper.class.getSimpleName());

    private String keyName;

    public String getStatus() {
        String result;
        try {
            InetAddress byName = InetAddress.getByName(ConstantsFor.SRV_VPN);
            if (byName.isReachable(200)) {
                result = execSSHCommand(byName.getHostAddress(), GET_STATUS_COMMAND);
            }
            else {
                result = byName + " is not Reachable".toUpperCase();
            }
        }
        catch (IOException e) {
            result = e.getMessage();
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
        okBuild.connectTimeout(2, TimeUnit.SECONDS);
        okBuild.readTimeout(10, TimeUnit.SECONDS);
        okBuild.callTimeout(20, TimeUnit.SECONDS);
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
                    stringBuilder.append(ConstantsFor.GIT_SERVER).append(html.get(1).attr("href")).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", VpnHelper.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}