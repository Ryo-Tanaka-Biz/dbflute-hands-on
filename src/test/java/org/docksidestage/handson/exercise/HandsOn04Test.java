package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.dbflute.optional.OptionalScalar;
import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author tanaryo
 */
public class HandsOn04Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    @Resource
    private PurchaseBhv purchaseBhv;

    //    退会会員の未払い購入を検索
    //    退会会員のステータスコードは "WDL"。ひとまずベタで
    //    支払完了フラグは "0" で未払い。ひとまずベタで
    //            購入日時の降順で並べる
    //    会員名称と商品名と一緒にログに出力
    //            購入が未払いであることをアサート
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

    //    会員退会情報も取得して会員を検索
    //    退会会員でない会員は、会員退会情報を持っていないことをアサート
    //    退会会員のステータスコードは "WDL"。ひとまずベタで
    //    不意のバグや不意のデータ不備でもテストが(できるだけ)成り立つこと
    public void test_2() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
        	// 不意のバグや不意のデータ不備でもテストが(できるだけ)成り立つこと
        	// TODO tanaryo 万が一、テストデータに退会会員でない会員がいなかったら？素通りしちゃう by jflute (2025/02/13)
            if (!member.isMemberStatusCode退会会員()) {
                log(member.getMemberName(), member.getMemberStatusCode());
                assertTrue(member.getMemberWithdrawalAsOne().isEmpty());
            }
        });
    }

    //    一番若い仮会員の会員を検索
    //    区分値メソッドの JavaDoc コメントを確認する
    //    会員ステータス名称も取得する(ログに出力)
    //    会員が仮会員であることをアサート
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
            // TODO tanaryo 万が一、正式会員で同じ生年月日を持っている人がいたら？ by jflute (2025/02/13)
            cb.query().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setMemberStatusCode_Equal_仮会員();
            });
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            log(member.getMemberStatus().get().getMemberStatusName());
            assertTrue(member.isMemberStatusCode仮会員());
        });
    }

    //    支払済みの購入の中で一番若い正式会員のものだけ検索
    //    会員ステータス名称も取得する(ログに出力)
    //    購入日時の降順で並べる。
    //            購入の紐づいている会員が正式会員であることをアサート
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
            // TODO tanaryo 外側の条件、一個足りない by jflute (2025/02/13)
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().queryMember().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setMemberStatusCode_Equal_正式会員();
                // TODO tanaryo [いいね] これを導き出したのは素晴らしい by jflute (2025/02/13)
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

    //    生産販売可能な商品の購入を検索
    //    商品ステータス名称、退会理由テキスト (退会理由テーブル) も取得する(ログに出力) ※1
    //    購入価格の降順で並べる
    //            購入の紐づいている商品が生産販売可能であることをアサート
    public void test_5() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Member().withMemberWithdrawalAsOne();
            cb.query().queryProduct().setProductStatusCode_Equal_生産販売可能();
            cb.query().addOrderBy_PurchasePrice_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(purchase -> {
            ProductStatus productStatus = purchase.getProduct().get().getProductStatus().get();
            OptionalEntity<MemberWithdrawal> optMemberWithdrawal = purchase.getMember().get().getMemberWithdrawalAsOne();
            // TODO tanaryo "退会理由入力テキスト" ではなく "退会理由テキスト (退会理由テーブル) " です by jflute (2025/02/13)
            String reason = optMemberWithdrawal.map(op -> op.getWithdrawalReasonInputText()).orElse("none");//mapでいけた。flatmapでもいけるのか
            log(productStatus.getProductStatusName(), reason);
            assertTrue(productStatus.isProductStatusCode生産販売可能());//productテーブルでもいける
        });
    }
    //ここまでやった（by tanaryo 2025/02/03）
    //TODO tanaryo セクション3のページング忘れてたので次回以降やる

    //    正式会員と退会会員の会員を検索
    //            会員ステータスの表示順で並べる
    //    会員が正式会員と退会会員であることをアサート
    //    両方とも存在していることをアサート
    //            (検索されたデータに対して)Entity上だけで正式会員を退会会員に変更する
    //    変更した後、Entityが退会会員に変更されていることをアサート
    //    変更した後、データベース上は退会会員に変更されて "いない" ことをアサート ※1
    public void test_6() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
        	// TODO tanaryo 修行++: orScopeQuery()を使わずにやってみましょう by jflute (2025/02/13)
        	// (同じカラムに対してEqualで複数指定するなら...違うのできるよね)
            cb.orScopeQuery(orCB -> {
                orCB.query().setMemberStatusCode_Equal_正式会員();
                orCB.query().setMemberStatusCode_Equal_退会会員();
            });
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
        // TODO tanaryo 修行++: Stringのcodeは使わずCDefで扱ってみましょう by jflute (2025/02/13)
        // 基本的にStringのcodeで扱う場面はほとんどないと思ってよくて、enumがその抽象化された代わりのオブジェクトである。
        List<String> statusCodeList = memberList.stream().map(Member::getMemberStatusCode).collect(Collectors.toList());
        boolean containsFML = statusCodeList.contains(CDef.MemberStatus.正式会員.code());
        boolean containsWDL = statusCodeList.contains(CDef.MemberStatus.退会会員.code());
        // TODO tanaryo ここも分けたほうがいい by jflute (2025/02/13)
        assertTrue(containsFML && containsWDL);

        // TODO tanaryo もうちょいこのへんコメントがあるといいかなと by jflute (2025/02/13)

        List<Member> memberFMLList =
                memberList.stream().filter(cb -> cb.isMemberStatusCode正式会員()).collect(Collectors.toList());

        memberFMLList.forEach(member -> {
            member.setMemberStatusCode_退会会員();
        });

        memberFMLList.forEach(member -> {
            assertTrue(member.isMemberStatusCode退会会員());
        });

        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberId_InScope(memberFMLList.stream().map(Member::getMemberId).collect(Collectors.toList()));
        });

        members.forEach(member -> {
            assertTrue(member.isMemberStatusCode正式会員());
        });
    }

    //    銀行振込で購入を支払ったことのある、会員ステータスごとに一番若い会員を検索
    //    正式会員で一番若い、仮会員で一番若い、という風にそれぞれのステータスで若い会員を検索
    //    一回の ConditionBean による検索で会員たちを検索すること (PartitionBy...)
    //    ログのSQLを見て、検索が妥当であることを目視で確認すること
    //            検索結果が想定されるステータスの件数以上であることをアサート
    //    ひとまず動作する実装ができたら、ArrangeQueryを活用してみましょう
    public void test_7() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
        	// TODO tanaryo MemberCQにもauthorをぜひお願い by jflute (2025/02/13)
            cb.query().arrangeYoungestNiceMember();
        });
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

        // ## Assert ##
        assertHasAnyElement(memberList);
        // TODO tanaryo この3は導出してみましょう by jflute (2025/02/13)
        assertTrue(memberList.size() >= 3);
        memberList.forEach(member -> {
            log(member.getMemberStatusCode(), member.getBirthdate());
        });
    }
    //ここまでやった（by tanaryo 2025/02/08）
}
