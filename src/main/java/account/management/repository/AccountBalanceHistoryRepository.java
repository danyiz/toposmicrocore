package account.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountBalanceHistoryRepository extends JpaRepository<AccountBalanceHistory,Long> {

    @Query("from AccountBalanceHistory where accountNumber=:accountNumber and currencyCode=:currencyCode and id = " +
            "(select max(id) from AccountBalanceHistory where accountNumber=:accountNumber and currencyCode=:currencyCode)")
    AccountBalanceHistory findLastRecordByAccountNumberCurrency(String accountNumber, String currencyCode);
}
