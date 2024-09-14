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
import java.time.LocalDateTime;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@ToString

@Entity(name="TransactionBalances")
public class TransactionBalances {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "schema_code")
    private String schemaCode;

    @Column(name = "transaction_group")
    private String transactionGroup;

    @CreatedDate
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "modify_date")
    private java.util.Date modifyDate;

    @Column(name = "balance_components")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Integer> balanceComponents;

}
