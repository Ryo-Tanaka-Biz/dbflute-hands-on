package org.docksidestage.handson.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO jflute 1on1, 次回DBFluteを最新版にしようくらいから (2025/04/11)
/**
 * author tanaryo
 */
public class HandsOn06Logic {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(HandsOn06Logic.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;

    // ===================================================================================
    //                                                                              suffix
    //                                                                        ============
    // done tanaryo 引数と戻り値のJavaDoc、nullを明示お願いします。(呼び出す人がとっても知りたい情報なので) by jflute (2025/03/28)
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // e.g.
    //  @param suffix 会員名称の後方一致キーワード (NotNull)
    //  @return 検索された会員リスト (NotNull)
    // _/_/_/_/_/_/_/_/_/_/
    // done tanaryo @throws, IllegalStateExceptionじゃなくてIllegalArgumentの間違いでは？ by jflute (2025/03/28)
    /**
     * 指定された suffix で会員名称を後方一致検索
     * 会員名称の昇順で並べる
     * suffixが無効な値なら例外: IllegalArgumentException
     * 会員名称、生年月日、正式会員日時を画面に表示する想定でログ出力 (Slf4j or CommonsLogging)
     * そのログのログレベル、INFO/DEBUGどちらがいいか考えて実装してみましょう (この先ずっと同じ)
     * このメソッドは、他の人が呼び出すことを想定して public にしましょう (この先ずっと同じ)
     *
     * @param suffix 指定する文字列 (NotNull)
     * @return 会員リスト(NotNull)
     * @throws IllegalArgumentException 文字列が{@code null}と空文字とトリムして空文字になる場合
     */
    public List<Member> selectSuffixMemberList(String suffix) {
        assertValidString(suffix);

        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // javadocに和名はないことを確認。
            // dfprop修正後、(会員名称)があることを確認。またスキーマhtmlでは説明のみになっていることを確認
            cb.query().setMemberName_LikeSearch(suffix, op -> op.likeSuffix());
            cb.query().addOrderBy_MemberName_Asc();
        });

		 // done tanaryo ログについての学習 by jflute (2025/04/11)
		 // http://www.slideshare.net/miyakawataku/concepts-and-tools-of-logging-in-java
		 //
		 // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
		 // 前提: デバッグログは本番では出力しない。あくまで開発時のデバッグ用のため。本番では出力する処理がパフォーマンスコストになるため。
		 // 現場によって違うが、ERROR や INFO は本番で出力されて DEBUG は本番では出力されない、というパターンが多いような気がする。
		 //
		 //   logger.debug("member: " + member.getMemberName() + ", " + member.getBirthdate());
		 //
		 // と、書いても。本番では出力されない。debug()メソッドの中で "(極端な話)本番だったら何もしない" という処理が入っている。
		 // なので、"ログを出力する" という処理自体は何も気にしなくてもスキップできるが、
		 //
		 //   "member: " + member.getMemberName() + ", " + member.getBirthdate()
		 //
		 // という文字列を連結する処理 (引数の文字列を生成する処理) は本番でも走ってしまう。
		 // どうせその文字列はどこにも出力されないのに生成されてしまう。これは無駄な処理である。
		 // なので...
		 //
		 //   # if (logger.isDebugEnabled()) {
		 //   #     logger.debug("member: " + member.getMemberName() + ", " + member.getBirthdate());
		 //   #}
		 //
		 // というように、判定を入れると文字列生成処理も本番でスキップすることができる。
		 // だが、うざい。
		 //
		 // Slf4j ではこのように書けるようになったので、isDebugEnabled()しなくてもOK。文字列連結処理が発生しない。
		 // 
		 //   # logger.debug("member: {}, {}", member.getMemberName(), member.getBirthdate());
		 //
		 // とはいえ、
		 //
		 //   # for (Member member : memberList) {
		 //   #     logger.debug("member: {}, {}", member.getMemberName(), member.getBirthdate());
		 //   # }
		 //
		 // という場合は、デバッグ処理のためだけのループが本番で回ってしまうので、isDebugEnabled()を全く使わないわけではない。
		 //
		 //   # if (logger.isDebugEnabled()) {
		 //   #     for (Member member : memberList) {
		 //   #         logger.debug("member: {}, {}", member.getMemberName(), member.getBirthdate());
		 //   #     }
		 //   # }
		 // _/_/_/_/_/_/_/_/_/_/
        // TODO tanaryo なので、本番はループさせたくない by jflute (2025/05/09)
        memberList.forEach(member -> {
            debugMember(member); // メソッド名を変更
        });

        return memberList;
    }

    private void assertValidString(String suffix) {
        if (suffix == null || suffix.trim().isEmpty()) {
        	// done tanaryo [いいね] 素晴らしい、ちゃんとsuffixがnullだったのか空だったのかわかる by jflute (2025/03/28)
            throw new IllegalArgumentException("The specified suffix is null or empty. suffix:" + suffix);
        }
    }

    // done tanaryo 毎度の検索結果を本番でログに残すことはあまりないので、DEBUGにしてみましょう by jflute (2025/03/28)
    // [1on1でのふぉろー] 本番のログの容量との兼ね合いの話 (トラブルが起きたときだけ見るものだし)
    // done tanaryo メソッド名もINFOじゃなくDEBUGで by jflute (2025/04/04)
    // logってメソッド名はわりとよくある。一方で、debugMember() みたいなのもアリ。
    private void debugMember(Member member) { // メソッド名を変更
        String name = member.getMemberName();
        LocalDate birthDate = member.getBirthdate();
        LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
        logger.debug("会員名称={}, 生年月日={}, 正式会員日時={}", name, birthDate, formalizedDatetime);
    }
}
