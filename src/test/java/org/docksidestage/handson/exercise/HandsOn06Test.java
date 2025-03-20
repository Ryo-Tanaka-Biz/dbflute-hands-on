package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author tanaryo
 */
public class HandsOn06Test extends UnitContainerTestCase {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                          =========
    @Resource
    private MemberBhv memberBhv;

    // ===================================================================================
    //                                                       デバッグログ設定 (Lasta Di なら)
    //                                                                        ============
    // logback.xml のloggerをコメントアウトすることでログが出ないことを確認
    // name単位で出力されるログが異なる。パッケージに対応している。
    // name="org.docksidestage"をコメントアウトすればHandsOnTest~のログが消える


    // ===================================================================================
    //                                                         別名(和名)の利用 (+ Logic作成)
    //                                                                        ============
    // TableComment	とColumnCommentを確認
    public void test_1() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============
    public void test_2() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============
    public void test_3() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }

}
