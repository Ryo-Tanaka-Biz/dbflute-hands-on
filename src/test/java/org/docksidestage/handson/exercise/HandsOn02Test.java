package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// done tanaryo javadocお願いします by jflute (2025/01/17)
// done tanaryo package, 正しくは、exercise パッケージです by jflute (2025/01/17)
// done tanaryo UnitContainerTestCase も一緒に連れてきちゃってる (unit) by jflute (2025/01/24)

// done tanaryo [読み物課題] 既存コードの甘い匂い (悪意なきチグハグコードの誕生) by jflute (2025/01/24)
// https://jflute.hatenadiary.jp/entry/20160203/existingcode

/**
 * @author tanaryo
 */
public class HandsOn02Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    // done tanaryo Arrange, Act, Assert のコメントを入れてるようにお願いします by jflute (2025/01/17)
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
    	// [tanaryo質問] ListResultBean が null になることってあるのか？
    	// [へんじ ]selectList()のjavadocを読んでみましょう。
    	// The result bean of selected list. (NotNull: if no data, returns empty list)
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S",op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });
        //selectEntityはエンティティを一件に特定する
        //一意に特定できないとエラー起きる
        //selectListとselectEntityの使い分け

        //なぜかログが出力されない。
        //依存関係は問題なさそうだった。一旦System.out.printlnで進める

        // done tanaryo 万が一、テストデータが空っぽとかで検索0件だったら...素通りgreenになっちゃう by jflute (2025/01/17)
        // allMatch()のjavadocを見ると、空っぽの時はtrueを戻す、とのこと。空っぽでallMatch(),anyMatch()のときは気を付けて。
        // Optionalのアリナシと同じ用に、リストの0か1以上かってのもわりと大違いなので常に意識を。
        // ということで、素通りgreenならないように素通り防止をしましょう。(この後のエクササイズすべて同じ)
        // done tanaryo ログ出すなら、assertよりも前の方が、落ちたとき見れる by jflute (2025/01/17)
        // done tanaryo ListResultBean自体が、ListでtoString()オーバーライドしてるので、getSelectedList()の必要ない by jflute (2025/01/17)

        // ## Assert ##
        log("members:" + members);

        // done tanaryo [tips] こう書いてもらって全然OKですが、HandsOnでは専用のメソッドが用意されていて... by jflute (2025/01/24)
        // assH -> assertHasAnyElement(members); もう定型的でよく呼ぶので、これを覚えちゃってください。
        assertHasAnyElement(members);
        // done tanaryo ここも似た話で、ListResultBean自体がListなので、stream()直接呼べます by jflute (2025/01/24)
        // ちなみに、ListResultBeanはEntityの一覧を意識したクラスで便利メソッドが付いてるから具象クラスで受け取っている。
        assertTrue(members.stream().allMatch(member -> member.getMemberName().startsWith("S")));
    }

    public void test_searchMembers_memberId_equal_1() throws Exception {
    	// done tanaryo membersではない、単体なので by jflute (2025/01/17)
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
        //
        // ↑それだけではなく、さらに改善したいところがあった。
        // java8のOptionalだと、ifPresent()しかできなくて、elseするならisPresent()/get()に戻す。
		//optMember.ifPresent(member -> {
		//	// あったときの処理
		//}).orElse(() -> { // DBFluteオリジナル
		//	// なかったときの処理
		//});
        // ただ、java9から、ifPresentOrElse()が入った。
		//optMember.ifPresentOrElse(member -> {
		//	// あったときの処理
		//}, () -> {
		//	// なかったときの処理
		//});
        // なので、DBFluteのorElse()は要らなくなったかなと思いきや、
        // Lambda式の引数が２つ以上とかって、けっこう書きづらいので、java9以降もorElse()使う人いらっしゃる。
        //

        // [1on1でのふぉろー] get() というメソッド
        //
        // 通常のOptionalでは、get()は避けようと言われています。(isPresent()の時は除き)
        // でも、DBFlute(DB)の場合だと、引数によって戻り値の有無が変わることが多く、業務的に絶対あるよって言い切れる場合も多い。
        // そのとき、orElseThrow(() -> new例外) をつどつど実装するかというと、みなさんなかなか面倒。
        // でもデバッグのためには必要だが...面倒の中でやったエラーハンドリングって雑になりがち。
        // 結局役に立たない例外throwになることが多い。それって、get()とデバッグ情報量が結局変わらない。
        //
        // DBFluteのOptionalでは、そのジレンマをなくすために、get()の例外情報をリッチにして気にしなくていいようにしている。
        // 業務的に必ず存在するケースなら遠慮なくget()していいよと。名前が気持ち悪いなら alwaysPresent() を。
        //
        // 一方で、じゃあ標準のOptionalだったらやっぱりget()は絶対にしないのか？
        // Java10から、引数のない orElseThrow() が導入された。
        //  e.g. optMember.orElseThrow();
        // これって、get()をやってること何も変わらない。メソッド名が変わっただけ。
        // デフォルトの例外をthrowするよって言ってるだけの強引取得メソッド。
        // これが追加されたってことは...標準のOptionalでも「業務的に絶対あるよ」ってケースはもうget()しちゃいたい、
        // というニーズがあったんじゃないか？せめて名前をちょっとわかりやすくしたものが出てきたのかなという解釈。
        //
        // ちなみに、DBFluteのOptionalは、一応標準Optionalのメソッドをすべて追従している。
        // なので、固有メソッドもあるけど、標準Optionalのつもりで使っても使えるようにしている。
        // その代わり、ifPresent().orElse(), ifPresentOrElse() 両方使えちゃうけどね。
		
        // done tanaryo Optionalをリスト的に扱ってるけど、ここでは一件なのでその必要がない by jflute (2025/01/17)
        // Optional@stream() は、リストと区別なく抽象的に扱いたい時に使うもので普段はあまり使わなくていいかなと。
        // done tanaryo 一応、Integerは == ではなく equals() で比較しておきましょう by jflute (2025/01/17)
        // https://dbflute.seasar.org/ja/manual/topic/programming/java/beginners.html#equalsequal
        // dotって打ってequals()が出てきたらequals()使うくらいでもいい。
        // enumはインスタンスが単一のため、比較は ==の方が望ましい？
        // done tanaryo [へんじ] "==" の方が望ましいと考える人もいます by jflute (2025/01/24)
        // "==" でやると、enumじゃない値を間違えて指定したときに型違いでエラーになってくれるので。
        // 一方で、ぼくは下手に "==" を使おうとして、ついenumじゃないところで "==" する間違いを生むくらいなら...
        // . (dot)で補完されたら equals() 使うmyルールにしちゃってます。
        // あと、Eclipseだと、equals() で違う型を入れたときに教えてくれるので、それで賄えている。
        // (IntelliJでも一応言われているみたい)

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
        // [1on1でのふぉろー] 改めてここで ListResultBean から、概念と実装は別物、ということを学べたかなと。
        // 以前紹介した「HashSet は内部では HashMap を new して使ってるだけ」にちょっと構造が近い。
        log("members:" + members);
        assertHasAnyElement(members);

        //member.getBirthdate().equals(null)は常にfalseを返すようになっていた

        members.forEach(member -> assertNull(member.getBirthdate()));
        
        // [1on1でのふぉろー] ListResultBean の固有メソッドの紹介
        //List<String> memberNameList = members.extractColumnList(member -> {
        //    return member.getMemberName();
        //});
        // stream().map(...).toList() と同じだけど、目的が明確な分すっかり書ける。
        // というか Java6 版から存在していたメソッド。(stream()の前からあるメソッド)
        //
        // 別のオブジェクトに詰め替えをするメソッド
        //List<NanikashiraResult> nanikashiraList = members.mappingList(member -> {
		//	return new NanikashiraResult(member);
		//});
        //
        // groupに分けるメソッド groupingList(), groupingMap()
        //List<ListResultBean<Member>> statusGroupedMemberList = members.groupingList((rowResource, nextEntity) -> {
		//    // 違うステータスが来るの境目かどうか
		//	  Member currentEntity = rowResource.getCurrentEntity();
		//	  return !currentEntity.getMemberStatusCode().equals(nextEntity.getMemberStatusCode());
		//});
        // これも stream() でできるものではあるが。
        // つまり、stream()が来る前にもこういうことがやりたくて作っていたメソッド。
        // 目的がハッキリしていてスッキリできる分、今でもわかっていれば全然使える。
        //
        // e.g. grouping per initial character of MEMBER_NAME
        //Map<String, ListResultBean<Member>> groupingMap = members.groupingMap(member -> {
        //    return member.getMemberName().substring(0, 1);
        //});
    }
}

