package account.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;

public interface AccountBalancesRepository extends JpaRepository<AccountBalances,Long> {

    @Query("from AccountBalances where accountNumber=:accountNumber and currencyCode=:currencyCode and id = " +
            "(select max(id) from AccountBalances where accountNumber=:accountNumber and currencyCode=:currencyCode)")
    AccountBalances findLastRecordByAccountNumberCurrency(@Param("accountNumber") String accountNumber,@Param("currencyCode") String currencyCode);

    AccountBalances findByAccountNumberAndBookDateAndCurrencyCode(String accountNumber, Date bookDate,String currencyCode);
}
