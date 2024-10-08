package account.management.service;

import account.management.model.AnalyticalTransactionDTO;
import account.management.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gruelbox.transactionoutbox.TransactionOutbox;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@NoArgsConstructor
@Getter
@Setter
public class Account {
    private String accountNumber;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ModelMapper mapper;

    @Autowired
    AnalyticalTransactionRepository analyticalTransactionRepository;

    @Autowired
    AccountBalancesRepository accountBalancesRepository;

    @Autowired
    AccountBalanceHistoryRepository accountBalanceHistoryRepository;

    @Autowired
    TransactionBalancesRepository transactionBalancesRepository;

    @Autowired
    TransactionDefinitionsRepository transactionDefinitionsRepository;

    @Autowired
    SchemaBalanceDefinitionsRepository schemaBalanceDefinitionsRepository;

    @Autowired
    ModelMapper modelMapper;

    //@Autowired
    //private TransactionOutbox outbox;

//    @Autowired
//    KafkaTemplate kafkaTemplate;

    @Autowired
    AccountAttributesRepository accountAttributesRepository;

    Account createAccount(String accountNumber){
        this.accountNumber = accountNumber;
        return this;
    }

    public AnalyticalTransactionDTO update(AnalyticalTransactionDTO analyticalTransactionDTO) {
        log.info("Executor:{}, {}",accountNumber,analyticalTransactionDTO.getProcessID());
        //lock the account
        try {
            AccountAttributes accountAttributes = accountAttributesRepository.lockTheAttributes(analyticalTransactionDTO.getAccountNumber());
            log.info("account attributes {}", accountAttributes.getAccountNumber());
            try {
                AnalyticalTransactions analyticalTransactions = modelMapper.map(analyticalTransactionDTO, AnalyticalTransactions.class);
                AnalyticalTransactions savedTransaction = analyticalTransactionRepository.save(analyticalTransactions);
                accountAttributes.setLastTransactionId(analyticalTransactions.getTransactionID());
                AccountBalances newBalances = balanceUpdate(analyticalTransactions,accountAttributes);
                AccountBalanceHistory newBalanceHistory = mapToHistory(newBalances);
                newBalanceHistory= accountBalanceHistoryRepository.save(newBalanceHistory);
                // insert into balance change log and outbox it as balance change
                log.info("Transaction succeed: {}", analyticalTransactions.getId().toString());
                accountAttributesRepository.save(accountAttributes);
                //raise an event with new posting
                //outbox.schedule(getClass()).publishCreatedAnalyticalTransaction(analyticalTransactions.getId());
                //raise an event with new balances + "put it to outbox"
                //outbox.schedule(getClass()).publishCreatedBalanceHistory(newBalanceHistory.getId());
                //outbox.schedule(getClass()).publishUpdatedBalances(analyticalTransactions.getId());

            } catch (Exception e) {
                log.info("Transaction failed: {}", e);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            }
        } catch (Exception e){
            log.info("Account lock failed: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        }
        return analyticalTransactionDTO;
    }

    private AccountBalanceHistory mapToHistory(AccountBalances newBalances) {
        AccountBalanceHistory newHist =  new AccountBalanceHistory();
        newHist.setAccountNumber(newBalances.getAccountNumber());
        newHist.setBookDate(newBalances.getBookDate());
        newHist.setCurrencyCode(newBalances.getCurrencyCode());
        newHist.setTransactionID(newBalances.getLastTransactionID());
        newHist.setBalanceBuckets(newBalances.getBalanceBuckets());
        return newHist;
    }

//    void publishCreatedAnalyticalTransaction(long id) throws JsonProcessingException {
//        log.info("Scheduled event executed outside transaction.......{}",id);
//        AnalyticalTransactions tran = (AnalyticalTransactions)analyticalTransactionRepository.findById(id).subscribe();
//        var analyticalTransactionDTO = mapper.map(tran,AnalyticalTransactionDTO.class);
//        kafkaTemplate.send("topos.core.postings", objectMapper.writeValueAsString(analyticalTransactionDTO));
//
//    }
//
//    void publishCreatedBalanceHistory(long id) throws JsonProcessingException {
//        log.info("Scheduled event executed outside transaction.......{}",id);
//        AccountBalanceHistory historyRecord = (AccountBalanceHistory)accountBalanceHistoryRepository.findById(id).subscribe();
//        if(historyRecord!=null){
//            kafkaTemplate.send("topos.core.balances", objectMapper.writeValueAsString(historyRecord));
//        }else {
//            log.info("History record with {} not found",id);
//        }
//    }

    public AccountBalances   balanceUpdate(AnalyticalTransactions analyticalTransactions, AccountAttributes accountAttributes) {
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
            if (!(lastBalance.get().getBalanceBuckets()==null)) {
                realBalanceBuckets = lastBalance.get().getBalanceBuckets();
                // todo check the particular balance item, if not exists add it to the bucket by definition
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

        // todo Fill up real balance bucket with zero for that particular bucket item

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
        newBalances.setBalanceBuckets(realBalanceBuckets);
        newBalances.setAccountNumber(analyticalTransactions.getAccountNumber());
        newBalances.setLastTransactionID(analyticalTransactions.getTransactionID());
        newBalances.setSequence(1);
        newBalances.setCurrencyCode(analyticalTransactions.getTransactionCurrency());
        newBalances.setBookDate(analyticalTransactions.getBookDate());
        newBalances.setValueDate(analyticalTransactions.getValueDate());
        return newBalances;
    }

    public AccountBalances createNextBalances(AnalyticalTransactions analyticalTransactions, TransactionBalances transactionBalances, AccountBalances actualBalances){

        RealBalanceBuckets actualBalanceComponents = actualBalances.getBalanceBuckets();
        Map<String,Integer> balanceComponentsToUpdate = transactionBalances.getBalanceComponents();

        List<RealBucket> newBuckets = new ArrayList<>();
        List<RealBucket> realBuckets = actualBalanceComponents.getBalanceBuckets();
        Map<String,Integer> validation = new HashMap<>();
        for (Map.Entry<String,Integer> balanceComponent : balanceComponentsToUpdate.entrySet()) {
            validation.put(balanceComponent.getKey(),1);
        }

        for (RealBucket bucket : realBuckets){
            Map<String,BigDecimal> bucketItems = bucket.getBucketItems();
            Map<String,BigDecimal> newBalances = new HashMap<>();

            for (Map.Entry<String,BigDecimal> balanceItem: bucketItems.entrySet()){
                String balanceName = balanceItem.getKey();
                BigDecimal newBalance = balanceItem.getValue();
                newBalances.put(balanceItem.getKey(),balanceItem.getValue());

                for (Map.Entry<String,Integer> balanceComponent : balanceComponentsToUpdate.entrySet()) {

                    //log.info("Bucket : {}, Balance to Update: {}", bucket.getBucketName(), balanceComponent.getKey());
                    //log.info("Balance name : {}, balance old value: {}",balanceName,newBalance);
                    if (balanceName.equals(balanceComponent.getKey())) {
                        validation.remove(balanceComponent.getKey());
                        //log.info("Bucket : {}", bucket.getBucketName() );

                        newBalance = updateOneBalanceComponent(analyticalTransactions.getCreditDebitFlag(),
                                analyticalTransactions.getTransactionAmount(), balanceItem.getValue(), BigDecimal.valueOf(balanceComponent.getValue().longValue()));
                       // log.info("Update. Schema code: {}, balance name = {}, new value = {}" ,transactionBalances.getSchemaCode(),balanceItem.getKey(),newBalance);
                        newBalances.put(balanceName, newBalance);
                    }

                }
            }

            bucket.setBucketItems(newBalances);
            newBuckets.add(bucket);
        }
        if(!validation.isEmpty()){
            //todo if balance is not found add the bucket and the balance
            log.info("Pardon. These Balance component isn't present in buckets {}",validation.keySet().stream().toList());
        }
        else{
            log.info("All balance components are updated.");
        }
        actualBalanceComponents.setBalanceBuckets(newBuckets);
        actualBalances.setValueDate(analyticalTransactions.getValueDate());
        actualBalances.setBalanceBuckets(actualBalanceComponents);
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
