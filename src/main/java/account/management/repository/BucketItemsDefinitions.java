package account.management.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Getter
@Setter
@JsonSerialize
@JsonDeserialize

public class BucketItemsDefinitions {
    @JsonProperty("BucketName")
    private String bucketName;
    @JsonProperty("BucketItems")
    private List<Map<String, Integer>> bucketItems;
    @JsonProperty("Restriction")
    private ArrayList<String> restriction;
}
