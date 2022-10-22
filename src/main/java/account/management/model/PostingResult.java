package account.management.model;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostingResult {
    private Long processID;
    private Long transactionID;
    private Long batchID;
    String postingStatus;

}
