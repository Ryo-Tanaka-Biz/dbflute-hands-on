package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author tanaryo
 */
public class HandsOn03Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;

    // [1on1でのふぉろー] 以前と言われたらどう思うか？どう実装するか？ => 実務では聞き返すがGood by たなりょうさん
    // 日本語の日付表現ってのは曖昧部分が多いので、勝手に解釈して決めるとよくすれ違い。どっちが文法的に正しいかはもはや関係ない。
    // データベースのSTART/END, BEGIN/ENDの期間を表すカラムの終了日時とか、どっち？どっちもよくありえる。
    public void test_searchMembers_silver_1() {
        // ## Arrange ##
        // ## Act ##
    	// TODO tanaryo targetDateはArrangeでOKです。Actは純粋にテスト対象の実行のみするのが慣習 by jflute (2025/01/30)
    	LocalDate targetDate = LocalDate.of(1968, 1, 1);
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
        	// TODO tanaryo メソッド呼び出し順序、慣習ではあるけれども、select句、where句、order by句に合わせて by jflute (2025/01/30)
        	// http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
            cb.query().addOrderBy_Birthdate_Asc();
            cb.setupSelect_MemberStatus();
            cb.query().setBirthdate_LessEqual(targetDate);
        });
        // TODO tanaryo log()も似たような話で、logはAssert配下でOK。アサートじゃないと言えるかもだけど、目視確認の一環なので by jflute (2025/01/30)
        log(members);

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
        	// TODO tanaryo ちょっとコードが膨れてるなと思ったら変数抽出して欲しい。 by jflute (2025/01/30)
        	// 特にここは判定ロジック入るところで、できるだけ判定ロジックがパット見で認識できるようにしたい。
        	// IntelliJのショートカットでサクッとできるはず。
        	// 一方で、最初から変数に出すのではなく後から出すでOK。書くときは流れに任せてどっとどっとした方が速いってもあるので。
        	// 出した後に見栄えを考えて、ちょっとこれは...と思ったらショートカット使えば良い。
        	// なので、変数抽出ショートカットは、息を吸うかのごとくできるのがオススメ。
            assertTrue(member.getBirthdate().isBefore(targetDate) || member.getBirthdate().isEqual(targetDate));
        });
    }

    public void test_searchMembers_silver_2() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            //生年月日がない人は後ろに回す
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            // TODO tanaryo [いいね] nullの制御できててGood by jflute (2025/01/30)
            // TODO tanaryo 一方で、若い順になってない。「若いと数字が若いは別 by tanaryo」 by jflute (2025/01/30)
            // 実は、DescにするとMySQLだと自然とnullは後ろになるので、nullsLastさんなくても良かったとは言える。
            // ただ、nullを小さな値と解釈して並べるのが世界標準かどうかは？はちょと怪しい。別のDBMSだったら違うかも。
            // ということなので、MySQLのデフォルト挙動に依存したSQLにしてると、いつかDBMS移行したときに痛い目に遭う。
            // なので、結果としては、一瞬要らないように思えても、NullsLastしておいても良い。
            cb.query().addOrderBy_Birthdate_Asc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
        });
        log(members);

        // ## Assert ##
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
    	// TODO tanaryo 試しに、cbの条件をなくしてわざと落とすようにしてみたけど、落ちない by jflute (2025/01/30)
    	// 一度は、assertがredになってちゃんとassertのロジックが合ってることを確認しましょう。
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
        	// [1on1でのふぉろー] 関連テーブルの絞り込みGood
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        log(members);
        // ## Assert ##
        assertHasAnyElement(members);
        // TODO tanaryo さすがに、インライン過ぎて見逃しとかしやすいので、もうちょい変数出してstep踏んでもいいかなと by jflute (2025/01/30)
        members.forEach(member -> assertNotNull(memberSecurityBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(member.getMemberId());
            cb.query().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        })));
    }
    public void test_searchMembers_gold_4() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberStatus().isEmpty());
        });
        // TODO tanaryo やりかけのときは、todoコメントで書いておきましょう。忘れちゃうケースがよくあるので by jflute (2025/01/30)
        // よもやま: 働き方の多様性のためにも、自分のやりかけをしっかり管理する習慣を付けておいたほうが良い。
        //会員が会員ステータスごとに固まって並んでいることをアサート
    }
    public void test_searchMembers_gold_5() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }
    public void test_searchMembers_gold_6() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }
    public void test_searchMembers_platinum_7() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }
    public void test_searchMembers_platinum_8() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }
    public void test_searchMembers_platinum_9() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }
}
