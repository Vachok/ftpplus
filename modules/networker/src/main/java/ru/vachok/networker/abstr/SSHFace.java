package ru.vachok.networker.abstr;


import com.jcraft.jsch.JSchException;

import java.io.IOException;


/**
 @since 10.04.2019 (13:15) */
@FunctionalInterface public interface SSHFace {
    
    
    String doCommand() throws JSchException, IOException;
    
}
