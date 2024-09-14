package account.management.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@ToString

@Entity(name="SchemaBalanceDefinitions")
public class SchemaBalanceDefinitions {

    @Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "schema_code")
    private String schemaCode;

    @Column(name = "balance_definitions")
    @JdbcTypeCode(SqlTypes.JSON)
    private BalanceBucketDefinitions balanceBucketDefinitions;

}
