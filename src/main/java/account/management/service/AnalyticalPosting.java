/************************************************************************************************
/*      Copyright 2022-2023 Dhisor-Group Kft. All rights reserved. Used by permission.
/*
/*************************************************************************************************/

package account.management.service;

import account.management.model.AnalyticalTransactionDTO;
import account.management.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gruelbox.transactionoutbox.TransactionOutbox;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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

   AccountExecutor system = null;

    Map<String, AccountExecutor.RunnableAccount> accountMap;


    @Autowired
    Account account;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void createAnalyticalTransaction(AnalyticalTransactionDTO analyticalTransactionDTO) throws InterruptedException {

        if (Objects.isNull(accountMap)) {
            accountMap = new HashMap<>();
            system = new AccountExecutor();
        }
        var actualRunner = accountMap.get(analyticalTransactionDTO.getAccountNumber());
        if (Objects.isNull(actualRunner)) {
            var acc = account.createAccount(analyticalTransactionDTO.getAccountNumber());
            actualRunner = system.actorOf(acc);
            accountMap.put(analyticalTransactionDTO.getAccountNumber(), actualRunner);
            log.info("Queue item added {}", analyticalTransactionDTO.getAccountNumber());
            actualRunner.tell(analyticalTransactionDTO);
        } else {
            actualRunner.tell(analyticalTransactionDTO);
        }
    }
}

