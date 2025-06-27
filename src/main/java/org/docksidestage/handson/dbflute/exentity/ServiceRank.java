package org.docksidestage.handson.dbflute.exentity;

import org.docksidestage.handson.dbflute.bsentity.BsServiceRank;

/**
 * The entity of service_rank.
 * <p>
 * You can implement your original methods here.
 * This class remains when re-generating.
 * </p>
 * @author DBFlute(AutoGenerator)
 * @author tanaryo
 */
public class ServiceRank extends BsServiceRank {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    public static final String ALIAS_memberCount = "MEMBER_COUNT";

    public static final String ALIAS_totalPurchasePrice = "TOTAL_PURCHASE_PRICE";

    public static final String ALIAS_avgMaxPurchasePrice = "AVG_MAX_PURCHASE_PRICE";

    public static final String ALIAS_totalLoginCount = "TOTAL_LOGIN_COUNT";

    protected Integer memberCount;

    protected Integer totalPurchasePrice;

    protected Integer avgMaxPurchasePrice;

    protected Integer totalLoginCount;

    public Integer getMemberCount() {
        return memberCount;
    }

    public Integer getTotalPurchasePrice() {
        return totalPurchasePrice;
    }

    public Integer getAvgMaxPurchasePrice() {
        return avgMaxPurchasePrice;
    }

    public Integer getTotalLoginCount() {
        return totalLoginCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public void setTotalPurchasePrice(Integer totalPurchasePrice) {
        this.totalPurchasePrice = totalPurchasePrice;
    }

    public void setAvgMaxPurchasePrice(Integer avgMaxPurchasePrice) {
        this.avgMaxPurchasePrice = avgMaxPurchasePrice;
    }

    public void setTotalLoginCount(Integer totalLoginCount) {
        this.totalLoginCount = totalLoginCount;
    }
}
