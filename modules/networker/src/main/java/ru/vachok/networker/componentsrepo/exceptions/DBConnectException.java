package ru.vachok.networker.componentsrepo.exceptions;


import com.eclipsesource.json.JsonObject;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.DBConnectException
 <p>

 @since 16.06.2020 (14:35) */
public class DBConnectException extends RuntimeException {


    private final String message;

    public DBConnectException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, "DBConnectException");
        jsonObject.add("time", System.currentTimeMillis());
        jsonObject.add(ConstantsFor.STR_ERROR, message);
        jsonObject.add("trace", AbstractForms.networkerTrace(getStackTrace()));
        return jsonObject.toString();
    }
}