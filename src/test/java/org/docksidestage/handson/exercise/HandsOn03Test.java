package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.exception.NonSpecifiedColumnAccessException;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author tanaryo
 */
public class HandsOn03Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    // [1on1でのふぉろー] 以前と言われたらどう思うか？どう実装するか？ => 実務では聞き返すがGood by たなりょうさん
    // 日本語の日付表現ってのは曖昧部分が多いので、勝手に解釈して決めるとよくすれ違い。どっちが文法的に正しいかはもはや関係ない。
    // データベースのSTART/END, BEGIN/ENDの期間を表すカラムの終了日時とか、どっち？どっちもよくありえる。
    public void test_searchMembers_silver_1() {
        // ## Arrange ##
        LocalDate targetDate = LocalDate.of(1968, 1, 1);
        // ## Act ##
        // TODO done tanaryo targetDateはArrangeでOKです。Actは純粋にテスト対象の実行のみするのが慣習 by jflute (2025/01/30)
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            // TODO done tanaryo メソッド呼び出し順序、慣習ではあるけれども、select句、where句、order by句に合わせて by jflute (2025/01/30)
            // http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.setupSelect_MemberStatus();
            cb.query().setBirthdate_LessEqual(targetDate);
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
            cb.query().addOrderBy_Birthdate_Asc();
        });
        // TODO done tanaryo log()も似たような話で、logはAssert配下でOK。アサートじゃないと言えるかもだけど、目視確認の一環なので by jflute (2025/01/30)

        // ## Assert ##
        log(members);
        assertHasAnyElement(members);
        members.forEach(member -> {
            // TODO done tanaryo ちょっとコードが膨れてるなと思ったら変数抽出して欲しい。 by jflute (2025/01/30)
            // 特にここは判定ロジック入るところで、できるだけ判定ロジックがパット見で認識できるようにしたい。
            // IntelliJのショートカットでサクッとできるはず。
            // 一方で、最初から変数に出すのではなく後から出すでOK。書くときは流れに任せてどっとどっとした方が速いってもあるので。
            // 出した後に見栄えを考えて、ちょっとこれは...と思ったらショートカット使えば良い。
            // なので、変数抽出ショートカットは、息を吸うかのごとくできるのがオススメ。
            LocalDate birthdate = member.getBirthdate();
            assertTrue(birthdate.isBefore(targetDate) || birthdate.isEqual(targetDate));
        });
    }

    public void test_searchMembers_silver_2() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            //生年月日がない人は後ろに回す
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            // TODO done tanaryo [いいね] nullの制御できててGood by jflute (2025/01/30)
            // TODO done tanaryo 一方で、若い順になってない。「若いと数字が若いは別 by tanaryo」 by jflute (2025/01/30)
            // 実は、DescにするとMySQLだと自然とnullは後ろになるので、nullsLastさんなくても良かったとは言える。
            // ただ、nullを小さな値と解釈して並べるのが世界標準かどうかは？はちょと怪しい。別のDBMSだったら違うかも。
            // ということなので、MySQLのデフォルト挙動に依存したSQLにしてると、いつかDBMS移行したときに痛い目に遭う。
            // なので、結果としては、一瞬要らないように思えても、NullsLastしておいても良い。
            cb.query().addOrderBy_Birthdate_Desc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        log(members);
        assertHasAnyElement(members);
        members.forEach(member -> {
            // [1on1でのふぉろー] 思考エクササイズ (カーディナリティ)
            // 検索条件は絞り込みが一つもなく全件取っている。
            //
            // 会員ステータスが問答無用で存在するとコードで言い切ってるけど、それはなぜ？保証されてるか？if文がない理由は？
            // → シンプルに言うと「NotNullのFKカラムだから」(nullのケースもでたらめコードのケースも排除しているから)
            //
            // 会員セキュリティも同じだけど、それはなぜ？保証されてるか？if文がない理由は？
            // → (宿題)
            // member_idはmemberのPKカラム(NotNull)かつ会員セキュリティのFKカラム(Notnull)である（by tanaryo 2025/02/01）
            // そのため、共通のmember_idを持つmemberレコードと会員セキュリティレコードが必ず存在する（by tanaryo 2025/02/01）
            //
            //
            // 学び:
            // データベースの構造がそうなっているから、プログラムがこうなっている、の関係性に注目。
            //
            // 格言:
            // コード一行一行に必ず理由がある。そう書いている理由を開発者は説明できないと。
            //
            assertTrue(member.getMemberStatus().isPresent());
            assertTrue(member.getMemberSecurityAsOne().isPresent());
        });
    }

    public void test_searchMembers_silver_3() {
        // ## Arrange ##
        // ## Act ##
        // TODO done tanaryo 試しに、cbの条件をなくしてわざと落とすようにしてみたけど、落ちない by jflute (2025/01/30)
        // コメントアウトしてテストが落ちることを確認（by tanaryo 2025/02/01）
        // 一度は、assertがredになってちゃんとassertのロジックが合ってることを確認しましょう。
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            // [1on1でのふぉろー] 関連テーブルの絞り込みGood
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        log(members);
        // ## Assert ##
        assertHasAnyElement(members);
        // TODO done tanaryo さすがに、インライン過ぎて見逃しとかしやすいので、もうちょい変数出してstep踏んでもいいかなと by jflute (2025/01/30)
        members.forEach(member -> {
            OptionalEntity<MemberSecurity> optMemberSecurity = memberSecurityBhv.selectEntity(cb -> {
                cb.query().setMemberId_Equal(member.getMemberId());
                cb.query().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
            });
            assertTrue(optMemberSecurity.isPresent());
        });
    }

    public void test_searchMembers_gold_4() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();//コメントアウトしたらテストは通らないことを確認
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        log(members);
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberStatus().isEmpty());
        });
        // TODO done tanaryo やりかけのときは、todoコメントで書いておきましょう。忘れちゃうケースがよくあるので by jflute (2025/01/30)
        // よもやま: 働き方の多様性のためにも、自分のやりかけをしっかり管理する習慣を付けておいたほうが良い。
        //ここから再開（by tanaryo 2025/02/01）
        //会員ステータスコードごとに固まっていることをアサート
        //OK
        //AAABBBBCCCC
        //AAABCCCC
        //ABBBCCCC
        //NG
        //AABBBCCAAA
        //AABBCCBB
        //値が切り替わった後に切り替わる前の値が再登場したら固まっていないとする
        assertTrue(isGroupedByMemberStatusCode(members));
    }

    private boolean isGroupedByMemberStatusCode(ListResultBean<Member> members) {
        HashSet<String> statusCodes = new HashSet<>();
        String lastStatusCode = "";

        for (Member member : members) {
            String memberStatusCode = member.getMemberStatusCode();
            //初回または値が切り替わった場合に通過
            if (!memberStatusCode.equals(lastStatusCode)) {
                //値が再登場していたらfalseを返す
                if (statusCodes.contains(memberStatusCode)) {
                    return false;
                }
                statusCodes.add(memberStatusCode);
                lastStatusCode = memberStatusCode;
            }
        }
        return true;
    }

    //    [5] 生年月日が存在する会員の購入を検索
    //    会員名称と会員ステータス名称と商品名を取得する(ログ出力)
    //    購入日時の降順、購入価格の降順、商品IDの昇順、会員IDの昇順で並べる
    //    OrderBy がたくさん追加されていることをログで目視確認すること
    //    購入に紐づく会員の生年月日が存在することをアサート
    public void test_searchMembers_gold_5() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            //MEMBER_IDはNotnullでmemberのFK
            cb.setupSelect_Member().withMemberStatus();//MemberとMemberStatusの両方取得
            //PRODUCT_IDはNotnullでproductのFK
            cb.setupSelect_Product();
            cb.query().queryMember().setBirthdate_IsNotNull();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.query().addOrderBy_PurchasePrice_Desc();
            cb.query().addOrderBy_PurchaseId_Asc();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(purchase -> {
            Member member = purchase.getMember().get();
            MemberStatus memberStatus = purchase.getMember().get().getMemberStatus().get();
            Product product = purchase.getProduct().get();
            log(member.getMemberName(), memberStatus.getMemberStatusName(), product.getProductName());

            LocalDate birthdate = member.getBirthdate();
            assertNotNull(birthdate);
        });
    }

    //[6] 2005年10月の1日から3日までに正式会員になった会員を検索
    //    画面からの検索条件で2005年10月1日と2005年10月3日がリクエストされたと想定して...
    //    Arrange で String の "2005/10/01", "2005/10/03" を一度宣言してから日時クラスに変換し...
    //    自分で日付移動などはせず、DBFluteの機能を使って、そのままの日付(日時)を使って条件を設定
    //    会員ステータスも一緒に取得
    //    ただし、会員ステータス名称だけ取得できればいい (説明や表示順カラムは不要)
    //    会員名称に "vi" を含む会員を検索
    //    会員名称と正式会員日時と会員ステータス名称をログに出力
    //    会員ステータスがコードと名称だけが取得されていることをアサート
    //    会員の正式会員日時が指定された条件の範囲内であることをアサート
    public void test_searchMembers_gold_6() {
        // ## Arrange ##
        String targetStartDateString = "2005/10/01";//00:00:00を含む
        String targetEndDateString = "2005/10/03";//翌日の00:00:00を含まない
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime targetStartDate = LocalDateTime.parse(targetStartDateString + " 00:00:00", formatter);
        LocalDateTime targetEndDate = LocalDateTime.parse(targetEndDateString + " 00:00:00", formatter);

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.specify().specifyMemberStatus().columnMemberStatusName();
            //where dfloc.FORMALIZED_DATETIME >= '2005-10-01 00:00:00.000'
            //and dfloc.FORMALIZED_DATETIME < '2005-10-04 00:00:00.000'
            cb.query().setFormalizedDatetime_FromTo(targetStartDate, targetEndDate, op -> op.compareAsDate());
            cb.query().setMemberName_LikeSearch("vi", op -> op.likeContain());
        });
        log(memberList);

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            String name = member.getMemberName();
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
            MemberStatus memberStatus = member.getMemberStatus().get();
            log(memberStatus);
            log(name, formalizedDatetime, memberStatus.getMemberStatusName());

            assertNotNull(memberStatus.getMemberStatusCode());
            assertNotNull(memberStatus.getMemberStatusName());
            //assertNull使うとエラー。そもそも該当からむが存在しないからgetできない。
            //getMemberStatusCodeではテスト通らないことを確認
            assertException(NonSpecifiedColumnAccessException.class, () -> memberStatus.getDescription());
            assertException(NonSpecifiedColumnAccessException.class, () -> memberStatus.getDisplayOrder());
        });
    }

    //    [7] 正式会員になってから一週間以内の購入を検索
    //    会員と会員ステータス、会員セキュリティ情報も一緒に取得
    //    商品と商品ステータス、商品カテゴリ、さらに上位の商品カテゴリも一緒に取得
    //    上位の商品カテゴリ名が取得できていることをアサート
    //    購入日時が正式会員になってから一週間以内であることをアサート
    public void test_searchMembers_platinum_7() {
        // ## Arrange ##
        // ## Act ##
        //　正式会員日時＜＝　購入日時　＜＝正式会員日時＋24h*7
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Member().withMemberSecurityAsOne();
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Product().withProductCategory().withProductCategorySelf();
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .greaterEqual(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime());//丁度は含む
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .lessEqual(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime())
                    .convert(op -> op.truncTime().addDay(7));//丁度1週間後は含む
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(purchase -> {
            String categoryName =
                    purchase.getProduct().get().getProductCategory().get().getProductCategorySelf().get().getProductCategoryName();
            LocalDateTime formalizedDatetime = purchase.getMember().get().getFormalizedDatetime();
            LocalDateTime formalizedDatetimeAfterOneWeek = (formalizedDatetime).plusDays(7);//丁度24h*7後
            LocalDateTime purchaseDatetime = purchase.getPurchaseDatetime();

            log(categoryName);
            log("正式会員日時={}, 購入日時={}", formalizedDatetime, purchaseDatetime);
            assertNotNull(categoryName);
            assertTrue(purchaseDatetime.isAfter(formalizedDatetime) || purchaseDatetime.equals(formalizedDatetime));
            assertTrue(
                    purchaseDatetime.isBefore(formalizedDatetimeAfterOneWeek) || purchaseDatetime.equals(formalizedDatetimeAfterOneWeek));
        });
    }

    //    [8] 1974年までに生まれた、もしくは不明の会員を検索
    //            画面からの検索条件で1974年がリクエストされたと想定
    //    Arrange で String の "1974/01/01" を一度宣言してから日付クラスに変換
    //    その日付クラスの値を、(日付移動などせず)そのまま使って検索条件を実現
    //    会員ステータス名称、リマインダ質問と回答、退会理由入力テキストを取得する(ログ出力) ※1
    //    若い順だが生年月日が null のデータを最初に並べる
    //    生年月日が指定された条件に合致することをアサート (1975年1月1日なら落ちるように)
    //    Arrangeで "きわどいデータ" ※2 を作ってみましょう (Behavior の updateNonstrict() ※3 を使って)
    //    検索で含まれるはずの "きわどいデータ" が検索されてることをアサート (アサート自体の保証のため)
    //    生まれが不明の会員が先頭になっていることをアサート
    public void test_searchMembers_platinum_8() {
        // ## Arrange ##
        String targetBirthDateString = "1974/01/01";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate targetBirthDate = LocalDate.parse(targetBirthDateString, formatter);
        makeLimitBirthDateMember();
        makeOverBirthDateMember();

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.setupSelect_MemberWithdrawalAsOne();
            cb.orScopeQuery(orCB -> {
                orCB.query().setBirthdate_FromTo(LocalDate.of(1, 1, 1), targetBirthDate, op -> op.compareAsYear());//1975/1/1は含まない
                orCB.query().setBirthdate_IsNull();
            });
            cb.query().addOrderBy_Birthdate_Desc().withNullsFirst();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        boolean containsLimitBirthDateMember = false;
        for (Member member : memberList) {
            MemberStatus status = member.getMemberStatus().get();
            MemberSecurity security = member.getMemberSecurityAsOne().get();
            OptionalEntity<MemberWithdrawal> optWithdrawal = member.getMemberWithdrawalAsOne();//データがないこともある
            String reason = optWithdrawal.map(withdrawal -> withdrawal.getWithdrawalReasonInputText()).orElse("none");
            LocalDate birthDate = member.getBirthdate();

            log("会員ステータス名称={}, リマインダ質問={}, リマインダ回答={}, 退会理由入力テキスト={}", status.getMemberStatusName(),
                    security.getReminderQuestion(), security.getReminderAnswer(), reason);
            if (birthDate != null) {
                assertTrue(birthDate.isBefore(LocalDate.of(1975, 1, 1)));
                if (birthDate.equals(LocalDate.of(1974, 12, 31))) {
                    containsLimitBirthDateMember = true;
                }
            }
        }
        assertTrue(containsLimitBirthDateMember);
        assertNull(memberList.get(0).getBirthdate());
    }

    private void makeLimitBirthDateMember() {
        Member member = new Member();
        member.setMemberId(1);
        member.setBirthdate(LocalDate.of(1974, 12, 31));
        memberBhv.updateNonstrict(member);
    }

    private void makeOverBirthDateMember() {
        Member member = new Member();
        member.setMemberId(2);
        member.setBirthdate(LocalDate.of(1975, 1, 1));
        memberBhv.updateNonstrict(member);
    }

    //    [9] 2005年6月に正式会員になった会員を先に並べて生年月日のない会員を検索
    //            画面からの検索条件で2005年6月がリクエストされたと想定
    //    Arrange で String の "2005/06/01" を一度宣言してから日付クラスに変換
    //    その日付クラスの値を、(日付移動などせず)そのまま使って検索条件を実現
    //            第二ソートキーは会員IDの降順
    //    検索された会員の生年月日が存在しないことをアサート
    //2005年6月に正式会員になった会員が先に並んでいることをアサート (先頭だけじゃなく全体をチェック)
    public void test_searchMembers_platinum_9() {
        // ## Arrange ##
        String targetDateString = "2005/06/01";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime targetDate = LocalDateTime.parse(targetDateString + " 00:00:00", formatter);
        LocalDateTime startTime = LocalDateTime.of(2005, 6, 1, 0, 0, 0);//を含む
        LocalDateTime endTime = LocalDateTime.of(2005, 7, 1, 0, 0, 0);//を含まない
        LocalDate anyBirthDate = LocalDate.of(2000, 1, 1);
        createTest9Member(1, startTime, anyBirthDate);//正式会員日時の条件を満たし、かつ生年月日がnullじゃない
        createTest9Member(2, endTime, null);//正式会員日時の条件を満たさない、かつ生年月日がnullじゃない
        createTest9Member(3, startTime, null);//正式会員日時の条件を満たし、かつ生年月日がnull

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query()
                    .addOrderBy_FormalizedDatetime_Asc()
                    .withManualOrder(
                            op -> op.when_FromTo(targetDate, targetDate, ftOp -> ftOp.compareAsMonth()));//正式会員日時が古い順に並べる。//2005/07/01は含まない
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
            int memberId = member.getMemberId();
            LocalDate birthDate = member.getBirthdate();
            log(formalizedDatetime, memberId);
            assertNull(birthDate);
        }
        assertTrue(isSortedFirstByValidFormalizedDatetime(memberList));
    }

    private void createTest9Member(int memberId, LocalDateTime formalizedDatetime, LocalDate birthDate) {
        Member member = new Member();
        member.setMemberId(memberId);
        member.setFormalizedDatetime(formalizedDatetime);
        member.setBirthdate(birthDate);
        memberBhv.updateNonstrict(member);
    }

    private boolean isSortedFirstByValidFormalizedDatetime(ListResultBean<Member> memberList) {
        LastState lastState = LastState.UNKNOWN;
        for (Member member : memberList) {
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
            if (isValidFormalizedDatetime(formalizedDatetime)) {
                if (lastState == LastState.INVALID) {
                    return false;
                }
                lastState = LastState.VALID;
            } else {
                lastState = LastState.INVALID;
            }
        }
        return true;
    }

    private enum LastState {
        VALID, INVALID, UNKNOWN
    }

    private boolean isValidFormalizedDatetime(LocalDateTime formalizedDatetime) {
        if (formalizedDatetime == null) {
            return false;
        }
        LocalDateTime startTime = LocalDateTime.of(2005, 6, 1, 0, 0, 0);//を含む
        LocalDateTime endTime = LocalDateTime.of(2005, 7, 1, 0, 0, 0);//を含まない
        return isWithinRange(formalizedDatetime, startTime, endTime);
    }

    private static boolean isWithinRange(LocalDateTime formalizedDatetime, LocalDateTime startTime, LocalDateTime endTime) {
        return (formalizedDatetime.isAfter(startTime) || formalizedDatetime.equals(startTime)) && formalizedDatetime.isBefore(endTime);
    }
}
