package org.docksidestage.handson.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberServiceBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO tanaryo ↑unusedの警告を見ましょう by jflute (2025/06/19)
/**
 * author tanaryo
 */
public class HandsOn07Logic {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
	// TODO tanaryo unusedの警告を見ましょう by jflute (2025/06/19)
    private static final Logger logger = LoggerFactory.getLogger(HandsOn07Logic.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private MemberServiceBhv memberServiceBhv;
    @Resource
    private CurrentTimeLogic currentTimeLogic;

    // ===================================================================================
    //                                                                              suffix
    //                                                                        ============
    // TODO tanaryo javadoc, (NotNull) をお願いします by jflute (2025/06/19)
    /**
     * 自分自身の会員を登録
     *
     * @param member 会員
     */
    public void insertMyselfMember(Member member) {
        doInsertMyselfMember(member);
    }

    // TODO tanaryo javadoc, 会員セキュリティのところ、"登録時の動的項目のみsetしてください" みたいな一言 by jflute (2025/06/19)
    // TODO tanaryo javadoc, 会員のところ、"新規登録会員" みたいなニュアンスが入ると良い by jflute (2025/06/19)
    /**
     * 誰かを正式会員として登録
     * 業務的に必須の関連テーブルも登録
     *　ER図的には会員セキュリティと会員サービスか
     *　若干schemaHTMLと差異あり
     *
     * @param member 会員
     * @param memberSecurity 会員セキュリティ
     */
    public void insertYourselfMember(Member member, MemberSecurity memberSecurity) {
        doInsertMyselfMember(member);
        // [1on1でのふぉろー] SecurityをEntityで受け取るか？個別引数で受け取るか？のジレンマ
        // スーパー厳密性を求めるなら、個別引数 or 専用引数入れ物クラス、だけど...
        // 個別引数も数が多かったら問題あるし...専用引数入れ物クラスは大げさ？
        // ここはもうアクセルの踏み具合次第なので完全にケースバイケース。
        // 現状なら今のままでもそこまで悪くない。このジレンマを意識しておくことが大事。
        // 業務の重要性にも寄る。超重要業務だったら、ガチガチにやってもいいしと。
        insertMemberSecurity(member.getMemberId(), memberSecurity);
        insertMemberService(member.getMemberId());
    }

    // TODO tanaryo タグコメント Assist Logic 入れてみましょう by jflute (2025/06/19)
    // TODO tanaryo 御自身で気づいたら点、ここはもうMyselfではない by jflute (2025/06/19)
    /**
     * 会員を登録
     * 正式会員で登録
     * 現在日時を取得する Logic を作成して、正式会員日時を入れる
     *
     * @param member 会員
     */
    private void doInsertMyselfMember(Member member) {
        member.setMemberStatusCode_正式会員();
        member.setFormalizedDatetime(currentTimeLogic.currentDateTime());
        memberBhv.insert(member);
    }

    /**
     * 会員セキュリティを登録
     *
     * @param memberId 会員Id
     * @param memberSecurity 会員セキュリティ
     */
    private void insertMemberSecurity(Integer memberId, MemberSecurity memberSecurity) {
        memberSecurity.setMemberId(memberId);
        memberSecurityBhv.insert(memberSecurity);
    }

    /**
     * 会員サービスの初回登録
     * 初回なので会員ポイントは0、会員ランクはプラスチック
     *
     * @param memberId 会員id
     */
    private void insertMemberService(Integer memberId) {
    	// [いいね] Serviceは会員登録時に動的な値がないので内部で閉じてるのGood
        MemberService memberService = new MemberService();
        memberService.setMemberId(memberId);
        memberService.setAkirakaniOkashiiKaramuMei(0);
        memberService.setServiceRankCode_Plastic();
        memberServiceBhv.insert(memberService);
    }
}
