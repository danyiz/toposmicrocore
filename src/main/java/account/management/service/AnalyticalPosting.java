package account.management.service;

import account.management.entity.*;
import account.management.model.AnalyticalTransactionDTO;
import account.management.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AnalyticalPosting {

    @Autowired
    AnalyticalTransactionRepository analyticalTransactionRepository;

    @Autowired
    AccountBalancesRepository accountBalancesRepository;

    @Autowired
    TransactionBalancesRepository transactionBalancesRepository;

    @Autowired
    TransactionDefinitionsRepository transactionDefinitionsRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccountAttributesRepository accountAttributesRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AnalyticalTransaction createAnalyticalTransaction(AnalyticalTransactionDTO analyticalTransactionDTO) {

        //lock the account
        try {
            AccountAttributes accountAttributes = accountAttributesRepository.lockTheAttributes(analyticalTransactionDTO.getAccountNumber());
            try {
                AnalyticalTransaction analyticalTransaction = modelMapper.map(analyticalTransactionDTO, AnalyticalTransaction.class);
                analyticalTransactionRepository.save(analyticalTransaction);
                log.info("Transaction succeed: {}", analyticalTransaction.getId().toString());
                balanceUpdate(analyticalTransaction,accountAttributes);
                return analyticalTransaction;
            } catch (Exception e) {
                log.info("Transaction failed: {}", e.getMessage());
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return null;
            }
        } catch (Exception e){
            log.info("Account lock failed: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }

    }

    public boolean balanceUpdate(AnalyticalTransaction analyticalTransaction,AccountAttributes accountAttributes) {
        //read the last balance record if not exist we need to create the first
        String currencyCode = analyticalTransaction.getTransactionCurrency();
        Optional<AccountBalances> lastBalance = Optional.ofNullable(accountBalancesRepository.findByAccountNumberAndBookDateAndCurrencyCode(analyticalTransaction.getAccountNumber(),analyticalTransaction.getBookDate(),currencyCode));

        TransactionBalances transactionBalancesParams = readParamsForTheUpdate(analyticalTransaction,accountAttributes);

        AccountBalances oldBalances;
        if (lastBalance.isPresent()) {

            oldBalances = lastBalance.get();
            oldBalances.setValueDate(analyticalTransaction.getValueDate());
            log.debug("Account Balance1 record found: {}, Last trn ID: {}", oldBalances.getId(), oldBalances.getLastTransactionID());

        }
        else{
             oldBalances = createFirstForTheBookingDate(analyticalTransaction, transactionBalancesParams);
        }
        createNextBalances(analyticalTransaction,transactionBalancesParams,oldBalances);

        return true;
    }
    public TransactionBalances readParamsForTheUpdate(AnalyticalTransaction analyticalTransaction,AccountAttributes accountAttributes){

        TransactionDefinitions transactionDefinitions = transactionDefinitionsRepository.findBySchemaCodeAndTransactionCode(accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"),
                analyticalTransaction.getTransactionCode());

        TransactionBalances transactionBalancesParams = transactionBalancesRepository.findBySchemaCodeAndTransactionGroup(
                                            accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"),
                                            transactionDefinitions.getTransactionGroup());
        return transactionBalancesParams;
    }

    public AccountBalances createFirstForTheBookingDate(AnalyticalTransaction analyticalTransaction, TransactionBalances transactionBalances){

        // read for the previous max, copy it for book_date, else create the empty record
        //read the last balance record if not exist we need to create the first
        String currencyCode = analyticalTransaction.getTransactionCurrency();
        Optional<AccountBalances> lastBalance = Optional.ofNullable(accountBalancesRepository.findLastRecordByAccountNumberCurrency(analyticalTransaction.getAccountNumber(),currencyCode));

        AccountBalances accountBalances = null;
        if (lastBalance.isPresent()) {

                accountBalances = lastBalance.get();
                accountBalances.setSequence(1);
                log.debug("Account Balance1 record found: {}, Last trn ID: {}", accountBalances.getId(), accountBalances.getLastTransactionID());

        }else{

         List<String> balances = transactionBalances.getBalanceComponents().entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            AccountBalances newBalances = new AccountBalances();
            newBalances.setBalanceComponents(balances.stream().distinct()
                .collect(Collectors.toMap(s -> s.toString(), s -> BigDecimal.ZERO)));

            newBalances.setAccountNumber(analyticalTransaction.getAccountNumber());
            newBalances.setLastTransactionID(analyticalTransaction.getTransactionID());
            newBalances.setSequence(1);
            newBalances.setCurrencyCode(analyticalTransaction.getTransactionCurrency());
            newBalances.setBookDate(analyticalTransaction.getBookDate());
            newBalances.setValueDate(analyticalTransaction.getValueDate());

            accountBalances = newBalances ;
        }
        return accountBalances;
    }

    public AccountBalances createNextBalances( AnalyticalTransaction analyticalTransaction, TransactionBalances transactionBalances,AccountBalances actualBalances){

        Map<String,BigDecimal> actualBalanceComponents = actualBalances.getBalanceComponents();
        Map<String,Integer> balanceComponentsToUpdate = transactionBalances.getBalanceComponents();

        for (Map.Entry<String,Integer> entry : balanceComponentsToUpdate.entrySet()) {
            log.debug(transactionBalances.getSchemaCode() + ": Key = " + entry.getKey() + ", Value = " + entry.getValue());

            if(!actualBalanceComponents.containsKey(entry.getKey())){
                actualBalanceComponents.put(entry.getKey(),new BigDecimal("0"));
            }
            //1. IF Value date <> book date =  special update rule update book bal and trigger back valuation calculation

            //2.
            // implement exception rule on balance component
            // DUE_INTEREST IS NEGATIVE, NOT ALLOWED TO GO TO POSITIVE SO
            // THE EXEPTION BALANCE UPDATE IS NEEDED HERE.

            BigDecimal actualBalance = updateOneBalanceComponent(analyticalTransaction.getCreditDebitFlag(),analyticalTransaction.getTransactionAmount(), actualBalanceComponents.get(entry.getKey()),BigDecimal.valueOf(entry.getValue()));

            log.debug("Account number:{},Balance Type: {} Actual balance value:{} , new balance {}",analyticalTransaction.getAccountNumber(),entry.getKey(),actualBalanceComponents.get(entry.getKey()),actualBalance);
            actualBalanceComponents.replace(entry.getKey(), actualBalance);
        }

        actualBalances.setValueDate(analyticalTransaction.getValueDate());
        actualBalances.setBalanceComponents(actualBalanceComponents);
        log.debug("Next New record: {}, Last trn ID: {}", actualBalances.getId(), actualBalances.getLastTransactionID());
        accountBalancesRepository.save(actualBalances);
        return actualBalances;
    }

    public BigDecimal updateOneBalanceComponent(String creditDebitFlag,BigDecimal transactionAmount, BigDecimal actualBalance, BigDecimal multiplier) {

        if(creditDebitFlag.equals("C")){
            actualBalance =actualBalance.add(transactionAmount.multiply(multiplier));
        }else{
            actualBalance =actualBalance.subtract(transactionAmount.multiply(multiplier));
        }
        return actualBalance;
    }


}
