package account.management.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@JsonSerialize
@JsonDeserialize
@NoArgsConstructor
public class RealBucket {
        @JsonProperty("BucketName")
        private String bucketName;
        @JsonProperty("BucketItems")
        private Map<String, BigDecimal> bucketItems;

        public RealBucket(String bucketName, Map<String, BigDecimal> bucketItems) {
                this.bucketName = bucketName;
                this.bucketItems = bucketItems;
        }
}
