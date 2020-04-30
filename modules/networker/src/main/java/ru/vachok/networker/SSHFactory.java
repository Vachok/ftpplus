// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.jcraft.jsch.*;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Ssh factory.
 <p>
 Фабрика, для sshactions-комманд.

 @see ru.vachok.networker.SSHFactoryTest */
@SuppressWarnings({"unused", "ClassWithTooManyFields"})
public class SSHFactory implements Callable<String> {


    private static final String DBTABLE_GENERALJSCH = "general-jsch";

    /**
     Файл с ошибкой.
     */
    private static final File SSH_ERR = new File("ssh_err.txt");

    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
        .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, SSHFactory.class.getSimpleName());

    private final InitProperties jschProperties = InitProperties.getInstance(DBTABLE_GENERALJSCH);

    private final String classCaller;

    private final File sshErr = new File(FileNames.SSH_ERR);

    private static final String IS_CONNECTED = " session is Connected";

    private String connectToSrv;

    private String commandSSH;

    private String sessionType;

    private String userName;

    private Path tempFile;

    private Session session;

    private Channel respChannel;

    private String builderToStr;

    public Path getTempFile() {
        return tempFile;
    }

    public void setTempFile(Path tempFile) {
        this.tempFile = tempFile;
        this.tempFile.toFile().deleteOnExit();
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

    protected SSHFactory(@NotNull SSHFactory.Builder builder) {
        this.connectToSrv = builder.connectToSrv;
        this.commandSSH = builder.commandSSH;
        this.sessionType = builder.sessionType;
        this.userName = builder.userName;
        this.classCaller = builder.classCaller;
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

    @Override
    public String call() {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setName(classCaller + "SSH");
        chkTempFile();
        StringBuilder stringBuilder = new StringBuilder();
        Queue<String> recQueue = new LinkedList<>();
        byte[] bytes = new byte[ConstantsFor.KBYTE];
        int readBytes;
        try (InputStream connect = connect()) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connect))) {
                bufferedReader.lines().forEach(recQueue::add);
            }
            this.session.disconnect();
            messageToUser.info("CALL FROM CLASS: ", classCaller, MessageFormat.format("Command {1} ok on server: {0}", connectToSrv, commandSSH));
        }
        catch (IOException | RuntimeException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
            recQueue.add(e.getMessage());
            recQueue.add(AbstractForms.networkerTrace(e));
            this.session.disconnect();
            recQueue.add(session.isConnected() + IS_CONNECTED);
        }
        finally {
            if (this.respChannel.isConnected()) {
                this.respChannel.disconnect();
            }
            while (!recQueue.isEmpty()) {
                stringBuilder.append(recQueue.poll()).append("<br>\n");
            }
            FileSystemWorker.writeFile(tempFile.toAbsolutePath().normalize().toString(), stringBuilder.toString());
        }
        return stringBuilder.toString();
    }

    private void chkTempFile() {
        if (this.tempFile == null) {
            try {
                this.tempFile = Files.createTempFile(classCaller, ConstantsFor.FILESUF_SSHACTIONS);
            }
            catch (IOException e) {
                messageToUser.error("SSHFactory.call", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            }
            finally {
                if (this.tempFile != null) {
                    this.tempFile.toFile().deleteOnExit();
                }
            }
        }
    }

    private InputStream connect() throws IOException {
        boolean isConnected;
        try {
            setRespChannelToField();
        }
        catch (RuntimeException e) {
            setRespChannelToField();
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        try {
            respChannel.connect(ConstantsFor.SSH_TIMEOUT);
        }
        catch (JSchException | RuntimeException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return respChannel.getInputStream();
    }

    private void setRespChannelToField() {
        Thread.currentThread().setName(MessageFormat.format("SSH:{0}:{1}", connectToSrv, classCaller));
        JSch jSch = new JSch();
        String classMeth = "SSHFactory.setRespChannelToField";
        this.session = createSession(jSch);
        Properties properties = getConProps();
        try {
            session.setConfig(properties);
            session.connect(ConstantsFor.SSH_TIMEOUT);
        }
        catch (JSchException | RuntimeException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        try {
            this.respChannel = session.openChannel(sessionType);
            ((ChannelExec) respChannel).setCommand(commandSSH);
        }
        catch (JSchException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        Objects.requireNonNull(respChannel);
    }

    /**
     @param jSch {@link #setRespChannelToField()}
     @return {@link #session}

     @throws IllegalStateException if {@link #session} is null
     */
    private Session createSession(JSch jSch) {
        Session sessionLoc = null;
        try {
            sessionLoc = jSch.getSession(userName, getConnectToSrv());
        }
        catch (JSchException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        try {
            jSch.addIdentity(getPem());
        }
        catch (JSchException | RuntimeException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        if (sessionLoc != null) {
            return sessionLoc;
        }
        else {
            throw new IllegalStateException(getConnectToSrv() + " session null!");
        }
    }

    private Properties getConProps() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/static/sshclient.properties"));
        }
        catch (IOException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return properties;
    }

    @Contract(pure = true)
    private String getConnectToSrv() {
        return connectToSrv;
    }

    public void setConnectToSrv(String connectToSrv) {
        this.connectToSrv = connectToSrv;
    }

    @NotNull
    private String getPem() {
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
                FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
            }
        }
        pemFile.deleteOnExit();
        return pemFile.getAbsolutePath();
    }

    private InetAddress triedIP() {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        try {
            inetAddress = InetAddress.getByAddress(InetAddress.getByName(this.connectToSrv).getAddress());
        }
        catch (UnknownHostException e) {
            FileSystemWorker.appendObjectToFile(sshErr, new Date() + ": " + e.getMessage() + "\n" + AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return inetAddress;
    }

    private void tryReconnection() {
        final long startTries = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ConstantsFor.ONE_DAY_HOURS);
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

    /**
     BuildBinger.
     <p>
     Сам строитель.

     @since <a href="https://github.com/Vachok/ftpplus/commit/7bc45ca4f1968a61dfda3b009d7b0e394d573de5" target=_blank>14.11.2018 (15:25)</a>
     */
    @SuppressWarnings({"unused"})
    public static class Builder {


        private String userName = "ITDept";

        private String pass;

        private String sessionType = "exec";

        private String connectToSrv;

        private String classCaller;

        private String commandSSH;

        private SSHFactory sshFactory;

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

        public Builder(String connectToSrv, String commandSSH, String classCaller) {
            this.commandSSH = commandSSH;
            this.connectToSrv = connectToSrv;
            this.classCaller = classCaller;
            this.sshFactory = new SSHFactory(this);
        }

        protected Builder() {
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
            int result = connectToSrv != null ? connectToSrv.hashCode() : 0;
            result = 31 * result + (classCaller != null ? classCaller.hashCode() : 0);
            result = 31 * result + (commandSSH != null ? commandSSH.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SSHFactory.Builder)) {
                return false;
            }

            SSHFactory.Builder builder = (SSHFactory.Builder) o;

            if (connectToSrv != null ? !connectToSrv.equals(builder.connectToSrv) : builder.connectToSrv != null) {
                return false;
            }
            if (classCaller != null ? !classCaller.equals(builder.classCaller) : builder.classCaller != null) {
                return false;
            }
            return commandSSH != null ? commandSSH.equals(builder.commandSSH) : builder.commandSSH == null;
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