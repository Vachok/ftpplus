package ru.vachok.networker.restapi.message;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.nio.charset.Charset;
import java.util.StringJoiner;


/**
 @since 31.08.2019 (10:59) */
@Service
public class MessageSwing extends ru.vachok.messenger.MessageSwing implements MessageToUser {
    
    
    private final ru.vachok.messenger.MessageSwing libMessageSwing;
    
    public MessageSwing(String header) {
        libMessageSwing = new ru.vachok.messenger.MessageSwing(header);
    }
    
    public MessageSwing() {
        libMessageSwing = new ru.vachok.messenger.MessageSwing("BEAN");
    }
    @Contract("_ -> new")
    public static @NotNull MessageSwing getI(String messengerHeader) {
        return new MessageSwing(messengerHeader);
    }
    
    @Contract(value = ConstantsFor.NULL_FALSE, pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        MessageSwing swing = (MessageSwing) o;
        
        return libMessageSwing != null ? libMessageSwing.equals(swing.libMessageSwing) : swing.libMessageSwing == null;
    }
    
    @Override
    public int hashCode() {
        return libMessageSwing != null ? libMessageSwing.hashCode() : 0;
    }
    
    @Override
    public void out(String fileName, byte[] bytesToWrite) {
        FileSystemWorker.writeFile(fileName, new String(bytesToWrite, Charset.defaultCharset()));
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", MessageSwing.class.getSimpleName() + "[\n", "\n]")
            .add(libMessageSwing.toString())
            .toString();
    }
}