package account.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticalTransactionRepository extends JpaRepository<AnalyticalTransactions, Long> {
}
