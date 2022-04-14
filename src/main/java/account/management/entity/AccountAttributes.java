package account.management.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
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

    @Type(type = "json")
    @Column(name = "transaction_attributes",columnDefinition = "jsonb")
    private Map<String, String> transactionAttributes;

    @Type(type = "json")
    @Column(name = "template_attributes",columnDefinition = "jsonb")
    private Map<String, String> templateAttributes;

    @Type(type = "json")
    @Column(name = "custom_attributes",columnDefinition = "jsonb")
    private Map<String, String> customAttributes;
}
