package org.docksidestage.handson.dbflute.cbean.cq;

import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.docksidestage.handson.dbflute.cbean.cq.bs.BsMemberCQ;

/**
 * The condition-query of member.
 * <p>
 * You can implement your original methods here.
 * This class remains when re-generating.
 * </p>
 * @author DBFlute(AutoGenerator)
 * @author tanaryo
 */
public class MemberCQ extends BsMemberCQ {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    // You should NOT touch with this constructor.
    /**
     * Auto-generated constructor to create query instance, basically called in DBFlute.
     * @param referrerQuery The instance of referrer query. (NullAllowed: if null, this is base query)
     * @param sqlClause The instance of SQL clause. (NotNull)
     * @param aliasName The alias name for this query. (NotNull)
     * @param nestLevel The nest level of this query. (if zero, this is base query)
     */
    public MemberCQ(ConditionQuery referrerQuery, SqlClause sqlClause, String aliasName, int nestLevel) {
        super(referrerQuery, sqlClause, aliasName, nestLevel);
    }

    // ===================================================================================
    //                                                                       Arrange Query
    //                                                                       =============
    // You can make your arranged query methods here. e.g. public void arrangeXxx()
    // done tanaryo 銀行振込のくだりくらいはjavadocに説明があったほうがいいかも by jflute (2025/02/13)
    // (あんまりオウム返しもしたくはないけど、重要な業務要件ではあるので)
    /**
     * 銀行振込で購入を支払ったことのあるナイスな最年少会員
     */
    public void arrangeYoungestNiceMember(){
        //scalar_(条件)は特定カラムで絞り込み。cb.query().〜の粒度で使用される
        //selectScalarは特定カラムの取得。selectListやselectEntityと同じ粒度で使用される
    	// done tanaryo ここも外側の条件が足りない話 by jflute (2025/02/13)
    	// TODO tanaryo 修行++: existsがコピーになっちゃってるので、そこは再利用してみましょう by jflute (2025/02/21)
        existsPurchase(purchaseCB -> {
            purchaseCB.query().existsPurchasePayment(purchasePaymentCB ->{
                purchasePaymentCB.query().setPaymentMethodCode_Equal_BankTransfer();
            });
        });
        scalar_Equal().max(memberCB -> {
            memberCB.specify().columnBirthdate();
            memberCB.query().existsPurchase(purchaseCB -> {
                purchaseCB.query().existsPurchasePayment(purchasePaymentCB -> {
                    purchasePaymentCB.query().setPaymentMethodCode_Equal_BankTransfer();
                });
            });
        }).partitionBy(memberCB -> {
            memberCB.specify().columnMemberStatusCode();
        });
    }
}
