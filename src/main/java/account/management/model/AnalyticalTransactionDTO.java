package account.management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@NoArgsConstructor//(onConstructor_={@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)})
@AllArgsConstructor//(onConstructor_={@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)})
public class AnalyticalTransactionDTO {
    public Long getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Long transactionID) {
        this.transactionID = transactionID;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Date getBookDate() {
        return bookDate;
    }

    public void setBookDate(Date bookDate) {
        this.bookDate = bookDate;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public void setValueDate(Date valueDate) {
        this.valueDate = valueDate;
    }

    public LocalDateTime getInputDate() {
        return inputDate;
    }

    public void setInputDate(LocalDateTime inputDate) {
        this.inputDate = inputDate;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public String getTransactionNote() {
        return transactionNote;
    }

    public void setTransactionNote(String transactionNote) {
        this.transactionNote = transactionNote;
    }

    public String getCreditDebitFlag() {
        return creditDebitFlag;
    }

    public void setCreditDebitFlag(String creditDebitFlag) {
        this.creditDebitFlag = creditDebitFlag;
    }

    public String getTellerCode() {
        return tellerCode;
    }

    public void setTellerCode(String tellerCode) {
        this.tellerCode = tellerCode;
    }

    public Long getProcessID() {
        return processID;
    }

    public void setProcessID(Long processID) {
        this.processID = processID;
    }

    private Long transactionID;
    private String transactionCode;
    private BigDecimal transactionAmount;
    private String accountNumber;
    private Date bookDate;
    private Date valueDate;
    @JsonDeserialize(using = CustomLocalDateTimeDeSerializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime inputDate;
    private String transactionCurrency;
    private String transactionNote;
    private String creditDebitFlag;
    private String tellerCode;
    private Long processID;



}
