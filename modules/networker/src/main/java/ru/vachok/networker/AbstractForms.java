package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;

import java.util.Properties;


public abstract class AbstractForms {
    
    
    private static final TForms T_FORMS = new TForms();
    
    public static @NotNull String exceptionNetworker(StackTraceElement[] trace) {
        return T_FORMS.exceptionNetworker(trace);
    }
    
    public static String fromArray(Properties props) {
        return T_FORMS.fromArray(props);
    }
}
