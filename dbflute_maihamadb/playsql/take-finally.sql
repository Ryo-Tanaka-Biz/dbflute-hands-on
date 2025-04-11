-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- Member addresses should be only one at any time.
-- - - - - - - - - - -/
SELECT adr.member_address_id,
       adr.member_id,
       adr.valid_begin_date,
       adr.valid_end_date,
       adr.address
  FROM member_address adr
 WHERE EXISTS (
           SELECT subadr.member_address_id
             FROM member_address subadr
            WHERE subadr.member_id = adr.member_id
              AND subadr.valid_begin_date > adr.valid_begin_date
              AND subadr.valid_begin_date <= adr.valid_end_date
       );

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- 正式会員日時を持ってる仮会員がいないことをアサート
-- - - - - - - - - - -/
SELECT member_id,
       member_status_code,
       formalized_datetime
  FROM member
 WHERE member_status_code = 'PRV'
   AND formalized_datetime IS NOT NULL;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- まだ生まれていない会員がいないことをアサート
-- - - - - - - - - - -/
SELECT member_id,
       birthdate,
       CURRENT_DATE()
  FROM member
 WHERE birthdate IS NOT NULL
   AND birthdate > CURRENT_DATE();

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- 退会会員が退会情報を持っていることをアサート
-- - - - - - - - - - -/
SELECT member.member_id,
       member.member_status_code
  FROM member
 WHERE member.member_status_code = 'WDL'
   AND NOT EXISTS (
           SELECT wdl.member_id
             FROM member_withdrawal wdl
            WHERE wdl.member_id = member.member_id
       );
