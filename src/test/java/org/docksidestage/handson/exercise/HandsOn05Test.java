package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.dbflute.optional.OptionalThing;
import org.docksidestage.handson.dbflute.exbhv.MemberAddressBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberAddress;
import org.docksidestage.handson.dbflute.exentity.MemberLogin;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// TODO tanaryo javadocお願いします〜 by jflute (2025/03/16)
public class HandsOn05Test extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;

    @Resource
    private MemberAddressBhv memberAddressBhv;

    @Resource
    private PurchaseBhv purchaseBhv;

    // ===================================================================================
    //                                                       　　　　　　業務的one-to-oneとは
    //                                                                        ============
    /**
     * 会員住所情報を検索
     * 会員名称、有効開始日、有効終了日、住所、地域名称をログに出して確認する
     * 会員IDの昇順、有効開始日の降順で並べる
     */
    public void test_1() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<MemberAddress> memberAddresses = memberAddressBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Region();
            cb.query().addOrderBy_MemberId_Asc();
            cb.query().addOrderBy_ValidBeginDate_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberAddresses);
        memberAddresses.forEach(memberAddress -> {
            String name = memberAddress.getMember().map(member -> member.getMemberName()).orElse(null);
            LocalDate validBeginDate = memberAddress.getValidBeginDate();
            LocalDate validEndDate = memberAddress.getValidEndDate();
            String address = memberAddress.getAddress();
            String regionName = memberAddress.getRegion().map(region -> region.getRegionName()).orElse(null);
            log("会員名称={}, 有効開始日={}, 有効終了日={}, 住所={}, 地域名称={}", name, validBeginDate, validEndDate, address, regionName);
        });
    }
    // 会員名称が同じレコードがたくさんある

    // ===================================================================================
    //                                                                業務的one-to-oneの設定
    //                                                                        ============
    // schemaHTMLにて、member_address(AsValid)を確認

    // ===================================================================================
    //                                                         業務的one-to-oneを利用した実装
    //                                                                        ============
    /**
     * 会員と共に現在の住所を取得して検索
     * SetupSelectのJavaDocにcommentがあることを確認すること
     * 会員名称と住所をログに出して確認すること
     * 現在日付はスーパークラスのメソッドを利用 ("c" 始まりのメソッド)
     * 会員住所情報が取得できていることをアサート
     */
    public void test_2() {
        // ## Arrange ##
        LocalDate targetDate = currentLocalDate();

        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberAddressAsValid(targetDate);
        });

        // ## Assert ##
        boolean existsValidAddressMember = false;
        assertHasAnyElement(members);

        //        memberBhv.loadMemberAddress(memberList, memberAddress -> {});
        //これだと有効じゃないMemberAddressもとってしまうのでアカン

        for (Member member : members) {
            String name = member.getMemberName();
            String address = member.getMemberAddressAsValid().map(ma -> ma.getAddress()).orElse(null);
            log("会員名称={}, 住所={}", name, address);
            if (address != null) {
                existsValidAddressMember = true;
                assertTrue(member.getMemberAddressAsValid().isPresent());
            }
        }

        // TODO tanaryo [いいね] パーフェクト by jflute (2025/03/16)
        if (!existsValidAddressMember) {
            fail("有効な会員住所を持つ会員が存在しない、もしくはsetupSelectし忘れています");
        }
    }

    /**
     * 千葉に住んでいる会員の支払済み購入を検索
     * 会員ステータス名称と住所をログに出して確認すること
     * 購入に紐づいている会員の住所の地域が千葉であることをアサート
     */
    public void test_3() {
        // ## Arrange ##
        LocalDate targetDate = currentLocalDate();

        // ## Act ##
        ListResultBean<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberAddressAsValid(targetDate);
            cb.setupSelect_Member().withMemberStatus();
            cb.query().setPaymentCompleteFlg_Equal_True();
            // TODO tanaryo 業務的one-to-oneとして定義しているので、one-to-many的な絞り込みをしなくても良いぞぅ by jflute (2025/03/16)
            cb.query().queryMember().existsMemberAddress(subMemberAddressCB -> {
                subMemberAddressCB.query().setRegionId_Equal_千葉();
                subMemberAddressCB.query().setValidBeginDate_LessEqual(targetDate);
                subMemberAddressCB.query().setValidEndDate_GreaterEqual(targetDate);
            });
        });

        // ## Assert ##
        assertHasAnyElement(purchases);
        for (Purchase purchase : purchases) {
        	// TODO tanaryo Lambda引数名、Optional(op)自体ではなく、Optionalの中身が来ているので中身に合わせた名前にしましょう by jflute (2025/03/16)
        	// map(op -> op.getMemberStatusName()) → map(status -> status.getMemberStatusName())
            String statusName = purchase.getMember().flatMap(op -> op.getMemberStatus()).map(op -> op.getMemberStatusName()).orElse(null);
            OptionalThing<MemberAddress> memberAddressOpt = purchase.getMember().flatMap(op -> op.getMemberAddressAsValid());
            String address = memberAddressOpt.map(op -> op.getAddress()).orElse(null);
            log("会員名称={}, 住所={}", statusName, address);
            assertTrue(memberAddressOpt.map(op -> op.isRegionId千葉()).orElse(false));
        }
    }

    // ===================================================================================
    //                                                         導出的one-to-oneを利用した実装
    //                                                                        ============
    // schemaHTMLにて、member_login(AsLatest) を確認
    // TODO jflute 1on1にて、現場での利用のケースを一緒におさらい予定 (2025/03/16)
    /**
     * 最終ログイン時の会員ステータスを取得して会員を検索
     * SetupSelectのJavaDocに自分で設定したcommentが表示されることを目視確認
     * 会員名称と最終ログイン日時と最終ログイン時の会員ステータス名称をログに出す
     * 最終ログイン日時が取得できてることをアサート
     */
    public void test_4() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberLoginAsLatest().withMemberStatus();
        });
        //javadocで自分で設定した「最終ログイン情報」を確認

        // ## Assert ##
        boolean existsLatestLogin = false;
        assertHasAnyElement(members);

        for (Member member : members) {
            String name = member.getMemberName();
            OptionalEntity<MemberLogin> memberLoginAsLatestOpt = member.getMemberLoginAsLatest();
            LocalDateTime latestLoginDateTime = memberLoginAsLatestOpt.map(op -> op.getLoginDatetime()).orElse(null);
            String statusName = memberLoginAsLatestOpt.flatMap(op -> op.getMemberStatus()).map(op -> op.getMemberStatusName()).orElse(null);
            log("会員名称={}, 最終ログイン日時={}, 最終ログイン時の会員ステータス名称={}", name, latestLoginDateTime, statusName);
            if (latestLoginDateTime != null) {
                existsLatestLogin = true;
                //最終ログイン日時はnotNullカラムなので、最終ログイン情報を取得できていればOK
                assertTrue(member.getMemberLoginAsLatest().isPresent());
            }
        }

        if (!existsLatestLogin) {
            fail("最新ログイン情報を持つ会員が存在しない、もしくはsetupSelectし忘れています");
        }
    }

    // ===================================================================================
    //                                                             テストデータの登録時チェック
    //                                                                        ============
    // MEMBER_ID=1、ADDRESS_ID=2のVALID_END_DATEを1997/03/31 ->1997/05/31に変更。
    // これはMEMBER_ID=1、ADDRESS_ID=3の1997/04/01を超えている。
    // [df-replace-schema] 2025-03-15 13:30:57,207 INFO  - MEMBER_ADDRESS:{2, 1, 1949/01/01, 1997/05/31....を確認

    // take-finally.sqlにSQL追加したらエラーが出て以下のログ
    // この問題なければチェックに引っかかるデータは0なのに、1件引っかかっている模様
    //[df-replace-schema] [Result Count]
    //[df-replace-schema] 1
    //[df-replace-schema]
    //[df-replace-schema] [Result List]
    //[df-replace-schema] {MEMBER_ADDRESS_ID=2, MEMBER_ID=1, VALID_BEGIN_DATE=1949-01-01, VALID_END_DATE=1997-05-31, ADDRESS=New York}

    // 以下をチェックするSQLをtake-finally.sqlに追加

    // 正式会員日時を持ってる仮会員がいないこと。以下をログで確認
    //[df-replace-schema] [Executed SQL]
    //[df-replace-schema] -- #df:assertListZero#
    //[df-replace-schema] -- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //[df-replace-schema] -- 正式会員日時を持ってる仮会員がいないことをアサート
    //[df-replace-schema] -- - - - - - - - - - -/
    //[df-replace-schema] select MEMBER_ID
    //[df-replace-schema]      , MEMBER_STATUS_CODE
    //[df-replace-schema]      , FORMALIZED_DATETIME
    //[df-replace-schema] from MEMBER
    //[df-replace-schema] where MEMBER_STATUS_CODE = 'PRV'
    //[df-replace-schema]   and FORMALIZED_DATETIME IS NOT NULL
    //[df-replace-schema]
    //[df-replace-schema] [Result Count]
    //[df-replace-schema] 1
    //[df-replace-schema]
    //[df-replace-schema] [Result List]
    //[df-replace-schema] {MEMBER_ID=1, MEMBER_STATUS_CODE=PRV, FORMALIZED_DATETIME=2007-12-01 11:01:10.0}
    // MEMBER_ID=1のMEMBER_STATUS_CODEをFML->PRVにする

    // まだ生まれていない会員がいないこと。以下をログで確認
    //[df-replace-schema] [Executed SQL]
    //[df-replace-schema] -- #df:assertListZero#
    //[df-replace-schema] -- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //[df-replace-schema] -- まだ生まれていない会員がいないことをアサート
    //[df-replace-schema] -- - - - - - - - - - -/
    //[df-replace-schema] select MEMBER_ID
    //[df-replace-schema]      , BIRTHDATE
    //[df-replace-schema]      , CURRENT_DATE()
    //[df-replace-schema] from MEMBER
    //[df-replace-schema] where BIRTHDATE IS NOT NULL
    //[df-replace-schema]   and BIRTHDATE > CURRENT_DATE()
    //[df-replace-schema]
    //[df-replace-schema] [Result Count]
    //[df-replace-schema] 1
    //[df-replace-schema]
    //[df-replace-schema] [Result List]
    //[df-replace-schema] {MEMBER_ID=1, BIRTHDATE=2030-03-03, CURRENT_DATE()=2025-03-15}

    // 退会会員が退会情報を持っていること。以下をログで確認
    //[df-replace-schema] [Executed SQL]
    //[df-replace-schema] -- #df:assertListZero#
    //[df-replace-schema] -- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //[df-replace-schema] -- 退会会員が退会情報を持っていることをアサート
    //[df-replace-schema] -- - - - - - - - - - -/
    //[df-replace-schema] select member.MEMBER_ID
    //[df-replace-schema]      , member.MEMBER_STATUS_CODE
    //[df-replace-schema] from MEMBER member
    //[df-replace-schema] where member.MEMBER_STATUS_CODE = 'WDL'
    //[df-replace-schema]   and not exists(select wdl.MEMBER_ID
    //[df-replace-schema]                  from MEMBER_WITHDRAWAL wdl
    //[df-replace-schema]                  WHERE wdl.MEMBER_ID = member.MEMBER_ID)
    //[df-replace-schema]
    //[df-replace-schema] [Result Count]
    //[df-replace-schema] 1
    //[df-replace-schema]
    //[df-replace-schema] [Result List]
    //[df-replace-schema] {MEMBER_ID=3, MEMBER_STATUS_CODE=WDL}

    //おまけチェック
    // 以下の場合は現状だとビルドが成功してしまう。なので=入れる
    // 1949/01/01	1997/04/01
    //1997/04/01	2000/09/30
    // 以下のログを確認
    //[df-replace-schema] [Executed SQL]
    //[df-replace-schema] -- #df:assertListZero#
    //[df-replace-schema] -- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //[df-replace-schema] -- Member addresses should be only one at any time.
    //[df-replace-schema] -- - - - - - - - - - -/
    //[df-replace-schema] select adr.MEMBER_ADDRESS_ID
    //[df-replace-schema]      , adr.MEMBER_ID
    //[df-replace-schema]      , adr.VALID_BEGIN_DATE
    //[df-replace-schema]      , adr.VALID_END_DATE
    //[df-replace-schema]      , adr.ADDRESS
    //[df-replace-schema] from MEMBER_ADDRESS adr
    //[df-replace-schema] where exists (select subadr.MEMBER_ADDRESS_ID
    //[df-replace-schema]               from MEMBER_ADDRESS subadr
    //[df-replace-schema]               where subadr.MEMBER_ID = adr.MEMBER_ID
    //[df-replace-schema]                 and subadr.VALID_BEGIN_DATE > adr.VALID_BEGIN_DATE
    //[df-replace-schema]                 and subadr.VALID_BEGIN_DATE <= adr.VALID_END_DATE)
    //[df-replace-schema]
    //[df-replace-schema] [Result Count]
    //[df-replace-schema] 1
    //[df-replace-schema]
    //[df-replace-schema] [Result List]
    //[df-replace-schema] {MEMBER_ADDRESS_ID=2, MEMBER_ID=1, VALID_BEGIN_DATE=1949-01-01, VALID_END_DATE=1997-04-01, ADDRESS=New York}

    //エイリアス名は少なくとも１文字はあかん！！
    // TODO tanaryo [いいね] コラムを読んでくれてありがとう。名前は識別が一番の目的。 by jflute (2025/03/16)
    
    // TODO jflute 1on1にて、SQLのフォーマット談義 (2025/03/16)
}

