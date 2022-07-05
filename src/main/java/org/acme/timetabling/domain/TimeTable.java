package org.acme.timetabling.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

import java.util.*;
import java.util.stream.Collectors;

@PlanningSolution
@XStreamAlias("TimeTable")
public class TimeTable {

    //VALUES
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    private List<Timeslot> timeslotList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id= "roomRange")
    private List<Room> roomList;


    //VARIABLES
    @PlanningEntityCollectionProperty
    private List<LessonAssignment> lessonAssignmentList;

    /*@PlanningEntityCollectionProperty*/
    private List<Lesson> lessonList;

    //PROBLEM FACTS
    @ProblemFactCollectionProperty
    private List<StudentGroup> studentGroupList;

    @ProblemFactCollectionProperty
    private List<Teacher> teacherList;

    @ProblemFactCollectionProperty
    private List<Preference> preferenceList;

    @ProblemFactCollectionProperty
    private List<LessonTask> lessonTaskList;

    @ProblemFactCollectionProperty
    private  List<CourseLevel> courseLevelList;

    @ProblemFactCollectionProperty
    private  List<SubjectCollection> subjectCollectionList;

    @ProblemFactCollectionProperty
    private List<ThemeCollection> themeCollectionList;

    //SCORE
    @PlanningScore
    private HardSoftScore score;

    private SolverStatus solverStatus;


    // ************************************************************************
    // CONSTRUCTORS
    // ************************************************************************
    public TimeTable() { // No-arg constructor required for OptaPlanner
    }

    public TimeTable(Map<Long, Integer> studentGroupConfiguration,
                     List<Timeslot> timeslotList,
                     List<Room> roomList,
                     List<Lesson> lessonList,
                     List<LessonTask> lessonTaskList,
                     List<CourseLevel> courseLevelList,
                     List<StudentGroup> studentGroupList,
                     List<Teacher> teacherList,
                     List<Preference> preferenceList,
                     List<SubjectCollection> subjectCollectionList,
                     List<ThemeCollection> themeCollectionList) {
        lessonTaskList.stream().map(LessonTask::getCourseLevel).filter(Objects::nonNull).collect(Collectors.toSet()).forEach(CourseLevel::updateCourseLevel);
        this.timeslotList = timeslotList;
        this.roomList = roomList;
        this.lessonList = lessonList;
        this.lessonTaskList = lessonTaskList;
        this.courseLevelList = courseLevelList;
        this.lessonAssignmentList = generateAssignmentFromLessonTask(studentGroupConfiguration, lessonTaskList);
        this.studentGroupList = studentGroupList;
        this.teacherList = teacherList;
        this.preferenceList = preferenceList;
        this.subjectCollectionList = subjectCollectionList;
        this.themeCollectionList = themeCollectionList;
    }

    // ************************************************************************
    // Getters
    // ************************************************************************


    public List<LessonAssignment> getLessonAssignmentList() {return lessonAssignmentList;}

    public List<SubjectCollection> getSubjectCollectionList() {
        return subjectCollectionList;
    }

    public List<ThemeCollection> getThemeCollectionList() {
        return themeCollectionList;
    }

    public List<LessonTask> getLessonTaskList() {
        return lessonTaskList;
    }

    public List<CourseLevel> getCourseLevelList() {
        return courseLevelList;
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

    // ************************************************************************
    // SETTERS
    // ************************************************************************

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    // ADVANCED

    private List<LessonAssignment> generateAssignmentFromLessonTask(Map<Long, Integer> studentGroupConfiguration, List<LessonTask> lessonTaskList){

        boolean emptyConfiguration = studentGroupConfiguration == null;
        List<LessonAssignment> lessonAssignmentList = new ArrayList<>();

        for (LessonTask lessonTask:lessonTaskList){
            Integer partitionNumber = 0;
            if (! emptyConfiguration && lessonTask.getCourseLevel() != null){
                partitionNumber = studentGroupConfiguration.get(lessonTask.getCourseLevel().getCourseLevelId());
                System.out.println(lessonTask.getCourseLevel().numberOfPossiblePartitions());
                System.out.println("pn" + partitionNumber);
            }
            Set<StudentGroup> studentGroups = lessonTask.getStudentGroupsFromPartition(partitionNumber);
                for (Lesson lesson: lessonTask.getLessonsOfTaskList()) {
                    LessonAssignment lessonAssignment = new LessonAssignment(lesson.getLessonId(),
                            lessonTask,
                            studentGroups,
                            lesson.getSubject(),
                            lesson.getRoom(),
                            lesson.getTimeslot(),
                            lesson.isPinned());
                    lessonAssignmentList.add(lessonAssignment);
                }
        }
        return lessonAssignmentList;
    }
}
