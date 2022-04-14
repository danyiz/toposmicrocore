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
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
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

    @Type(type = "json")
    @Column(name = "custom_analitycal_attributes",columnDefinition = "jsonb")
    private Map<String, String> customAnalyticalAttributes;

}
