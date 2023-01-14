package account.management.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;

import  jakarta.persistence.*;
import  jakarta.persistence.Entity;
import  jakarta.persistence.Index;
import  jakarta.persistence.Table;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString

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

    //@CreationTimestamp
    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private java.util.Date createDate;

    //@UpdateTimestamp
    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_date")
    private java.util.Date modifyDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "balance_components",columnDefinition = "json")
    private Map<String, Integer> balanceComponents;

}
