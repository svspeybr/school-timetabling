package org.acme.timetabling.domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

import java.util.ArrayList;
import java.util.List;

@PlanningSolution
public class TimeTable {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    private List<Timeslot> timeslotList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id= "roomRange")
    private List<Room> roomList;
    @PlanningEntityCollectionProperty
    private List<Lesson> lessonList;

    @ProblemFactCollectionProperty
    private List<LessonTask> lessonTaskList;
    //Can be extracted from all lessons??

    private List<Teacher> teacherList;

    @ProblemFactCollectionProperty
    private List<Preference> preferenceList;

    private List<StudentGroup> studentGroupList;

    @PlanningScore
    private HardSoftScore score;

    private SolverStatus solverStatus;

    // No-arg constructor required for OptaPlanner
    public TimeTable() {
    }

    public TimeTable(List<Timeslot> timeslotList,
                     List<Room> roomList,
                     List<Lesson> lessonList,
                     List<LessonTask> lessonTaskList,
                     List<StudentGroup> studentGroupList,
                     List<Teacher> teacherList,
                     List<Preference> preferenceList)  {
        this.timeslotList = timeslotList;
        this.roomList = roomList;
        this.lessonList = lessonList;
        this.lessonTaskList = lessonTaskList;
        this.studentGroupList = studentGroupList;
        this.teacherList = teacherList;
        this.preferenceList = preferenceList;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************


    public List<LessonTask> getLessonTaskList() {
        return lessonTaskList;
    }

    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public List<Lesson> getLessonList() {
        return lessonList;
    }

    public List<Preference> getPreferenceList() {
        return preferenceList;
    }

    public List<StudentGroup> getStudentGroupList() {
        return studentGroupList;
    }

    public List<Teacher> getTeacherList() {
        return teacherList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
