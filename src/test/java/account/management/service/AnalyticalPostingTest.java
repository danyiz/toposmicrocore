package account.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AnalyticalPostingTest {

    static String DEF_CREDIT = "C";
    static String DEF_DEBIT = "D";
    static String DEF_ONE_THOUSAND = "1000";
    static String DEF_MINUS_ONE_THOUSAND = "-1000";
    static String DEF_ZERO = "0";
    static String DEF_ONE = "1";

    static BigDecimal TOPOS_ONE_THOUSAND = new BigDecimal(DEF_ONE_THOUSAND);
    static BigDecimal TOPOS_MINUS_ONE_THOUSAND = new BigDecimal(DEF_MINUS_ONE_THOUSAND);
    static BigDecimal TOPOS_ZERO = new BigDecimal(DEF_ZERO);
    static BigDecimal TOPOS_ONE = new BigDecimal(DEF_ONE);

    @BeforeEach
    void init(){
        analyticalPostingService = new AnalyticalPosting();
    }
    AnalyticalPosting analyticalPostingService;

    @Test
    void updateOneBalanceComponentCreditTest() {
       BigDecimal newBalance = analyticalPostingService.updateOneBalanceComponent(DEF_CREDIT,TOPOS_ONE_THOUSAND, TOPOS_ZERO,TOPOS_ONE);
       assertEquals(newBalance,TOPOS_ONE_THOUSAND);
    }

    @Test
    void updateOneBalanceComponentDebitTest() {
        BigDecimal newBalance = analyticalPostingService.updateOneBalanceComponent(DEF_DEBIT,TOPOS_ONE_THOUSAND,TOPOS_ZERO,TOPOS_ONE);
        assertEquals(newBalance,TOPOS_MINUS_ONE_THOUSAND);
    }

}


