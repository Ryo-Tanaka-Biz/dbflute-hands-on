-- add column
alter table MEMBER_SECURITY add REMINDER_USE_COUNT INTEGER NOT NULL after REMINDER_ANSWER;

-- add identity column as primary key
alter table MEMBER_SERVICE add MEMBER_SERVICE_ID INTEGER NOT NULL;
alter table MEMBER_SERVICE drop foreign key FK_MEMBER_SERVICE_MEMBER;
alter table MEMBER_SERVICE drop primary key;
update MEMBER_SERVICE set MEMBER_SERVICE_ID = MEMBER_ID;
alter table MEMBER_SERVICE add primary key (MEMBER_SERVICE_ID);
alter table MEMBER_SERVICE add constraint FK_MEMBER_SERVICE_MEMBER
    foreign key (MEMBER_ID) references `MEMBER` (MEMBER_ID);
alter table MEMBER_SERVICE modify column MEMBER_SERVICE_ID INTEGER AUTO_INCREMENT NOT NULL;
alter table MEMBER_SERVICE add constraint UQ_MEMBER_SERVICE unique (MEMBER_ID);

-- add column for display order
alter table PRODUCT_STATUS add DISPLAY_ORDER INTEGER NOT NULL after PRODUCT_STATUS_NAME;
update PRODUCT_STATUS set DISPLAY_ORDER = 1 where PRODUCT_STATUS_CODE= 'ONS';
update PRODUCT_STATUS set DISPLAY_ORDER = 2 where PRODUCT_STATUS_CODE= 'PST';
update PRODUCT_STATUS set DISPLAY_ORDER = 3 where PRODUCT_STATUS_CODE= 'SST';
alter table PRODUCT_STATUS add constraint unique(DISPLAY_ORDER);

-- add table as member following
create table MEMBER_FOLLOWING(
                                 MEMBER_FOLLOWING_ID BIGINT AUTO_INCREMENT NOT NULL COMMENT '会員フォローイングID: 連番',
                                 MY_MEMBER_ID INTEGER NOT NULL COMMENT 'わたし: 気になった人がいて...勇気を振り絞った会員のID。',
                                 YOUR_MEMBER_ID INTEGER NOT NULL COMMENT 'あなた: いきなりのアクションに...ちょっと心揺らいだ会員のID。',
                                 FOLLOW_DATETIME DATETIME NOT NULL COMMENT 'その瞬間: ふりかえるとちょっと恥ずかしい気持ちになる日時',
                                 PRIMARY KEY (MEMBER_FOLLOWING_ID),
                                 UNIQUE (MY_MEMBER_ID, YOUR_MEMBER_ID)
) COMMENT='会員フォローイング: とある会員が他の会員をフォローできる。すると、フォローした会員の購入履歴が閲覧できる。';
alter table MEMBER_FOLLOWING add constraint FK_MEMBER_FOLLOWING_MY_MEMBER
    foreign key (MY_MEMBER_ID) references `MEMBER` (MEMBER_ID);
alter table MEMBER_FOLLOWING add constraint FK_MEMBER_FOLLOWING_YOUR_MEMBER
    foreign key (YOUR_MEMBER_ID) references `MEMBER` (MEMBER_ID);
create index IX_MEMBER_FOLLOWING_UNIQUE_REVERSE on MEMBER_FOLLOWING(YOUR_MEMBER_ID, MY_MEMBER_ID);
create index IX_MEMBER_FOLLOWING_FOLLOW_DATETIME on MEMBER_FOLLOWING(FOLLOW_DATETIME);
