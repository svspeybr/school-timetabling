package org.acme.timetabling.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.Set;

@PlanningEntity
public class LessonAssignment {

    Long lessonId;
    LessonTask lessonTask;

    @PlanningVariable(valueRangeProviderRefs = "roomRange")
    Room room;
    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    Timeslot timeslot;
    @PlanningVariable(valueRangeProviderRefs = "studentGroupRange")
    StudentGroup studentGroup;

    private Boolean pinned= false;


    /*PINNED */
    @PlanningPin
    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public LessonAssignment(){
    }

    public LessonAssignment(LessonTask lessonTask, Room room, Timeslot timeslot, StudentGroup studentGroup) {
        this.lessonTask = lessonTask;
        this.room = room;
        this.timeslot = timeslot;
        this.studentGroup = studentGroup;
    }

    public LessonAssignment(Long lessonId,
                            LessonTask lessonTask,
                            Room room,
                            Timeslot timeslot,
                            StudentGroup studentGroup,
                            Boolean pinned) {
        this.lessonId = lessonId;
        this.lessonTask = lessonTask;
        this.room = room;
        this.timeslot = timeslot;
        this.studentGroup = studentGroup;
        this.pinned = pinned;
    }

    public Integer getTaskNumber() {return lessonTask.getTaskNumber();}

    public LessonTask getLessonTask() {
        return lessonTask;
    }


    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public Long getLessonId(){return lessonId;}

    public Set<Teacher> getTaughtBy() {
        return lessonTask.getTaughtBy();
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }


    public void setStudentGroup(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }

    /*Placed on last resort */
    public Boolean isOnLastResortTimeslot() {
        if (this.timeslot == null){
            return false;
        }
        return this.timeslot.isLastResort();
    }
}
