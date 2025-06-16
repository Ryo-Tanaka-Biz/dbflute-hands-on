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

/**
 * author tanaryo
 */
public class HandsOn07Logic {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
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

    /**
     * 自分自身の会員を登録
     *
     * @param member 会員
     */
    public void insertMyselfMember(Member member) {
        doInsertMyselfMember(member);
    }

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
        insertMemberSecurity(member.getMemberId(), memberSecurity);
        insertMemberService(member.getMemberId());
    }

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
        MemberService memberService = new MemberService();
        memberService.setMemberId(memberId);
        memberService.setAkirakaniOkashiiKaramuMei(0);
        memberService.setServiceRankCode_Plastic();
        memberServiceBhv.insert(memberService);
    }
}
