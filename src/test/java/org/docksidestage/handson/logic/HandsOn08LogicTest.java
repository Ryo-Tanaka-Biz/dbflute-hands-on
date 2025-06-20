package org.docksidestage.handson.logic;

import java.util.Optional;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author tanaryo
 */
public class HandsOn08LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                    =========
    @Resource
    private MemberBhv memberBhv;

    @Resource
    private HandsOn08Logic logic;

    // ===================================================================================
    //                                                     updateMemberChangedToFormalized
    //                                                                        ============

    /**
     * 任意の仮会員の会員IDとバージョンNOを渡して更新すること
     * テストデータ上で任意の仮会員のIDが何番なのかに依存しないように
     * 更新処理後、DB上のデータが更新されていることをアサート
     */
    public void test_updateMemberChangedToFormalized_会員が更新されていること() {
        // ## Arrange ##
    	// TODO tanaryo Optionalの解決をもっと丁寧に by jflute (2025/06/19)
        Optional<Member> member = findProvisionalMember();
        Integer memberId = member.map(Member::getMemberId).orElse(null);
        Long versionNo = member.map(Member::getVersionNo).orElse(null);

        // ## Act ##
        logic.updateMemberChangedToFormalized(memberId, versionNo);

        // ## Assert ##
        // TODO tanaryo 全体のアサートは、Arrangeの中にあってもいい by jflute (2025/06/19)
        member.ifPresent(mb -> {
            assertTrue(mb.isMemberStatusCode仮会員());
        });

        memberBhv.selectByPK(memberId).ifPresent(updatedMember -> {
            assertTrue(updatedMember.isMemberStatusCode正式会員());
            // TODO tanaryo assertEquals()というメソッドがあるので、そっちを使いましょう by jflute (2025/06/19)
            assertTrue(updatedMember.getVersionNo().equals(versionNo + 1));
        });
    }

    /**
     * 何かしらの方法で排他制御例外を発生させてみること
     * 排他制御例外が発生することをアサート
     * 排他制御例外の内容をログに出力して目視確認すること
     */
    public void test_updateMemberChangedToFormalized_排他制御例外が発生すること() {
        // ## Arrange ##
        Optional<Member> member = findProvisionalMember();
        Integer memberId = member.map(Member::getMemberId).orElse(null);
        Long versionNo = member.map(Member::getVersionNo).orElse(null);

        // ## Act ##
        // TODO tanaryo 一個目のupdateは、Arrangeと捉えていいかなと by jflute (2025/06/19)
        logic.updateMemberChangedToFormalized(memberId, versionNo);

        // ## Assert ##
        // TODO tanaryo 例外クラス名、FQCNである必要はないかと by jflute (2025/06/19)
        assertException(org.dbflute.exception.EntityAlreadyUpdatedException.class, () -> {
            logic.updateMemberChangedToFormalized(memberId, versionNo);
        });
        // ログ出力も確認した
        // versionNoを+1すると例外発生しない
    }

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============

    /**
     * 任意の仮会員の会員IDを渡して更新すること
     * 更新処理後、DB上のデータが更新されていることをアサート
     */
    public void test_updateMemberChangedToFormalizedNonstrict_会員が更新されていること() {
        // ## Arrange ##
        Optional<Member> member = findProvisionalMember();
        Integer memberId = member.map(Member::getMemberId).orElse(null);

        // ## Act ##
        logic.updateMemberChangedToFormalizedNonstrict(memberId);

        // ## Assert ##
        member.ifPresent(mb -> {
            assertTrue(mb.isMemberStatusCode仮会員());
        });

        memberBhv.selectByPK(memberId).ifPresent(updatedMember -> {
            assertTrue(updatedMember.isMemberStatusCode正式会員());
        });
    }

    /**
     * 通常なら排他制御例外が起きるはずの状況でも排他制御例外が発生しないことをアサート
     */
    public void test_updateMemberChangedToFormalizedNonstrict_排他制御例外が発生しないこと() {
        // ## Arrange ##
        Optional<Member> member = findProvisionalMember();
        Integer memberId = member.map(Member::getMemberId).orElse(null);
        boolean isThrowException = false;

        // ## Act ##
        logic.updateMemberChangedToFormalizedNonstrict(memberId);
        try {
            logic.updateMemberChangedToFormalizedNonstrict(memberId);
        } catch (org.dbflute.exception.EntityAlreadyUpdatedException e) {
        	// TODO jflute できてるんだけど、書き方のフォロー (2025/06/19)
            isThrowException = true;
        }

        // ## Assert ##
        assertFalse(isThrowException);
    }

    private Optional<Member> findProvisionalMember() {
        return memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_Equal_仮会員();
        }).stream().findFirst();
    }
}
