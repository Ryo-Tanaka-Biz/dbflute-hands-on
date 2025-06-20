package org.docksidestage.handson.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * author tanaryo
 */
public class CurrentTimeLogic {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // TODO done tanaryo message by jflute (2025/06/19)

    // ===================================================================================
    //
    //                                                                        ============
    // TODO tanaryo javadoc, せっかくなので @return 書きましょう by jflute (2025/06/19)
    /**
     * LocalDate で現在日時を戻す
     */
    public LocalDate currentDate(){
        return LocalDate.now();
    }

    /**
     * LocalDateTime で現在日時を戻す
     */
    public LocalDateTime currentDateTime(){
        return  LocalDateTime.now();
    }
}
