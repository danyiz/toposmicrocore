package account.management.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonSerialize
@JsonDeserialize
public class BalanceBucketDefinitions {
    @JsonProperty("BalanceBuckets")
    List<BucketItemsDefinitions> balanceBuckets;
}

