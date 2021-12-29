package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Fetch;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


/*@PlanningEntity*/
@XStreamAlias("Lesson")
@Entity
public class Lesson extends PanacheEntityBase{
    /* FIELDS*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LESSONID")
    private Long lessonId;

    private boolean pinned = false;

    //Initialized during planning
    @ManyToOne
    @JoinColumn(name = "LESSON_TIMESLOTID")
    private Timeslot timeslot;

    @ManyToOne
    private Room room;

    @Column(name = "TASKNUMBER")
    private Integer taskNumber;

    @ManyToOne(targetEntity = LessonTask.class)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "taskNumber")
    @JoinColumn(name = "TASKNUMBER", insertable = false, updatable = false)
    private LessonTask lessonTask;

    /*CONSTRUCTORS*/

    // No-arg constructor required for Hibernate and OptaPlanner
    public Lesson() {
    }

    public Lesson(LessonTask lessonTask){
        this.lessonTask = lessonTask;
        this.taskNumber = lessonTask.getTaskNumber();
    }

    /*PINNED */
    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Override
    public String toString() {
        return  lessonId.toString() ;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getLessonId() {
        return lessonId;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public LessonTask getLessonTask() {
        return lessonTask;
    }

    public Integer getTaskNumber() {return taskNumber;};

    public String getSubject() {return lessonTask.getSubject();}

    public Set<StudentGroup> getStudentGroups() {
        return lessonTask.getStudentGroups();}

    public Set<Teacher> getTaughtBy() {
        return lessonTask.getTaughtBy();
    }


    public void setLessonTask(LessonTask lessonTask) {
        if(this.lessonTask != null) {
            this.lessonTask.deleteLessonsOfTaskList(this);
        }
        this.lessonTask = lessonTask;
        this.taskNumber = lessonTask.getTaskNumber();
        lessonTask.addLessonsToTaskList(this);
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }


    public Boolean isConsecutiveTo(Lesson lesson) {
        return this.timeslot.isConsecutiveTo(lesson.getTimeslot());
    }

    // MORE EFFICIENT?????
    public long nLesSameDaySameTask() {
        return this.lessonTask.getLessonsOfTaskList()
                .stream()
                .filter(lesson -> lesson.getTimeslot().getDayOfWeek()
                        .equals(this.timeslot.getDayOfWeek()))
                .count();
    }
    // CONDITION ACCORDING TO SCHOOL...
    public boolean accToLesSepCriteriaSPC () {
        long nlessons = this.nLesSameDaySameTask();
        return (nlessons < 3 & this.lessonTask.getMultiplicity() > 5) ||
                nlessons < 2 ;
    }

}


