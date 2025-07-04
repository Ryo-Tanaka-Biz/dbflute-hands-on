package org.docksidestage.handson.logic;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.*;
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
    private PurchaseBhv purchaseBhv;
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

    /**
     * 商品も取得できることをアサート
     * 購入商品種類数が妥当であることをアサート
     * 生産中止の商品を買ったことのある会員が(一人でも)検索されていることをアサート
     * どんな手段でもいいので、手渡しだけでも...(略)ている会員が(一人でも)検索されていることを目視確認
     */
    public void test_selectOnParadeSecondStepMember_購入のみならず商品も検索() {
        // ## Arrange ##
        // memberId=20は商品ステータスが "生産中止" の商品を買ったことがあり、購入商品種類数が3である
        int count = purchaseBhv.selectCount(cb -> {
            cb.query().setMemberId_Equal(20);
            cb.query().queryProduct().setProductStatusCode_Equal_生産中止();
        });
        assertTrue(count > 0);

        int kindCount = purchaseBhv.selectScalar(Integer.class).countDistinct(cb -> {
            cb.specify().columnProductId();
            cb.query().setMemberId_Equal(20);
        });
        assertEquals(3, kindCount);

        // ## Act ##
        List<Member> members = logic.selectOnParadeSecondStepMember();

        // ## Assert ##
        assertHasAnyElement(members);
        assertTrue(members.stream().map(Member::getMemberId).anyMatch(id -> id.equals(20)));
        members.forEach(member -> {
            List<Purchase> purchaseList = member.getPurchaseList();
            if (!purchaseList.isEmpty()) {
                List<Product> productList = purchaseList.stream().flatMap(op -> op.getProduct().stream()).collect(Collectors.toList());
                assertHasAnyElement(productList);
            }
            if (member.getMemberId().equals(20)) {
                assertEquals(3, member.getProductKindCount());
            }
        });
        // memberId=4のデバッグログより、purchaseId=31において、購入価格:1700円で未払い、支払い価格:1700.85円（手渡し）を確認
        // 手渡しの分割払い考慮しても、memberId=4のデバックログが出ることを確認
    }
    /**
     * ログイン回数が 2 回より多い会員を検索し、結果がその通りであることをアサート
     * 最終ログイン日時の降順と会員IDの昇順で並んでいることをアサート
     * 支払済み購入の最大購入価格が妥当であることをアサート
     * 仮会員のときにログインをしたことのある会員であることをアサート
     * 自分だけが購入している商品を買ったことのある会員であることをアサート
     */
    public void test_selectOnParadeXStepMember_オンパレードであること() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = logic.selectOnParadeXStepMember(3);

        // ## Assert ##
        assertHasAnyElement(members);
        List<Member> sortedMembers = members.stream()
                .sorted(Comparator.comparing(Member::getLastLoginDatetime).reversed().thenComparing(Member::getMemberId))
                .collect(Collectors.toList());
        // 該当データが１件なので、アサートの意味があまりないか。
        // ヒットするデータを作るのが大変そうだ。。。。
        assertEquals(sortedMembers, members);
        for (Member member : members) {
            Integer memberId = member.getMemberId();
            int loginCount = memberLoginBhv.selectCount(cb -> {
                cb.query().setMemberId_Equal(memberId);
            });
            assertTrue(loginCount > 2);

            int payedMaxPurchasePrice = member.getPayedMaxPurchasePrice();
            int maxPrice = purchaseBhv.selectScalar(Integer.class).max(purchaseCB -> {
                purchaseCB.specify().columnPurchasePrice();
                purchaseCB.query().setMemberId_Equal(memberId);
                purchaseCB.query().setPaymentCompleteFlg_Equal_True();
            }).orElseThrow();
            assertEquals(maxPrice, payedMaxPurchasePrice);

            int loginCountPRV = memberLoginBhv.selectCount(cb -> {
                cb.query().setMemberId_Equal(memberId);
                cb.query().setLoginMemberStatusCode_Equal_仮会員();
            });
            assertTrue(loginCountPRV > 0);

            List<Integer> productIdList = purchaseBhv.selectList(cb -> {
                cb.query().setMemberId_Equal(memberId);
            }).stream().map(Purchase::getProductId).collect(Collectors.toList());
            boolean isOnlyMePurchase = checkOnlyMePurchase(productIdList);
            assertTrue(isOnlyMePurchase);
        }
    }

    /**
     * 会員数が妥当であることをアサート
     * 検索した内容をログに綺麗に出して目視で確認すること
     */
    public void test_selectServiceRankSummary_集計されていること() {
        // ## Arrange ##
        int memberTotalCount = memberBhv.selectCount(cb -> {});

        // ## Act ##
        List<ServiceRank> serviceRanks = logic.selectServiceRankSummary();

        // ## Assert ##
        assertHasAnyElement(serviceRanks);
        int sum = serviceRanks.stream().map(ServiceRank::getMemberCount).mapToInt(Integer::intValue).sum();
        assertEquals(memberTotalCount, sum);
        serviceRanks.forEach(serviceRank -> {
            Integer memberCount = serviceRank.getMemberCount();
            Integer totalPurchasePrice = serviceRank.getTotalPurchasePrice();
            Integer avgMaxPurchasePrice = serviceRank.getAvgMaxPurchasePrice();
            Integer totalLoginCount = serviceRank.getTotalLoginCount();
            log("会員数={}, 合計購入価格={}, 平均最大購入価格={},ログイン数={}", memberCount, totalPurchasePrice, avgMaxPurchasePrice,
                    totalLoginCount);
        });
    }

    /**
     * 平均の最大価格に該当する会員が存在することをアサート
     */
    public void test_selectMaxAvgPurchasePrice_平均の最大の会員() {
        // ## Arrange ##
        // ## Act ##
        Integer value = logic.selectMaxAvgPurchasePrice();

        // ## Assert ##
        assertNotNull(value);
        int count = memberBhv.selectCount(cb -> {
            cb.query().derivedPurchase().avg(purchaseCB -> {
                purchaseCB.specify().columnPurchasePrice();
            }).equal(value);
        });
        assertTrue(count > 0);
    }

    // ===================================================================================
    //                                                                   Assist Test Logic
    //                                                                        ============
    /**
     * 自分だけが購入している商品を買ったことのある会員であるか調べる
     * @param productIdList 商品Idリスト(NotNull)
     * @return boolean
     */
    private boolean checkOnlyMePurchase(List<Integer> productIdList) {
        boolean isOnlyMePurchase = false;
        for (Integer productId : productIdList) {
            int recordCount = purchaseBhv.selectScalar(Integer.class).countDistinct(purchaseCB -> {
                purchaseCB.specify().columnMemberId();
                purchaseCB.query().setProductId_Equal(productId);
            });
            if (recordCount == 1) {
                isOnlyMePurchase = true;
            }
        }
        return isOnlyMePurchase;
    }
}
