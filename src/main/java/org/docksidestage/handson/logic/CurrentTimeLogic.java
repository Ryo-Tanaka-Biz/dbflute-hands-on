package org.docksidestage.handson.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author tanaryo
 */
public class CurrentTimeLogic {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(CurrentTimeLogic.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;

    // ===================================================================================
    //
    //                                                                        ============

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
