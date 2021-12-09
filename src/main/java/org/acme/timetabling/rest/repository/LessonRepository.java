package org.acme.timetabling.rest.repository;

import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.LessonTask;
import org.acme.timetabling.domain.dto.LessonDto;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class LessonRepository {

    public void deleteAllGroups(){
        try {
            String stringQuery = "DELETE FROM LESSONTASK_STUDENTGROUP";
            Query q = LessonTask.getEntityManager().createNativeQuery(stringQuery);
            q.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addGroups(Set<String> values){
        try {
            StringBuilder stringQuery = new StringBuilder("");
            for (String value: values){
                stringQuery.append(", "+value);
            }
            Query q = LessonTask.getEntityManager().createNativeQuery("INSERT INTO LESSONTASK_STUDENTGROUP (LESSONTASKS_TASKNUMBER,  STUDENTGROUPS_GROUPNAME) VALUES"+ stringQuery.substring(1));
            q.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<LessonDto> findAllLessonsDTO() {
        String queryString = "SELECT x.TASKNUMBER, LESSONID, SUBJECT, POSITION, COUPLED, ACRONYM, GROUPNAME\n" +
                "FROM\n" +
                "(\n" +
                "SELECT TASKNUMBER, COUPLED, SUBJECT, TAUGHTBY_ACRONYM AS ACRONYM, STUDENTGROUPS_GROUPNAME AS GROUPNAME\n" +
                "FROM LESSONTASK l \n" +
                "INNER JOIN LESSONTASK_STUDENTGROUP st \n" +
                "ON l.TASKNUMBER = st.LESSONTASKS_TASKNUMBER\n" +
                "INNER JOIN LESSONTASK_TEACHER lt \n" +
                "ON l.TASKNUMBER = lt.LESSONTASKS_TASKNUMBER)\n" +
                "AS x \n" +
                "INNER JOIN \n" +
                "(\n" +
                "SELECT TASKNUMBER, LESSONID, POSITION\n" +
                "FROM LESSON le \n" +
                "INNER JOIN TIMESLOT ti\n" +
                "ON le.TIMESLOT_TIMESLOTID = ti.TIMESLOTID\n" +
                ") AS y\n" +
                "ON x.TASKNUMBER = y.TASKNUMBER";

        try {
            List<LessonDto> lesDtoList = new ArrayList<>();
            Query q = LessonTask.getEntityManager()
                    .createNativeQuery(queryString);
            for (Object o: q.getResultList()) {
                Object[] ob = (Object[]) o;
                Integer lessonTaskId = (Integer) ob[0];
                BigInteger lessonId = (BigInteger) ob[1];
                String subject = (String) ob[2];
                Integer position = (Integer) ob[3];
                Boolean coupled = (Boolean) ob[4];
                String acronym = (String) ob[5];
                String groupName = (String) ob[6];
                lesDtoList.add(new LessonDto(lessonTaskId,
                        lessonId.longValue(),
                        subject,
                        coupled,
                        position,
                        acronym,
                        groupName));
            }
            return lesDtoList;

        } catch(NoResultException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
