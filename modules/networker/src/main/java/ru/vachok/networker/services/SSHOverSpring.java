package ru.vachok.networker.services;


import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.springframework.shell.Shell;
import org.springframework.shell.result.DefaultResultHandler;

import java.util.List;

/**
 * @since 19.09.2018 (15:16)
 */
public class SSHOverSpring {

    public ProcessShellFactory sshTestConnector() {
        ProcessShellFactory factory = new ProcessShellFactory();
        Command command = factory.create();
        command.setInputStream(System.in);
        command.setOutputStream(System.out);
        command.setErrorStream(System.err);
        List<String> commandsList = factory.getCommand();
        commandsList.add("ls");
        factory.setCommand(commandsList);
        return factory;
    }

    public void execUte() {
        DefaultResultHandler resultHandler = new DefaultResultHandler();
        Shell shell = new Shell(resultHandler);
    }
}

