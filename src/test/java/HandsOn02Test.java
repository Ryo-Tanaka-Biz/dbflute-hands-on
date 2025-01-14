import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn02Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    public void test_existsTestData() throws Exception {
        int count = memberBhv.selectCount(cb ->{});
        assertTrue(count != 0);

        /////////////////////////////////////////////////////////////////////////////////
        // 20-member.xls と 30-product.xls を配置してReplaceSchemaしたらエラー出た
        // エラーメッセージは"Field 'REGISTER_DATETIME' doesn't have a default value"
        // Initialize Schema（初期化）  ->Create Schema（箱作る）　-> Load Data（レコード挿入）
        // Create Schemaには成功していそう
        // 20-member.xlsを元にしたデータ挿入で、REGISTER_DATETIMEがおかしい？
        // 共通カラムが挿入されていない。どこで挿入されている？
        // commonColumnMap.dfpropでコメントアウトされている部分がある。そこを有効にしてみるといけるのかな。。。
        // 分析はこのくらいにして、ドキュメントを見る
        ///////////////////////////////////////////////////////////////////////////////////
    }

    public void test_searchMembers_memberName_startWith_S() throws Exception {
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S",op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });
        //selectEntityはエンティティを一件に特定する
        //一意に特定できないとエラー起きる
        //selectListとselectEntityの使い分け

        //なぜかログが出力されない。
        //依存関係は問題なさそうだった。一旦System.out.printlnで進める

        assertTrue(members.stream().allMatch(member -> member.getMemberName().startsWith("S")));
        System.out.println(members);
    }

    public void test_searchMembers_memberId_equal_1() throws Exception {
        OptionalEntity<Member> members = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(1);
        });

        assertTrue(members.stream().allMatch(member -> member.getMemberId() == 1));
        System.out.println(members);
    }

    public void test_searchMembers_birthDate_isNull() throws Exception {
        ListResultBean<Member> members = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Asc();
        });

        assertTrue(members.stream().allMatch(member -> member.getBirthdate() == null));
        System.out.println(members);
    }
}

