package org.docksidestage.handson.dbflute.exentity;

import java.time.LocalDateTime;
import java.util.Date;

import org.docksidestage.handson.dbflute.bsentity.BsMember;

/**
 * The entity of member.
 * <p>
 * You can implement your original methods here.
 * This class remains when re-generating.
 * </p>
 * @author DBFlute(AutoGenerator)
 */
public class Member extends BsMember {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    public static final String ALIAS_lastLoginDatetime = "LAST_LOGIN_DATETIME";

    public static final String ALIAS_mobileLoginCount = "MOBILE_LOGIN_COUNT";

    protected LocalDateTime lastLoginDatetime;

    protected Integer mobileLoginCount;

    public LocalDateTime getLastLoginDatetime() {
        return lastLoginDatetime;
    }

    public Integer getMobileLoginCount() {
        return mobileLoginCount;
    }

    public void setLastLoginDatetime(LocalDateTime latestLoginDatetime) {
        this.lastLoginDatetime = latestLoginDatetime;
    }

    public void setMobileLoginCount(Integer mobileLoginCount) {
        this.mobileLoginCount = mobileLoginCount;
    }
}
