/************************************************************************************************
/*      Copyright 2022-2023 Dhisor-Group Kft. All rights reserved. Used by permission.
/*
/*************************************************************************************************/

package account.management.service;

import account.management.model.AnalyticalTransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AnalyticalPosting {

   @Autowired
   Account account;
   @Autowired
   private ApplicationContext context;

   @Transactional
   public void createAnalyticalTransaction(AnalyticalTransactionDTO analyticalTransactionDTO) throws InterruptedException {
       account.update(analyticalTransactionDTO);
    }
}

