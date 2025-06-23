package org.docksidestage.handson.logic;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberStatusBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchasePaymentBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanaryo
 */
public class HandsOn11Logic {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(HandsOn11Logic.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private PurchaseBhv purchaseBhv;
    @Resource
    private PurchasePaymentBhv purchasePaymentBhv;
    @Resource
    private MemberStatusBhv memberStatusBhv;

    // ===================================================================================
    //                                                                               Logic
    //                                                                        ============

    /**
     * 指定された memberName を含んでいる会員名称の会員を検索する
     * その会員に紐づく支払済み購入のデータも取得する
     * @param memberName 指定されたキーワード(NotNull)
     * @return 会員リスト
     */
    public List<Member> selectPurchaseMemberList(String memberName) {
        assertNotNull(memberName);
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberName, op -> op.likeContain());
        });
        memberBhv.loadPurchase(members, purchaseCB -> {
            purchaseCB.query().setPaymentCompleteFlg_Equal_True();
        });
        return members;
        // memo by tanaryo
        // 指定された memberName を含んでいる会員を取得し、支払い済みの購入があれば、それも取得するという前提
        // 支払い済みの購入をもつ会員というわけではない
    }

    /**
     * 未払い購入のある会員を検索する
     * 指定された memberName で含んでいる会員名称の会員を検索する
     * @param memberName 指定されたキーワード(NotNull)
     * @return 会員リスト
     */
    public List<Member> selectUnpaidMemberList(String memberName) {
        assertNotNull(memberName);

        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberName, op -> op.likeContain());
            cb.query().existsPurchase(subCB -> {
                subCB.query().setPaymentCompleteFlg_Equal_False();
            });//存在する購入が全て未払いというわけではない
        });

        memberBhv.loadPurchase(members, purchaseCB -> {});//ただ子テーブルを取得

        return members;
    }

    /**
     * 会員と最終ログイン日時を(一緒に)検索する
     * 指定された memberName で含んでいる会員名称の会員を検索する
     *
     * @param memberName 指定されたキーワード(NotNull)
     * @return 会員リスト
     */
    List<Member> selectLoggedInMemberList(String memberName) {
        assertNotNull(memberName);

        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberName, op -> op.likeContain());
            cb.query().existsMemberLogin(subCB -> {});//MemberLoginテーブルを最低でも1つ持っている
            cb.specify().derivedMemberLogin().max(loginCB -> {
                //あくまでここで取得するのは、特定カラム。上のexistないと、ここで取得するカラムはnullの場合あり
                loginCB.specify().columnLoginDatetime();
            }, Member.ALIAS_lastLoginDatetime);
        });

        memberBhv.loadMemberLogin(members, loginCB -> {}); //テストするために関連テーブル取得している

        return members;
    }

    /**
     * 会員ステータス、会員サービス、サービスランク、購入、購入支払、会員ステータス経由の会員ログインも取得
     * (基点テーブルごとの)モバイルからのログイン回数も導出して取得する
     * 指定された判定次第で支払済み購入しか存在しない会員だけを対象にできるように
     * 購入は商品の定価の高い順、購入価格の高い順で並べる
     * 会員ごとの方のログイン回数と購入一覧と購入支払一覧をデバッグログに綺麗に出力する
     * 購入支払は、最も推奨されている方法のみ検索
     *
     * @param completeOnly 支払い済み購入に絞るかどうか
     * @return 会員リスト
     */
    public List<Member> selectOnParadeFirstStepMember(boolean completeOnly) {
        List<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberServiceAsOne().withServiceRank();//one-to-oneはAsOneがつく
            cb.specify().derivedMemberLogin().count(loginCB -> {
                loginCB.query().setMobileLoginFlg_Equal_True();
                loginCB.specify().columnMemberLoginId();
            }, Member.ALIAS_mobileLoginCount);//ログインしたことなければnull
            if (completeOnly) {
                cb.query().existsPurchase(subCB -> {});
                cb.query().notExistsPurchase(subCB -> {
                    subCB.query().setPaymentCompleteFlg_Equal_False();
                });
            }
        });

        memberBhv.loadPurchase(members, purchaseCB -> {
            purchaseCB.query().queryProduct().addOrderBy_RegularPrice_Desc();
            purchaseCB.query().addOrderBy_PurchasePrice_Desc();
        }).withNestedReferrer(purchases -> {
            purchaseBhv.loadPurchasePayment(purchases, paymentCB -> {
                paymentCB.query().setPaymentMethodCode_Equal_ByHand();
            });
        });

        List<MemberStatus> statusList = memberBhv.pulloutMemberStatus(members);
        memberStatusBhv.loadMemberLogin(statusList, loginCB -> {});

        if (logger.isDebugEnabled()) {
            members.forEach(member -> {
                debugMember(member);
            });
        }
        return members;
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
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

    /**
     * 会員のログイン回数と購入一覧と購入支払一覧をデバッグログに出力する
     *
     * @param member 会員クラス(NotNull)
     */
    private void debugMember(Member member) {
        MemberStatus status = member.getMemberStatus().orElseThrow();
        int loginCount = status.getMemberLoginList().size();
        String purchaseList = member.getPurchaseList().toString();
        String purchasePaymentList =member.getPurchaseList().stream().flatMap(op ->op.getPurchasePaymentList().stream()).collect(
                Collectors.toList()).toString();
        logger.debug("ログイン回数={}, 購入リスト={}, 購入支払い一覧={}", loginCount, purchaseList, purchasePaymentList);
    }
}
