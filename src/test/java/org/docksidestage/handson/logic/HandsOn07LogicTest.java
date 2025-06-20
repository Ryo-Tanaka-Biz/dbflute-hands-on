package org.docksidestage.handson.logic;

import java.time.LocalDate;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberServiceBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// TODO done tanaryo テストクラス、Logicのテストクラスの名前と配置にしましょう by jflute (2025/06/19)

/**
 * @author tanaryo
 */
public class HandsOn07LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                    =========
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private MemberServiceBhv memberServiceBhv;
    @Resource
    private PurchaseBhv purchaseBhv;
    @Resource
    private HandsOn07Logic logic;

    // ===================================================================================
    //                                                                            register
    //                                                                        ============

    /**
     * 登録後の Entity から主キーの値を使って検索すること
     * とりあえず、会員名称と生年月日だけアサート
     */
    public void test_insertMyselfMember_会員が登録されていること() {
        // ## Arrange ##
    	// TODO done tanaryo 定数でも、ローカル変数の場合は、普通のキャメルケースでも良い (というかそっちが多い) by jflute (2025/06/19)
        String memberAccount = "AAAAA";
        String memberName = "田中太郎";
        LocalDate memberBirthdate = LocalDate.of(2000, 12, 31);

        Member member = new Member();
        member.setMemberAccount(memberAccount);
        member.setMemberName(memberName);
        member.setBirthdate(memberBirthdate);

        // [1on1でのふぉろー] UnitTestでのAccessContextのセットしているところついて。(あと、現場での話)
        // TODO tanaryo コメントアウトにはコメントを by jflute (2025/06/19)
        //        member.setRegisterDatetime(LocalDateTime.of(2000, 12, 31, 12, 0));
        //        member.setRegisterUser("あああああ");
        //        member.setUpdateDatetime(LocalDateTime.of(2000, 12, 31, 12, 0));
        //        member.setUpdateUser("あああああ");
        //        member.setVersionNo(1L);

        // ## Act ##
        logic.insertMyselfMember(member);//このタイミングでPKをentityにセットしているんだっけ確か
        // TODO tanaryo この時点で、Optionalは解決してしまった方が万が一のときのエラーメッセージがわかりやすくなる by jflute (2025/06/19)
        OptionalEntity<Member> memberOpt = memberBhv.selectByPK(member.getMemberId());

        // ## Assert ##
        assertEquals(memberName, memberOpt.map(op -> op.getMemberName()).orElse(null));
        assertEquals(memberBirthdate, memberOpt.map(op -> op.getBirthdate()).orElse(null));
    }
    // 共通カラムの設定したらschemaHTMLの共通カラムがグレーアウトした
    // 共通カラムのセットをコメントアウトしてテスト通ることを確認

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============
    public void test_insertYourselfMember_会員が登録されていること() {
        // ## Arrange ##
        String memberAccount = "BBBBB";
        String memberName = "山田ゆりこ";
        LocalDate memberBirthdate = LocalDate.of(1995, 6, 5);

        String loginPassword = "aaa";
        String reminderQuestion = "bbb";
        String reminderAnswer = "ccc";

        Member member = new Member();
        member.setMemberAccount(memberAccount);
        member.setMemberName(memberName);
        member.setBirthdate(memberBirthdate);

        MemberSecurity memberSecurity = new MemberSecurity();
        memberSecurity.setLoginPassword(loginPassword);
        memberSecurity.setReminderQuestion(reminderQuestion);
        memberSecurity.setReminderAnswer(reminderAnswer);

        // ## Act ##
        logic.insertYourselfMember(member, memberSecurity);
        OptionalEntity<Member> memberOpt = memberBhv.selectByPK(member.getMemberId());
        // TODO tanaryo そもそも、orElse(null)ってピンポイント以外は使わないって感覚で良い by jflute (2025/06/19)
        // そのピンポイントとは？ => 本当に相手が null を求めているとき (引数とか、JSONの項目とか)
        // それ以外では、ちゃんと「ないかもしれない」という状態で管理して、解決すべきに解決する。
        // 業務的にあった当然でなければ例外でも良いような場合は、orElseThrow()系。
        // 分岐であれば ifPresent() だし、デフォルト値解決であれば orElse(デフォルト値)。
        Integer memberId = memberOpt.map(op -> op.getMemberId()).orElse(null);

        int securityCount = memberSecurityBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));
        int serviceCount = memberServiceBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));
        int purchaseCount = purchaseBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));

        // ## Assert ##
        assertEquals(memberName, memberOpt.map(op -> op.getMemberName()).orElse(null));
        assertEquals(1, securityCount);
        assertEquals(1, serviceCount);
        assertEquals(0, purchaseCount);

        //自身の登録か、誰かの登録なのかで登録処理は一緒にした
    }
}
