package ru.vachok.networker.logic.moneypack;



import java.util.List;


/**
 * <b>Сервисный класс</b>
 *
 * @since 20.08.2018 (11:29)
 */
public class BankService {

    public BankService() {
        super();
    }


    public List<Banking> findAll() {
        return BankRepository.getInstance().findAll();
    }


    public Banking findID( Integer idBanking ) {
        return BankRepository.getInstance().findID(idBanking);
    }
}
