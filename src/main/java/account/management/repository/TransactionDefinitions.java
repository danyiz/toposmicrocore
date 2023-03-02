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

@Table(name="TransactionDefinitions",
        indexes = {@Index(   name = "transaction_definitions_index_by_transaction_group_transaction_code",
                columnList = "schema_code,transaction_code",
                unique = true)})
public class TransactionDefinitions {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name="schema_code", length = 50)
    private String schemaCode;

    @Column(name="transaction_code", length = 50)
    private String transactionCode;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_analitycal_attributes",columnDefinition = "json")
    private Map<String, String> customAnalyticalAttributes;

}
