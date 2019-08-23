// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.jcraft.jsch.*;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Ssh factory.
 <p>
 Фабрика, для sshactions-комманд.
 
 @see ru.vachok.networker.SSHFactoryTest */
@SuppressWarnings("unused")
public class SSHFactory extends AbstractNetworkerFactory implements Callable<String> {
    
    
    private static final int SSH_TIMEOUT = LocalTime.now().toSecondOfDay() * 10;
    
    /**
     Файл с ошибкой.
     */
    private static final File SSH_ERR = new File("ssh_err.txt");
    
    private static final MessageToUser messageToUser = new MessageLocal(SSHFactory.class.getSimpleName());
    
    private InitProperties initProperties = new DBRegProperties(ConstantsFor.DBTABLE_GENERALJSCH);
    
    private String connectToSrv;
    
    private String commandSSH;
    
    private String sessionType;
    
    private String userName;
    
    private String classCaller;
    
    private Path tempFile;
    
    private Session session;
    
    private Channel respChannel;
    
    private String builderToStr;
    
    protected SSHFactory(@NotNull SSHFactory.Builder builder) {
        this.connectToSrv = builder.connectToSrv;
        this.commandSSH = builder.commandSSH;
        this.sessionType = builder.sessionType;
        this.userName = builder.userName;
        this.classCaller = builder.classCaller;
    }
    
    public Path getTempFile() {
        return tempFile;
    }
    
    public void setTempFile(Path tempFile) {
        this.tempFile = tempFile;
    }
    
    public String getSessionType() {
        return sessionType;
    }
    
    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     Gets command sshactions.
 
     @return the command sshactions
     */
    public String getCommandSSH() {
        return commandSSH;
    }
    
    public void setCommandSSH(String commandSSH) {
        this.commandSSH = commandSSH;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SSHFactory{");
        sb.append("classCaller='").append(classCaller).append('\'');
        sb.append(", commandSSH='").append(commandSSH).append('\'');
        sb.append(", connectToSrv='").append(connectToSrv).append('\'');
        sb.append(", sessionType='").append(sessionType).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    public String call() {
        StringBuilder stringBuilder = new StringBuilder();
        Queue<String> recQueue = new LinkedList<>();
        byte[] bytes = new byte[ConstantsFor.KBYTE];
        int readBytes;
        try {
            this.tempFile = Files.createTempFile(classCaller, ConstantsFor.FILESUF_SSHACTIONS);
            try (InputStream connect = connect()) {
                try (OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
                    while (true) {
                        readBytes = connect.read(bytes, 0, bytes.length);
                        if (readBytes <= 0) {
                            break;
                        }
                        outputStream.write(bytes, 0, readBytes);
                        messageToUser.info("Bytes. ", tempFile.toAbsolutePath().toString(), " is " + readBytes + " (file.len = " + tempFile.toFile().length() + ")");
                    }
                }
            }
            recQueue = FileSystemWorker.readFileToQueue(tempFile.toAbsolutePath());
            tempFile.toFile().deleteOnExit();
            this.session.disconnect();
        }
        catch (IOException | JSchException | InvokeIllegalException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".call", e));
            this.session.disconnect();
        }
        messageToUser.warn("CALL FROM CLASS: ", classCaller, MessageFormat.format("session connected {1}, to server: {0}", connectToSrv, session.isConnected()));
        while (!recQueue.isEmpty()) {
            stringBuilder.append(recQueue.poll()).append("<br>\n");
        }
        return stringBuilder.toString();
    }
    
    private InputStream connect() throws IOException, JSchException {
        boolean isConnected;
        try {
            setRespChannelToField();
        }
        catch (NullPointerException e) {
            setRespChannelToField();
            messageToUser.error(MessageFormat
                .format("SSHFactory.connect\n{0}: {1}\nParameters: []\nReturn: java.io.InputStream\nStack:\n{2}", e.getClass().getTypeName(), e
                    .getMessage(), new TForms().fromArray(e)));
        }
        respChannel.connect(SSH_TIMEOUT);
        isConnected = respChannel.isConnected();
        if (!isConnected) {
            netScanServiceFactory();
            throw new InvokeIllegalException(MessageFormat.format("RespChannel: {0} is {1} connected to {2} ({3})!",
                respChannel.toString(), NetScanService.isReach(connectToSrv), connectToSrv, triedIP()));
        }
        else {
            ((ChannelExec) Objects.requireNonNull(respChannel)).setErrStream(new FileOutputStream(SSH_ERR));
            return respChannel.getInputStream();
        }
    }
    
    private InetAddress triedIP() {
        return new NameOrIPChecker(this.connectToSrv).resolveIP();
    }
    
