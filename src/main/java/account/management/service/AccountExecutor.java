package account.management.service;

import account.management.model.AnalyticalTransactionDTO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class AccountExecutor {
    private ExecutorService executorService;
        AccountExecutor() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        }
        public   RunnableAccount actorOf(Account account ){
            var accountExecution = new RunnableAccount(account);
            executorService.execute(accountExecution);
            return accountExecution;

    }

    class RunnableAccount implements Runnable {

        Account account;
         final LinkedBlockingQueue<AnalyticalTransactionDTO> mailbox = new LinkedBlockingQueue<>();

        RunnableAccount(Account account) {
            this.account = account;
        }

        public void tell(AnalyticalTransactionDTO msg) {
            mailbox.offer(msg);
        }

        public void run() {

            while (true) {
                try {
                    AnalyticalTransactionDTO message = mailbox.take();
                    account.update(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
