package account.management.repository;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name="AccountBalanceHistory",
        indexes = @Index(name = "acc_bal_hist_index_by_account_number_book_date_sequence_ccy",
                columnList = "account_number asc,book_date asc, currency asc"))
public class AccountBalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name="account_number", length = 16)
    private String accountNumber;

    @Column(name="currency", length = 3)
    private String currencyCode;

    @Column(name="book_date")
    private Date bookDate;

    @Column(name="value_date")
    private Date valueDate;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private java.util.Date createDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "balance_buckets",columnDefinition = "json")
    private RealBalanceBuckets balanceBuckets;

    @Column
    private Long transactionID;

}