// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonObject;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.data.enums.SwitchesWiFi;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;


/**
 @see TraceroutingTest
 @since 24.05.2019 (9:30) */
public class Tracerouting implements Callable<String> {


    private static final Pattern COMPILE = Pattern.compile(";");

    private static final String DB_REFERENCE = "ProviderName";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, Tracerouting.class.getSimpleName());

    @Override
    public String call() throws InterruptedException, ExecutionException, TimeoutException {
        String result;
        if (!ConstantsFor.argNORUNExist()) {
            result = getProviderTraceStr();
        }
        else {
            result = FileSystemWorker.readFile(FileNames.ARG_NO_RUN);
        }
        return result;
    }

    /**
     Traceroute
     <p>
     Соберём {@link SSHFactory} - {@link SSHFactory.Builder#build()} ({@link SwitchesWiFi#IPADDR_SRVGIT}, "traceroute ya.ru;exit") <br>
     Вызовем в строку {@code callForRoute} - {@link SSHFactory#call()}
     <p>
     Переопределим {@link SSHFactory} - {@link SSHFactory.Builder#build()} ({@link SwitchesWiFi#IPADDR_SRVNAT}, "sudo cat /home/kudr/inet.log") <br>
     Переобределим {@code callForRoute} - {@code callForRoute} + {@code "LOG: "} + {@link SSHFactory#call()}
     <p>
     Если {@code callForRoute.contains("91.210.85.")} : добавим в {@link StringBuilder} - {@code "FORTEX"} <br>
     Else if {@code callForRoute.contains("176.62.185.129")} : добавим {@code "ISTRANET"} <br>
     Если {@code callForRoute.contains("LOG: ")} добавим {@link String#split(String)}[1] по {@code "LOG: "}

     @return {@link StringBuilder#toString()} собравший инфо из строки с сервера.

     @throws ArrayIndexOutOfBoundsException при разборе строки
     */
    private @NotNull String getProviderTraceStr() throws InterruptedException, ExecutionException, TimeoutException {
        StringBuilder stringBuilder = new StringBuilder();
        SSHFactory sshFactory = new SSHFactory.Builder(new SshActs().whatSrvNeed(), "traceroute velkomfood.ru && exit", getClass().getSimpleName()).build();
        Future<String> curProvFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(sshFactory);
        String callForRoute = curProvFuture.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        stringBuilder.append("<br><a href=\"/makeok\">");
        String provNameResolved;
        if (callForRoute.contains("91.210.85.")) {
            stringBuilder.append("<h3>FORTEX</h3>");
            provNameResolved = ConstantsFor.FORTEX;
            FirebaseDatabase.getInstance().getReference(DB_REFERENCE).setValue(ConstantsFor.FORTEX, new Tracerouting.CompleteListener(ConstantsFor.FORTEX));

        }
        else {
            if (callForRoute.contains("176.62.185.129")) {
                stringBuilder.append("<h3>ISTRANET</h3>");
                provNameResolved = ConstantsFor.ISTRANET;
                FirebaseDatabase.getInstance().getReference(DB_REFERENCE).setValue(ConstantsFor.ISTRANET, new Tracerouting.CompleteListener(ConstantsFor.ISTRANET));

            }
        }
        stringBuilder.append("</a></br>");
        String logStr = "LOG: ";
        callForRoute = callForRoute + "<br>LOG: " + getInetLog();
        if (callForRoute.contains(logStr)) {
            try {
                stringBuilder.append("<br><font color=\"gray\">").append(COMPILE.matcher(callForRoute.split(logStr)[1]).replaceAll("<br>")).append("</font>");
            }
            catch (ArrayIndexOutOfBoundsException e) {
                stringBuilder.append(FileSystemWorker.error("SshActs.getProviderTraceStr", e));
            }
        }
        FirebaseDatabase.getInstance().getReference(DB_REFERENCE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                String key = snapshot.getKey();
                messageToUser.info(Tracerouting.this.getClass().getSimpleName(), "on data change", MessageFormat.format("{0}:{1}", key, value));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                messageToUser.error("Tracerouting.onCancelled", error.toException().getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace()));
            }
        });
        return stringBuilder.toString();
    }

    /**
     Лог переключений инета.

     @return {@code "sudo cat /home/kudr/inet.log"} or {@link InterruptedException}, {@link ExecutionException}, {@link TimeoutException}
     */
    private String getInetLog() {

        SSHFactory sshFactory = new SSHFactory.Builder(new SshActs().whatSrvNeed(), "sudo cat /home/kudr/inet.log", getClass().getSimpleName()).build();
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().submit(sshFactory);

        try {
            return submit.get(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return e.getMessage() + " inet switching log. May be getDefaultDS error. Keep calm - it's ok";
        }
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, "Tracerouting");
        jsonObject.add(PropertiesNames.HASH, this.hashCode());
        jsonObject.add(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
        return jsonObject.toString();
    }

    private class CompleteListener implements DatabaseReference.CompletionListener {


        private final String prName;

        CompleteListener(String prName) {
            this.prName = prName;
        }

        @Override
        public void onComplete(DatabaseError error, DatabaseReference ref) {
            Firestore fireStore = FirestoreClient.getFirestore();
            Map<String, Object> upMap = new ConcurrentHashMap<>();
            upMap.put(prName, new Date().toString());
            fireStore.collection("stats").document(DB_REFERENCE).update(upMap);
            if (error != null) {
                messageToUser.error("CompleteListener.onComplete", error.getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace()));
            }
            else if (ref != null) {
                messageToUser.info(getClass().getSimpleName(), "onComplete", ref.toString());
            }
            else {
                messageToUser.warn(getClass().getTypeName(), "DatabaseReference", "null");
            }
        }

        @Override
        public String toString() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(PropertiesNames.CLASS, "Tracerouting.CompleteListener");
            jsonObject.add("prName", prName);
            return jsonObject.toString();
        }
    }
}
