package org.docksidestage.handson.logic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberLoginBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author tanaryo
 */
public class HandsOn11LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberLoginBhv memberLoginBhv;
    @Resource
    private HandsOn11Logic logic;

    // ===================================================================================
    //                                                                                Test
    //                                                                        ============
    /**
     * 会員名称が "vi" を含んでいる会員を検索
     * 支払済み購入が取得できていることをアサート
     */
    public void test_selectPurchaseMemberList_会員と購入が検索されていること() {
        // ## Arrange ##
        String memberNameKeyword = "vi";

        // ## Act ##
        List<Member> members = logic.selectPurchaseMemberList(memberNameKeyword);

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberName().contains(memberNameKeyword));
            List<Purchase> purchases = member.getPurchaseList();
            if (!purchases.isEmpty()) {
                assertTrue(member.getPurchaseList().stream().allMatch(Purchase::isPaymentCompleteFlgTrue));
                // allMatchはstreamがemptyの場合にtrueを返すことに注意
            }
        });
    }

    /**
     * 会員名称が "vi" を含んでいる会員を検索
     * 検索された会員が未払い購入を持っていることをアサート
     */
    public void test_selectUnpaidMemberList_未払い購入がある会員が検索されていること() {
        // ## Arrange ##
        String memberNameKeyword = "vi";

        // ## Act ##
        List<Member> members = logic.selectUnpaidMemberList(memberNameKeyword);
        memberBhv.loadPurchase(members, purchaseCB -> {});//ただ子テーブルを取得

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberName().contains(memberNameKeyword));
            List<Purchase> purchases = member.getPurchaseList();
            assertHasAnyElement(purchases);
            assertTrue(purchases.stream().anyMatch(Purchase::isPaymentCompleteFlgFalse));
        });
    }

    /**
     * 会員名称が "vi" を含んでいる会員を検索
     * 会員の最終ログイン日時が本当に最終ログイン日時であることをアサート
     */
    public void test_selectLoggedInMemberList_会員と最終ログイン日時が検索されていること() {
        // ## Arrange ##
        String memberNameKeyword = "vi";

        // ## Act ##
        List<Member> members = logic.selectLoggedInMemberList(memberNameKeyword);

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            log(member.getMemberId());
            assertTrue(member.getMemberName().contains(memberNameKeyword));

            LocalDateTime lastLoginDatetime = member.getLastLoginDatetime();
            List<MemberLogin> memberLogins = member.getMemberLoginList();
            if (!memberLogins.isEmpty()) {
                assertTrue(memberLogins.stream().map(op -> op.getLoginDatetime()).anyMatch(op -> op.equals(lastLoginDatetime)));
                long count = memberLogins.stream().filter(op -> op.getLoginDatetime().isAfter(lastLoginDatetime)).count();
                assertEquals(0, count);
            }
        });
    }

    /**
     * 未払い購入の存在しない会員だけを検索
     * 未払い購入が存在しないことをアサート
     * 会員ステータス経由の会員ログインが取得できていることをアサート
     * 購入支払が最も推奨されている方法のみ検索されていることをアサート
     * その他、ロジックの中で出力したログを見て期待通りであることを確認
     * 検索された全会員の購入支払金額の合計をstream()で求めてログに出す！
     */
    public void test_selectOnParadeFirstStepMember_未払い購入の存在しない会員() {
        // ## Arrange ##
        boolean completeOnly = true;

        //memberId=1は会員ログインを持っている
        int memberLogin1Count = memberLoginBhv.selectCount(cb -> {
            cb.query().setMemberId_Equal(1);
        });
        assertTrue(memberLogin1Count >= 1);

        // memberId=6はモバイルで1回だけログインしたことがある
        int memberMobileLogin6Count = memberLoginBhv.selectCount(cb -> {
            cb.query().setMemberId_Equal(6);
            cb.query().setMobileLoginFlg_Equal_True();
        });
        assertEquals(1, memberMobileLogin6Count);

        int sum = 0;

        // ## Act ##
        List<Member> members = logic.selectOnParadeFirstStepMember(completeOnly);

        // ## Assert ##
        assertHasAnyElement(members);
        for (Member member : members) {
            List<Purchase> purchaseList = member.getPurchaseList();
            assertHasAnyElement(purchaseList);
            assertFalse(purchaseList.stream().anyMatch(Purchase::isPaymentCompleteFlgFalse));

            if (member.getMemberId().equals(1)) {
                MemberStatus status = member.getMemberStatus().orElseThrow();
                assertHasAnyElement(status.getMemberLoginList());
            }

            if (member.getMemberId().equals(6)) {
                assertEquals(1, member.getMobileLoginCount());
            }

            List<PurchasePayment> purchasePaymentList =
                    purchaseList.stream().flatMap(op -> op.getPurchasePaymentList().stream()).collect(Collectors.toList());
            assertHasAnyElement(purchasePaymentList);
            assertTrue(purchasePaymentList.stream().allMatch(op -> op.isPaymentMethodCode_Recommended()));

            sum = sum + purchaseList.stream().map(op -> op.getPurchasePrice()).mapToInt(Integer::intValue).sum();
        }
        log("検索された全会員の購入支払金額の合計={}", sum + "円");
    }

    // ===================================================================================
    //                                                                   Assist Test Logic
    //                                                                        ============

}
