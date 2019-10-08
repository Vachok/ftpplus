package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;


public abstract class AbstractForms {
    
    
    private static final TForms T_FORMS = new TForms();
    
    public static @NotNull String exceptionNetworker(StackTraceElement[] trace) {
        return T_FORMS.exceptionNetworker(trace);
    }
}
