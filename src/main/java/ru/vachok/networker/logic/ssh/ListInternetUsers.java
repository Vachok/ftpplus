package ru.vachok.networker.logic.ssh;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 List internet users.
 <p>
 Отдаёт все файлы, которые может править программа, в один txt
 */
public class ListInternetUsers implements Callable<String> {

   /**
    The Open file.
    <p>
    <b>FOR FUTURE</b>
    */
   private static final String SOURCE_CLASS = ListInternetUsers.class.getSimpleName();

   private static final File SSH_OUT = ConstantsFor.SSH_OUT;

   private static final File SSH_ERR = ConstantsFor.SSH_ERR;

   private static MessageToUser messageToUser = new MessageCons();

   /**
    Сообщения
    */

   private ConcurrentLinkedQueue<File> fileList = new ConcurrentLinkedQueue<>();

/*?
    static {
        SSH_FACTORY = new SSHFactory.Builder(ConstantsFor.SRV_NAT, "cat /etc/pf/squid;cat /etc/pf/allowip;cat /etc/pf/allowdomain;cat /etc/pf/allowurl;cat /etc/pf/squidlimited;cat /etc/pf/tempfull;cat /etc/pf/vipnet").build();
    }
*/

   private String sverka;


   /**
    Конструктор.

    @param sverka строка с именем компа или IP
    */
   public ListInternetUsers(String sverka) {
      this.sverka = sverka;
      call();
   }

   @SuppressWarnings ("InjectedReferences")
   @Override
   public String call() {
      synchronized(SSH_OUT) {
         String commandSSH = "cat /etc/pf/squid;cat /etc/pf/allowip;cat /etc/pf/allowdomain;cat /etc/pf/allowurl;cat /etc/pf/squidlimited;cat /etc/pf/tempfull;cat /etc/pf/vipnet";
         SSHFactory build = new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandSSH).build();
         String s = build.call();
         return s;
      }
   }

   public ListInternetUsers() {
      call();
   }
}
