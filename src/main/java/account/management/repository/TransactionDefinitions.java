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
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString

@Entity(name="TransactionDefinitions")
public class TransactionDefinitions {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "schema_code")
    private String schemaCode;

    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "transaction_group")
    private String transactionGroup;

    @CreatedDate
    @Column(name = "create_date")
    private java.util.Date createDate;

    @LastModifiedDate
    @Column(name = "modify_date")
    private java.util.Date modifyDate;

    @Column(name = "custom_analitycal_attributes")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> customAnalyticalAttributes;

}
