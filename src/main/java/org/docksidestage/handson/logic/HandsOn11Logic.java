package org.docksidestage.handson.logic;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberStatusBhv;
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
    private MemberStatusBhv memberStatusBhv;

    // ===================================================================================
    //                                                                               Logic
    //                                                                        ============
    // TODO done tanaryo javadoc, ぜひ戻り値にも (NotNull) を by jflute (2025/06/24)
    /**
     * 指定された memberName を含んでいる会員名称の会員を検索する
     * その会員に紐づく支払済み購入のデータも取得する
     * @param memberName 指定されたキーワード(NotNull)
     * @return 会員リスト(NotNull)
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
     * @return 会員リスト(NotNull)
     */
    public List<Member> selectUnpaidMemberList(String memberName) {
        assertNotNull(memberName);

        return memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberName, op -> op.likeContain());
            cb.query().existsPurchase(subCB -> {
                subCB.query().setPaymentCompleteFlg_Equal_False();
            });//存在する購入が全て未払いというわけではない
        });
        // TODO done tanaryo こっちはテスト都合のloadなので、テスト側で実施しましょう by jflute (2025/06/24)
    }

    /**
     * 会員と最終ログイン日時を(一緒に)検索する
     * 指定された memberName で含んでいる会員名称の会員を検索する
     *
     * @param memberName 指定されたキーワード(NotNull)
     * @return 会員リスト(NotNull)
     */
    List<Member> selectLoggedInMemberList(String memberName) {
        assertNotNull(memberName);

        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(memberName, op -> op.likeContain());
            // TODO done tanaryo 要件的には最終ログイン日時が必須というわけではないので絞らなくてもOK by jflute (2025/06/24)
            // (最終ログイン日時がnullの会員がいても良いということで)
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
     * @return 会員リスト(NotNull)
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
                // TODO done tanaryo [いいね] 確かに、existsも必要だね！ by jflute (2025/06/24)
                // TODO done tanaryo subCBではなく、purchaseCB というようにテーブル名キーワードを入れて欲しい by jflute (2025/06/24)
                // (subCBはJava6版の慣習で、Java8版から非推奨、10年経った...けどドキュメント直ってないところあるかも)
                cb.query().existsPurchase(purchaseCB -> {});
                cb.query().notExistsPurchase(purchaseCB -> {
                    purchaseCB.query().setPaymentCompleteFlg_Equal_False();
                });
            }
        });

        // TODO done tanaryo 修行++: これはこれでOKとして、loader方式のLoadReferrerの書き方もやってみましょう by jflute (2025/06/24)
        // (↓は思い出コメントアウトして残しておくとして)
        // Loader方式の活用するので、こっちはコメントアウト
        //        memberBhv.loadPurchase(members, purchaseCB -> {
        //            purchaseCB.query().queryProduct().addOrderBy_RegularPrice_Desc();
        //            purchaseCB.query().addOrderBy_PurchasePrice_Desc();
        //        }).withNestedReferrer(purchases -> {
        //            purchaseBhv.loadPurchasePayment(purchases, paymentCB -> {
        //                paymentCB.query().setPaymentMethodCode_Equal_ByHand();
        //            });
        //        });

        // こっちのほうが他のテーブルも辿れて拡張性の観点で便利？
        memberBhv.load(members, memberLoader -> {
            memberLoader.loadPurchase(purchaseCB -> {
                purchaseCB.query().queryProduct().addOrderBy_RegularPrice_Desc();
                purchaseCB.query().addOrderBy_PurchasePrice_Desc();
            }).withNestedReferrer(purchaseLoader -> {
                purchaseLoader.loadPurchasePayment(paymentCB -> {
                    paymentCB.query().setPaymentMethodCode_Equal_ByHand();
                });
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
        // TODO done tanaryo ちょこちょこ空白のフォーマットが不統一 by jflute (2025/06/24)
        // フォーマッターをかけていなかったですね。。by tanayro (2025/06/24))
        String purchasePaymentList = member.getPurchaseList()
                .stream()
                .flatMap(op -> op.getPurchasePaymentList().stream())
                .collect(Collectors.toList())
                .toString();
        logger.debug("ログイン回数={}, 購入リスト={}, 購入支払い一覧={}", loginCount, purchaseList, purchasePaymentList);
    }
}
