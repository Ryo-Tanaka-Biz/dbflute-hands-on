package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// done tanaryo [読み物課題] MySQLでお手軽デッドロック by jflute (2025/03/28)
// https://jflute.hatenadiary.jp/entry/20120831/1346393321
// トランザクション分離レベルを学んでみましょう。
// REPEATABLE READでは、トランザクションAで削除/更新対象が0の場合にネクストキーロックがかかる。
// その後、トランザクションBでもネクストキーロックがかかったとする
// トランザクションAでレコードをインサートしようとした場合にトランザクションBのネクストキーロックのために待機
// トランザクションBでも同様にトランザクションAのネクストキーロックのために待機
// その結果、デッドロックが発生する。
// READ COMMITTEDでは、トランザクションAで削除/更新対象が0の場合にネクストキーロックがかからない。
// そのため、トランザクションBでレコードをインサートしてもトランザクションAのネクストキーロックのために待機しない。
// [1on1でのふぉろー] MySQLのREPEATABLE READのお話をたっぷり
// 使ってるツールの特徴をちゃんと知ろう。

// [1on1でのふぉろー] 目の前のツールの勉強に費用対効果
// o どのツールでもしっかり追求して学べば、他のツール使うときにも通じるスキルになる
// o 優秀な人は、好きじゃないツールでも仕事としてやるからには追求してくる
//
//  → 追求していれば無駄はない

// [1on1でのふぉろー] DBFluteのアップグレードと自動生成の仕組み
// DBFluteは本体ごとgitにコミットしてもらうスタイルを推奨してる話。
// 本気でやるならGradle Wrapper的なことになるけど、そこまでやることはないという現状。

// [1on1でのふぉろー] 仕組み回りを知っておくメリット
// o ある程度詳しくないと詳しい人に聞くための質問の言語化もできず躊躇する

// done tanaryo mydbfluteの古いDBFlute Engine-1.2.7は削除コミットしちゃってOK by jflute (2025/04/18)
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
    //                                                                        レビュー自動化
    //                                                                        ============

    //わざと引っ掛かるようにして、以下のエラーが発生することを確認
//   [df-doc] /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//   [df-doc] Failed to execute DBFlute Task 'Doc'.
//   [df-doc]
//   [df-doc] [Advice]
//   [df-doc] Check the exception messages and the stack traces.
//   [df-doc] * * * * * * * * * */
//            [df-doc] org.dbflute.exception.DfSchemaPolicyCheckViolationException: Look! Read the message below.
//            [df-doc] /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//   [df-doc] The schema policy has been violated.
//   [df-doc]
//   [df-doc] [Advice]
//   [df-doc] Make sure your violating schema (ERD and DDL).
//   [df-doc] You can see violations on this message or SchemaHTML.
//   [df-doc] And after that, execute renewal (or regenerate) again.
//   [df-doc] (tips: The schema policy is on schemaPolicyMap.dfprop)
//   [df-doc]
//   [df-doc] [Violation]
//   [df-doc] column.statement: if columnName is suffix:_FLG then notNull
//   [df-doc]  +-The column should be not-null: (購入.支払完了フラグ)PURCHASE.PAYMENT_COMPLETE_FLG INT(10) (NullAllowed)

    // ===================================================================================
    //                                                                             XXXXXXX
    //                                                                        ============
    public void test_3() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }

}
