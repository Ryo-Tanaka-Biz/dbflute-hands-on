package org.docksidestage.handson.dbflute.bsentity.dbmeta;

import java.util.List;
import java.util.Map;

import org.dbflute.Entity;
import org.dbflute.dbmeta.AbstractDBMeta;
import org.dbflute.dbmeta.info.*;
import org.dbflute.dbmeta.name.*;
import org.dbflute.dbmeta.property.PropertyGateway;
import org.dbflute.dbway.DBDef;
import org.docksidestage.handson.dbflute.allcommon.*;
import org.docksidestage.handson.dbflute.exentity.*;

/**
 * The DB meta of product_status. (Singleton)
 * @author DBFlute(AutoGenerator)
 */
public class ProductStatusDbm extends AbstractDBMeta {

    // ===================================================================================
    //                                                                           Singleton
    //                                                                           =========
    private static final ProductStatusDbm _instance = new ProductStatusDbm();
    private ProductStatusDbm() {}
    public static ProductStatusDbm getInstance() { return _instance; }

    // ===================================================================================
    //                                                                       Current DBDef
    //                                                                       =============
    public String getProjectName() { return DBCurrent.getInstance().projectName(); }
    public String getProjectPrefix() { return DBCurrent.getInstance().projectPrefix(); }
    public String getGenerationGapBasePrefix() { return DBCurrent.getInstance().generationGapBasePrefix(); }
    public DBDef getCurrentDBDef() { return DBCurrent.getInstance().currentDBDef(); }

    // ===================================================================================
    //                                                                    Property Gateway
    //                                                                    ================
    // -----------------------------------------------------
    //                                       Column Property
    //                                       ---------------
    protected final Map<String, PropertyGateway> _epgMap = newHashMap();
    { xsetupEpg(); }
    protected void xsetupEpg() {
        setupEpg(_epgMap, et -> ((ProductStatus)et).getProductStatusCode(), (et, vl) -> {
            CDef.ProductStatus cls = (CDef.ProductStatus)gcls(et, columnProductStatusCode(), vl);
            if (cls != null) {
                ((ProductStatus)et).setProductStatusCodeAsProductStatus(cls);
            } else {
                ((ProductStatus)et).mynativeMappingProductStatusCode((String)vl);
            }
        }, "productStatusCode");
        setupEpg(_epgMap, et -> ((ProductStatus)et).getProductStatusName(), (et, vl) -> ((ProductStatus)et).setProductStatusName((String)vl), "productStatusName");
        setupEpg(_epgMap, et -> ((ProductStatus)et).getDisplayOrder(), (et, vl) -> ((ProductStatus)et).setDisplayOrder(cti(vl)), "displayOrder");
    }
    public PropertyGateway findPropertyGateway(String prop)
    { return doFindEpg(_epgMap, prop); }

    // ===================================================================================
    //                                                                          Table Info
    //                                                                          ==========
    protected final String _tableDbName = "product_status";
    protected final String _tableDispName = "PRODUCT_STATUS";
    protected final String _tablePropertyName = "productStatus";
    protected final TableSqlName _tableSqlName = new TableSqlName("PRODUCT_STATUS", _tableDbName);
    { _tableSqlName.xacceptFilter(DBFluteConfig.getInstance().getTableSqlNameFilter()); }
    public String getTableDbName() { return _tableDbName; }
    public String getTableDispName() { return _tableDispName; }
    public String getTablePropertyName() { return _tablePropertyName; }
    public TableSqlName getTableSqlName() { return _tableSqlName; }
    protected final String _tableAlias = "商品ステータス";
    public String getTableAlias() { return _tableAlias; }

    // ===================================================================================
    //                                                                         Column Info
    //                                                                         ===========
    protected final ColumnInfo _columnProductStatusCode = cci("PRODUCT_STATUS_CODE", "PRODUCT_STATUS_CODE", null, "商品ステータスコード", String.class, "productStatusCode", null, true, false, true, "CHAR", 3, 0, null, null, false, null, null, null, "productList", CDef.DefMeta.ProductStatus, false);
    protected final ColumnInfo _columnProductStatusName = cci("PRODUCT_STATUS_NAME", "PRODUCT_STATUS_NAME", null, "商品ステータス名称", String.class, "productStatusName", null, false, false, true, "VARCHAR", 50, 0, null, null, false, null, null, null, null, null, false);
    protected final ColumnInfo _columnDisplayOrder = cci("DISPLAY_ORDER", "DISPLAY_ORDER", null, "表示順", Integer.class, "displayOrder", null, false, false, true, "INT", 10, 0, null, null, false, null, null, null, null, null, false);

