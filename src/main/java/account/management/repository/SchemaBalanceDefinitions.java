package account.management.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;


import  jakarta.persistence.*;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString

@Table(name="SchemaBalanceDefinitions",
        indexes = {@Index(   name = "schema_balance_definitions_index_by_schema_code",
                columnList = "schema_code",
                unique = true)})
public class SchemaBalanceDefinitions {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name="schema_code", length = 50)
    private String schemaCode;

     @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "balance_definitions",columnDefinition = "JSON")
    private BalanceBucketDefinitions balanceBucketDefinitions;

}
