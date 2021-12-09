package org.acme.timetabling.rest.repository;

import org.acme.timetabling.domain.LessonTask;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.Query;

@ApplicationScoped
public class LessonTaskRepository {

/*    public void updateStudentgroups(List<StudentGroupAssignment> studentGroupAssignmentList) {

        try {
            Query q = LessonTask.getEntityManager()
                    .createNativeQuery("SELECT * "+
                            "FROM LESSONTASK T LEFT JOIN LESSON_LESSONTASK L "+
                            "WHERE L.LESSONTASK = T.TASKNUMBER "+
                            "AND L.LESSONID  = ");

        } catch(NoResultException ex) {
            ex.printStackTrace();
        }
    }*/
    public LessonTask getLessonTaskByLessonId(Long lessonId) {

        try {
            Query q = LessonTask.getEntityManager()
                    .createNativeQuery("SELECT * "+
                                          "FROM LESSONTASK T LEFT JOIN LESSON_LESSONTASK L "+
                                          "WHERE L.LESSONTASK = T.TASKNUMBER "+
                                               "AND L.LESSONID  = "+ lessonId.toString(), LessonTask.class);
            return (LessonTask) q.getSingleResult();

        } catch(NoResultException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Boolean removeBlockRecord(Long taskId, int size) {

        try {
            Query q = LessonTask.getEntityManager()
                    .createNativeQuery("DELETE TOP 1 FROM LESSONTASK_COUPLINGNUMBERS "
                                       +"L WHERE L.LESSONTASK_TASKNUMBER = :taskId "
                                       +"AND L.COUPLINGNUMBERS = :size")
                    .setParameter("taskId", taskId)
                    .setParameter("size", size);
            q.executeUpdate();
            return true;

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
