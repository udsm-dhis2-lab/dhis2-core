-- Some stats about the demo DB names

-- w75KJ2mc4zz firstname
-- zDhUuAYrxNC lastname
	
-- 73076 total in query
-- 73124 total in trackedentityinstance

-- Open question:
-- Do we have people without a name?

-- Example names (firstname lastname) with special characters:
-- Fre-Swera Aatifa
-- Fre-qalsi Yonas
-- Inés Bebea
-- Sara Jørgensen

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
-- Answer: Firstnames contain hyphens while lastnames do not. Examples: Fre-Swera or Fre-qalsi

select w75KJ2mc4zz.value as w75KJ2mc4zz_lastname, zDhUuAYrxNC.value as zDhUuAYrxNC_firstname from trackedentityinstance tei
	join trackedentitytype tet on tei.trackedentitytypeid = tet.trackedentitytypeid
	join trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid
	join trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid
		and w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'
	join trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid
	join trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid
		and zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC'
	where position('-' in w75KJ2mc4zz.value)>0 or position('-' in zDhUuAYrxNC.value)>0
	order by w75KJ2mc4zz_lastname, zDhUuAYrxNC_firstname;

-- Question: do firstnames or lastnames contain apostrophes '?
-- Answer: If my query is correct, no

-- select w75KJ2mc4zz.value as w75KJ2mc4zz_lastname, zDhUuAYrxNC.value as zDhUuAYrxNC_firstname from trackedentityinstance tei
-- 	join trackedentitytype tet on tei.trackedentitytypeid = tet.trackedentitytypeid
-- 	join trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid
-- 		and w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'
-- 	join trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid
-- 		and zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC'
-- 	where position('''' in w75KJ2mc4zz.value)>0 or position('''' in zDhUuAYrxNC.value)>0
-- 	order by w75KJ2mc4zz_lastname, zDhUuAYrxNC_firstname;

-- Question: do firstnames or lastnames contain diacritics like 'á'?
-- Answer: Yes both firstnames and lastnames contain diacritics. Examples lastnames: Jørgensen firstnames: Inés

-- This next one needs the following extension: CREATE EXTENSION unaccent;

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

-- Question: do we have people with multiple firstnames or lastnames separated by spaces (above is a dedicated question for hyphens '-')?
-- Answer: no

-- select w75KJ2mc4zz.value as w75KJ2mc4zz_lastname, zDhUuAYrxNC.value as zDhUuAYrxNC_firstname from trackedentityinstance tei
-- 	join trackedentitytype tet on tei.trackedentitytypeid = tet.trackedentitytypeid
-- 	join trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid
-- 		and w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'
-- 	join trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid
-- 		and zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC'
-- 	where position(' ' in w75KJ2mc4zz.value)>0 or position(' ' in zDhUuAYrxNC.value)>0
-- 	order by w75KJ2mc4zz_lastname, zDhUuAYrxNC_firstname;
