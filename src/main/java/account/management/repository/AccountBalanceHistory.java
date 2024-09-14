package account.management.repository;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Date;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity(name="AccountBalanceHistory")
public class AccountBalanceHistory {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "currency")
    private String currencyCode;

    @Column(name = "book_date")
    private Date bookDate;

    @Column(name = "value_date")
    private Date valueDate;


    @Column(name = "create_date")
    @CreatedDate
    private LocalDateTime createDate;

    @Column(name = "balance_buckets")
    private RealBalanceBuckets balanceBuckets;

    @Column
    private Long transactionID;

}