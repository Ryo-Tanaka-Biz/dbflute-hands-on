package org.docksidestage.handson.dbflute.exentity;

import java.time.LocalDateTime;

import org.docksidestage.handson.dbflute.bsentity.BsMember;

/**
 * The entity of member.
 * <p>
 * You can implement your original methods here.
 * This class remains when re-generating.
 * </p>
 * @author DBFlute(AutoGenerator)
 * @author tanaryo
 */
public class Member extends BsMember {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    public static final String ALIAS_lastLoginDatetime = "LAST_LOGIN_DATETIME";

    public static final String ALIAS_fmlLoginCount = "FML_LOGIN_COUNT";

    public static final String ALIAS_mobileLoginCount = "MOBILE_LOGIN_COUNT";

    public static final String ALIAS_productKindCount = "PRODUCT_KIND_COUNT";

    public static final String ALIAS_payedMaxPurchasePrice = "PAYED_MAX_PURCHASE_PRICE";

    protected LocalDateTime lastLoginDatetime;

    protected Integer fmlLoginCount;

    protected Integer mobileLoginCount;

    protected Integer productKindCount;

    protected Integer payedMaxPurchasePrice;

    public LocalDateTime getLastLoginDatetime() {
        return lastLoginDatetime;
    }

    public Integer getFmlLoginCount() {
        return fmlLoginCount;
    }

    public Integer getMobileLoginCount() {
        return mobileLoginCount;
    }

    public Integer getProductKindCount() {
        return productKindCount;
    }

    public Integer getPayedMaxPurchasePrice() {
        return payedMaxPurchasePrice;
    }

    public void setLastLoginDatetime(LocalDateTime latestLoginDatetime) {
        this.lastLoginDatetime = latestLoginDatetime;
    }

    public void setFmlLoginCount(Integer fmlLoginCount) {
        this.fmlLoginCount = fmlLoginCount;
    }

    public void setMobileLoginCount(Integer mobileLoginCount) {
        this.mobileLoginCount = mobileLoginCount;
    }

    public void setProductKindCount(Integer productKindCount) {
        this.productKindCount = productKindCount;
    }

    public void setPayedMaxPurchasePrice(Integer payedMaxPurchasePrice) {
        this.payedMaxPurchasePrice = payedMaxPurchasePrice;
    }
}
