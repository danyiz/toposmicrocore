package account.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionBalancesRepository extends JpaRepository<TransactionBalances, Long> {
    TransactionBalances findBySchemaCodeAndTransactionGroup(String schemaCode,String transactionCode);
}
