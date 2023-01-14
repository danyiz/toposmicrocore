package account.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import  jakarta.persistence.LockModeType;

public interface AccountAttributesRepository extends JpaRepository<AccountAttributes, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AccountAttributes c where c.accountNumber = :accountNumber")
    AccountAttributes lockTheAttributes(String accountNumber);
}
