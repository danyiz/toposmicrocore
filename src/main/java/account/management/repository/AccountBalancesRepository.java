package account.management.repository;

import account.management.entity.AccountBalances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;

public interface AccountBalancesRepository extends JpaRepository<AccountBalances,Long> {

    @Query("from AccountBalances where accountNumber=:accountNumber and currencyCode=:currencyCode and id = " +
            "(select max(id) from AccountBalances where accountNumber=:accountNumber and currencyCode=:currencyCode)")
    AccountBalances findLastRecordByAccountNumberCurrency(String accountNumber, String currencyCode);

    AccountBalances findByAccountNumberAndBookDateAndCurrencyCode(String accountNumber, Date bookDate,String currencyCode);
}
