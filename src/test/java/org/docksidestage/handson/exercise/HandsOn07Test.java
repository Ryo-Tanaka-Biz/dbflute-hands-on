package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberServiceBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.logic.HandsOn07Logic;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author tanaryo
 */
public class HandsOn07Test extends UnitContainerTestCase {
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
        String MEMBER_ACCOUNT = "AAAAA";
        String MEMBER_NAME = "田中太郎";
        LocalDate MEMBER_BIRTHDATE = LocalDate.of(2000, 12, 31);

        Member member = new Member();
        member.setMemberAccount(MEMBER_ACCOUNT);
        member.setMemberName(MEMBER_NAME);
        member.setBirthdate(MEMBER_BIRTHDATE);

        //        member.setRegisterDatetime(LocalDateTime.of(2000, 12, 31, 12, 0));
        //        member.setRegisterUser("あああああ");
        //        member.setUpdateDatetime(LocalDateTime.of(2000, 12, 31, 12, 0));
        //        member.setUpdateUser("あああああ");
        //        member.setVersionNo(1L);

        // ## Act ##
        logic.insertMyselfMember(member);//このタイミングでPKをentityにセットしているんだっけ確か
        OptionalEntity<Member> memberOpt = memberBhv.selectByPK(member.getMemberId());

        // ## Assert ##
        assertEquals(MEMBER_NAME, memberOpt.map(op -> op.getMemberName()).orElse(null));
        assertEquals(MEMBER_BIRTHDATE, memberOpt.map(op -> op.getBirthdate()).orElse(null));
    }
    // 共通カラムの設定したらschemaHTMLの共通カラムがグレーアウトした
    // 共通カラムのセットをコメントアウトしてテスト通ることを確認

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============
    public void test_insertYourselfMember_会員が登録されていること() {
        // ## Arrange ##
        String MEMBER_ACCOUNT = "BBBBB";
        String MEMBER_NAME = "山田ゆりこ";
        LocalDate MEMBER_BIRTHDATE = LocalDate.of(1995, 6, 5);

        String LOGIN_PASSWORD = "aaa";
        String REMINDER_QUESTION = "bbb";
        String REMINDER_ANSWER = "ccc";

        Member member = new Member();
        member.setMemberAccount(MEMBER_ACCOUNT);
        member.setMemberName(MEMBER_NAME);
        member.setBirthdate(MEMBER_BIRTHDATE);

        MemberSecurity memberSecurity = new MemberSecurity();
        memberSecurity.setLoginPassword(LOGIN_PASSWORD);
        memberSecurity.setReminderQuestion(REMINDER_QUESTION);
        memberSecurity.setReminderAnswer(REMINDER_ANSWER);

        // ## Act ##
        logic.insertYourselfMember(member, memberSecurity);
        OptionalEntity<Member> memberOpt = memberBhv.selectByPK(member.getMemberId());
        Integer memberId = memberOpt.map(op -> op.getMemberId()).orElse(null);

        int securityCount = memberSecurityBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));
        int serviceCount = memberServiceBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));
        int purchaseCount = purchaseBhv.selectCount(cb -> cb.query().setMemberId_Equal(memberId));

        // ## Assert ##
        assertEquals(MEMBER_NAME, memberOpt.map(op -> op.getMemberName()).orElse(null));
        assertEquals(1, securityCount);
        assertEquals(1, serviceCount);
        assertEquals(0, purchaseCount);

        //自身の登録か、誰かの登録なのかで登録処理は一緒にした
    }
}
