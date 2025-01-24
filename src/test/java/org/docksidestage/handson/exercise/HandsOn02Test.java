package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;

// TODO done tanaryo javadocお願いします by jflute (2025/01/17)
// TODO done tanaryo package, 正しくは、exercise パッケージです by jflute (2025/01/17)

/**
 * @author tanaryo
 */
public class HandsOn02Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    // TODO done tanaryo Arrange, Act, Assert のコメントを入れてるようにお願いします by jflute (2025/01/17)
    // Arrangeないときは空っぽでOK
    public void test_existsTestData() throws Exception {
        // ## Arrange ##
        // ## Act ##
        int count = memberBhv.selectCount(cb ->{});

        // ## Assert ##
        assertTrue(count > 0);

        /////////////////////////////////////////////////////////////////////////////////
        // 20-member.xls と 30-product.xls を配置してReplaceSchemaしたらエラー出た
        // エラーメッセージは"Field 'REGISTER_DATETIME' doesn't have a default value"
        // Initialize Schema（初期化）  ->Create Schema（箱作る）　-> Load Data（レコード挿入）
        // Create Schemaには成功していそう
        // 20-member.xlsを元にしたデータ挿入で、REGISTER_DATETIMEがおかしい？
        // 共通カラムが挿入されていない。どこで挿入されている？
        // commonColumnMap.dfpropでコメントアウトされている部分がある。そこを有効にしてみるといけるのかな。。。
        // 分析はこのくらいにして、ドキュメントを見る
        ///////////////////////////////////////////////////////////////////////////////////
        // [1on1でのフォロー] ReplaceSchemaの例外メッセージで、例外の翻訳の再勉強
        // MySQLの中で例外の翻訳ができてない説
    }

    public void test_searchMembers_memberName_startWith_S() throws Exception {
    	// [1on1でのふぉろー] DBFluteは、Java6版とJava8版があります。
    	//  Java6版(2014年まで): 条件値でnullを受け付けたら条件なしになる
    	//  Java8版(2015年より): 条件値でnullを受け付けたら例外になる
    	// Java6版からJava8版に移行しても、既存コードの互換性を配慮して、互換性モードを追加することが多い。
    	// 移行 1.0.x to 1.1 | DBFlute
    	// https://dbflute.seasar.org/ja/environment/upgrade/migration/migrate10xto11x.html
    	// littleAdjustmentMap.dfprop にて、以下が設定されてるとJava8版でもJava6版の挙動になる。
    	// ; isNullOrEmptyQueryAllowed = true
    	// こういう現場があれば、できれば移行して欲しい。

        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S",op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });
        //selectEntityはエンティティを一件に特定する
        //一意に特定できないとエラー起きる
        //selectListとselectEntityの使い分け

        //なぜかログが出力されない。
        //依存関係は問題なさそうだった。一旦System.out.printlnで進める

        // TODO done tanaryo 万が一、テストデータが空っぽとかで検索0件だったら...素通りgreenになっちゃう by jflute (2025/01/17)
        // allMatch()のjavadocを見ると、空っぽの時はtrueを戻す、とのこと。空っぽでallMatch(),anyMatch()のときは気を付けて。
        // Optionalのアリナシと同じ用に、リストの0か1以上かってのもわりと大違いなので常に意識を。
        // ということで、素通りgreenならないように素通り防止をしましょう。(この後のエクササイズすべて同じ)
        // TODO done tanaryo ログ出すなら、assertよりも前の方が、落ちたとき見れる by jflute (2025/01/17)
        // TODO done tanaryo ListResultBean自体が、ListでtoString()オーバーライドしてるので、getSelectedList()の必要ない by jflute (2025/01/17)

        // ## Assert ##
        log("members:" + members);

        assertTrue(members.getAllRecordCount() > 0);
        assertTrue(members.getSelectedList().stream().allMatch(member -> member.getMemberName().startsWith("S")));
    }

    public void test_searchMembers_memberId_equal_1() throws Exception {
    	// TODO done tanaryo membersではない、単体なので by jflute (2025/01/17)
    	// でもこのまま members を member にすると、本物の member と変数名がかぶる。
    	// OptionalEntityの変数名は？
    	// https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/select/selectentity.html#optionalname
    	// の通り、optMember がオススメではあります。
    	// 一方で、Optionalを極力変数で置かないやり方(メソッドチェーン)もあります。
    	// ただこのケースではちょっと向かないかも。

        // ## Arrange ##
        // ## Act ##
        OptionalEntity<Member> optMember = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(1);
        });

        // [1on1でのふぉろー] selectEntity()のときはPK,UQがほとんど、業務ルールで1件とか最初の1件もなくはない
        // [1on1でのふぉろー] DBFluteのOptionalでは、alwaysPresent() というメソッドも用意されている。
        // このケースだと常に存在する(存在しなかったら例外で落ちていいよ)ってケース、
        // かつ、その場で消費しておしまいのケース(Lambda式で完結する)、に使うと良い。
		//memberBhv.selectEntity(cb -> {
		//    cb.query().setMemberId_Equal(99999);
		//}).alwaysPresent(member -> {
		//	  assertTrue(member.getMemberId() == 1);
		//});
        // あと、DBFluteのOptionalであれば、get()やalwaysPresent()の例外メッセージがリッチなので、
        // 業務的に必ず存在してるってケースでは積極的に使って良い。
        // (Java標準のOptionalの場合は、デバッグ情報ない例外が上がるだけなので問答無用get()はあまりしない)
		
        // TODO done tanaryo Optionalをリスト的に扱ってるけど、ここでは一件なのでその必要がない by jflute (2025/01/17)
        // Optional@stream() は、リストと区別なく抽象的に扱いたい時に使うもので普段はあまり使わなくていいかなと。
        // TODO done tanaryo 一応、Integerは == ではなく equals() で比較しておきましょう by jflute (2025/01/17)
        // https://dbflute.seasar.org/ja/manual/topic/programming/java/beginners.html#equalsequal
        // dotって打ってequals()が出てきたらequals()使うくらいでもいい。
        // enumはインスタンスが単一のため、比較は ==の方が望ましい？


        // ## Assert ##
        log("optMember:" + optMember);
        assertTrue(optMember.get().getMemberId().equals(1));
    }

    public void test_searchMembers_birthDate_isNull() throws Exception {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Asc();
        });

        // ## Assert ##
        log("members:" + members);
        assertTrue(members.getAllRecordCount() > 0);

        //member.getBirthdate().equals(null)は常にfalseを返すようになっていた

        members.getSelectedList().forEach(member -> assertNull(member.getBirthdate()));
    }
}

