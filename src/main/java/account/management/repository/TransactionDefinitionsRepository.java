package account.management.repository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionDefinitionsRepository extends JpaRepository<TransactionDefinitions,Long> {
    TransactionDefinitions findBySchemaCodeAndTransactionCode(String schemaCode,String transactionCode);
}
