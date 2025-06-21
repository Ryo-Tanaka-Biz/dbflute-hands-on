package org.docksidestage.handson.logic;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberServiceBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberService;

// done tanaryo ↑unusedの警告を見ましょう by jflute (2025/06/19)

// done tanaryo 細かいけど、authorは @author というようにアットマークを付けないと認識されないです by jflute (2025/06/21)
/**
 * @author tanaryo
 */
public class HandsOn07Logic {
    // done tanaryo unusedの警告を見ましょう by jflute (2025/06/19)
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
    //                                                                               Logic
    //                                                                        ============
    // done tanaryo javadoc, (NotNull) をお願いします by jflute (2025/06/19)
    /**
     * 自分自身の会員を登録
     *
     * @param member 会員(NotNull)
     */
    public void insertMyselfMember(Member member) {
        doInsertMember(member);
    }

    // done tanaryo javadoc, 会員セキュリティのところ、"登録時の動的項目のみsetしてください" みたいな一言 by jflute (2025/06/19)
    // done tanaryo javadoc, 会員のところ、"新規登録会員" みたいなニュアンスが入ると良い by jflute (2025/06/19)
    /**
     * 誰かを正式会員として登録
     * 業務的に必須の関連テーブルも登録
     *　ER図的には会員セキュリティと会員サービスか
     *　若干schemaHTMLと差異あり
     *
     * @param newMember 新規登録会員(NotNull)
     * @param memberSecurity 会員セキュリティ(NotNull、登録時の動的項目のみsetしてください)
     */
    public void insertYourselfMember(Member newMember, MemberSecurity memberSecurity) {
        doInsertMember(newMember);
        // [1on1でのふぉろー] SecurityをEntityで受け取るか？個別引数で受け取るか？のジレンマ
        // スーパー厳密性を求めるなら、個別引数 or 専用引数入れ物クラス、だけど...
        // 個別引数も数が多かったら問題あるし...専用引数入れ物クラスは大げさ？
        // ここはもうアクセルの踏み具合次第なので完全にケースバイケース。
        // 現状なら今のままでもそこまで悪くない。このジレンマを意識しておくことが大事。
        // 業務の重要性にも寄る。超重要業務だったら、ガチガチにやってもいいしと。
        insertMemberSecurity(newMember.getMemberId(), memberSecurity);
        insertMemberService(newMember.getMemberId());
    }
    /**
     * 会員セキュリティを登録
     *
     * @param memberId 会員Id(NotNull)
     * @param memberSecurity 会員セキュリティ(NotNull)
     */
    private void insertMemberSecurity(Integer memberId, MemberSecurity memberSecurity) {
        memberSecurity.setMemberId(memberId);
        memberSecurityBhv.insert(memberSecurity);
    }

    /**
     * 会員サービスの初回登録
     * 初回なので会員ポイントは0、会員ランクはプラスチック
     *
     * @param memberId 会員Id(NotNull)
     */
    private void insertMemberService(Integer memberId) {
        // [いいね] Serviceは会員登録時に動的な値がないので内部で閉じてるのGood
        MemberService memberService = new MemberService();
        memberService.setMemberId(memberId);
        memberService.setServicePointCount(0);
        memberService.setServiceRankCode_Plastic();
        memberServiceBhv.insert(memberService);
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    // done tanaryo タグコメント Assist Logic 入れてみましょう by jflute (2025/06/19)
    // done tanaryo 御自身で気づいたら点、ここはもうMyselfではない by jflute (2025/06/19)
    /**
     * 会員を登録
     * 正式会員で登録
     * 現在日時を取得する Logic を作成して、正式会員日時を入れる
     *
     * @param member 会員(NotNull)
     */
    private void doInsertMember(Member member) {
        member.setMemberStatusCode_正式会員();
        member.setFormalizedDatetime(currentTimeLogic.currentDateTime());
        memberBhv.insert(member);
    }
}
