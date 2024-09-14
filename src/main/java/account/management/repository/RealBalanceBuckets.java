package account.management.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonSerialize
@JsonDeserialize
@NoArgsConstructor
public class RealBalanceBuckets implements Serializable {

    @JsonProperty("balanceBuckets")
    List<RealBucket> balanceBuckets;
}
