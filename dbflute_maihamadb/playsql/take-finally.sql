-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- Member addresses should be only one at any time.
-- - - - - - - - - - -/
select adr.MEMBER_ADDRESS_ID
     , adr.MEMBER_ID
     , adr.VALID_BEGIN_DATE
     , adr.VALID_END_DATE
     , adr.ADDRESS
from MEMBER_ADDRESS adr
where exists (select subadr.MEMBER_ADDRESS_ID
              from MEMBER_ADDRESS subadr
              where subadr.MEMBER_ID = adr.MEMBER_ID
                and subadr.VALID_BEGIN_DATE > adr.VALID_BEGIN_DATE
                and subadr.VALID_BEGIN_DATE <= adr.VALID_END_DATE)
;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- 正式会員日時を持ってる仮会員がいないことをアサート
-- - - - - - - - - - -/
select MEMBER_ID
     , MEMBER_STATUS_CODE
     , FORMALIZED_DATETIME
from MEMBER
where MEMBER_STATUS_CODE = 'PRV'
  and FORMALIZED_DATETIME IS NOT NULL
;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- まだ生まれていない会員がいないことをアサート
-- - - - - - - - - - -/
select MEMBER_ID
     , BIRTHDATE
     , CURRENT_DATE()
from MEMBER
where BIRTHDATE IS NOT NULL
  and BIRTHDATE > CURRENT_DATE()
;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- 退会会員が退会情報を持っていることをアサート
-- - - - - - - - - - -/
select member.MEMBER_ID
     , member.MEMBER_STATUS_CODE
from MEMBER member
where member.MEMBER_STATUS_CODE = 'WDL'
  and not exists(select wdl.MEMBER_ID
                 from MEMBER_WITHDRAWAL wdl
                 WHERE wdl.MEMBER_ID = member.MEMBER_ID)
;