    /**
     * (商品ステータスコード)PRODUCT_STATUS_CODE: {PK, NotNull, CHAR(3), classification=ProductStatus}
     * @return The information object of specified column. (NotNull)
     */
    public ColumnInfo columnProductStatusCode() { return _columnProductStatusCode; }
    /**
     * (商品ステータス名称)PRODUCT_STATUS_NAME: {NotNull, VARCHAR(50)}
     * @return The information object of specified column. (NotNull)
     */
    public ColumnInfo columnProductStatusName() { return _columnProductStatusName; }
    /**
     * (表示順)DISPLAY_ORDER: {UQ, NotNull, INT(10)}
     * @return The information object of specified column. (NotNull)
     */
    public ColumnInfo columnDisplayOrder() { return _columnDisplayOrder; }

    protected List<ColumnInfo> ccil() {
        List<ColumnInfo> ls = newArrayList();
        ls.add(columnProductStatusCode());
        ls.add(columnProductStatusName());
        ls.add(columnDisplayOrder());
        return ls;
    }

    { initializeInformationResource(); }

    // ===================================================================================
    //                                                                         Unique Info
    //                                                                         ===========
    // -----------------------------------------------------
    //                                       Primary Element
    //                                       ---------------
    protected UniqueInfo cpui() { return hpcpui(columnProductStatusCode()); }
    public boolean hasPrimaryKey() { return true; }
    public boolean hasCompoundPrimaryKey() { return false; }

    // -----------------------------------------------------
    //                                        Unique Element
    //                                        --------------
    public UniqueInfo uniqueOf() { return hpcui(columnDisplayOrder()); }

    // ===================================================================================
    //                                                                       Relation Info
    //                                                                       =============
    // cannot cache because it uses related DB meta instance while booting
    // (instead, cached by super's collection)
    // -----------------------------------------------------
    //                                      Foreign Property
    //                                      ----------------

    // -----------------------------------------------------
    //                                     Referrer Property
    //                                     -----------------
    /**
     * (商品)PRODUCT by PRODUCT_STATUS_CODE, named 'productList'.
     * @return The information object of referrer property. (NotNull)
     */
    public ReferrerInfo referrerProductList() {
        Map<ColumnInfo, ColumnInfo> mp = newLinkedHashMap(columnProductStatusCode(), ProductDbm.getInstance().columnProductStatusCode());
        return cri("FK_PRODUCT_PRODUCT_STATUS", "productList", this, ProductDbm.getInstance(), mp, false, "productStatus");
    }

    // ===================================================================================
    //                                                                        Various Info
    //                                                                        ============

    // ===================================================================================
    //                                                                           Type Name
    //                                                                           =========
    public String getEntityTypeName() { return "org.docksidestage.handson.dbflute.exentity.ProductStatus"; }
    public String getConditionBeanTypeName() { return "org.docksidestage.handson.dbflute.cbean.ProductStatusCB"; }
    public String getBehaviorTypeName() { return "org.docksidestage.handson.dbflute.exbhv.ProductStatusBhv"; }

    // ===================================================================================
    //                                                                         Object Type
    //                                                                         ===========
    public Class<ProductStatus> getEntityType() { return ProductStatus.class; }

    // ===================================================================================
    //                                                                     Object Instance
    //                                                                     ===============
    public ProductStatus newEntity() { return new ProductStatus(); }

    // ===================================================================================
    //                                                                   Map Communication
    //                                                                   =================
    public void acceptPrimaryKeyMap(Entity et, Map<String, ? extends Object> mp)
    { doAcceptPrimaryKeyMap((ProductStatus)et, mp); }
    public void acceptAllColumnMap(Entity et, Map<String, ? extends Object> mp)
    { doAcceptAllColumnMap((ProductStatus)et, mp); }
    public Map<String, Object> extractPrimaryKeyMap(Entity et) { return doExtractPrimaryKeyMap(et); }
    public Map<String, Object> extractAllColumnMap(Entity et) { return doExtractAllColumnMap(et); }
}