    private void tryReconnection() {
        final long startTries = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(UsefulUtilities.ONE_DAY_HOURS);
        final String showTime = this + "\nTries for: " + new Date(startTries);
        while (true) {
            boolean isTimeOut = System.currentTimeMillis() > (startTries);
            if (isTimeOut) {
                System.err.println(this + " is timed out.");
                break;
            }
            System.out.println(showTime);
            setRespChannelToField();
        }
    }
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private void setRespChannelToField() {
        Thread.currentThread().setName(MessageFormat.format("SSH:{0}:{1}", connectToSrv, classCaller));
        
        JSch jSch = new JSch();
        String classMeth = "SSHFactory.setRespChannelToField";
        try {
            this.session = jSch.getSession(userName, getConnectToSrv());
        }
        catch (JSchException e) {
            messageToUser.error(e.getMessage());
        }
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/static/sshclient.properties"));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("SSHFactory.setRespChannelToField: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        
        try {
            jSch.addIdentity(getPem());
        }
        catch (JSchException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".setRespChannelToField", e));
        }
        Objects.requireNonNull(session).setConfig(properties);
        try {
            System.out.println("Connecting to: " + connectToSrv + "\nUsing command(s): \n" + commandSSH.replace(";", "\n") + ".\nClass: " + classCaller);
            session.connect(SSH_TIMEOUT);
        }
        catch (JSchException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".setRespChannelToField", e));
        }
        try {
            this.respChannel = session.openChannel(sessionType);
            ((ChannelExec) respChannel).setCommand(commandSSH);
        }
        catch (JSchException e) {
            messageToUser.error(e.getMessage());
        }
        Objects.requireNonNull(respChannel);
    }
    
    @Contract(pure = true)
    private String getConnectToSrv() {
        return connectToSrv;
    }
    
    public void setConnectToSrv(String connectToSrv) {
        this.connectToSrv = connectToSrv;
    }
    
    private @NotNull String getPem() {
        File pemFile = new File("a161.pem");
        if (pemFile.exists()) {
            return pemFile.getAbsolutePath();
        }
        else {
            MysqlDataSource source = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
            String pemFileStr = "";
            String sqlGetKey = "SELECT *  FROM `sshid` WHERE `pc` LIKE 'do0213'";
            try (Connection c = source.getConnection()) {
                try (PreparedStatement p = c.prepareStatement(sqlGetKey)) {
                    try (ResultSet r = p.executeQuery()) {
                        while (r.next()) {
                            pemFileStr = r.getString("pem");
                        }
                        try (OutputStream outputStream = new FileOutputStream(pemFile)) {
                            try (PrintStream printStream = new PrintStream(outputStream, true)) {
                                printStream.print(pemFileStr);
                            }
                        }
                    }
                }
            }
            catch (SQLException | IOException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".Pem", e));
            }
        }
        pemFile.deleteOnExit();
        return pemFile.getAbsolutePath();
    }
    
    /**
     BuildBinger.
     <p>
     Сам строитель.
 
     @since <a href="https://github.com/Vachok/ftpplus/commit/7bc45ca4f1968a61dfda3b009d7b0e394d573de5" target=_blank>14.11.2018 (15:25)</a>
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class Builder {
    
    
        private String userName = "ITDept";
    
        private String pass;
    
        private String sessionType = "exec";
    
        private String connectToSrv;
    
        private String classCaller;
    
        private String commandSSH;
    
        private SSHFactory sshFactory;
    
        public Builder(String connectToSrv, String commandSSH, String classCaller) {
            this.commandSSH = commandSSH;
            this.connectToSrv = connectToSrv;
            this.classCaller = classCaller;
            this.sshFactory = new SSHFactory(this);
        }
    
        @Contract(pure = true)
        protected Builder() {
        }
    
        /**
         Gets command sshactions.
     
         @return the command sshactions
         */
        public String getCommandSSH() {
            return commandSSH;
        }
        
        /**
         Sets command sshactions.
 
         @param commandSSH the command sshactions
         @return the command sshactions
         */
        public SSHFactory.Builder setCommandSSH(String commandSSH) {
            this.commandSSH = commandSSH;
            return this;
        }
    
        /**
         Gets user name.
     
         @return the user name
         */
        public String getUserName() {
            return userName;
        }
    
        /**
         Sets user name.
     
         @param userName the user name
         @return the user name
         */
        public SSHFactory.Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }
    
        /**
         Gets pass.
     
         @return the pass
         */
        public String getPass() {
            return pass;
        }
        
        /**
         Sets pass.
         
         @param pass the pass
         @return the pass
         */
        public SSHFactory.Builder setPass(String pass) {
            this.pass = pass;
            return this;
        }
        
        /**
         Gets session type.
 
         @return the session type
         */
        public String getSessionType() {
            return sessionType;
        }
    
        /**
         Sets session type.
     
         @param sessionType the session type
         @return the session type
         */
        public SSHFactory.Builder setSessionType(String sessionType) {
            this.sessionType = sessionType;
            return this;
        }
    
        /**
         Gets connect to srv.
     
         @return the connect to srv
         */
        public String getConnectToSrv() {
            return connectToSrv;
        }
    
        /**
         Sets connect to srv.
     
         @param connectToSrv the connect to srv
         @return the connect to srv
         */
        public SSHFactory.Builder setConnectToSrv(String connectToSrv) {
            this.connectToSrv = connectToSrv;
            return this;
        }
    
        /**
         Build sshactions factory.
     
         @return the sshactions factory
         */
        public SSHFactory build() {
            System.out.println("getPem() = " + getPem());
            return sshFactory;
        }
    
        public String getPem() {
            return this.sshFactory.getPem();
        }
    
        @Override
        public int hashCode() {
            int result = getUserName().hashCode();
            result = 31 * result + (getPass() != null ? getPass().hashCode() : 0);
            result = 31 * result + getSessionType().hashCode();
            result = 31 * result + (getConnectToSrv() != null ? getConnectToSrv().hashCode() : 0);
            result = 31 * result + (getCommandSSH() != null ? getCommandSSH().hashCode() : 0);
            return result;
        }
    
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Builder{");
            sb.append("userName='").append(userName).append('\'');
            sb.append(", pass='").append(pass).append('\'');
            sb.append(", sessionType='").append(sessionType).append('\'');
            sb.append(", connectToSrv='").append(connectToSrv).append('\'');
            sb.append(", classCaller='").append(classCaller).append('\'');
            sb.append(", commandSSH='").append(commandSSH).append('\'');
            sb.append(", sshFactory=").append(sshFactory);
            sb.append('}');
            return sb.toString();
        }
    }
}