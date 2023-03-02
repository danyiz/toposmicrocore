package account.management.repository;

import lombok.*;
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
@Table(name="AccountAttributes",
        indexes = @Index(name = "account_attributes_index_by_account_number",
                columnList = "account_number",unique = true))
public class AccountAttributes  {

    @Id
    @Column(name="account_number", length = 16)
    String accountNumber;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private java.util.Date createDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_date")
    private java.util.Date modifyDate;

    @Column(name = "last_transaction_id")
    private Long lastTransactionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transaction_attributes",columnDefinition = "json")
    private Map<String, String> transactionAttributes;
    //pointers to template, instance, custom attributes which should include in postings metadata
    // {template_attributes:"ATTR1,ATTR2,ATTR2",
    // instance_attributes:"ATTR1, ATTR2",
    // custom_attributes:"ATTR1,ATTR2"} //                                                                                             ATTR"}

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_attributes",columnDefinition = "json")
    private Map<String, String> templateAttributes; // product level descriptor needed and maintenance in separate MS

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "instance_attributes",columnDefinition = "json")
    private Map<String, String> instanceAttributes; // product level descriptor needed and maintenance in separate MS

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes",columnDefinition = "json")
    private Map<String, String> customAttributes; // product level descriptor needed and maintenance in separate MS
}
