package org.acme.timetabling.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.List;
import java.util.Set;

@PlanningEntity
public class LessonAssignment {


    Long lessonId;
    LessonTask lessonTask;
    String subject;

    @PlanningVariable(valueRangeProviderRefs = "roomRange")
    Room room;
    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    Timeslot timeslot;
    @PlanningVariable(valueRangeProviderRefs = "studentGroupPartitionNumberRange")
    Integer partitionNumber = 0;

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

    public LessonAssignment(LessonTask lessonTask, Room room, Timeslot timeslot) {
        this.lessonTask = lessonTask;
        this.room = room;
        this.timeslot = timeslot;
    }

    public LessonAssignment(Long lessonId,
                            LessonTask lessonTask,
                            String subject,
                            Room room,
                            Timeslot timeslot,
                            Boolean pinned) {
        this.lessonId = lessonId;
        this.lessonTask = lessonTask;
        this.subject = subject;
        this.room = room;
        this.timeslot = timeslot;
        this.pinned = pinned;
    }

    @ValueRangeProvider(id = "studentGroupPartitionNumberRange")
    public CountableValueRange<Integer> getStudentGroupPartitionNumberRange() {
        int to = 1;
        CourseLevel courseLevel = this.lessonTask.getCourseLevel();
        if (courseLevel != null) {
            to = courseLevel.numberOfPossiblePartitions();
        }
        return ValueRangeFactory.createIntValueRange(0, to, 1);
    }

    public String getSubject() {
        return subject;
    }

    public Integer getTaskNumber() {return lessonTask.getTaskNumber();}

    public LessonTask getLessonTask() {
        return lessonTask;
    }

    public Integer getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(Integer partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    public Set<StudentGroup> getStudentGroups(){
        return lessonTask.getStudentGroupsFromPartition(partitionNumber);
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

    public String getDayOfWeek(){
        if (timeslot != null){
            return timeslot.getDayOfWeek().toString();
        }
        return "NA";
    }

    public String getHalfDay(){
        if (timeslot != null) {
            String day = timeslot.getDayOfWeek().toString().substring(0, 2);
            String index = "0"; //ONLY FORENOON
            if (timeslot.inAfternoon()) {
                index = "1"; //index =1 -> ONLY AFTENOON
                if (timeslot.inForenoon()) {
                    index = ""; //BOTH --> index --> nothing
                }
            }
            return day + index;
        }
        return null;
    }


      /*Placed on last resort */
    public Boolean isOnLastResortTimeslot() {
        if (this.timeslot == null){
            return false;
        }
        return this.timeslot.isLastResort();
    }
    @Override
    public String toString() {
        return "les" + "("+ lessonTask.getTaskNumber() +"-" +timeslot.toString() +")";
    }
}
