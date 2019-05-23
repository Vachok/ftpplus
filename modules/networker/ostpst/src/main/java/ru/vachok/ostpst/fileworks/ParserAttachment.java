package ru.vachok.ostpst.fileworks;


import com.pff.PSTAttachment;
import com.pff.PSTException;
import com.pff.PSTMessage;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @since 23.05.2019 (12:41) */
class ParserAttachment {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    String saveAttachment(String path, PSTMessage message, StringBuilder stringBuilder) {
        if (message.hasAttachments()) {
            for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                hasAtt(stringBuilder, i, path, message);
            }
        }
        else {
            stringBuilder.append(message.getSubject() + " has no attachments");
        }
        return stringBuilder.toString();
    }
    
    private void hasAtt(StringBuilder stringBuilder, int i, String path, PSTMessage message) {
        Path directories = null;
        try {
            Path dir = Paths.get(path + ConstantsOst.SYSTEM_SEPARATOR + message.getDescriptorNodeId());
            if (!(dir.toFile().exists() & dir.toFile().isDirectory())) {
                directories = Files.createDirectories(dir);
            }
            else {
                directories = dir;
            }
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        stringBuilder.append(i);
        stringBuilder.append(" attachment:\n");
        PSTAttachment attachment = null;
        try {
            attachment = message.getAttachment(i);
        }
        catch (PSTException | IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        String nameAtt = directories.toAbsolutePath() + ConstantsOst.SYSTEM_SEPARATOR + message.getDescriptorNodeId() + "_" + attachment.getFilename();
        if (attachment.getSize() > 1024 * 10 & attachment.getFilename().contains(".jpg")) {
            writeAttachment(attachment, nameAtt, stringBuilder);
        }
    }
    
    private void writeAttachment(PSTAttachment attachment, String nameAtt, StringBuilder stringBuilder) {
        try (InputStream attStream = attachment.getFileInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(attStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    byte[] bytes = new byte[attachment.getSize()];
                    int readB = attStream.read(bytes);
                    stringBuilder.append(readB).append(" bytes ").append(attachment.getMimeTag()).append(" ");
                    try (OutputStream attOut = new FileOutputStream(nameAtt)) {
                        attOut.write(bytes);
                        System.out.println("Attachment saved to: " + nameAtt);
                    }
                }
            }
        }
        catch (IOException | PSTException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        
    }
}
