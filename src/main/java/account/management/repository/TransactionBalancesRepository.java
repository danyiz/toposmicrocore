package account.management.repository;

import account.management.entity.TransactionBalances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionBalancesRepository extends JpaRepository<TransactionBalances, Long> {
    TransactionBalances findBySchemaCodeAndTransactionGroup(String schemaCode,String transactionCode);
}
