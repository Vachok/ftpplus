package ru.vachok.networker.logic.ssh;


import com.jcraft.jsch.*;
import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.config.AppComponents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.*;


/**
 Ssh factory.
 <p>
 Фабрика, для ssh-комманд.
 */
public class SSHFactory implements Callable<String> {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String SOURCE_CLASS = SSHFactory.class.getSimpleName();

    private static InitProperties initProperties = new DBRegProperties("general-jsch");

    private static MessageToUser messageToUser = new MessageCons();

    private String connectToSrv;

    private String commandSSH;

    private String sessionType;

    private String userName;

    /*Instances*/
    private SSHFactory(Builder builder) {
        this.connectToSrv = builder.connectToSrv;
        this.commandSSH = builder.commandSSH;
        this.sessionType = builder.sessionType;
        this.userName = builder.userName;
        String pass = builder.pass;
    }

    @Override
    public String call() {
        String retString = this.getCommandSSH() + " " + this.pem();
        try(InputStream connect = connect()){
            retString = retString + " connect = " + connect().available();
            byte[] bytes = new byte[ConstantsFor.MBYTE];
            while(connect.available() > 0){
                int r = connect.read(bytes);
                messageToUser.infoNoTitles("r = " + r);
            }
            retString = retString + " " + new String(bytes, StandardCharsets.UTF_8);
        }
        catch(IOException | JSchException e){
            messageToUser.errorAlert(SOURCE_CLASS, " Exception id 123", e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
        return retString;
    }

    /**
     Gets command ssh.

     @return the command ssh
     */
    public String getCommandSSH() {
        return commandSSH;
    }

    private String pem() {
        File pemFile = new File("a161.pem");
        return pemFile.getAbsolutePath();
    }

    private InputStream connect() throws IOException, JSchException {
        Channel channel = chanRespChannel();
        InputStream inputStream = null;
        if(channel==null){
            messageToUser.out("SSHFactory_67", ("Channel is NULL!" + "\n\n" + "\nSSHFactory.connect, and ID (lineNum) is 67").getBytes());
            messageToUser.infoNoTitles(MessageFormat.format("{0} id 82. {1}", SOURCE_CLASS, " JSch channel==null"));
        }
        else{
            inputStream = channel.getInputStream();
        }

        ((ChannelExec) Objects.requireNonNull(channel)).setErrStream(new FileOutputStream(ConstantsFor.SSH_ERR));

        byte[] bytes = new byte[ConstantsFor.MBYTE];
        while(inputStream.available() > 0){
            try(FileOutputStream fileOutputStream = new FileOutputStream(ConstantsFor.SSH_OUT)){
                fileOutputStream.write(inputStream.read(bytes));
            }
            catch(IOException e){
                LOGGER.error(e.getMessage(), e);
            }
        }
        return inputStream;
    }

    private Channel chanRespChannel() throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(userName, getConnectToSrv());
        Properties properties;
        try {
            properties = initProperties.getProps();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            initProperties = new FileProps(SOURCE_CLASS);
            properties = initProperties.getProps();

        }
        jSch.addIdentity(pem());
        session.setConfig(properties);
        session.connect(ConstantsFor.TIMEOUT_2);
        Objects.requireNonNull(session).setInputStream(System.in);
        String format = MessageFormat.format("{0} {1} connected {2}|SSHFactory.chanRespChannel line 83", session.getServerVersion(), session.getHost(), session.isConnected());
        LOGGER.info(format);
        Channel channel = session.openChannel(sessionType);
        (( ChannelExec ) channel).setCommand(commandSSH);
        channel.connect();
        Objects.requireNonNull(channel);
        initProperties.setProps(properties);
        return channel;
    }

    private String getConnectToSrv() {
        return connectToSrv;
    }

    /*END FOR CLASS*/

    /**
     Builder.
     <p>
     Сам строитель.
     */
    public static class Builder implements Callable<Map<String, Boolean>> {

        /*Fields*/
        private static final ExecutorService EXECUTOR_SERVICE = Executors.unconfigurableExecutorService(Executors.newCachedThreadPool());

        private String userName = "ITDept";

        private String pass;

        private String sessionType = "exec";

        private String connectToSrv;

        private String commandSSH;

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
        public Builder setSessionType(String sessionType) {
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
        public Builder setConnectToSrv(String connectToSrv) {
            this.connectToSrv = connectToSrv;
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
        public Builder setUserName(String userName) {
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
        public Builder setPass(String pass) {
            this.pass = pass;
            return this;
        }

        @Override
        public Map<String, Boolean> call() throws ExecutionException, InterruptedException {
            Map<String, Boolean> myHashMap = new ConcurrentHashMap<>();
            Callable<String> callFactory = new SSHFactory(this);
            Future<String> b = EXECUTOR_SERVICE.submit(callFactory);
            myHashMap.put(getCommandSSH(), b.get().equalsIgnoreCase("true"));
            return myHashMap;
        }

        /*Instances*/

        /**
         Instantiates a new Builder.

         @param connectToSrv the connect to srv
         @param commandSSH   the command ssh
         */
        public Builder(String connectToSrv, String commandSSH) {
            this.commandSSH = commandSSH;
            this.connectToSrv = connectToSrv;
        }

        @Override
        public String toString() {
            return "SSH {" + "commandSSH='" + commandSSH + '\'' + ", connectToSrv='" + connectToSrv + '\'' + ", sessionType='" + sessionType + '\'' + ", userName='" + userName + '\'' + '}';
        }

        /**
         Build ssh factory.

         @return the ssh factory
         */
        public synchronized SSHFactory build() {
            return new SSHFactory(this);
        }

        /**
         Gets command ssh.

         @return the command ssh
         */
        public String getCommandSSH() {
            return commandSSH;
        }


        /**
         Sets command ssh.

         @param commandSSH the command ssh
         @return the command ssh
         */
        public Builder setCommandSSH(String commandSSH) {
            this.commandSSH = commandSSH;
            return this;
        }

    }
}