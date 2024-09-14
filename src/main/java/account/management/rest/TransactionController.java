package account.management.rest;

import account.management.model.AnalyticalTransactionDTO;
import account.management.service.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytical-transactions")
public class TransactionController {

    private final Account transactionService;
    public TransactionController(Account service) {
        this.transactionService = service;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnalyticalTransactionDTO createTransaction(@RequestBody AnalyticalTransactionDTO transactionDTO) {
        return transactionService.update(transactionDTO);
    }


}
