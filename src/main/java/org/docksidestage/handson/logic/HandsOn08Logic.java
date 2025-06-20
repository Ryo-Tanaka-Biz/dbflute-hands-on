package org.docksidestage.handson.logic;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberServiceBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author tanaryo
 */
public class HandsOn08Logic {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(HandsOn08Logic.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private PurchaseBhv purchaseBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private MemberServiceBhv memberServiceBhv;
    @Resource
    private CurrentTimeLogic currentTimeLogic;

    // ===================================================================================
    //                                                                              XXXXXX
    //                                                                        ============

    /**
     * 指定された会員を正式会員に更新する
     * 排他制御ありで更新する
     * 引数の値で null は許されない
     *
     * @param memberId 会員Id
     * @param versionNo バージョンNo
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
     * @param memberId 会員Id
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
     * @param memberId 会員Id
     */
    public void deletePurchaseSimply(Integer memberId){
        if (memberId == null) {
            return; // nullなら何もしない
        }


    }

    /**
     * nullチェック
     *
     * @param args 引数
     */
    private void assertNotNull(Object args) {
        if (args == null) {
            throw new IllegalArgumentException("args is null");
        }
    }
}
