package account.management.entity;


import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonType.class)
})
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

    @Type(type = "jsonb")
    @Column(name = "balance_definitions",columnDefinition = "jsonb")
    private BalanceBucketDefinitions balanceBucketDefinitions;

}
