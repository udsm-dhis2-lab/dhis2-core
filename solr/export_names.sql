-- view of names
select tei.uid as tei, w75KJ2mc4zz.value as firstname, zDhUuAYrxNC.value as lastname, concat(w75KJ2mc4zz.value, ' ', zDhUuAYrxNC.value) as fullname from trackedentityinstance tei
	join trackedentityattributevalue w75KJ2mc4zz on w75KJ2mc4zz.trackedentityinstanceid = tei.trackedentityinstanceid
	join trackedentityattribute w75KJ2mc4zz_att on w75KJ2mc4zz_att.trackedentityattributeid = w75KJ2mc4zz.trackedentityattributeid
		and w75KJ2mc4zz_att.uid = 'w75KJ2mc4zz'
	join trackedentityattributevalue zDhUuAYrxNC on zDhUuAYrxNC.trackedentityinstanceid = tei.trackedentityinstanceid
	join trackedentityattribute zDhUuAYrxNC_att on zDhUuAYrxNC_att.trackedentityattributeid = zDhUuAYrxNC.trackedentityattributeid
		and zDhUuAYrxNC_att.uid = 'zDhUuAYrxNC';

-- select tei.uid as tei, tea.uid as tea, teav.value as teav from trackedentityinstance tei
-- 	join trackedentityattributevalue teav on teav.trackedentityinstanceid = tei.trackedentityinstanceid
-- 	join trackedentityattribute tea on tea.trackedentityattributeid = teav.trackedentityattributeid;
	
-- w75KJ2mc4zz firstname
-- zDhUuAYrxNC lastname
	
-- 73076 total in query
-- 73124 total in trackedentityinstance

-- do we have people without a name?