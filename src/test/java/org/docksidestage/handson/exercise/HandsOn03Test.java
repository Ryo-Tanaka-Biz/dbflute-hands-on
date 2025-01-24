package org.docksidestage.handson.exercise;

import java.time.LocalDate;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;

public class HandsOn03Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    public void test_searchMembers_silver_1 () {
        LocalDate targetDate = LocalDate.of(1968,1,1);
         ListResultBean<Member> members = memberBhv.selectList(cb ->{
             cb.query().setMemberName_LikeSearch("S",op -> op.likePrefix());
             cb.query().addOrderBy_Birthdate_Asc();
             cb.setupSelect_MemberStatus();
             cb.query().setBirthdate_LessEqual(targetDate);
         });
        System.out.println(members);
    }
}
