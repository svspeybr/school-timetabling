package org.acme.timetabling.rest.repository;

import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.LessonTask;

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

}
