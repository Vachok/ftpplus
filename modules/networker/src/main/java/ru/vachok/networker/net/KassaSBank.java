package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;


public enum KassaSBank implements Iterable {
    ;

    public static final String T101 = "10.200.200.101";

    public static final String T102 = "10.200.200.102";

    public static final String T103 = "10.200.200.103";

    public static final String T104 = "10.200.200.104";

    public static final String T105 = "10.200.200.105";

    public static final String T106 = "10.200.200.106";

    public static final String T204 = "10.200.200.204";

    public static final String T203 = "10.200.200.203";

    public static final String T111 = "10.200.200.111";

    public static final String T113 = "10.200.200.113";

    public static final String T114 = "10.200.200.114";

    public static final String T116 = "10.200.200.116";

    public static final String T054 = "10.200.200.54";

    public static final String T151 = "10.200.200.151";

    public static final String T152 = "10.200.200.152";

    public static final String T153 = "10.200.200.153";

    public static final String T154 = "10.200.200.154";

    public static final String T155 = "10.200.200.155";

    public static final String T156 = "10.200.200.156";

    public static final String T157 = "10.200.200.157";

    public static final String T159 = "10.200.200.159";

    private static final Logger LOGGER = AppComponents.getLogger();

    public static void main(String[] args) {

    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer action) {
        List<InetAddress> inetAddresses = new ArrayList<>();
        Consumer<String> runnable = (x) -> {
            try {
                InetAddress inetAddress = InetAddress.getByName(x);
                byte[] bytes = inetAddress.getAddress();
                inetAddresses.add(InetAddress.getByAddress(bytes));
            } catch (IOException e) {
                AppComponents.getLogger().warn(e.getMessage());
            }
        };
    }
}
