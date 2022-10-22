package account.management.service;

import account.management.entity.*;
import account.management.model.AnalyticalTransactionDTO;
import account.management.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    SchemaBalanceDefinitionsRepository schemaBalanceDefinitionsRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccountAttributesRepository accountAttributesRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AnalyticalTransaction createAnalyticalTransaction(AnalyticalTransactionDTO analyticalTransactionDTO) {

        //lock the account
        try {
            AccountAttributes accountAttributes = accountAttributesRepository.lockTheAttributes(analyticalTransactionDTO.getAccountNumber());
            try {
                AnalyticalTransaction analyticalTransaction = modelMapper.map(analyticalTransactionDTO, AnalyticalTransaction.class);
                AnalyticalTransaction savedTransaction = analyticalTransactionRepository.save(analyticalTransaction);
                accountAttributes.setLastTransactionId(analyticalTransaction.getTransactionID());
                AccountBalances newBalances = balanceUpdate(analyticalTransaction,accountAttributes);
                log.info("Transaction succeed: {}", analyticalTransaction.getId().toString());
                accountAttributesRepository.save(accountAttributes);
                //raise an event with new balances + "put it to outbox"
                return savedTransaction;
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

    public AccountBalances balanceUpdate(AnalyticalTransaction analyticalTransaction,AccountAttributes accountAttributes) {
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
             oldBalances = createFirstForTheBookingDate(analyticalTransaction, transactionBalancesParams,accountAttributes);
        }

        return createNextBalances(analyticalTransaction,transactionBalancesParams,oldBalances);
    }
    public TransactionBalances readParamsForTheUpdate(AnalyticalTransaction analyticalTransaction,AccountAttributes accountAttributes){

        TransactionDefinitions transactionDefinitions = transactionDefinitionsRepository.findBySchemaCodeAndTransactionCode(accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"),
                analyticalTransaction.getTransactionCode());

        return transactionBalancesRepository.findBySchemaCodeAndTransactionGroup(
                                            accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"),
                                            transactionDefinitions.getTransactionGroup());
    }

    public AccountBalances createFirstForTheBookingDate(AnalyticalTransaction analyticalTransaction, TransactionBalances transactionBalances,AccountAttributes accountAttributes){

        // read for the previous max, copy it for book_date, else create the empty record
        //read the last balance record if not exist we need to create the first
        String currencyCode = analyticalTransaction.getTransactionCurrency();
        Optional<AccountBalances> lastBalance = Optional.ofNullable(accountBalancesRepository.findLastRecordByAccountNumberCurrency(analyticalTransaction.getAccountNumber(),currencyCode));

        AccountBalances accountBalances = null;
        List<RealBalanceBuckets> realBalanceBuckets = null;
        if (lastBalance.isPresent()) {
          realBalanceBuckets = lastBalance.get().getRealBalanceBuckets();
            // TODO
            // check the particular balance item, if not exists add it to the bucket by definition

          log.debug("Account Balance1 record found: {}, Last trn ID: {}", lastBalance.get().getId(), lastBalance.get().getLastTransactionID());
        }else{

            SchemaBalanceDefinitions schemaBalanceDefinitions = schemaBalanceDefinitionsRepository.findBySchemaCode(accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"));;
            BalanceBucketDefinitions balanceBucketDefinitionsList  = schemaBalanceDefinitions.getBalanceBucketDefinitions();

            log.info(balanceBucketDefinitionsList.toString());

            // TODO
            // Fill up real balance bucket with zero for that particular bucket item
            // (only those one created which really needed)
          List<String> balances = transactionBalances.getBalanceComponents().entrySet().stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            Map<String, BigDecimal> balanceBuckets = balances.stream().distinct()
                    .collect(Collectors.toMap(s -> s.toString(), s -> BigDecimal.ZERO));
            log.debug("Account Balance record  not found! Create the first record ");
        }
        return createNewBalances(analyticalTransaction, realBalanceBuckets,transactionBalances);
    }

    AccountBalances createNewBalances(AnalyticalTransaction analyticalTransaction, List<RealBalanceBuckets> realBalanceBuckets, TransactionBalances transactionBalances){
        AccountBalances newBalances = new AccountBalances();
        newBalances.setRealBalanceBuckets(realBalanceBuckets);
        newBalances.setAccountNumber(analyticalTransaction.getAccountNumber());
        newBalances.setLastTransactionID(analyticalTransaction.getTransactionID());
        newBalances.setSequence(1);
        newBalances.setCurrencyCode(analyticalTransaction.getTransactionCurrency());
        newBalances.setBookDate(analyticalTransaction.getBookDate());
        newBalances.setValueDate(analyticalTransaction.getValueDate());
        return newBalances;
    }



    public AccountBalances createNextBalances( AnalyticalTransaction analyticalTransaction, TransactionBalances transactionBalances,AccountBalances actualBalances){

        List<RealBalanceBuckets> actualBalanceComponents = actualBalances.getRealBalanceBuckets();
        Map<String,Integer> balanceComponentsToUpdate = transactionBalances.getBalanceComponents();

        for (Map.Entry<String,Integer> balanceComponent : balanceComponentsToUpdate.entrySet()) {
            log.debug(transactionBalances.getSchemaCode() + ": Key = " + balanceComponent.getKey() + ", Value = " + balanceComponent.getValue());

//            if(!actualBalanceComponents.containsKey(balanceComponent.getKey())){
//
//                BalanceBucket bucket = new BalanceBucket();
//                bucket.setBalancBucketName(balanceComponent.getKey().toString());
//
//                Map<String,BigDecimal> bucketMap = new HashMap<>();
//
//                for(Map.Entry<String,Integer> balanceDef : balanceComponent.getValue().getBalanceIdentifiers().entrySet()){
//                    bucketMap.put(balanceDef.getKey(),BigDecimal.ZERO);
//                }
//                bucket.setBalance(bucketMap);
//
//                actualBalanceComponents.put(balanceComponent.getKey(),bucket);
//            }
//            //1. IF Value date <> book date =  special update rule update book bal and trigger back valuation calculation
//
//            //2.
//            // implement exception rule on balance component
//            // e.g DUE_INTEREST IS NEGATIVE, NOT ALLOWED TO GO TO POSITIVE SO
//            // THE EXEPTION BALANCE UPDATE IS NEEDED HERE.
//            for(Map.Entry<String,BalanceBucket> balanceActualItem : actualBalanceComponents.entrySet()){
//                BalanceBucket newBucket = new BalanceBucket();
//                for(Map.Entry<String,BigDecimal> balanceBucketItem :  balanceActualItem.getValue().getBalance().entrySet()){
//                    BigDecimal actualBalance = updateOneBalanceComponent(analyticalTransaction.getCreditDebitFlag(),
//                            analyticalTransaction.getTransactionAmount(),
//                            balanceBucketItem.getValue(),
//                            BigDecimal.valueOf(balanceComponent.getValue().getBalanceIdentifiers().get(balanceBucketItem.getKey().toString())));
//
//                }
//
//
//                actualBalanceComponents.replace(balanceComponent.getKey(), newBucket);
//            }
//
//
//            log.debug("Account number:{},Balance Type: {} Actual balance value:{} , new balance {}",analyticalTransaction.getAccountNumber(),balanceComponent.getKey(),actualBalanceComponents.get(balanceComponent.getKey()),actualBalance);

        }

        actualBalances.setValueDate(analyticalTransaction.getValueDate());
        actualBalances.setRealBalanceBuckets(actualBalanceComponents);
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
