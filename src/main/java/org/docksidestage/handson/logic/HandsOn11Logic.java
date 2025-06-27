package org.docksidestage.handson.logic;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.ServiceRankBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.dbflute.exentity.Product;
import org.docksidestage.handson.dbflute.exentity.ServiceRank;
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
    private ServiceRankBhv serviceRankBhv;

    // ===================================================================================
    //                                                                               Logic
    //                                                                        ============
    // done tanaryo javadoc, ぜひ戻り値にも (NotNull) を by jflute (2025/06/24)
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
        // done tanaryo こっちはテスト都合のloadなので、テスト側で実施しましょう by jflute (2025/06/24)
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
            // done tanaryo 要件的には最終ログイン日時が必須というわけではないので絞らなくてもOK by jflute (2025/06/24)
            // (最終ログイン日時がnullの会員がいても良いということで)
            cb.specify().derivedMemberLogin().max(loginCB -> {
                //あくまでここで取得するのは、特定カラム。上のexistないと、ここで取得するカラムはnullの場合あり
                loginCB.specify().columnLoginDatetime();
            }, Member.ALIAS_lastLoginDatetime);
            // TODO donetanaryo specify()上の方が嬉しい by jflute (2025/06/27)
            cb.query().setMemberName_LikeSearch(memberName, op -> op.likeContain());
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
            cb.specify().derivedMemberLogin().count(loginCB -> {
                // TODO done tanaryo specify()上の方が嬉しい by jflute (2025/06/27)
                loginCB.specify().columnMemberLoginId();
                loginCB.query().setMobileLoginFlg_Equal_True();
            }, Member.ALIAS_mobileLoginCount);//ログインしたことなければnull
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberServiceAsOne().withServiceRank();//one-to-oneはAsOneがつく
            // TODO done tanaryo Entity側もauthorを追加で by jflute (2025/06/27)
            if (completeOnly) {
                // done tanaryo [いいね] 確かに、existsも必要だね！ by jflute (2025/06/24)
                // done tanaryo subCBではなく、purchaseCB というようにテーブル名キーワードを入れて欲しい by jflute (2025/06/24)
                // (subCBはJava6版の慣習で、Java8版から非推奨、10年経った...けどドキュメント直ってないところあるかも)
                cb.query().existsPurchase(purchaseCB -> {});
                cb.query().notExistsPurchase(purchaseCB -> {
                    purchaseCB.query().setPaymentCompleteFlg_Equal_False();
                });
            }
        });

        // done tanaryo 修行++: これはこれでOKとして、loader方式のLoadReferrerの書き方もやってみましょう by jflute (2025/06/24)
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
        // done tanaryo loader方式でpulloutの方も混ぜることできます。 by jflute (2025/06/25)
        memberBhv.load(members, memberLoader -> {
            memberLoader.loadPurchase(purchaseCB -> {
                purchaseCB.query().queryProduct().addOrderBy_RegularPrice_Desc();
                purchaseCB.query().addOrderBy_PurchasePrice_Desc();
            }).withNestedReferrer(purchaseLoader -> {
                purchaseLoader.loadPurchasePayment(paymentCB -> {
                    paymentCB.query().setPaymentMethodCode_Equal_ByHand();
                });
            });
            memberLoader.pulloutMemberStatus().loadMemberLogin(status -> {});
        });

        if (logger.isDebugEnabled()) {
            members.forEach(member -> {
                debugFirstStepMember(member);
            });
        }
        return members;
    }

    /**
     * 会員ステータス、購入と商品と購入商品種類数(*1)を一緒に検索
     * 商品ステータスが "生産中止" の商品を買ったことのある会員...もしくは(続く)
     * (続き)手渡しだけでも払い過ぎてるのに未払いになっている購入を持ってる会員にフォローされている会員
     * 購入は商品ステータスの表示順の昇順、購入日時の降順で並べる
     * 会員ごとの購入一覧と商品名称、購入商品種類数をデバッグログに綺麗に出力する
     * *1: 購入商品種類数は、例えば、A, B, C という商品を買ったことがあるなら 3 (種類)
     *
     * @return 会員リスト(NotNull)
     */
    public List<Member> selectOnParadeSecondStepMember() {
        List<Member> members = memberBhv.selectList(cb -> {
            // done tanaryo select句に関するspecifyは、絞り込み条件(query)よりも前に定義でお願い by jflute (2025/06/27)
            // https://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.specify().derivedPurchase().countDistinct(purchaseCB -> {
                purchaseCB.specify().columnProductId();
            }, Member.ALIAS_productKindCount);
            cb.orScopeQuery(orCB -> {
                orCB.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().queryProduct().setProductStatusCode_Equal_生産中止();
                });
                orCB.query().existsMemberFollowingByYourMemberId(followingCB -> {
                    // TODO done tanaryo Yourで降りてYourで上がったら同じ人になっちゃう by jflute (2025/06/27)
                    // (現状だと、検索される会員自体が払いすぎ購入をもっていて、かつ、誰かしらからフォローされている)
                    followingCB.query().queryMemberByMyMemberId().existsPurchase(purchaseCB -> {
                        purchaseCB.query().setPaymentCompleteFlg_Equal_False();
                        // done tanaryo "手渡しだけでも払い過ぎ" ですが、分割支払いできるので手渡しが複数ありえる by jflute (2025/06/27)
                        purchaseCB.columnQuery(colCB -> {
                            colCB.specify().derivedPurchasePayment().sum(paymentCB -> {
                                paymentCB.specify().columnPaymentAmount();
                                paymentCB.query().setPaymentMethodCode_Equal_ByHand();
                            }, null, op -> op.coalesce(0));
                        }).greaterThan(colCB -> {
                            colCB.specify().columnPurchasePrice();
                        });
                    });
                });
            });
        });

        memberBhv.load(members, memberLoader -> {
            memberLoader.loadPurchase(purchaseCB -> {
                purchaseCB.setupSelect_Product();
                purchaseCB.query().queryProduct().queryProductStatus().addOrderBy_DisplayOrder_Asc();
                purchaseCB.query().addOrderBy_PurchaseDatetime_Desc();
            });
        });

        if (logger.isDebugEnabled()) {
            members.forEach(member -> {
                debugSecondStepMember(member);
            });
        }

        return members;
    }

    /**
     * 正式会員のときにログインした最終ログイン日時とログイン回数を導出して会員を検索
     * さらに、支払済み購入の最大購入価格を導出して取得
     * もっとさらに、購入と商品と商品ステータスと商品カテゴリと親商品カテゴリ(*1)も取得
     * もっともっとさらに、会員ログイン情報も取得
     * 正式会員のときにログインした最終ログイン日時の降順、会員IDの昇順で並べる
     * ログイン回数が指定された回数以上で絞り込み
     * 仮会員のときにログインをしたことのある会員を検索
     * 自分だけが購入している商品を買ったことのある会員を検索
     * 購入は商品カテゴリ(*1)の親カテゴリ名称の昇順、子カテゴリ名称の昇順、購入日時の降順
     * 会員ログイン情報はログイン日時の降順
     * *1: 商品カテゴリは、二階層になっていることが前提として
     *
     * @param leastLoginCount ログイン回数(NotNull)
     * @return 会員リスト(NotNull)
     */
    public List<Member> selectOnParadeXStepMember(int leastLoginCount) {
        List<Member> members = memberBhv.selectList(cb -> {
            cb.specify().derivedMemberLogin().max(loginCB -> {
                loginCB.specify().columnLoginDatetime();
                // TODO done tanaryo login自体がコードを持っているので、queryMemberStatus()まで行かなくてOK by jflute (2025/06/27)
                loginCB.query().setLoginMemberStatusCode_Equal_正式会員();
            }, Member.ALIAS_lastLoginDatetime);

            cb.specify().derivedMemberLogin().count(loginCB -> {
                loginCB.specify().columnMemberLoginId();
                loginCB.query().setLoginMemberStatusCode_Equal_正式会員();
            }, Member.ALIAS_fmlLoginCount);

            cb.specify().derivedPurchase().max(purchaseCB -> {
                purchaseCB.specify().columnPurchasePrice();
                purchaseCB.query().setPaymentCompleteFlg_Equal_True();
            }, Member.ALIAS_payedMaxPurchasePrice);

            // TODO done tanaryo ここは count() なので、nullにならないから coalesce() なくてもいい by jflute (2025/06/27)
            cb.query().derivedMemberLogin().count(loginCB -> {
                loginCB.specify().columnMemberLoginId();
            }).greaterEqual(leastLoginCount);

            cb.query().existsMemberLogin(loginCB -> {
                loginCB.query().queryMemberStatus().setMemberStatusCode_Equal_仮会員();
            });

            // TODO done tanaryo [いいね] 絞り込み論理は合ってます。素晴らしい。 by jflute (2025/06/27)
            // TODO done tanaryo [ひんと] 絞り込み論理は二種類あります。その片方は自分で導き出すことができています。 by jflute (2025/06/27)
            // ただ、いま導いてもらった絞り込み論理を実装しようとすると、ちょっとトリッキーな技を知らないとできない。
            // (逆に言うと、もう一つのまだ見ぬ絞り込み論理は、実装はもう片方ほどトリッキーではない)
            cb.query().existsPurchase(purchaseCB -> {
                // TODO done tanaryo existsPurchase()のpurchaseCBは絞り込み専用なので、specify()は呼べない by jflute (2025/06/27)
                // cbのspecify()だと、全体の検索のカラム指定をするspecify()なので、関係ない。
                // あくまで、ここのcolCBに紐づけてあげないといけない。
                purchaseCB.query().queryProduct().derivedPurchase().countDistinct(pchCB -> {
                    pchCB.specify().columnMemberId();
                }).equal(1);
            });

            cb.query().addSpecifiedDerivedOrderBy_Desc(Member.ALIAS_lastLoginDatetime);
            cb.query().addOrderBy_MemberId_Asc();
        });
        memberBhv.load(members, memberLoader -> {
            memberLoader.loadPurchase(purchaseCB -> {
                purchaseCB.setupSelect_Product().withProductStatus();
                purchaseCB.setupSelect_Product().withProductCategory().withProductCategorySelf();
                purchaseCB.query().queryProduct().queryProductCategory().queryProductCategorySelf().addOrderBy_ProductCategoryName_Asc();
                purchaseCB.query().queryProduct().queryProductCategory().addOrderBy_ProductCategoryName_Asc();
                purchaseCB.query().addOrderBy_PurchaseDatetime_Desc();
            });

            memberLoader.loadMemberLogin(loginCB -> {
                loginCB.query().addOrderBy_LoginDatetime_Desc();
            });

        });
        return members;
    }

    /**
     * サービスランクごとの会員数、合計購入価格、平均最大購入価格(*1)、ログイン数を検索
     *      紐付く会員とその会員に紐付く購入と会員ログインも取得する
     *     会員数の多い順に並べる
     * *1: 会員ごとの最大購入価格のサービスランクごとの平均 (nullにならないようにすること)
     *
     * @return サービスランクリスト(NotNull)
     */
    public List<ServiceRank> selectServiceRankSummary() {
        List<ServiceRank> serviceRanks = serviceRankBhv.selectList(cb -> {
            cb.specify().derivedMemberService().count(serviceCB -> {
                serviceCB.specify().columnMemberServiceId();
            }, ServiceRank.ALIAS_memberCount);

            cb.specify().derivedMemberService().sum(serviceCB -> {//各会員の合計
                serviceCB.specify().specifyMember().derivedPurchase().sum(purchaseCB -> {//1人の会員の合計購入価格
                    purchaseCB.specify().columnPurchasePrice();
                }, null);
            }, ServiceRank.ALIAS_totalPurchasePrice);

            cb.specify().derivedMemberService().avg(serviceCB -> {//各会員の平均
                serviceCB.specify().specifyMember().derivedPurchase().max(purchaseCB -> {//1人の会員の最大購入価格
                    purchaseCB.specify().columnPurchasePrice();
                }, null);
            }, ServiceRank.ALIAS_avgMaxPurchasePrice);

            cb.specify().derivedMemberService().sum(serviceCB -> {//各会員の合計
                serviceCB.specify().specifyMember().derivedMemberLogin().count(purchaseCB -> {//1人の会員のログイン数
                    purchaseCB.specify().columnMemberLoginId();
                }, null);
            }, ServiceRank.ALIAS_totalLoginCount);

            cb.query().addSpecifiedDerivedOrderBy_Desc(ServiceRank.ALIAS_memberCount);
        });

        serviceRankBhv.load(serviceRanks, serviceRankLoader -> {
            serviceRankLoader.loadMemberService(serviceCB -> {
                serviceCB.setupSelect_Member();
            }).withNestedReferrer(serviceLoader -> {
                serviceLoader.pulloutMember().loadPurchase(purchaseCB -> {});
                serviceLoader.pulloutMember().loadMemberLogin(loginCB -> {});
            });
        });
        return serviceRanks;
    }

    /**
     * それぞれの会員の平均購入価格の会員全体での最大値を検索
     *
     * @return Integer
     */
    public Integer selectMaxAvgPurchasePrice() {
        return memberBhv.selectScalar(Integer.class).max(memberCB -> {
            memberCB.specify().derivedPurchase().avg(purchaseCB -> {
                purchaseCB.specify().columnPurchasePrice();
            }, null);
        }).orElseThrow();
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
     * @param member 会員(NotNull)
     */
    private void debugFirstStepMember(Member member) {
        MemberStatus status = member.getMemberStatus().orElseThrow();
        int loginCount = status.getMemberLoginList().size();
        String purchaseList = member.getPurchaseList().toString();
        // done tanaryo ちょこちょこ空白のフォーマットが不統一 by jflute (2025/06/24)
        // フォーマッターをかけていなかったですね。。by tanayro (2025/06/24))
        String purchasePaymentList = member.getPurchaseList()
                .stream()
                .flatMap(op -> op.getPurchasePaymentList().stream())
                .collect(Collectors.toList())
                .toString();
        logger.debug("ログイン回数={}, 購入リスト={}, 購入支払い一覧={}", loginCount, purchaseList, purchasePaymentList);
    }

    /**
     * 会員ごとの購入一覧と商品名称、購入商品種類数をデバッグログに綺麗に出力する
     *
     * @param member 会員(NotNull)
     */
    private void debugSecondStepMember(Member member) {
        String purchaseList = member.getPurchaseList().toString();
        String productNameList = member.getPurchaseList()
                .stream()
                .flatMap(op -> op.getProduct().stream())
                .map(Product::getProductName)
                .collect(Collectors.toList())
                .toString();
        int count = member.getProductKindCount();
        logger.debug("購入一覧={}, 商品名称={}, 商品購入数={}", purchaseList, productNameList, count);
    }
}
