package org.docksidestage.handson.logic;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// TODO tanaryo javadocをよろしくです by jflute (2025/03/28)
public class HandsOn06LogicTest extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============
    /**
     * suffix は "vic" で
     * テストメソッド名通りのアサート
     * テストが成り立っていることも(できる範囲で)アサート
     */
    public void test_selectSuffixMemberList_指定したsuffixで検索されること(){
        // ## Arrange ##
        HandsOn06Logic logic = new HandsOn06Logic();
        inject(logic);

        // ## Act ##
        List<Member> suffixMemberList = logic.selectSuffixMemberList("vic");

        // 小文字対応のsql。カラム名は大文字。またschemaHTMLのテーブル名は小文字だった。
//        select dfloc.MEMBER_ID as MEMBER_ID, dfloc.MEMBER_NAME as MEMBER_NAME, dfloc.MEMBER_ACCOUNT as MEMBER_ACCOUNT, dfloc.MEMBER_STATUS_CODE as MEMBER_STATUS_CODE, dfloc.FORMALIZED_DATETIME as FORMALIZED_DATETIME, dfloc.BIRTHDATE as BIRTHDATE, dfloc.REGISTER_DATETIME as REGISTER_DATETIME, dfloc.REGISTER_USER as REGISTER_USER, dfloc.UPDATE_DATETIME as UPDATE_DATETIME, dfloc.UPDATE_USER as UPDATE_USER, dfloc.VERSION_NO as VERSION_NO
//        from member dfloc
//        where dfloc.MEMBER_NAME like '%vic' escape '|'
//        order by dfloc.MEMBER_NAME asc

        //大文字対応のsql。またschemaHTMLのテーブル名は大文字だった。
//        select dfloc.MEMBER_ID as MEMBER_ID, dfloc.MEMBER_NAME as MEMBER_NAME, dfloc.MEMBER_ACCOUNT as MEMBER_ACCOUNT, dfloc.MEMBER_STATUS_CODE as MEMBER_STATUS_CODE, dfloc.FORMALIZED_DATETIME as FORMALIZED_DATETIME, dfloc.BIRTHDATE as BIRTHDATE, dfloc.REGISTER_DATETIME as REGISTER_DATETIME, dfloc.REGISTER_USER as REGISTER_USER, dfloc.UPDATE_DATETIME as UPDATE_DATETIME, dfloc.UPDATE_USER as UPDATE_USER, dfloc.VERSION_NO as VERSION_NO
//        from MEMBER dfloc
//        where dfloc.MEMBER_NAME like '%vic' escape '|'
//        order by dfloc.MEMBER_NAME asc

        //空文字対応でconvertEmptyToNullが追加

        // ## Assert ##
        assertHasAnyElement(suffixMemberList);
        suffixMemberList.forEach(member ->{
            assertTrue(member.getMemberName().endsWith("vic"));
        });
    }

    /**
     * 無効な値とは、nullと空文字とトリムして空文字になる値
     */
    public void test_selectSuffixMemberList_suffixが無効な値なら例外が発生すること(){
        // ## Arrange ##
        HandsOn06Logic logic = new HandsOn06Logic();
        inject(logic);

        // ## Act ##
        // ## Assert ##
        assertException(IllegalArgumentException.class, () -> logic.selectSuffixMemberList(null));
        assertException(IllegalArgumentException.class, () -> logic.selectSuffixMemberList(""));
        assertException(IllegalArgumentException.class, () -> logic.selectSuffixMemberList("   "));
    }

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============
    public void test_2() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }
}
