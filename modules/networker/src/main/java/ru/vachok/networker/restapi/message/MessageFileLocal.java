package ru.vachok.networker.restapi.message;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.io.*;
import java.time.LocalTime;


class MessageFileLocal implements MessageToUser {


    private File appLog = new File(FileNames.APP_JSON);

    private static final String WARN = "warn";

    private static final String INFO = "info";

    private String headerMsg;

    private String bodyMsg;

    private String titleMsg;

    @Override
    public void setHeaderMsg(String headerMsg) {
        this.headerMsg = headerMsg;
    }

    @Contract(pure = true)
    MessageFileLocal(String headerMsg) {
        this.headerMsg = headerMsg;
    }

    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.bodyMsg = bodyMsg;
        this.titleMsg = titleMsg;
        try {
            pringAppLog(ConstantsFor.STR_ERROR);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        pringAppLog(INFO);
    }

    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        pringAppLog(INFO);
    }

    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        pringAppLog(INFO);
    }

    @Override
    public void error(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        pringAppLog(ConstantsFor.STR_ERROR);
    }

    @Override
    public void error(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        pringAppLog(ConstantsFor.STR_ERROR);
    }

    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        pringAppLog(WARN);
    }

    @Override
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        pringAppLog(WARN);
    }

    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        pringAppLog(WARN);
    }

    @Override
    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        pringAppLog(WARN);
    }

    private void pringAppLog(String logType) {
        chkAppLogFile();
        JsonObject jsonObject = new JsonObject();
        try (OutputStream outputStream = new FileOutputStream(appLog, true);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            jsonObject.add(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
            jsonObject.add(logType, this.headerMsg);
            jsonObject.add(titleMsg, bodyMsg);
            printStream.println(jsonObject.toString());
        }
        catch (RuntimeException | IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void chkAppLogFile() {
        System.out.println(this.headerMsg);
        System.out.println(this.bodyMsg);
        System.out.println(this.titleMsg);
        if (appLog.exists() && appLog.length() > ConstantsFor.MBYTE) {
            boolean isDelete = this.appLog.delete();
            if (isDelete) {
                appLog = new File(FileNames.APP_JSON);
            }
            else {
                appLog = new File(FileNames.APP_JSON + "." + LocalTime.now().toSecondOfDay());
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageFileLocal{");
        sb.append("titleMsg='").append(titleMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
