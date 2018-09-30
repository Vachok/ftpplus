package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.util.*;

/**
 @since 27.09.2018 (9:23) */
@Component("mailmessage")
public class MailMessage implements Serializable {

    private static final long serialVersionUID;

    private static final Logger LOGGER = LoggerFactory.getLogger(MailMessage.class.getSimpleName());

    private static InitProperties initProperties = new FileProps(MailMessage.serialVersionUID + "");

    private static Properties properties;

    private static OutputStream fileOut;

    private static InputStream fileIn;

    private static ObjectOutputStream objectOutputStream;

    private static ObjectInputStream objectInputStream;

    static {
        try {
            fileOut = new FileOutputStream(ConstantsFor.MAIL_MSG_FILE_OBJ_NAME);
            fileIn = new FileInputStream(ConstantsFor.MAIL_MSG_FILE_OBJ_NAME);
            objectOutputStream = new ObjectOutputStream(fileOut);
            objectInputStream = new ObjectInputStream(fileIn);
            properties = initProperties.getProps();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        serialVersionUID = Long.parseLong(properties.getProperty("uid", System.currentTimeMillis() + ""));
    }

    private Object o;

    private List<MailMessage> allMail = new ArrayList<>();

    private Date chkDate;

    private String fromHead;

    private String allRecepHead;

    private Date sentDate;

    private String subj;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public int hashCode() {
        int result = o != null ? o.hashCode() : 0;
        result = 31 * result + (allMail != null ? allMail.hashCode() : 0);
        result = 31 * result + (chkDate != null ? chkDate.hashCode() : 0);
        result = 31 * result + (fromHead != null ? fromHead.hashCode() : 0);
        result = 31 * result + (allRecepHead != null ? allRecepHead.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;

        MailMessage that = (MailMessage) o1;

        if (o != null ? !o.equals(that.o) : that.o != null) return false;
        if (allMail != null ? !allMail.equals(that.allMail) : that.allMail != null) return false;
        if (chkDate != null ? !chkDate.equals(that.chkDate) : that.chkDate != null) return false;
        if (fromHead != null ? !fromHead.equals(that.fromHead) : that.fromHead != null) return false;
        return allRecepHead != null ? allRecepHead.equals(that.allRecepHead) : that.allRecepHead == null;
    }

    @Override
    public String toString() {
        return new StringJoiner("\n", MailMessage.class.getSimpleName() + "\n", "\n")
            .add("allRecepHead='" + allRecepHead + "'")
            .add("fromHead='" + fromHead + "'")
            .add("sentDate=" + sentDate)
            .add("subj='" + subj + "'")
            .toString();
    }

    public MailMessage mailMessage() {
        try {
            readObject(objectInputStream);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            return new MailMessage();
        }
        MailMessage mailMessage = (MailMessage) o;
        if (o.equals(mailMessage)) LOGGER.warn("ЗЕР ГУД ВАЛЬДЕМАР!");
        return mailMessage;
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException {
        try {
            o = in.readObject();
        } catch (IOException e) {
            LoggerFactory.getLogger(MailMessage.class.getSimpleName());
        }
    }

    public List<MailMessage> getAllMail() {
        return allMail;
    }

    public void setAllMail(List<MailMessage> allMail) {
        this.allMail = allMail;
    }

    public boolean saveState() throws IOException {
        File file = new File(ConstantsFor.MAIL_MSG_FILE_OBJ_NAME);
        writeObject(objectOutputStream);
        return file.isFile() && file.exists() && file.canRead();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        properties.setProperty("uid", serialVersionUID + "");
        initProperties.setProps(properties);
        out.writeObject(this);
    }

    public Date getChkDate() {
        return chkDate;
    }

    public void setChkDate(Date chkDate) {
        this.chkDate = chkDate;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public String getSubj() {
        return subj;
    }

    public void setSubj(String subj) {
        this.subj = subj;
    }

    public String getFromHead() {
        return fromHead;
    }

    public void setFromHead(String fromHead) {
        this.fromHead = fromHead;
    }

    public String getAllRecepHead() {
        return allRecepHead;
    }

    public void setAllRecepHead(String allRecepHead) {
        this.allRecepHead = allRecepHead;
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new UnsupportedOperationException();
    }
}
