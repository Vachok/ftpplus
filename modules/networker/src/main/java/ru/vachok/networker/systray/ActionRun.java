package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;


/**
 Class ru.vachok.networker.systray.ActionRun
 <p>

 @since 04.03.2019 (2:24) */
public class ActionRun extends AbstractAction {


    private String commandToRun;
    
    private MessageToUser messageToUser = new MessageCons(ActionRun.class.getSimpleName());

    /**
     Creates an {@code Action}.
     @param commandToRun команда, для {@link Runtime#exec(java.lang.String)}
     */
    public ActionRun(String commandToRun) {
        this.commandToRun = commandToRun;
    }

    /**
     Invoked when an action occurs.  @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            Runtime.getRuntime().exec(commandToRun);
        }
        catch(IOException e1){
            messageToUser.error(e1.getMessage());
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionRun{");
        sb.append("commandToRun='").append(commandToRun).append('\'');
        sb.append('}');
        return sb.toString();
    }
}