package account.management.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString

@Entity(name="AnalyticalTransactions")
public class AnalyticalTransactions {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "transaction_id")
    private Long transactionID;

    @Column(name = "transaction_amount")
    private BigDecimal transactionAmount;

    @Column(name = "credit_debit")
    private String creditDebitFlag;

    @Column(name = "book_date")
    private Date bookDate;

    @Column(name = "value_date")
    private Date valueDate;

    @Column(name = "input_date")
    LocalDateTime inputDate;

    @Column(name = "transaction_currency")
    String transactionCurrency;

    @Column(name = "teller_code")
    String tellerCode;

    @Column(name = "postingsMetaData")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String,String> postingMetaData;

    @Column(name = "process_id")
    Long processID;

    @Column(name = "batch_id")
    Long batchID;

    @Column(name = "batchMetaData")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String,String> batchMetaData;

    @CreatedDate
    @Column(name = "create_date")
    private LocalDateTime createDate;

}
