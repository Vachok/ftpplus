package ru.vachok.networker.restapi.message;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.StringJoiner;


/**
 @since 31.08.2019 (10:59) */
public class MessageSwing extends ru.vachok.messenger.MessageSwing implements MessageToUser {


    private static final ru.vachok.messenger.MessageSwing LIB_M_SWING = new ru.vachok.messenger.MessageSwing();

    private String headerMsg;

    public MessageSwing(String messengerHeader) {
        this.headerMsg = messengerHeader;
    }

    @Override
    public void setHeaderMsg(String headerMsg) {
        this.headerMsg = headerMsg;
    }

    @Contract("_ -> new")
    public static @NotNull MessageSwing getI(String messengerHeader) {
        ((MessageSwing) LIB_M_SWING).setHeaderMsg(messengerHeader);
        File file = new File(FileNames.APP_JSON);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
        jsonObject.add(PropertiesNames.CLASS, LIB_M_SWING.getClass().getTypeName());
        jsonObject.add("messengerHeader", messengerHeader);
        FileSystemWorker.appendObjectToFile(file, jsonObject);
        return (MessageSwing) LIB_M_SWING;
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerMsg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageSwing swing = (MessageSwing) o;
        return Objects.equals(headerMsg, swing.headerMsg);
    }

    @Override
    public void out(String fileName, byte[] bytesToWrite) {
        FileSystemWorker.writeFile(fileName, new String(bytesToWrite, Charset.defaultCharset()));
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", MessageSwing.class.getSimpleName() + "[\n", "\n]")
            .add(LIB_M_SWING.toString())
            .toString();
    }
}