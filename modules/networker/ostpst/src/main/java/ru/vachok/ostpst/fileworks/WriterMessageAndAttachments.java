// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTAttachment;
import com.pff.PSTException;
import com.pff.PSTMessage;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @since 23.05.2019 (12:41) */
class WriterMessageAndAttachments {
    
    
    private MessageToUser messageToUser = new MessengerOST(getClass().getSimpleName());
    
    public MessageToUser getMessageToUser() {
        return messageToUser;
    }
    
    public void setMessageToUser(MessageToUser messageToUser) {
        this.messageToUser = messageToUser;
    }
    
    String saveAttachment(String path, PSTMessage message, StringBuilder stringBuilder) {
        Path dirS = getDirectories(path, message.getDescriptorNodeId());
        if (message.hasAttachments()) {
            stringBuilder.append(message.getNumberOfAttachments()).append(" attachments:\n");
            for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                hasAtt(stringBuilder, i, path, message);
                writeMessageNoAtt(dirS, message);
            }
        }
        else {
            stringBuilder.append(writeMessageNoAtt(dirS, message));
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
    
    private String writeMessageNoAtt(Path directories, PSTMessage message) {
        String pathDirStr = directories.toAbsolutePath() + ConstantsOst.SYSTEM_SEPARATOR + "message.txt";
        try (OutputStream outputStream = new FileOutputStream(pathDirStr);
             PrintStream printStream = new PrintStream(outputStream, true, "Windows-1251")
        ) {
            printStream.println(message);
        }
        catch (IOException e) {
            return e.getMessage() + "\n" + new TFormsOST().fromArray(e);
        }
        return pathDirStr;
    }
    
    private void hasAtt(StringBuilder stringBuilder, int i, String path, PSTMessage message) {
        Path directories = getDirectories(path, message.getDescriptorNodeId());
        PSTAttachment attachment = null;
        try {
            attachment = message.getAttachment(i);
        }
        catch (PSTException | IOException e) {
            System.err.println(e.getMessage());
            System.err.println(new TFormsOST().fromArray(e));
        }
        String nameAtt = directories.toAbsolutePath() + ConstantsOst.SYSTEM_SEPARATOR + message.getDescriptorNodeId() + "_" + attachment.getFilename();
        stringBuilder.append(nameAtt).append("\n");
        if (attachment.getSize() > 1024 * 10 & attachment.getFilename().contains(".jpg")) {
            writeAttachment(attachment, nameAtt, stringBuilder);
        }
        else {
            directories.toFile().delete();
        }
    }
    
    private void writeAttachment(PSTAttachment attachment, String nameAtt, StringBuilder stringBuilder) {
        try (InputStream attStream = attachment.getFileInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(attStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    byte[] bytes = new byte[attachment.getSize()];
                    int readB = attStream.read(bytes);
                    try (OutputStream attOut = new FileOutputStream(nameAtt)) {
                        attOut.write(bytes);
                    }
                }
            }
        }
        catch (IOException | PSTException e) {
            System.err.println(e.getMessage());
            System.err.println(new TFormsOST().fromArray(e));
        }
        
    }
    
    private Path getDirectories(String path, long id) {
        Path directories = null;
        try {
            Path dir = Paths.get(path + ConstantsOst.SYSTEM_SEPARATOR + id);
            if (dir.toFile().exists() && dir.toFile().isDirectory()) {
                directories = dir;
            }
            else {
                directories = Files.createDirectories(dir);
            }
        }
        catch (IOException e) {
            messageToUser.info(getClass().getSimpleName() + ".getDirectories", "e", " = " + e);
        }
        return directories;
    }
}
