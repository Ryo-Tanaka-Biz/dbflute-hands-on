package org.docksidestage.handson.logic;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.*;
import org.docksidestage.handson.dbflute.exentity.Member;

/**
 * author tanaryo
 */
public class HandsOn08Logic {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private PurchaseBhv purchaseBhv;
    @Resource
    private PurchasePaymentBhv purchasePaymentBhv;

    // ===================================================================================
    //                                                                               Logic
    //                                                                        ============
    /**
     * 指定された会員を正式会員に更新する
     * 排他制御ありで更新する
     * 引数の値で null は許されない
     *
     * @param memberId 会員Id(NotNull)
     * @param versionNo バージョンNo(NotNull)
     */
    public void updateMemberChangedToFormalized(Integer memberId, Long versionNo) {
        assertNotNull(memberId);
        assertNotNull(versionNo);

        Member member = new Member();
        member.setMemberId(memberId);
        member.setMemberStatusCode_正式会員();
        member.setVersionNo(versionNo);
        memberBhv.update(member);
    }

    /**
     * 指定された会員を正式会員に更新する
     * 排他制御なしで更新する
     * 引数の値で null は許されない
     *
     * @param memberId 会員Id(NotNull)
     */
    public void updateMemberChangedToFormalizedNonstrict(Integer memberId){
        assertNotNull(memberId);

        Member member = new Member();
        member.setMemberId(memberId);
        member.setMemberStatusCode_正式会員();
        memberBhv.updateNonstrict(member);
    }

    /**
     * 指定された会員の購入を排他制御なしで削除する ※queryDelete(...)
     * 検索処理は入れずに削除してみましょう
     * 苦難があっても頑張って削除してみましょう
     * 引数の値は null も許される (null なら何もしない)
     * @param memberId 会員Id(NotNull)
     */
    public void deletePurchaseSimply(Integer memberId){
        if (memberId == null) {
            return; // nullなら何もしない
        }

        // FK制約あるので、先に購入支払いを削除
        purchasePaymentBhv.queryDelete(cb ->{
           cb.setupSelect_Purchase();
           cb.query().queryPurchase().setMemberId_Equal(memberId);
        });

        // そのあと購入を削除
        purchaseBhv.queryDelete(cb -> {
            cb.query().setMemberId_Equal(memberId);
        });
    }

    // TODO done tanaryo 引数名 args じゃなくて arg by jflute (2025/06/19)
    /**
     * nullチェック
     *
     * @param arg 引数
     */
    private void assertNotNull(Object arg) {
        if (arg == null) {
            throw new IllegalArgumentException("args is null");
        }
    }
}
