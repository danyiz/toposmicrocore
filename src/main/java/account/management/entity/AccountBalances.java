package account.management.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;


@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
@Table(name="AccountBalances",
        indexes = @Index(name = "account_balances_index_by_account_number_and_book_date_and_sequence",
                columnList = "account_number asc,book_date asc,sequence asc"))
public class AccountBalances {

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

    @Column(name="sequence")
    private Integer sequence;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private java.util.Date createDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_date")
    private java.util.Date modifyDate;

    @Type(type = "json")
    @Column(name = "balance_components",columnDefinition = "jsonb")
    private Map<String,BigDecimal> balanceComponents;

    @Column
    private Long lastTransactionID;

}

