package account.management.service;

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
import java.util.*;
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
    public AnalyticalTransactions createAnalyticalTransaction(AnalyticalTransactionDTO analyticalTransactionDTO) {

        //lock the account
        try {
            AccountAttributes accountAttributes = accountAttributesRepository.lockTheAttributes(analyticalTransactionDTO.getAccountNumber());
            try {
                AnalyticalTransactions analyticalTransactions = modelMapper.map(analyticalTransactionDTO, AnalyticalTransactions.class);
                AnalyticalTransactions savedTransaction = analyticalTransactionRepository.saveAndFlush(analyticalTransactions);
                accountAttributes.setLastTransactionId(analyticalTransactions.getTransactionID());
                AccountBalances newBalances = balanceUpdate(analyticalTransactions,accountAttributes);
                log.info("Transaction succeed: {}", analyticalTransactions.getId().toString());
                accountAttributesRepository.saveAndFlush(accountAttributes);
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

    public AccountBalances balanceUpdate(AnalyticalTransactions analyticalTransactions, AccountAttributes accountAttributes) {
        //read the last balance record if not exist we need to create the first
        String currencyCode = analyticalTransactions.getTransactionCurrency();
        Optional<AccountBalances> lastBalance = Optional.ofNullable(accountBalancesRepository.findByAccountNumberAndBookDateAndCurrencyCode(analyticalTransactions.getAccountNumber(),
                analyticalTransactions.getBookDate(),currencyCode));

        log.info("Read transaction params for: {} transaction",analyticalTransactions.getTransactionCode());
        TransactionBalances transactionBalancesParams = readParamsForTheUpdate(analyticalTransactions,accountAttributes);
        log.info("Transaction params read success for: {} transaction",analyticalTransactions.getTransactionCode());

        AccountBalances oldBalances = null;
        if (lastBalance.isPresent()) {
            if (!Objects.isNull(lastBalance.get())) {
                oldBalances = lastBalance.get();
                oldBalances.setValueDate(analyticalTransactions.getValueDate());
                log.info("Account balance record found, update the buckets: {}, Last trn ID: {}", oldBalances.getId(), oldBalances.getLastTransactionID());
            } else {
                oldBalances = createFirstForTheBookingDate(analyticalTransactions, transactionBalancesParams, accountAttributes);
                log.info("Account balance record found, but empty create first buckets: {}, Last trn ID: {}", oldBalances.getId(), oldBalances.getLastTransactionID());
            }
        } else {
            log.info("Create first balances...");
            oldBalances = createFirstForTheBookingDate(analyticalTransactions, transactionBalancesParams, accountAttributes);


        }

        return createNextBalances(analyticalTransactions,transactionBalancesParams,oldBalances);
    }
    public TransactionBalances readParamsForTheUpdate(AnalyticalTransactions analyticalTransactions, AccountAttributes accountAttributes){

        TransactionDefinitions transactionDefinitions = transactionDefinitionsRepository.findBySchemaCodeAndTransactionCode(accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"),
                analyticalTransactions.getTransactionCode());
        log.info("Going to read transaction params with schema: {}, with transaction group: {}",
                accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"),
                transactionDefinitions.getTransactionGroup());
        return transactionBalancesRepository.findBySchemaCodeAndTransactionGroup(
                                            accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"),
                                            transactionDefinitions.getTransactionGroup());
    }

    public AccountBalances createFirstForTheBookingDate(AnalyticalTransactions analyticalTransactions, TransactionBalances transactionBalances, AccountAttributes accountAttributes){

        // read for the previous max, copy it for book_date, else create the empty record
        //read the last balance record if not exist we need to create the first
        String currencyCode = analyticalTransactions.getTransactionCurrency();
        Optional<AccountBalances> lastBalance = Optional.ofNullable(accountBalancesRepository.findLastRecordByAccountNumberCurrency(analyticalTransactions.getAccountNumber(),currencyCode));

        AccountBalances accountBalances = null;
        RealBalanceBuckets realBalanceBuckets = null;
        if (lastBalance.isPresent()) {
            if (!(lastBalance.get().getRealBalanceBuckets()==null)) {
              realBalanceBuckets = lastBalance.get().getRealBalanceBuckets();
                // TODO
                // check the particular balance item, if not exists add it to the bucket by definition
              log.debug("Account Balance1 record found: {}, Last trn ID: {}", lastBalance.get().getId(), lastBalance.get().getLastTransactionID());
            }
            else{
                realBalanceBuckets = prepareBalanceBuckets(accountAttributes,transactionBalances,analyticalTransactions);
              }
        }
        else
        {
            realBalanceBuckets = prepareBalanceBuckets(accountAttributes,transactionBalances,analyticalTransactions);
        }
        return createNewBalances(analyticalTransactions, realBalanceBuckets,transactionBalances);
    }

    private RealBalanceBuckets prepareBalanceBuckets(AccountAttributes accountAttributes, TransactionBalances transactionBalances, AnalyticalTransactions analyticalTransactions) {
        log.info("Preparing buckets for transaction {}, account {}",analyticalTransactions.getTransactionID(),accountAttributes.getAccountNumber());
        RealBalanceBuckets realBalanceBuckets = new RealBalanceBuckets();
        SchemaBalanceDefinitions schemaBalanceDefinitions = schemaBalanceDefinitionsRepository.findBySchemaCode(accountAttributes.getTemplateAttributes().get("SCHEMA_CODE"));;
        BalanceBucketDefinitions balanceBucketDefinitionsList  = schemaBalanceDefinitions.getBalanceBucketDefinitions();

        log.info(balanceBucketDefinitionsList.toString());

        // TODO
        // Fill up real balance bucket with zero for that particular bucket item

        List<String> balances = transactionBalances.getBalanceComponents().entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        Map<String, BigDecimal> balanceBuckets = balances.stream().distinct()
                .collect(Collectors.toMap(s -> s.toString(), s -> BigDecimal.ZERO));
        log.debug("Account Balance record  not found! Create the first record ");

        List<RealBucket> realBucketList = new ArrayList<>();

        for (BucketItemsDefinitions bucketItemsDefinition : balanceBucketDefinitionsList.getBalanceBuckets()) {

            RealBucket realBucket = new RealBucket();
            List<Map<String, Integer>> bucketItems  = bucketItemsDefinition.getBucketItems();
            Map<String, BigDecimal> bucketItem = new HashMap<>();
            for (Map<String, Integer> item : bucketItems) {

                for (Map.Entry<String,Integer> balanceItem: item.entrySet()){
                             bucketItem.put(balanceItem.getKey(),BigDecimal.ZERO);
                             log.info("Bucket item add {}, {}",balanceItem.getKey(),BigDecimal.ZERO);
                }

            }
            realBucket.setBucketName(bucketItemsDefinition.getBucketName());
            log.info("Set Bucket name {}",bucketItemsDefinition.getBucketName());
            realBucket.setBucketItems(bucketItem);
            realBucketList.add(realBucket);

        }
        realBalanceBuckets.setBalanceBuckets(realBucketList);

        return realBalanceBuckets;
    }

    AccountBalances createNewBalances(AnalyticalTransactions analyticalTransactions, RealBalanceBuckets realBalanceBuckets, TransactionBalances transactionBalances){
        AccountBalances newBalances = new AccountBalances();
        newBalances.setRealBalanceBuckets(realBalanceBuckets);
        newBalances.setAccountNumber(analyticalTransactions.getAccountNumber());
        newBalances.setLastTransactionID(analyticalTransactions.getTransactionID());
        newBalances.setSequence(1);
        newBalances.setCurrencyCode(analyticalTransactions.getTransactionCurrency());
        newBalances.setBookDate(analyticalTransactions.getBookDate());
        newBalances.setValueDate(analyticalTransactions.getValueDate());
        return newBalances;
    }

    public AccountBalances createNextBalances(AnalyticalTransactions analyticalTransactions, TransactionBalances transactionBalances, AccountBalances actualBalances){

        RealBalanceBuckets actualBalanceComponents = actualBalances.getRealBalanceBuckets();
        Map<String,Integer> balanceComponentsToUpdate = transactionBalances.getBalanceComponents();

        List<RealBucket> newBuckets = new ArrayList<>();
        List<RealBucket> realBuckets = actualBalanceComponents.getBalanceBuckets();
        boolean balanceFound = false;
        for (RealBucket bucket : realBuckets){
            Map<String,BigDecimal> bucketItems = bucket.getBucketItems();
            Map<String,BigDecimal> newBalances = new HashMap<>();

            for (Map.Entry<String,BigDecimal> balanceItem: bucketItems.entrySet()){
                String balanceName = balanceItem.getKey();
                BigDecimal newBalance = balanceItem.getValue();
                newBalances.put(balanceItem.getKey(),balanceItem.getValue());
                balanceFound = false;
                for (Map.Entry<String,Integer> balanceComponent : balanceComponentsToUpdate.entrySet()) {

                     log.info("Bucket : {}, Balance to Update: {}", bucket.getBucketName(), balanceComponent.getKey());
                     log.info("Balance name : {}, balance old value: {}",balanceName,newBalance);
                    if (balanceName.equals(balanceComponent.getKey())) {
                        balanceFound = true;
                        log.info("Bucket : {}", bucket.getBucketName() );

                        newBalance = updateOneBalanceComponent(analyticalTransactions.getCreditDebitFlag(),
                                analyticalTransactions.getTransactionAmount(), balanceItem.getValue(), BigDecimal.valueOf(balanceComponent.getValue().longValue()));
                        log.info("Update. Schema code: {}, balance name = {}, new value = {}" ,transactionBalances.getSchemaCode(),balanceItem.getKey(),newBalance);
                        newBalances.put(balanceName, newBalance);
                    }
                }


            }

            bucket.setBucketItems(newBalances);
            newBuckets.add(bucket);
        }
        if(!balanceFound){
            //todo
            // if balance is not found add the bucket and the balance
            log.info("Pardon. One of the Balance component isn't present in buckets");
        }
        actualBalanceComponents.setBalanceBuckets(newBuckets);
        actualBalances.setValueDate(analyticalTransactions.getValueDate());
        actualBalances.setRealBalanceBuckets(actualBalanceComponents);
        actualBalances.setLastTransactionID(analyticalTransactions.getTransactionID());
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
