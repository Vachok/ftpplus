package ru.vachok.networker;


import com.jcraft.jsch.*;
import org.slf4j.Logger;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;


/**
 Ssh factory.
 <p>
 Фабрика, для ssh-комманд.
 */
public class SSHFactory implements Callable<String> {

    /**
     Файл с ошибкой.
     */
    private static final File SSH_ERR = new File("ssh_err.txt");

    private static final Logger LOGGER = AppComponents.getLogger(SSHFactory.class.getSimpleName());

    private static final String SOURCE_CLASS = SSHFactory.class.getSimpleName();

    private static final MessageToUser messageToUser = new MessageLocal();

    private InitProperties initProperties = new DBRegProperties("general-jsch");

    private String connectToSrv;

    private String commandSSH;

    private String sessionType;

    private String userName;

    private Channel respChannel;

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
     Gets command ssh.

     @return the command ssh
     */
    private String getCommandSSH() {
        return commandSSH;
    }

    public void setCommandSSH(String commandSSH) {
        this.commandSSH = commandSSH;
    }

    private SSHFactory(Builder builder) {
        this.connectToSrv = builder.connectToSrv;
        this.commandSSH = builder.commandSSH;
        this.sessionType = builder.sessionType;
        this.userName = builder.userName;
    }

    public String call() {
        StringBuilder stringBuilder = new StringBuilder();
        try(InputStream connect = connect()){
            messageToUser.info(connect().available() + "", " bytes, ssh-channel is ", respChannel.isConnected() + "");
            byte[] bytes = new byte[ConstantsFor.KBYTE * 20];
            while(connect.available() > 0){
                int r = connect.read(bytes);
                messageToUser.info(getClass().getSimpleName(), "connect read bytes", " = " + r);
            }
            stringBuilder.append(" <br>\n" + new String(bytes, StandardCharsets.UTF_8));
        }
        catch(IOException | JSchException e){
            messageToUser.errorAlert(SOURCE_CLASS, " Exception id 123", e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        return stringBuilder.toString();
    }

    private InputStream connect() throws IOException, JSchException {
        boolean isConnected = false;
        chanRespChannel();
        respChannel.connect();
        isConnected = respChannel.isConnected();
        if(!isConnected){
            messageToUser.out("SSHFactory_67", ("Channel is NULL!" + "\n\n" + "\nSSHFactory.connect, and ID (lineNum) is 67").getBytes());
            messageToUser.info(getClass().getSimpleName(), "connect()", MessageFormat.format("{0} id 82. {1}", SOURCE_CLASS, " JSch channel==null"));
            respChannel.disconnect();
        }
        else{
            (( ChannelExec ) Objects.requireNonNull(respChannel)).setErrStream(new FileOutputStream(SSH_ERR));
            return respChannel.getInputStream();
        }
        respChannel.disconnect();
        throw new RejectedExecutionException("ХУЙ FOR YOU!");
    }

    private void chanRespChannel() {
        JSch jSch = new JSch();
        Session session = null;
        try{
            session = jSch.getSession(userName, getConnectToSrv());
        }
        catch(JSchException e){
            messageToUser.errorAlert("SSHFactory", "chanRespChannel", e.getMessage());
            FileSystemWorker.error("SSHFactory.chanRespChannel", e);
        }
        Properties properties = new Properties();
        try{
            properties = initProperties.getProps();
        }
        catch(Exception e){
            sshException(e);
        }

        try{
            jSch.addIdentity(pem());
        }
        catch(JSchException e){
            messageToUser.errorAlert("SSHFactory", "chanRespChannel", e.getMessage());
            FileSystemWorker.error("SSHFactory.chanRespChannel", e);
        }

        session.setConfig(properties);

        try{
            session.connect(ConstantsFor.TIMEOUT_650);
        }
        catch(JSchException e){
            messageToUser.errorAlert("SSHFactory", "chanRespChannel", e.getMessage());
            FileSystemWorker.error("SSHFactory.chanRespChannel", e);
        }

        Objects.requireNonNull(session).setInputStream(System.in);

        try{
            this.respChannel = session.openChannel(sessionType);
        }
        catch(JSchException e){
            messageToUser.errorAlert("SSHFactory", "chanRespChannel", e.getMessage());
            FileSystemWorker.error("SSHFactory.chanRespChannel", e);
        }

        (( ChannelExec ) respChannel).setCommand(commandSSH);
        Objects.requireNonNull(respChannel);
    }

    private String getConnectToSrv() {
        return connectToSrv;
    }

    public void setConnectToSrv(String connectToSrv) {
        this.connectToSrv = connectToSrv;
    }

    private Properties sshException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        initProperties = new FileProps(SOURCE_CLASS);
        return initProperties.getProps();
    }

    private String pem() {
        File pemFile = new File("a161.pem");
        return pemFile.getAbsolutePath();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SSHFactory{");
        sb.append("commandSSH='").append(commandSSH).append('\'');
        sb.append(", connectToSrv='").append(connectToSrv).append('\'');
        sb.append(", sessionType='").append(sessionType).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /*END FOR CLASS*/
    /**
     Builder.
     <p>
     Сам строитель.

     @since <a href="https://github.com/Vachok/ftpplus/commit/7bc45ca4f1968a61dfda3b009d7b0e394d573de5" target=_blank>14.11.2018 (15:25)</a>
     */
    @SuppressWarnings ("WeakerAccess")
    public static class Builder {

        private String userName = "ITDept";

        private String pass;

        private String sessionType = "exec";

        private String connectToSrv;

        private String commandSSH;

        public Builder(String connectToSrv, String commandSSH) {
            this.commandSSH = commandSSH;
            this.connectToSrv = connectToSrv;
        }

        protected Builder() {

        }

        /**
         Build ssh factory.

         @return the ssh factory
         */
        public synchronized SSHFactory build() {
            return new SSHFactory(this);
        }

        public Map<String, Boolean> call() {
            Map<String, Boolean> myHashMap = new ConcurrentHashMap<>();
            String b = new SSHFactory(this).call();
            myHashMap.put(getCommandSSH(), b.equalsIgnoreCase("true"));
            return myHashMap;
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

        @Override
        public int hashCode() {
            int result = getUserName().hashCode();
            result = 31 * result + (getPass()!=null? getPass().hashCode(): 0);
            result = 31 * result + getSessionType().hashCode();
            result = 31 * result + (getConnectToSrv()!=null? getConnectToSrv().hashCode(): 0);
            result = 31 * result + (getCommandSSH()!=null? getCommandSSH().hashCode(): 0);
            return result;
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
         Sets pass.

         @param pass the pass
         @return the pass
         */
        public Builder setPass(String pass) {
            this.pass = pass;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if(this==o){
                return true;
            }
            if(!(o instanceof Builder)){
                return false;
            }

            Builder builder = ( Builder ) o;

            if(!getUserName().equals(builder.getUserName())){
                return false;
            }
            if(getPass()!=null? !getPass().equals(builder.getPass()): builder.getPass()!=null){
                return false;
            }
            if(!getSessionType().equals(builder.getSessionType())){
                return false;
            }
            if(getConnectToSrv()!=null? !getConnectToSrv().equals(builder.getConnectToSrv()): builder.getConnectToSrv()!=null){
                return false;
            }
            return getCommandSSH()!=null? getCommandSSH().equals(builder.getCommandSSH()): builder.getCommandSSH()==null;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Builder{");
            sb.append("userName='").append(userName).append('\'');
            sb.append(", pass='").append(pass).append('\'');
            sb.append(", sessionType='").append(sessionType).append('\'');
            sb.append(", connectToSrv='").append(connectToSrv).append('\'');
            sb.append(", commandSSH='").append(commandSSH).append('\'');
            sb.append('}');
            return sb.toString();
        }

    }
}