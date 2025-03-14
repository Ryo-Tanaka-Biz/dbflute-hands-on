package org.docksidestage.handson.exercise;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberStatusBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// done tanaryo タグコメント、こちらでもぜひ by jflute (2025/03/03)

// [1on1でのふぉろー] クラスアーキテクチャのジレンマ話をした。
// o 汎用アーキテクチャは、現場の必要な概念がすべて揃ってるとは限らない
// o 汎用アーキテクチャは、あくまでサンプルであって当てはめるものではない
// o 現地化ロジックの話に派生、現場フィットレイヤ
// o DBFluteの区分値の機能は、かなり現場フィットレイヤだけどDBFluteがおせっかい
//
// o ドメイン的な検索条件オブジェクトの作り方の妄想

// TODO tanaryo [読み物課題] フレームワーク選び、現場フィットレイヤを忘れずに by jflute (2025/03/14)
// https://jflute.hatenadiary.jp/entry/20161214/genbafitlayer

/**
 * @author tanaryo
 */
public class HandsOn04Test extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;

    @Resource
    private MemberStatusBhv memberStatusBhv;

    @Resource
    private PurchaseBhv purchaseBhv;

    // ===================================================================================
    //                                                                    Betabeta Stretch
    //                                                                        ============
    /**
     * 退会会員の未払い購入を検索
     * 退会会員のステータスコードは "WDL"。ひとまずベタで
     * 支払完了フラグは "0" で未払い。ひとまずベタで
     * 購入日時の降順で並べる
     * 会員名称と商品名と一緒にログに出力
     * 購入が未払いであることをアサート
     */
    public void test_1() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Product();
            //cb.query().queryMember().queryMemberStatus().setMemberStatusCode_Equal("WDL");
            cb.query().queryMember().queryMemberStatus().setMemberStatusCode_Equal_退会会員();
            //cb.query().setPaymentCompleteFlg_Equal(0);
            cb.query().setPaymentCompleteFlg_Equal_False();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(purchase -> {
            String memberName = purchase.getMember().get().getMemberName();
            String productName = purchase.getProduct().get().getProductName();
            log(memberName + " : " + productName);
            assertTrue(purchase.isPaymentCompleteFlgFalse());
        });
    }

    /**
     * 会員退会情報も取得して会員を検索
     * 退会会員でない会員は、会員退会情報を持っていないことをアサート
     * 退会会員のステータスコードは "WDL"。ひとまずベタで
     * 不意のバグや不意のデータ不備でもテストが(できるだけ)成り立つこと
     */
    public void test_2() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
        });

        // ## Assert ##
        // done tanaryo 万が一、setupSelectし忘れたら？ (assertが曖昧に、意味的に素通りしてる) by jflute (2025/02/21)
        // done tanaryo 万が一、退会会員が一人もいなくてsetupSelectし忘れたら？ by jflute (2025/03/03)
        assertHasAnyElement(memberList);
        boolean hasWithdrawnMember = false;
        boolean validSetupSelectMemberWithdrawal = false;
        for (Member member : memberList) {
            // done tanaryo すべての会員で退会情報があるって言い切っちゃってる by jflute (2025/03/07)
            // (元々はelseに入ってて、このアサートが一回以上動いたかどうかを保証したかった)
            if (!member.isMemberStatusCode退会会員()) {
                // 不意のバグや不意のデータ不備でもテストが(できるだけ)成り立つこと
                // done tanaryo 万が一、テストデータに退会会員でない会員がいなかったら？素通りしちゃう by jflute (2025/02/13)
                hasWithdrawnMember = true;
                log(member.getMemberName(), member.getMemberStatusCode());
                assertTrue(member.getMemberWithdrawalAsOne().isEmpty()); // ここがやりたいアサート
            } else {
                validSetupSelectMemberWithdrawal = true;
                assertTrue(member.getMemberWithdrawalAsOne().isPresent());
            }
        }

        if (!hasWithdrawnMember) {
            fail("テストデータに退会会員が含まれていないため、退会会員のテストが実施されていません");
        }
        if (!validSetupSelectMemberWithdrawal) {
            fail("会員退会情報テーブルを参照できていません");
        }
        // [1on1でのふぉろー] 保証するって本来すごく大変なこと、を知ること。
    }

    // ===================================================================================
    //                                                              Classification Stretch
    //                                                                        ============
    /**
     * 一番若い仮会員の会員を検索
     * 区分値メソッドの JavaDoc コメントを確認する
     * 会員ステータス名称も取得する(ログに出力)
     * 会員が仮会員であることをアサート
     */
    public void test_3() {
        // ## Arrange ##
        // ## Act ##
        // [1on1でのふぉろー] selectList() である理由は？ by jflute
        // => 同じ日付の人がいた場合に複数 by tanaryo
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            //            /**
            //             * Equal(=). As 仮会員 (PRV). And OnlyOnceRegistered. <br>
            //             * 仮会員: 入会直後のステータスで一部のサイトサービスが利用可能
            //             */
            cb.setupSelect_MemberStatus();
            // done tanaryo 万が一、正式会員で同じ生年月日を持っている人がいたら？ by jflute (2025/02/13)
            //複数レコード取得することになる。そのためselectEntityではなく、selectListを使う。　by. tanaryo (2025/02/17)
            // done tanaryo 複数件ヒットするのはその通りだけど、要件は仮会員のみなので、正式会員を取ってはいけない by jflute (2025/02/21)
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.query().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setMemberStatusCode_Equal_仮会員();
            });
            // [1on1でのふぉろー] ScalarConditionの現場の話
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            log(member.getMemberStatus().get().getMemberStatusName());
            assertTrue(member.isMemberStatusCode仮会員());
        });
    }

    /**
     * 支払済みの購入の中で一番若い正式会員のものだけ検索
     * 会員ステータス名称も取得する(ログに出力)
     * 購入日時の降順で並べる。
     * 購入の紐づいている会員が正式会員であることをアサート
     */
    public void test_4() {
        // ## Arrange ##
        // ## Act ##
        //        OptionalScalar<LocalDate> maxBirthDate = purchaseBhv.selectScalar(LocalDate.class).max(cb -> {
        //            cb.specify().specifyMember().columnBirthdate();
        //            cb.query().setPaymentCompleteFlg_Equal_True();
        //            cb.query().queryMember().setMemberStatusCode_Equal_正式会員();
        //        });

        //        OptionalScalar<LocalDate> maxBirthDate = memberBhv.selectScalar(LocalDate.class).max(cb -> {
        //            cb.specify().columnBirthdate();
        //            cb.query().existsPurchase(purchaseCB -> {
        //                purchaseCB.query().setPaymentCompleteFlg_Equal_True();
        //            });
        //            cb.query().setMemberStatusCode_Equal_正式会員();
        //        });
        //        log(maxBirthDate.get());

        //
        //        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
        //            cb.setupSelect_Member();
        //            cb.query().setPaymentCompleteFlg_Equal_True();
        //            cb.query().queryMember().setMemberStatusCode_Equal_正式会員();
        //            cb.query().queryMember().setBirthdate_Equal(maxBirthDate.get());
        //            cb.query().addOrderBy_PurchaseDatetime_Desc();
        //        });

        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            // done tanaryo 外側の条件、一個足りない by jflute (2025/02/13)
            cb.query().queryMember().setMemberStatusCode_Equal_正式会員();
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().queryMember().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setMemberStatusCode_Equal_正式会員();
                // done tanaryo [いいね] これを導き出したのは素晴らしい by jflute (2025/02/13)
                // [1on1でのふぉろー] ExistsReferrer は、DBFluteの力作。
                // あと、ログのSQLのフォーマット、どんだけやってもインデントしっかりも重要。
                memberCB.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().setPaymentCompleteFlg_Equal_True();
                });
            });
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        purchaseList.forEach(purchase -> {
            Member member = purchase.getMember().get();
            log(member.getMemberStatusCode());
            assertTrue(member.isMemberStatusCode正式会員());
        });
    }

    /**
     * 生産販売可能な商品の購入を検索
     * 商品ステータス名称、退会理由テキスト (退会理由テーブル) も取得する(ログに出力) ※1
     * 購入価格の降順で並べる
     *     購入の紐づいている商品が生産販売可能であることをアサート
     */
    public void test_5() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Member().withMemberWithdrawalAsOne().withWithdrawalReason();
            cb.query().queryProduct().setProductStatusCode_Equal_生産販売可能();
            cb.query().addOrderBy_PurchasePrice_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(purchase -> {
            ProductStatus productStatus = purchase.getProduct().get().getProductStatus().get();
            OptionalEntity<MemberWithdrawal> optMemberWithdrawal = purchase.getMember().get().getMemberWithdrawalAsOne();
            // done tanaryo "退会理由入力テキスト" ではなく "退会理由テキスト (退会理由テーブル) " です by jflute (2025/02/13)
            // OptionalEntity<>からOptionalEntity<>への変換をflatmapで行う。
            // get()をするとnullを許容できないwithdrawalReasonクラスになってしまうので、optionalの状態でmapを使って変換する
            // [1on1でのふぉろー] 一文字でも違ったら、本当にこのカラムかな？って疑うの大事。
            // done tanaryo Lambda式の変数はわりと短めにする慣習があるので、せめて withdrawalReason は reason でもいいかなと by jflute (2025/02/21)
            // もうちょい踏み込んで、withdrawal->wdl でも悪くはない。ただ、個人的には w はやらない。 
            String reason = optMemberWithdrawal.flatMap(wdl -> wdl.getWithdrawalReason())
                    .map(wdlReason -> wdlReason.getWithdrawalReasonText())
                    .orElse("none");
            log(productStatus.getProductStatusName(), reason);
            assertTrue(productStatus.isProductStatusCode生産販売可能());//productテーブルでもいける
        });
    }
    //ここまでやった（by tanaryo 2025/02/03）
    //done tanaryo セクション3のページング忘れてたので次回以降やる

    /**
     * 正式会員と退会会員の会員を検索
     *         会員ステータスの表示順で並べる
     * 会員が正式会員と退会会員であることをアサート
     * 両方とも存在していることをアサート
     *         (検索されたデータに対して)Entity上だけで正式会員を退会会員に変更する
     * 変更した後、Entityが退会会員に変更されていることをアサート
     * 変更した後、データベース上は退会会員に変更されて "いない" ことをアサート ※1
     */
    public void test_6() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // done tanaryo 修行++: orScopeQuery()を使わずにやってみましょう by jflute (2025/02/13)
            // (同じカラムに対してEqualで複数指定するなら...違うのできるよね)
            //            cb.orScopeQuery(orCB -> {
            //                orCB.query().setMemberStatusCode_Equal_正式会員();
            //                orCB.query().setMemberStatusCode_Equal_退会会員();
            //            });
            // [1on1でのふぉろー] もしフィットするんであれば限定的な機能の方を使うほうが、パフォーマンスが早くなる可能性が高い。
            cb.query().setMemberStatusCode_InScope_AsMemberStatus(Arrays.asList(CDef.MemberStatus.正式会員, CDef.MemberStatus.退会会員));

            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
        });

        // ## Assert ##
        //
        // [1on1でのふぉろー] DBFluteは、Entityに値をsetするだけではDBには反映されない。
        // Entityはある意味ただの入れ物クラスで、DBとの交信はあくまでBehavior。
        // 他のO/Rマッパーとかだと、Entityに値をsetするだけで更新されるものもあるけど、DBFluteは違う。
        // (あえて、それをやらないようにしている)
        //
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            assertTrue(member.isMemberStatusCode正式会員() || member.isMemberStatusCode退会会員());
        });//この時点で正式会員または退会会員のいずれかであることを保証
        // done tanaryo 修行++: Stringのcodeは使わずCDefで扱ってみましょう by jflute (2025/02/13)
        // 基本的にStringのcodeで扱う場面はほとんどないと思ってよくて、enumがその抽象化された代わりのオブジェクトである。
        // done tanaryo CDefになったから、変数名 statusCodeList じゃなくて statusList でいいかなと by jflute (2025/02/21)
        List<CDef.MemberStatus> statusList =
                memberList.stream().map(op -> op.getMemberStatusCodeAsMemberStatus()).collect(Collectors.toList());
        boolean containsFML = statusList.contains(CDef.MemberStatus.正式会員);
        boolean containsWDL = statusList.contains(CDef.MemberStatus.退会会員);
        // done tanaryo ここも分けたほうがいい by jflute (2025/02/13)
        assertTrue(containsFML);
        assertTrue(containsWDL);

        // done tanaryo もうちょいこのへんコメントがあるといいかなと by jflute (2025/02/13)
        // [1on1でのふぉろー] DBFluteのEntityの挙動と他のO/Rマッパーでの話

        //正式会員を抽出
        List<Member> memberFMLList = memberList.stream().filter(cb -> cb.isMemberStatusCode正式会員()).collect(Collectors.toList());

        //Entityの会員ステータスを退会会員に変更
        memberFMLList.forEach(member -> {
            member.setMemberStatusCode_退会会員();
        });

        //Entity上は退会会員であることをassert
        memberFMLList.forEach(member -> {
            assertTrue(member.isMemberStatusCode退会会員());
        });

        //DB上でmemberFMLListを検索
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberId_InScope(memberBhv.extractMemberIdList(memberFMLList));
        });

        //DB上では正式会員であることをassert
        members.forEach(member -> {
            assertTrue(member.isMemberStatusCode正式会員());
        });
    }

    /**
     * 銀行振込で購入を支払ったことのある、会員ステータスごとに一番若い会員を検索
     * 正式会員で一番若い、仮会員で一番若い、という風にそれぞれのステータスで若い会員を検索
     * 一回の ConditionBean による検索で会員たちを検索すること (PartitionBy...)
     * ログのSQLを見て、検索が妥当であることを目視で確認すること
     *         検索結果が想定されるステータスの件数以上であることをアサート
     * ひとまず動作する実装ができたら、ArrangeQueryを活用してみましょう
     */
    public void test_7() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // done tanaryo MemberCQにもauthorをぜひお願い by jflute (2025/02/13)
            cb.query().arrangeYoungestNiceMember();
        });
        // done jflute 次回1on1で、ArrangeQueryの意義とコンセプトをもっと話す (2025/02/21)
        //where dfloc.BIRTHDATE = (select max(sub1loc.BIRTHDATE)
        //                            from member sub1loc
        //                           where sub1loc.MEMBER_STATUS_CODE = dfloc.MEMBER_STATUS_CODE
        //                             and exists (select sub2loc.MEMBER_ID
        //                                           from purchase sub2loc
        //                                          where sub2loc.MEMBER_ID = sub1loc.MEMBER_ID
        //                                            and exists (select sub3loc.PURCHASE_ID
        //                                                          from purchase_payment sub3loc
        //                                                         where sub3loc.PURCHASE_ID = sub2loc.PURCHASE_ID
        //                                                           and sub3loc.PAYMENT_METHOD_CODE = 'BAK'
        //                                                )
        //                                 )
        //       )
        //
        // where句の再利用 (ArrangeQuery) | DBFlute
        // https://dbflute.seasar.org/ja/manual/function/genbafit/implfit/whererecycle/index.html

        // done tanaryo [読み物課題] ルーズなDaoパターンなら見たくない by jflute (2025/03/03)
        // https://jflute.hatenadiary.jp/entry/20160906/loosedao
        // リポジトリ層にあるDB検索のメソッドを大きい単位で再利用するといずれ肥大化して扱いづらくなる話 by tanaryo (2025/03/06)
        // また一般的なアーキテクチャのあるべきに加え、今いる現場におけるあるべきを考えて実装しよう by tanaryo (2025/03/06)

        // ## Assert ##
        assertHasAnyElement(memberList);
        // done tanaryo この3は導出してみましょう by jflute (2025/02/13)
        // done tanaryo すべてのステータスが会員テーブルに存在するわけではない by jflute (2025/02/21)
        // done tanaryo 修行++: MEMBERテーブルのMEMBER_STATUS_CODEの種類数を検索してみてください (同じ値になるはず) by jflute (2025/03/07)
        int minimumRecordCountByMemberStatus =
                memberStatusBhv.selectCount(cb -> cb.query().existsMember(mbCB -> {}));//会員テーブルに紐づく会員ステータスの種類数
        int minimumRecordCountByMember = memberBhv.selectScalar(Integer.class).countDistinct(cb -> {
            cb.specify().columnMemberStatusCode();
        });
        log("minimumRecordCountByMemberStatus:" + minimumRecordCountByMemberStatus);
        log("minimumRecordCountByMember:" + minimumRecordCountByMember);
        assertTrue(memberList.size() >= minimumRecordCountByMemberStatus);
        memberList.forEach(member -> {
            log(member.getMemberStatusCode(), member.getBirthdate());
        });
    }
    //ここまでやった（by tanaryo 2025/02/08）

    // ===================================================================================
    //                                                          Add Classification Stretch
    //                                                                        ============
    // [1on1でのふぉろー] 区分値の管理のおさらい
    // よもやま: LibreOffice
    // よもやま: IntelliJで、クリーンコンパイル (原因わかんないけど、targetの下を削除して再ビルドで直った)
    public void test_8() {
        memberBhv.selectList(cb -> {
            //            cb.query().queryMemberStatus().setMemberStatusCode_Equal_ハンズオン();
            //区分値の追加と削除のテスト。区分値追加時にはコンパイルが通るが削除後には通らなくなることを確認
        });
    }

    //暗黙の区分値チェック。ReplaceSchemaで以下のエラーが出た。なお戻すと成功。また新規追加のレコードはチェック対象外。 by tanaryo(2025/03/14)
    //    [df-replace-schema] 2025-03-14 16:43:12,559 INFO  - * * * * * * * * * * *
    //            [df-replace-schema] 2025-03-14 16:43:12,559 INFO  - *                   *
    //            [df-replace-schema] 2025-03-14 16:43:12,559 INFO  - * Load Data         *
    //            [df-replace-schema] 2025-03-14 16:43:12,559 INFO  - *                   *
    //            [df-replace-schema] 2025-03-14 16:43:12,559 INFO  - * * * * * * * * * * *
    //            [df-replace-schema] 2025-03-14 16:43:12,566 INFO  - /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    //            [df-replace-schema] 2025-03-14 16:43:12,566 INFO  - writeData(playsql/data/common/xls/10-master.xls)
    //[df-replace-schema] 2025-03-14 16:43:12,566 INFO  - = = = = = = =/
    //            [df-replace-schema] 2025-03-14 16:43:12,636 INFO  - ...Getting tables:
    //            [df-replace-schema] 2025-03-14 16:43:12,637 INFO  -   schema = {maihamadb.$$NoNameSchema$$ as main}
    //[df-replace-schema] 2025-03-14 16:43:12,637 INFO  -   types  = [TABLE, VIEW]
    //            [df-replace-schema] 2025-03-14 16:43:12,653 INFO  - MEMBER_STATUS:{PRV, 仮会員, 入会直後のステータスで一部のサイトサービスが利用可能, 3}
    //[df-replace-schema] 2025-03-14 16:43:12,662 INFO  - MEMBER_STATUS:{FML, 正式会員, 正式な会員としてサイトサービスが利用可能, 1}
    //[df-replace-schema] 2025-03-14 16:43:12,662 INFO  - MEMBER_STATUS:{WDLL, 退会会員, 退会が確定した会員でサイトサービスはダメ, 2}
    //[df-replace-schema] 2025-03-14 16:43:12,666 INFO  - ...Retrying by suppressing batch update: MEMBER_STATUS
    //[df-replace-schema] 2025-03-14 16:43:12,671 ERROR - Look! Read the message below.
    //            [df-replace-schema] /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //[df-replace-schema] Failed to execute DBFlute Task 'ReplaceSchema'.
    //[df-replace-schema]
    //[df-replace-schema] [Advice]
    //[df-replace-schema] Check the exception messages and the stack traces.
    //[df-replace-schema]
    //[df-replace-schema] [Database Product]
    //[df-replace-schema] MySQL 8.0.40
    //[df-replace-schema]
    //[df-replace-schema] [JDBC Driver]
    //[df-replace-schema] MySQL Connector Java mysql-connector-java-5.1.49 ( Revision: ad86f36e100e104cd926c6b81c8cab9565750116 ) for JDBC 4.0
    //[df-replace-schema] * * * * * * * * * */
    //            [df-replace-schema] org.dbflute.exception.DfXlsDataRegistrationFailureException: Look! Read the message below.
    //            [df-replace-schema] /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //[df-replace-schema] Failed to register the table data.
    //[df-replace-schema]
    //[df-replace-schema] [Advice]
    //[df-replace-schema] Please confirm the SQLException message.
    //[df-replace-schema]
    //[df-replace-schema] [Data Directory]
    //[df-replace-schema] playsql/data/common/xls
    //[df-replace-schema]
    //[df-replace-schema] [Xls File]
    //[df-replace-schema] 10-master.xls
    //[df-replace-schema]
    //[df-replace-schema] [Table]
    //[df-replace-schema] MEMBER_STATUS
    //[df-replace-schema]
    //[df-replace-schema] [SQLException]
    //[df-replace-schema] org.dbflute.exception.DfJDBCException
    //[df-replace-schema] JDBC said...
    //[df-replace-schema] /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //[df-replace-schema] [SQLException]
    //[df-replace-schema] java.sql.BatchUpdateException
    //[df-replace-schema] Data truncation: Data too long for column 'MEMBER_STATUS_CODE' at row 1
    //[df-replace-schema] - - - - - - - - - -/
    //[df-replace-schema]
    //[df-replace-schema] [Target Row]
    //[df-replace-schema] (derived from non-batch retry)
    //[df-replace-schema] com.mysql.jdbc.MysqlDataTruncation
    //[df-replace-schema] Data truncation: Data too long for column 'MEMBER_STATUS_CODE' at row 1
    //[df-replace-schema] /- - - - - - - - - - - - - - - - - - - -
    //[df-replace-schema] Column Def: [MEMBER_STATUS_CODE, MEMBER_STATUS_NAME, DESCRIPTION, DISPLAY_ORDER]
    //[df-replace-schema] Row Values: {WDLL, 退会会員, 退会が確定した会員でサイトサービスはダメ, 2}
    //[df-replace-schema] Row Number: 3
    //[df-replace-schema] - - - - - - - - - -/
    //[df-replace-schema]

    // ===================================================================================
    //                                                                            Grouping
    //                                                                        ============
    /**
     * サービスが利用できる会員を検索
     * グルーピングの設定によって生成されたメソッドを利用
     * 会員ステータスの表示順で並べる -> Asc?Desc?
     * 会員が "サービスが利用できる会員" であることをアサート
     */
    public void test_9() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_InScope_ServiceAvailable();
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.isMemberStatusCode_ServiceAvailable());
        });
    }
    // schemaHTMLにて、serviceAvailableの欄を確認
    // PaymentMethodにもrecommendedのgroupingあり

    // ===================================================================================
    //                                                                            Grouping
    //                                                                        ============
    /**
     * 未払い購入のある会員を検索
     * 未払いの購入か支払済みの購入かを簡単に切り替えられるようにする
     * それを判断するprivateメソッドを作成して、戻り値のtrue/falseで切り替える
     * とりあえず未払いの購入を求められているので、そのメソッドの戻り値はfalse固定で
     * 姉妹コードの設定によって生成されたメソッドを利用
     * 正式会員日時の降順(nullを後に並べる)、会員IDの昇順で並べる
     * 会員が未払いの購入を持っていることをアサート
     * Assertでの検索が一回になるようにしてみましょう (LoadReferrer)
     */
    public void test_10() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().existsPurchase(pcCB -> {
                pcCB.query().setPaymentCompleteFlg_Equal_AsFlg(getPaymentStatus());
            });
            cb.query().addOrderBy_FormalizedDatetime_Desc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(members);

        // memberのpurchaseテーブルを取得
        memberBhv.loadPurchase(members, purchaseCB -> {
            purchaseCB.query().setPaymentCompleteFlg_Equal_False();
        });

        members.forEach(member -> {
            assertFalse(member.getPurchaseList().isEmpty());
        });
        // getPurchaseListのjavadocには(NotNull: even if no loading, returns empty list)の記載。これはloadReferrer用か by tanaryo (2025/3/14)
    }
    //schemaHTMLでsistersを確認

    /**
     * 特定の支払い状況を取得します
     *
     * @return 支払い済みの場合はTrue、未払いの場合はFalse
     */
    private CDef.Flg getPaymentStatus() {
        return CDef.Flg.False;
    }

    // ===================================================================================
    //                                                                             SubItem
    //                                                                        ============
    /**
     * 会員ステータスの表示順カラムで会員を並べて検索
     * 会員ステータスの "表示順" カラムの昇順で並べる
     * 会員ステータスのデータ自体は要らない
     * その次には、会員の会員IDの降順で並べる
     * 会員ステータスのデータが取れていないことをアサート
     * 会員が会員ステータスの表示順ごとに並んでいることをアサート
     */
    public void test_11() {
        // ## Arrange ##
        Set<String> displayOrders = new HashSet<>();
        String lastDisplayOrder = null;

        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(members);
        for (Member member : members) {
            assertTrue(member.getMemberStatus().isEmpty());
            CDef.MemberStatus status = member.getMemberStatusCodeAsMemberStatus();
            String displayOrder = status.displayOrder();
            if (!displayOrder.equals(lastDisplayOrder)) {
                assertFalse(displayOrders.contains(displayOrder));
            }
            displayOrders.add(displayOrder);
            lastDisplayOrder = displayOrder;
        }
    }
    // schemaHTMLでsubItemを確認
    // 会員ステータスのデータがなくても、displayOrderに関する情報を区分値から取得できるのがメリット？ by tanaryo (2025/3/14)
}
