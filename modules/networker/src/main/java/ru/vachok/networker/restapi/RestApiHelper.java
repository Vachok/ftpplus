package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.inet.AllowDomainHelper;
import ru.vachok.networker.ad.inet.TempInetRestControllerHelper;


@FunctionalInterface
public interface RestApiHelper {


    String DOMAIN = "allowdomainhelper";

    @NotNull
    @Contract(value = " -> new")
    static RestApiHelper getInstance(@NotNull String type) {
        switch (type) {
            case "TempInetRestControllerHelper":
                return new TempInetRestControllerHelper();
            case DOMAIN:
                return new AllowDomainHelper();
        }
        return new TempInetRestControllerHelper();
    }

    String getResult(@NotNull JsonObject jsonObject);

}
