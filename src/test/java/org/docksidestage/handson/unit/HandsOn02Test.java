package org.docksidestage.handson.unit;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;

// TODO tanaryo javadocお願いします by jflute (2025/01/17)
// TODO tanaryo package, 正しくは、exercise パッケージです by jflute (2025/01/17)

public class HandsOn02Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    // TODO tanaryo Arrange, Act, Assert のコメントを入れてるようにお願いします by jflute (2025/01/17)
    // Arrangeないときは空っぽでOK
    public void test_existsTestData() throws Exception {
        int count = memberBhv.selectCount(cb ->{});
        assertTrue(count != 0);

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
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S",op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });
        //selectEntityはエンティティを一件に特定する
        //一意に特定できないとエラー起きる
        //selectListとselectEntityの使い分け

        //なぜかログが出力されない。
        //依存関係は問題なさそうだった。一旦System.out.printlnで進める

        // TODO tanaryo 万が一、テストデータが空っぽとかで検索0件だったら...素通りgreenになっちゃう by jflute (2025/01/17)
        // allMatch()のjavadocを見ると、空っぽの時はtrueを戻す、とのこと。空っぽでallMatch(),anyMatch()のときは気を付けて。
        // Optionalのアリナシと同じ用に、リストの0か1以上かってのもわりと大違いなので常に意識を。
        // ということで、素通りgreenならないように素通り防止をしましょう。(この後のエクササイズすべて同じ)
        // TODO tanaryo ログ出すなら、assertよりも前の方が、落ちたとき見れる by jflute (2025/01/17)
        // TODO tanaryo ListResultBean自体が、ListでtoString()オーバーライドしてるので、getSelectedList()の必要ない by jflute (2025/01/17)
        assertTrue(members.stream().allMatch(member -> member.getMemberName().startsWith("S")));
        log("members:" + members.getSelectedList());
    }

    public void test_searchMembers_memberId_equal_1() throws Exception {
    	// TODO tanaryo membersではない、単体なので by jflute (2025/01/17)
    	// でもこのまま members を member にすると、本物の member と変数名がかぶる。
    	// OptionalEntityの変数名は？
    	// https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/select/selectentity.html#optionalname
    	// の通り、optMember がオススメではあります。
    	// 一方で、Optionalを極力変数で置かないやり方(メソッドチェーン)もあります。
    	// ただこのケースではちょっと向かないかも。
        OptionalEntity<Member> members = memberBhv.selectEntity(cb -> {
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
		
        // TODO tanaryo Optionalをリスト的に扱ってるけど、ここでは一件なのでその必要がない by jflute (2025/01/17)
        // Optional@stream() は、リストと区別なく抽象的に扱いたい時に使うもので普段はあまり使わなくていいかなと。
        // TODO tanaryo 一応、Integerは == ではなく equals() で比較しておきましょう by jflute (2025/01/17)
        // https://dbflute.seasar.org/ja/manual/topic/programming/java/beginners.html#equalsequal
        // dotって打ってequals()が出てきたらequals()使うくらいでもいい。
        assertTrue(members.stream().allMatch(member -> member.getMemberId() == 1));
        log("members:" + members.get());
    }

    public void test_searchMembers_birthDate_isNull() throws Exception {
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Asc();
        });

        assertTrue(members.stream().allMatch(member -> member.getBirthdate() == null));
        log("members:" + members.getSelectedList());
    }
}

