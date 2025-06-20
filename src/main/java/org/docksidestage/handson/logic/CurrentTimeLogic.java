package org.docksidestage.handson.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author tanaryo
 */
public class CurrentTimeLogic {
    // done tanaryo message by jflute (2025/06/19)
    // ===================================================================================
    //                                                                               Logic
    //                                                                        ============
    // done tanaryo javadoc, せっかくなので @return 書きましょう by jflute (2025/06/19)
    /**
     * @return 現在日付(NotNull)
     */
    public LocalDate currentDate(){
        return LocalDate.now();
    }

    /**
     * @return 現在日時(NotNull)
     */
    public LocalDateTime currentDateTime(){
        return  LocalDateTime.now();
    }
}
