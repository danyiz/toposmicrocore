package account.management.repository;

import account.management.entity.AnalyticalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticalTransactionRepository extends JpaRepository<AnalyticalTransaction, Long> {
}
