-- Some stats about the demo DB names

-- view names
-- select w75KJ2mc4zz.value as lastname, zDhUuAYrxNC.value as firstname, concat(w75KJ2mc4zz.value, ' ', zDhUuAYrxNC.value) as fullname from trackedentityinstance tei
-- 	join trackedentitytype tet on tei.trackedentitytypeid = tet.trackedentitytypeid
-- 	join trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid
-- 		and w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'
-- 	join trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid
-- 		and zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC'
-- 	order by lastname, firstname;

-- Question: do firstnames or lastnames contain hyphens '-'?
-- Firstnames contain hyphens while lastnames do not. Examples: Fre-Swera or Fre-qalsi
select w75KJ2mc4zz.value as w75KJ2mc4zz_firstname, zDhUuAYrxNC.value as zDhUuAYrxNC_lastname, concat(w75KJ2mc4zz.value, ' ', zDhUuAYrxNC.value) as fullname from trackedentityinstance tei
	join trackedentitytype tet on tei.trackedentitytypeid = tet.trackedentitytypeid
	join trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid
	join trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid
		and w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'
	join trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid
	join trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid
		and zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC'
		where position('-' in w75KJ2mc4zz.value)>0
	order by zDhUuAYrxNC_lastname, w75KJ2mc4zz_firstname;

-- This next one needs the following extension: CREATE EXTENSION unaccent;
-- Question: do firstnames or lastnames contain diacritics like 'á'?
-- Yes both firstnames and lastnames contain diacritics. Examples lastnames: Jørgensen firstnames: Inés
-- SELECT w75KJ2mc4zz.value as w75KJ2mc4zz_lastname, unaccent(w75KJ2mc4zz.value) as w75KJ2mc4zz_lastname_unaccent,
--        zDhUuAYrxNC.value as zDhUuAYrxNC_firstname, unaccent(zDhUuAYrxNC.value) as zDhUuAYrxNC_firstname_unaccent
-- 	from trackedentityinstance tei
-- 	join trackedentitytype tet on tei.trackedentitytypeid = tet.trackedentitytypeid
-- 	join trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid
-- 		and w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'
-- 	join trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid
-- 		and zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC'
-- 		where zDhUuAYrxNC.value != unaccent(zDhUuAYrxNC.value) or w75KJ2mc4zz.value != unaccent(w75KJ2mc4zz.value);

-- w75KJ2mc4zz firstname
-- zDhUuAYrxNC lastname
	
-- 73076 total in query
-- 73124 total in trackedentityinstance

-- do we have people without a name?