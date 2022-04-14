package account.management.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticalTransactionDTO {

    private Long transactionID;
    private String transactionCode;
    private BigDecimal transactionAmount;
    private String accountNumber;
    private Date bookDate;
    private Date valueDate;
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime inputDate;
    private String transactionCurrency;
    private String transactionNote;
    private String creditDebitFlag;
    private String tellerCode;
    private Long processID;

}
