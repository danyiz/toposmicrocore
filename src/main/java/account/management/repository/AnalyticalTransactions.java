package account.management.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import  jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString

@Table(name="AnalyticalTransactions",
        indexes = @Index(name = "analytical_transactions_index_by_account_number",
                columnList = "account_number,book_date,transaction_id,transaction_code", unique = true))
public class AnalyticalTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name="account_number", length = 16)
    private String accountNumber;

    @Column(name="transaction_code", length = 50)
    private String transactionCode;

    @Column(name="transaction_id", length = 16)
    private Long transactionID;

    @Column(name="transaction_amount")
    private BigDecimal transactionAmount;

    @Column(name="credit_debit", length = 1)
    private String creditDebitFlag;

    @Column(name="book_date")
    private Date bookDate;

    @Column(name="value_date")
    private Date valueDate;

    @Column(name="input_date")
    LocalDateTime inputDate;

    @Column(name="transaction_currency",length = 3)
    String transactionCurrency;

    @Column(name="teller_code", length = 50)
    String tellerCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "postingsMetaData",columnDefinition = "json")
    private Map<String,String> postingMetaData;

    @Column(name="process_id")
    Long processID;

    @Column(name="batch_id")
    Long batchID;

     @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "batchMetaData",columnDefinition = "json")
    private Map<String,String> batchMetaData;

    //@CreationTimestamp
    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private java.util.Date createDate;

}
