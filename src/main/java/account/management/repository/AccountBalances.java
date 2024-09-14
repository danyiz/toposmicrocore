package account.management.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.sql.Date;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity(name="AccountBalances")
public class AccountBalances {

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

    @Column(name = "sequence")
    private Integer sequence;

    @CreatedDate
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "balance_buckets")
    @JdbcTypeCode(SqlTypes.JSON)
    private RealBalanceBuckets balanceBuckets;

    @Column
    private Long lastTransactionID;

}

