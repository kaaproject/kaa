1. remember (id,schems) :event_schems_versions
2. drop column (schems) :event_schems_versions
3. rename table event_schems_versions to events_class_family_versions
4. rename column (events_class_family_id) to (events_class_family_versions_id) :events_class
5. remember (id,schems,version) : events_class
6. drop column (schems,version) : events_class
7. (p.6 schems) search them as substring in (p.1 schems) and update (p.1 id) with (p.4 events_class_family_versions_id)
8. AdminClient: save ctl schems based on bodies (p.5 schems) , application_id = null, tenant_id as select :events_class by (p.5 id)
9. remember (p.5 EC, p.8 CTLDto)
10. find max(id) :base_schems
11. save base_schems:
                       id = (p.9 id + p.10) and update (id):events_class with this
                       created_time, created_username, version, ctl_id from (p.9 CTLDto)
                       name = parse "name" in (body) :ctl
                       description = null, application_id = null
