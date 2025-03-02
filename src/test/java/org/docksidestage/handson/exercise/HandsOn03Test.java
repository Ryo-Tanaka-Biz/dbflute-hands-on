package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.exception.NonSpecifiedColumnAccessException;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// TODO done tanaryo 不要なimportがいる by jflute (2025/02/21)
// [1on1でのふぉろー] Eclipseで言う Type Filters みたいなのしっかり設定していくと補完ノイズが少なくなる話。

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

    // ===================================================================================
    //                                                                      Silver Stretch
    //                                                                        ============

    // [1on1でのふぉろー] 以前と言われたらどう思うか？どう実装するか？ => 実務では聞き返すがGood by たなりょうさん
    // 日本語の日付表現ってのは曖昧部分が多いので、勝手に解釈して決めるとよくすれ違い。どっちが文法的に正しいかはもはや関係ない。
    // データベースのSTART/END, BEGIN/ENDの期間を表すカラムの終了日時とか、どっち？どっちもよくありえる。
    public void test_searchMembers_silver_1() {
        // ## Arrange ##
        LocalDate targetDate = LocalDate.of(1968, 1, 1);
        // ## Act ##
        // done tanaryo targetDateはArrangeでOKです。Actは純粋にテスト対象の実行のみするのが慣習 by jflute (2025/01/30)
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            // done tanaryo メソッド呼び出し順序、慣習ではあるけれども、select句、where句、order by句に合わせて by jflute (2025/01/30)
            // http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.setupSelect_MemberStatus();
            cb.query().setBirthdate_LessEqual(targetDate);
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
            cb.query().addOrderBy_Birthdate_Asc();
        });
        // done tanaryo log()も似たような話で、logはAssert配下でOK。アサートじゃないと言えるかもだけど、目視確認の一環なので by jflute (2025/01/30)

        // ## Assert ##
        log(members);
        assertHasAnyElement(members);
        members.forEach(member -> {
            // done tanaryo ちょっとコードが膨れてるなと思ったら変数抽出して欲しい。 by jflute (2025/01/30)
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
            // done tanaryo [いいね] nullの制御できててGood by jflute (2025/01/30)
            // done tanaryo 一方で、若い順になってない。「若いと数字が若いは別 by tanaryo」 by jflute (2025/01/30)
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
            // ↑1on1でふぉろー、探しに行く方向とFKの方向が逆なので、そのセオリーは通じない。
            // ER図の黒まるやSchemaHTMLのテーブルコメントにヒントがあった。
            // 業務的な制約で必ず存在する、という風に人間が決めている。
            //
            // 1:1と言ったとき、必ず存在する1:1なのか？いないかもしれない1:1なのか？わからない。
            // (ファーストレベルの表現だけの1:1なのか？セカンドレベルまで表現してて必ず存在する1:1のことを言ってるのか？)
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
        // done tanaryo 試しに、cbの条件をなくしてわざと落とすようにしてみたけど、落ちない by jflute (2025/01/30)
        // コメントアウトしてテストが落ちることを確認（by tanaryo 2025/02/01）
        // 一度は、assertがredになってちゃんとassertのロジックが合ってることを確認しましょう。
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            // [1on1でのふぉろー] 関連テーブルの絞り込みGood
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        log(members);
        // ## Assert ##
        assertHasAnyElement(members);
        // done tanaryo さすがに、インライン過ぎて見逃しとかしやすいので、もうちょい変数出してstep踏んでもいいかなと by jflute (2025/01/30)
        // done jflute 1on1にて、ループ内getの話 (2025/02/04)
        // n+1問題と言われる。パフォーマンス劣化の代表選手。ぐるぐる系のSQLとも呼ばれる。
        // 1発1発は早いSQLなんだけど、全体としてはボディーブローのように「うっ」とくる遅さになる。
        // DBに無駄に負荷(CPU)を掛けるきっかけになったりする。
        // これは業務でもめちゃくちゃ気をつけて欲しい。ループの中で検索は基本的に危ないと考えてもいいくらい。
        // done tanaryo securityだけで一回のSQLにしてみましょう by jflute (2025/02/06)
        // memberIDでsecurityは一意に特定できるので、getReminderQuestionで取得してそこでアサートする　by tanaryo(2025/02/08)
        // done tanaryo ↑の修正はそれはそれでオウム返しがなくなって良い。一方で、SQLの発行回数を減らして欲しい by jflute (2025/02/13)
        ListResultBean<MemberSecurity> memberSecurityList = memberSecurityBhv.selectList(cb -> {
            cb.query().setMemberId_InScope(memberBhv.extractMemberIdList(members));//単一のPK(memberId)を抽出
        });
        //        members.forEach(member -> {
        //            // done tanaryo あるかどうか？だけを見るのであれば、selectCount()を使う習慣を by jflute (2025/02/04)
        //            // UnitTestなので妥協はできますが、mainコードだったら無駄に1レコード分のデータを取得することになります。
        //            // countであればint型のデータが転送されるだけになるのでネットワーク負荷が低くなります。
        //            // 今回は使わずselectEntityのまま。selectCountはintを返す by tanaryo(2025/02/08)
        //        	// done tanaryo 直後にget()しちゃってるくらいなら、メソッドチェーンで.get()しちゃってもいいかなと by jflute (2025/02/13)
        //        	// というか、その場で消費し終わるロジックなので、alwaysPresent()でいいんじゃないかと。
        //            OptionalEntity<MemberSecurity> optMemberSecurity = memberSecurityBhv.selectEntity(cb -> {
        //                cb.query().setMemberId_Equal(member.getMemberId());
        //            });
        //            assertTrue(optMemberSecurity.get().getReminderQuestion().contains("2"));
        //        });
        //会員と会員セキュリティは必ず1:1で存在する。
        memberSecurityList.forEach(security -> {
            assertTrue(security.getReminderQuestion().contains("2"));
        });

    }

    // ===================================================================================
    //                                                                        Gold Stretch
    //                                                                        ============
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
        // done jflute tanaryo ここは1on1にて聞く (2025/02/04)
        // done tanaryo やりかけのときは、todoコメントで書いておきましょう。忘れちゃうケースがよくあるので by jflute (2025/01/30)
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

        // [tips] 別のやり方の紹介。
        // 値が切り替わったタイミングの回数と、登場したステータスの種類数が...
        // 「値が切り替わったタイミングの回数 = 登場したステータスの種類数 - 1」のはず。
        // という考え方。
        // この場合、assertはloopの外になる。でも、持つデータはほとんど変わらない。
        // 別にこのやり方が良いというわけではないけど。
    }

    private boolean isGroupedByMemberStatusCode(ListResultBean<Member> members) {
        // done tanaryo せっかくなのでinterfaceのSetで扱いましょう by jflute (2025/02/06)
        Set<String> statusCodes = new HashSet<>();
        // [1on1でのふぉろー] Stringの初期値の質問の回答: こういうやり方もあるので悪くはない。
        // ただ個人的には空文字ってあんまり使わないようにしてて null にしてる。
        // 空文字で下手に動いちゃってロジック間違ったのに落ちないとかいやなので。
        String lastStatusCode = null;

        for (Member member : members) {
            String memberStatusCode = member.getMemberStatusCode();
            // done tanaryo [細かいtips] 横のすらすらコメントで表現するとプログラムだけにフォーカス当てやすくなる by jflute (2025/02/13)
            // (ただ、長い文章のときは横長になって見づらいので、短いとき限定ではあるけど)
            if (!memberStatusCode.equals(lastStatusCode)) {//初回または値が切り替わった場合に通過
                if (statusCodes.contains(memberStatusCode)) {//値が再登場していたらfalseを返す
                    return false;
                }
                // done tanaryo tips: 切り替わったときにじゃなくても、常にaddと=代入して問題ないので... by jflute (2025/02/04)
                // シンプルにするためにifの外に出すやり方もあります。
                // statusCodesはとにかく登場したステータスのset, lastStatusCodeはとにかく一個前のステータス、というニュアンスで。
            }
            // if文に入れなくて良いものは入れなくて良いというスタンスでいく by tanaryo (2025/02/08)
            statusCodes.add(memberStatusCode);
            log(statusCodes);
            lastStatusCode = memberStatusCode;
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
        // done tanaryo [いいね] 完璧ですな by jflute (2025/02/04)
        // [1on1でのふぉろー] 基点テーブルの話
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
        // done tanaryo これまた完璧。一応、補足に書いてあった adjust... のところもやってみてください by jflute (2025/02/04)
        String targetStartDateString = "2005/10/01";//00:00:00を含む
        String targetEndDateString = "2005/10/03";//翌日の00:00:00を含まない
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime targetStartDate = LocalDateTime.parse(targetStartDateString + " 00:00:00", formatter);
        LocalDateTime targetEndDate = LocalDateTime.parse(targetEndDateString + " 00:00:00", formatter);
        adjustMember_FormalizedDatetime_FirstOnly(targetStartDate, "vi");//レコード数が2になっていることを確認

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.specify().specifyMemberStatus().columnMemberStatusName();
            //where dfloc.FORMALIZED_DATETIME >= '2005-10-01 00:00:00.000'
            //and dfloc.FORMALIZED_DATETIME < '2005-10-04 00:00:00.000'
            cb.query().setFormalizedDatetime_FromTo(targetStartDate, targetEndDate, op -> op.compareAsDate());
            cb.query().setMemberName_LikeSearch("vi", op -> op.likeContain());
        });
        log(memberList.getAllRecordCount());

        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            // done tanaryo 変数名の省略ポイントの統一性がちょっと微妙なので修正してみましょう by jflute (2025/02/06)
            String name = member.getMemberName();
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
            MemberStatus status = member.getMemberStatus().get();
            log(status);
            log(name, formalizedDatetime, status.getMemberStatusName());

            assertNotNull(status.getMemberStatusCode());
            assertNotNull(status.getMemberStatusName());
            //assertNull使うとエラー。そもそも該当からむが存在しないからgetできない。
            //getMemberStatusCodeではテスト通らないことを確認
            assertException(NonSpecifiedColumnAccessException.class, () -> status.getDescription());
            assertException(NonSpecifiedColumnAccessException.class, () -> status.getDisplayOrder());

            // done tanaryo 実装漏れ "会員の正式会員日時が指定された条件の範囲内であることをアサート" by jflute (2025/02/06)
            // done tanaryo 一気に全部assertTrue()すると、落ちたときにどの条件で落ちたのかがわからなくなるので分離した方がいい by jflute (2025/02/13)
            assertTrue(formalizedDatetime.isAfter(targetStartDate) || formalizedDatetime.equals(targetStartDate));
            assertTrue(formalizedDatetime.isBefore(targetEndDate.plusDays(1)));
        });
    }

    // ===================================================================================
    //                                                                    Platinum Stretch
    //                                                                        ============
    //    [7] 正式会員になってから一週間以内の購入を検索
    //    会員と会員ステータス、会員セキュリティ情報も一緒に取得
    //    商品と商品ステータス、商品カテゴリ、さらに上位の商品カテゴリも一緒に取得
    //    上位の商品カテゴリ名が取得できていることをアサート
    //    購入日時が正式会員になってから一週間以内であることをアサート
    public void test_searchMembers_platinum_7() {
        // ## Arrange ##
        // done tanaryo 補足にあった、adjustを使ったエクササイズをやってみてください by jflute (2025/02/06)
        // Kまで仕様だとレコードは増えない
        // ソースコード見ると、23:59:59にしてそうなので、Mにしてみる
        //レコード1つ増えた
        adjustPurchase_PurchaseDatetime_fromFormalizedDatetimeInWeek();

        // ## Act ##
        // done jflute 1on1で一週間以内の解釈議論 (2025/02/04)
        //
        // 10/3                    10/10     10/11
        //  13h                      0h  13h   0h
        //   |                       |    |    |
        //   |       D               | I  |    | P
        // A |                       |H  J|L   |O
        //   |C                  E   G    K    N
        //   B                      F|    |   M|
        //   |                       |         |
        //
        //　正式会員日時＜＝　購入日時　＜＝正式会員日時＋24h*7
        //
        // たなりょうさんの一週間は、24h*7のとおり K まで。
        // but truncTime()は不要では？ これだと G になっちゃう。
        // done tanaryo ↑自分の意図通りに実装ができていないので、それはそれは直しましょう。 by jflute (2025/02/06)
        //
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Member().withMemberSecurityAsOne();
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Product().withProductCategory().withProductCategorySelf();
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .greaterEqual(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime());//丁度は含む
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .lessThan(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime())
                    .convert(op -> op.addDay(8).truncTime());
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        purchaseList.forEach(purchase -> {
            String categoryName =
                    purchase.getProduct().get().getProductCategory().get().getProductCategorySelf().get().getProductCategoryName();
            LocalDateTime formalizedDatetime = purchase.getMember().get().getFormalizedDatetime();
            // done tanaryo (formalizedDatetime) の () は無くてOKです by jflute (2025/02/04)
            // done tanaryo queryの方は truncTime() してるけど、こっちは trunc 的な処理が見当たらないけど大丈夫かな？ by jflute (2025/02/04)
            // done tanaryo 一応、.withNano(0) を入れないといけないんじゃないかな？ by jflute (2025/02/13)
            LocalDateTime formalizedDatetimeAfterOneWeek = formalizedDatetime.plusDays(8).truncatedTo(ChronoUnit.DAYS);
            LocalDateTime purchaseDatetime = purchase.getPurchaseDatetime();

            log(categoryName);
            log("正式会員日時={}, 購入日時={}", formalizedDatetime, purchaseDatetime);
            assertNotNull(categoryName);
            // done tanaryo ここも分離 by jflute (2025/02/13)
            assertTrue(purchaseDatetime.isAfter(formalizedDatetime) || purchaseDatetime.equals(formalizedDatetime));
            assertTrue(purchaseDatetime.isBefore(formalizedDatetimeAfterOneWeek));
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
            // where (dfloc.BIRTHDATE < '1975-01-01' or dfloc.BIRTHDATE is null)
            cb.query().setBirthdate_FromTo(null, targetBirthDate, op -> op.compareAsYear().allowOneSide().orIsNull());
            //            cb.orScopeQuery(orCB -> {
            //                // done tanaryo なるほどぅ。fromがないから西暦1年からにしたということですね。opでダミー値を使わないようにもできます by jflute (2025/02/04)
            //                //FromToのメソッド確認
            //                // (basically NotNull: if op.allowOneSide(), null allowed)とあった
            //                orCB.query().setBirthdate_FromTo(null, targetBirthDate, op -> op.compareAsYear().allowOneSide().or);//1975/1/1は含まない
            //                orCB.query().setBirthdate_IsNull();
            //            });
            cb.query().addOrderBy_Birthdate_Desc().withNullsFirst();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        // done tanaryo [いいね] 良い変数名 by jflute (2025/02/04)
        boolean containsLimitBirthDateMember = false;
        for (Member member : memberList) {
            MemberStatus status = member.getMemberStatus().get();
            MemberSecurity security = member.getMemberSecurityAsOne().get();
            OptionalEntity<MemberWithdrawal> optWithdrawal = member.getMemberWithdrawalAsOne();//データがないこともある
            String reason = optWithdrawal.map(withdrawal -> withdrawal.getWithdrawalReasonInputText()).orElse("none");
            // done tanaryo 一応、カラム名表現が Birthdate なので、それに合わせて D は d にしましょう by jflute (2025/02/04)
            // (文法的にどっちが合ってるか？ってのは置いておいて、すでに定義されているカラムに合わせたほうが無難ということで)
            LocalDate birthdate = member.getBirthdate();

            log("会員ステータス名称={}, リマインダ質問={}, リマインダ回答={}, 退会理由入力テキスト={}", status.getMemberStatusName(),
                    security.getReminderQuestion(), security.getReminderAnswer(), reason);
            if (birthdate != null) {
                assertTrue(birthdate.isBefore(LocalDate.of(1975, 1, 1)));
                if (birthdate.equals(LocalDate.of(1974, 12, 31))) {
                    containsLimitBirthDateMember = true;
                }
            }
        }
        assertTrue(containsLimitBirthDateMember);
        // done  tanaryo [いいね] これよく気づきました。先頭ってありますからこれでOKです by jflute (2025/02/04)
        assertNull(memberList.get(0).getBirthdate());
    }

    // [1on1でのふぉろー] ちょこっと更新でテストデータを作るのGood。
    // このやり方の最大の敵は、既存テーブルに1レコードも入っていない状況。
    // テストデータは、最低1レコードは作りたいところ。
    private void makeLimitBirthDateMember() {
        // [1on1でのふぉろー] UnitTestでのテストデータへの依存の話
        // テストデータに依存しない方がいいかどうかは、現場のポリシー次第。
        // がんがん依存してアサート書いていこうってわりきってうまくやってる現場もあるし。
        // (その現場では、依存度を下げるためにUnitTestでロジックを書くことが良くないという考え方をしてる)
        // (UnitTestはできるだけベタに書いて難しいことをしない方が逆に間違い少ないという考え方)
        // (UnitTestは業務仕様のドキュメントとも言えるので、ロジックで抽象化をしない方がいいだろうという考え方)
        //
        // ちなみに、ちょこっと更新のやり方は、このケースだとIDくらいしか依存していないので、依存度は低い。
        // さらに依存度を下げるのであれば、こんな実装もある。
        //Member existingMember = memberBhv.selectEntity(cb -> {
        //	cb.fetchFirst(1);
        //}).get();
        //existingMember.setBirthdate(LocalDate.of(1974, 12, 31));
        //memberBhv.updateNonstrict(existingMember);

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

    // done jflute 次回1on1でのふぉろーここから (2025/02/06)
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
        // done tanaryo [いいね] 応用できてて素晴らしい by jflute (2025/02/04)
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
        // done tanaryo [見比べ課題] 模範の実装と見比べてみて学んでみてください by jflute (2025/02/13)
        //arrangeとactは大体一緒。assertは一見違う？
        // if文の中にassertを入れるのが一番の違いな気がする。こっちの方が簡潔に書けているし、1回のfor文ですむ。
        // 自分の場合isSortedFirstByValidFormalizedDatetimeはfor文の途中でreturnするロジックなので、各会員に対するアサートやログ出力とは共存できない。
        // ## Assert ##
        //        assertHasAnyElement(memberList);
        //        boolean existsTargetMonth = false;
        //        boolean passedBorder = false;
        //        HandyDate fromHandy = new HandyDate(fromDate);
        //        for (Member member : memberList) {
        //            assertNull(member.getBirthdate());
        //            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
        //            if (formalizedDatetime != null && fromHandy.isMonthOfYearSameAs(formalizedDatetime)) {//6月だったら
        //                assertFalse(passedBorder); //AAABBBBAAAみたいなパターンはここで引っかかる
        //                existsTargetMonth = true;//
        //            } else { // null or others　６月以外
        //                passedBorder = true;
        //            }
        //        }
        //        assertTrue(existsTargetMonth);
        //        assertTrue(passedBorder);
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

    // ===================================================================================
    //                                                                              Paging
    //                                                                        ============
    /**
     * 会員ステータス名称も取得
     * 会員IDの昇順で並べる
     * ページサイズは 3、ページ番号は 1 で検索すること
     * 会員ID、会員名称、会員ステータス名称をログに出力
     * SQLのログでカウント検索時と実データ検索時の違いを確認
     * 総レコード件数が会員テーブルの全件であることをアサート
     * 総ページ数が期待通りのページ数(計算で導出)であることをアサート
     * 検索結果のページサイズ、ページ番号が指定されたものであることをアサート
     * 検索結果が指定されたページサイズ分のデータだけであることをアサート
     * PageRangeを 3 にして PageNumberList を取得し、[1, 2, 3, 4]であることをアサート
     * 前のページが存在しないことをアサート
     * 次のページが存在することをアサート
     */
    public void test_paging() {
        // ## Arrange ##
        int pageSize = 3;
        int pageNumber = 1;

        // ## Act ##
        // 実データ：select sql_calc_found_rows dfloc.MEMBER_ID as MEMBER_ID,.....limit 0, 3
        // カウント数：select found_rows()
        // [1on1でのふぉろー] ページング検索のパフォーマンス的なジレンマの話
        // DBFluteでそのジレンマへの配慮をしている話
        PagingResultBean<Member> page = memberBhv.selectPage(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().addOrderBy_MemberId_Asc();
            cb.paging(pageSize, pageNumber);//1~3を取得
        });

        // ## Assert ##
        page.forEach(member -> {
            int memberId = member.getMemberId();
            String memberName = member.getMemberName();
            String memberStatusName = member.getMemberStatus().get().getMemberStatusName();
            log(memberId, memberName, memberStatusName);
        });

        int allRecordCount = page.getAllRecordCount();
        int allRecordCountMember = memberBhv.selectCount(cb -> {});
        assertEquals(allRecordCount, allRecordCountMember);

        int allPageCount = page.getAllPageCount();
        int totalPageNumber = (int) Math.ceil((double) allRecordCount / pageSize);//intの計算は整数の結果しか返さないので、doubleにキャスト
        assertEquals(allPageCount, totalPageNumber);

        int searchedPageSize = page.getPageSize();
        assertEquals(pageSize, searchedPageSize);

        int searchedPageNumber = page.getCurrentPageNumber();
        assertEquals(pageNumber, searchedPageNumber);

        assertEquals(pageSize, page.size());//getPageSize()は計算して出たページサイズ。size()はリストの要素数

        //PageRange：前後 n ページのリンクが表示 されるようにする方式
        // n =3、ページ番号6の場合、 3,4,5, 6 ,7,8,9のリンクが表示される
        int pageRange = 3;
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<Integer> pageList = page.pageRange(op -> op.rangeSize(pageRange)).createPageNumberList();//pageNumberは1
        assertEquals(list, pageList);
        //pageNumberが2の場合（pageRangeは3）、[1,2,3,4,5]となる。pageNumberが存在するまで前後のページを表示する仕様。だから1も表示される。
        //existsPreviousRangeはfalseだった。指定したレンジのページが全て存在していないといけない
        //pageNumberが1,pageRangeが9の場合、[1, 2, 3, 4, 5, 6, 7]となる。指定したレンジのページが全て存在しない状態。
        //existsNextRangeは予想通りfalse

        //      assertException(IllegalStateException.class, () -> page.getPreviousPageNumber());
        assertFalse(page.existsPreviousPage());
        assertTrue(page.existsNextPage());
    }

    // ===================================================================================
    //                                                                              Cursor
    //                                                                        ============
    /**
     * 会員ステータスの "表示順" カラムの昇順で並べる
     * 会員ステータスのデータも
     * 会員ステータスが取れていることをアサート
     * 会員が会員ステータスごとに固まって並んでいることをアサート
     * 検索したデータをまるごとメモリ上に持ってはいけない
     * (要は、検索結果レコード件数と同サイズのリストや配列の作成はダメ)
     */
    public void test_cursor() {
        // ## Arrange ##
        Set<String> statusCodes = new HashSet<>();
        String[] lastStatusCode = {null};


        // ## Act ##
        memberBhv.selectCursor(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        }, member -> {
            // ## Assert ##
            String memberStatusCode = member.getMemberStatusCode();
            assertTrue(member.getMemberStatus().isPresent());
            if (!memberStatusCode.equals(lastStatusCode[0])) {//初回または種類が切り替わった場合
                assertFalse(statusCodes.contains(memberStatusCode));//そのステータスが初登場であることをアサート
            }
            statusCodes.add(memberStatusCode);
            lastStatusCode[0] = memberStatusCode;//
        });
    }

    // ===================================================================================
    //                                                                 InnerJoinAutoDetect
    //                                                                        ============

    // left outer join でも inner join でも結果が変わらない結合を自動で判別して、inner joinにする機能
    // NotNullのカラムでFK制約のあるリレーションの場合、相手側にデータは必ず存在するので、inner joinにできる
    // where句で絞り込み条件として利用されているリレーションの場合、相手側にデータは必ず存在するので、inner joinにできる（一部例外あり）
    public void test_innerJoinAutoDetect() {
        // ## Act ##
        // inner join member_status dfrel_0 on dfloc.MEMBER_STATUS_CODE = dfrel_0.MEMBER_STATUS_CODE
        ListResultBean<Member> member_1 = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
        });

        // left outer join member_withdrawal dfrel_3 on dfloc.MEMBER_ID = dfrel_3.MEMBER_ID
        ListResultBean<Member> member_2 = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
        });

        // inner join member_withdrawal dfrel_3 on dfloc.MEMBER_ID = dfrel_3.MEMBER_ID
        ListResultBean<Member> member_3 = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
            cb.query().queryMemberWithdrawalAsOne().setWithdrawalReasonCode_Equal_Frt();
        });
    }
}
