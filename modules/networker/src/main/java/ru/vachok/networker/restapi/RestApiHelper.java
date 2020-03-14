package ru.vachok.networker.restapi;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.inet.AllowDomainHelper;
import ru.vachok.networker.ad.inet.TempInetRestControllerHelper;
import ru.vachok.networker.net.ssh.SSHCommander;


@FunctionalInterface
public interface RestApiHelper {


    String DOMAIN = "allowdomainhelper";

    String SSH = "sshcommandexec";

    @NotNull
    @Contract(value = " -> new")
    static RestApiHelper getInstance(@NotNull String type) {
        switch (type) {
            case "TempInetRestControllerHelper":
                return new TempInetRestControllerHelper();
            case DOMAIN:
                return new AllowDomainHelper();
            case SSH:
                return new SSHCommander();
        }
        return new TempInetRestControllerHelper();
    }

    String getResult(@NotNull JsonObject jsonObject);

}
