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

    public void test_searchMembers_silver_1() {
        // ## Arrange ##
        // ## Act ##
        LocalDate targetDate = LocalDate.of(1968, 1, 1);
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
            cb.query().addOrderBy_Birthdate_Asc();
            cb.setupSelect_MemberStatus();
            cb.query().setBirthdate_LessEqual(targetDate);
        });
        log(members);

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
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
            cb.query().addOrderBy_Birthdate_Asc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
        });
        log(members);

        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberStatus().isPresent());
            assertTrue(member.getMemberSecurityAsOne().isPresent());
        });
    }

    public void test_searchMembers_silver_3() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        log(members);
        // ## Assert ##
        assertHasAnyElement(members);
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
