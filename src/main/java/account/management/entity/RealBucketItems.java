package account.management.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonSerialize
@JsonDeserialize
public class RealBucketItems {
        @JsonProperty("BucketName")
        private String bucketName;
        @JsonProperty("BucketItems")
        private List<Map<String, BigDecimal>> bucketItems;
}
