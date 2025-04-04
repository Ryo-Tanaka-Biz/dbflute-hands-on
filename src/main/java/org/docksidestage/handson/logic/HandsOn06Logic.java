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

/**
 * @author tanaryo
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

        memberList.forEach(member -> {
            logMemberInfo(member);
        });

        // done tanaryo ListResultBean自体がListなので、内部Listを取り出す必要はないです by jflute (2025/03/28)
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
    // TODO tanaryo メソッド名もINFOじゃなくDEBUGで by jflute (2025/04/04)
    // logってメソッド名はわりとよくある。一方で、debugMember() みたいなのもアリ。
    private void logMemberInfo(Member member) {
        String name = member.getMemberName();
        LocalDate birthDate = member.getBirthdate();
        LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
        logger.debug("会員名称={}, 生年月日={}, 正式会員日時={}", name, birthDate, formalizedDatetime);
    }
}
