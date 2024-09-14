package account.management.repository;

import jakarta.persistence.*;
import lombok.*;
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
@Entity(name="AccountAttributes")
public class AccountAttributes  {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "account_number")
    String accountNumber;

    @CreatedDate
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "last_transaction_id")
    private Long lastTransactionId;

    @Column(name = "transaction_attributes")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> transactionAttributes;
    //pointers to template, instance, custom attributes which should include in postings metadata
    // {template_attributes:"ATTR1,ATTR2,ATTR2",
    // instance_attributes:"ATTR1, ATTR2",
    // custom_attributes:"ATTR1,ATTR2"} //                                                                                             ATTR"}

    @Column(name = "template_attributes")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> templateAttributes; // product level descriptor needed and maintenance in separate MS

    @Column(name = "instance_attributes")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> instanceAttributes; // product level descriptor needed and maintenance in separate MS

    @Column(name = "custom_attributes")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> customAttributes; // product level descriptor needed and maintenance in separate MS
}
