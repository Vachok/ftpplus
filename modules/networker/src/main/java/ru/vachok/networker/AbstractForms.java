package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;

import java.util.*;


public abstract class AbstractForms {
    
    
    private static final TForms T_FORMS = new TForms();
    
    public static @NotNull String exceptionNetworker(StackTraceElement[] trace) {
        return T_FORMS.exceptionNetworker(trace);
    }
    
    public static String fromArray(Properties props) {
        return T_FORMS.fromArray(props);
    }
    
    public static String fromArray(Exception e) {
        return T_FORMS.fromArray(e);
    }
    
    public static String fromArray(Map<?, ?> fromMap) {
        return T_FORMS.fromArray(fromMap);
    }
    
    public static String fromArray(StackTraceElement[] trace) {
        return T_FORMS.fromArray(trace);
    }
    
    public static String fromArray(Deque<?> objDequeue) {
        return T_FORMS.fromArray(objDequeue);
    }
}
