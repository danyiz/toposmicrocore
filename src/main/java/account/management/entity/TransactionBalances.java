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
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
@Table(name="TransactionBalances",
        indexes = {@Index(   name = "transaction_balances_index_by_schema_transaction_group",
                            columnList = "schema_code,transaction_group",
                            unique = true)})
public class TransactionBalances {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name="schema_code", length = 50)
    private String schemaCode;

    @Column(name="transaction_group", length = 50)
    private String transactionGroup;

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
    private Map<String, Integer> balanceComponents;

}
