package org.docksidestage.handson.logic;

import javax.annotation.Resource;

import org.dbflute.exception.EntityAlreadyUpdatedException;
import org.dbflute.utflute.core.cannonball.CannonballOption;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.ProductBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// done tanaryo javadocお願い by jflute (2025/06/21)

/**
 * @author tanaryo
 */
public class HandsOn08LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private PurchaseBhv purchaseBhv;
    @Resource
    private HandsOn08Logic logic;

    // ===================================================================================
    //                                                                                Test
    //                                                                        ============
    /**
     * 任意の仮会員の会員IDとバージョンNOを渡して更新すること
     * テストデータ上で任意の仮会員のIDが何番なのかに依存しないように
     * 更新処理後、DB上のデータが更新されていることをアサート
     */
    public void test_updateMemberChangedToFormalized_会員が更新されていること() {
        // ## Arrange ##
        // done tanaryo Optionalの解決をもっと丁寧に by jflute (2025/06/19)
        Member member = findProvisionalMember();
        Integer memberId = member.getMemberId();
        Long versionNo = member.getVersionNo();
        // done tanaryo 前提のアサートは、Arrangeの中にあってもいい by jflute (2025/06/19)
        assertTrue(member.isMemberStatusCode仮会員());

        // ## Act ##
        logic.updateMemberChangedToFormalized(memberId, versionNo);

        // ## Assert ##
        Member updatedMember = memberBhv.selectByPK(memberId).orElseThrow();
        assertTrue(updatedMember.isMemberStatusCode正式会員());
        // done tanaryo assertEquals()というメソッドがあるので、そっちを使いましょう by jflute (2025/06/19)
        long updatedVersionNo = updatedMember.getVersionNo();
        assertEquals(versionNo + 1, updatedVersionNo);
    }

    /**
     * 何かしらの方法で排他制御例外を発生させてみること
     * 排他制御例外が発生することをアサート
     * 排他制御例外の内容をログに出力して目視確認すること
     */
    public void test_updateMemberChangedToFormalized_排他制御例外が発生すること() {
        // ## Arrange ##
        Member member = findProvisionalMember();
        Integer memberId = member.getMemberId();
        Long versionNo = member.getVersionNo();
        // done tanaryo 一個目のupdateは、Arrangeと捉えていいかなと by jflute (2025/06/19)
        logic.updateMemberChangedToFormalized(memberId, versionNo);

        // ## Act ##
        // ## Assert ##
        // done tanaryo 例外クラス名、FQCNである必要はないかと by jflute (2025/06/19)
        assertException(EntityAlreadyUpdatedException.class, () -> {
            logic.updateMemberChangedToFormalized(memberId, versionNo);
        });
        // ログ出力も確認した
        // versionNoを+1すると例外発生しない
    }

    /**
     * 任意の仮会員の会員IDを渡して更新すること
     * 更新処理後、DB上のデータが更新されていることをアサート
     */
    public void test_updateMemberChangedToFormalizedNonstrict_会員が更新されていること() {
        // ## Arrange ##
        Member member = findProvisionalMember();
        Integer memberId = member.getMemberId();
        assertTrue(member.isMemberStatusCode仮会員());

        // ## Act ##
        logic.updateMemberChangedToFormalizedNonstrict(memberId);

        // ## Assert ##

        memberBhv.selectByPK(memberId).ifPresent(updatedMember -> {
            assertTrue(updatedMember.isMemberStatusCode正式会員());
        });
    }

    /**
     * 通常なら排他制御例外が起きるはずの状況でも排他制御例外が発生しないことをアサート
     */
    public void test_updateMemberChangedToFormalizedNonstrict_排他制御例外が発生しないこと() {
        // ## Arrange ##
        Member member = findProvisionalMember();
        Integer memberId = member.getMemberId();
        boolean isThrowException = false;

        // ## Act ##
        logic.updateMemberChangedToFormalizedNonstrict(memberId);
        try {
            logic.updateMemberChangedToFormalizedNonstrict(memberId);
        } catch (EntityAlreadyUpdatedException e) {
            // TODO jflute できてるんだけど、書き方のフォロー (2025/06/19)
            isThrowException = true;
        }

        // ## Assert ##
        assertFalse(isThrowException);
    }

    /**
     * 任意の正式会員の会員IDを渡して削除すること
     * 削除処理後、DB上のデータが削除されていることをアサート
     *
     */
    public void test_deletePurchaseSimply_購入が削除されていること() {
        // ## Arrange ##
        Member member = findExistsPurchaseMember();
        Integer memberId = member.getMemberId();
        int purchaseCount = purchaseBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));
        assertTrue(purchaseCount >= 1);//2件以上のテストもしたい

        // ## Act ##
        logic.deletePurchaseSimply(memberId);
        int deletedPurchaseCount = purchaseBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));

        // ## Assert ##
        assertEquals(deletedPurchaseCount, 0);
    }

    /**
     * cannonball()メソッドを使ってデッドロックを発生させてみること
     * 例外メッセージに "Deadlock" という文字が含まれていることをアサート
     */
    public void test_IfYouLike_DeadLock() {
        // ## Arrange ##
        Integer memberId = findExistsPurchaseMember().getMemberId();
        Purchase purchase = findPurchase(memberId);
        Long purchaseId = purchase.getPurchaseId();
        Long purchaseVersionNo = purchase.getVersionNo();

        // ## Act ##
        // ## Assert ##
        cannonball(car -> {
            //処理A
            car.projectA(dragon -> {
                Member member = new Member();
                member.setMemberId(memberId);
                member.setMemberName("田中");
                memberBhv.updateNonstrict(member);
            }, 1);//スレッド1はmemberの行をロック

            //処理B
            car.projectA(dragon -> {
                Purchase purchaseEntity = new Purchase();
                purchaseEntity.setPurchaseId(purchaseId);
                purchaseEntity.setPurchaseCount(5);
                purchaseEntity.setVersionNo(purchaseVersionNo);
                purchaseBhv.update(purchaseEntity);

            }, 2);//スレッド2はpurchaseの行をロック

            //処理C
            car.projectA(dragon -> {
                Purchase purchaseEntity = new Purchase();
                purchaseEntity.setPurchaseId(purchaseId);
                purchaseEntity.setPurchaseCount(5);
                purchaseEntity.setVersionNo(purchaseVersionNo);
                purchaseBhv.update(purchaseEntity);

            }, 1);//スレッド1はpurchaseの行ロックしようとするが、スレッド2がロックしているので待機状態

            //処理D
            car.projectA(dragon -> {
                Member member = new Member();
                member.setMemberId(memberId);
                member.setMemberName("田中");
                memberBhv.updateNonstrict(member);
            }, 2);//スレッド2はmemberの行ロックしようとするが、スレッド1がロックしているので待機状態
        }, new CannonballOption().threadCount(2).expectExceptionAny("Deadlock"));
        //A->B->C->Dの順の場合、お互いが相手のロックを待つ状態になる
        //A->D->B->Cの場合、スレッド2がロックを待つ状態で、スレッド1はロック待ちしない
        //ロックを保持したまま、別のロックの取得を待つのが前提？
    }

    // ===================================================================================
    //                                                                   Assist Test Logic
    //                                                                        ============
    // done tanaryo Assist Logic な感じのタグコメントが欲しいところ by jflute (2025/06/21)
    // done tanaryo javadoc, Nullの可否をお願いします by jflute (2025/06/21)
    /**
     * 仮会員を任意で検索
     *
     * @return Member(NotNull)
     */
    private Member findProvisionalMember() {
        // done tanaryo ConditionBeanのfetchFirst(1)でselectEntity()の方が無駄メモリがない by jflute (2025/06/21)
        return memberBhv.selectEntity(cb -> {
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.fetchFirst(1);
        }).orElseThrow();
        //確かに複数取得してjava内で1件に絞るより、取得の時点で1件に絞った方がクエリ負荷は軽そう（by tanaryo）
    }

    /**
     * 購入を持つ会員を任意で検索
     *
     * @return Member(NotNull)
     */
    private Member findExistsPurchaseMember() {
        return memberBhv.selectEntity(cb -> {
            cb.query().existsPurchase(subCB -> {
            });
            cb.fetchFirst(1);
        }).orElseThrow();
    }

    /**
     * 特定の会員に紐づく購入を任意で検索
     *
     * @param memberId 会員Id(NotNull)
     * @return Purchase(NotNull)
     */
    private Purchase findPurchase(Integer memberId) {
        return purchaseBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(memberId);
            cb.fetchFirst(1);
        }).orElseThrow();
    }
}
