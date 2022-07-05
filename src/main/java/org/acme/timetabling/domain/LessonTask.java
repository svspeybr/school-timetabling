package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@XStreamAlias("LessonTask")
public class LessonTask extends PanacheEntityBase{

    /*FIELDS */
    @Id
    @Column(name = "TASKNUMBER")
    private Integer taskNumber;

    @ManyToMany(targetEntity = StudentGroup.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Set<StudentGroup> studentGroups = new HashSet<>();

    @ManyToMany(targetEntity = Teacher.class, fetch = FetchType.EAGER)
    private Set<Teacher> taughtBy= new HashSet<>();
    /*private String teacher;*/
    private String subject;

    private Integer multiplicity;

    @ManyToOne(targetEntity = CourseLevel.class, cascade = CascadeType.REMOVE)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "courseLevelId")
    private CourseLevel courseLevel;

    @OneToMany(targetEntity = Lesson.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "TASKNUMBER")
    private Set<Lesson> lessonsOfTaskList;
    //Not necessary
    private Boolean coupled = false;
    private Boolean teachingTask = true;
    //From Large to small
    @ElementCollection(fetch = FetchType.EAGER)
    //Fetchmode Join?
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<Integer> couplingNumbers = new ArrayList<>();



    //Couple the first two elements of lessonsOfTaskList

    public LessonTask(){
    }


    public LessonTask(Integer taskNumber) {
        this.taskNumber = taskNumber;
        this.multiplicity = 0;
        this.lessonsOfTaskList = new HashSet<>();
    }

    public LessonTask(Integer taskNumber,
                      String subject,
                      List<StudentGroup> studentGroups,
                      List<Teacher> teachers) {
        this.taskNumber =taskNumber;
        this.multiplicity = 0;
        this.subject = subject;
        this.lessonsOfTaskList = new HashSet<>();
        addStudentGroups(studentGroups);
        addTeachers(teachers);
    }



    public void addStudentGroup(StudentGroup studentGroup) {
        if (!this.studentGroups.contains(studentGroup)) {
            this.studentGroups.add(studentGroup);
            studentGroup.addLessonTask(this);
        }
    }

    public void addStudentGroups(Collection<StudentGroup> studentGroups) {
        for (StudentGroup studentGroup: studentGroups) {
            addStudentGroup(studentGroup);
        }
    }

    public void addTeacher(Teacher teacher) {
        if (!this.taughtBy.contains(teacher)) {
            this.taughtBy.add(teacher);
            teacher.addLessonTask(this);
        }
    }

    public void addTeachers(Collection<Teacher> teachers) {
        for (Teacher teacher: teachers) {
            addTeacher(teacher);
        }
    }

    private void removeTeacher(Teacher teacher) {
        if(this.taughtBy.contains(teacher)){
            this.taughtBy.remove(teacher);
        }
    }


    /*GETTERS AND SETTERS */
    public void setStudentGroups(Set<StudentGroup> studentGroups){
        this.studentGroups = studentGroups;
    }

    public Set<Teacher> getTaughtBy() {
        return taughtBy;
    }

    public String getSubject() {
        return subject;
    }

    public Boolean isATeachingTask(){
        return teachingTask;
    }

    public void setTeachingTask(Boolean bool){
        teachingTask = bool;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Boolean hasCoupling() {return ! this.couplingNumbers.isEmpty();}

    public List<Integer> getCouplingNumbers() {return this.couplingNumbers;}

    public void addCoupling(Integer integer) {
        this.couplingNumbers.add(integer);
        this.couplingNumbers.sort(Comparator.reverseOrder());
        updateCoupling();
    }

    //**************************************************************$**
    // GETTERS AND SETTERS -COURSELEVEL
    //******************************************************************
    public CourseLevel getCourseLevel() {
        return courseLevel;
    }

    public void setCourseLevel(CourseLevel courseLevel) {
        this.courseLevel = courseLevel;
    }

    public Set<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public Set<StudentGroup> getStudentGroupsFromPartition(Integer partitionNumber) {
        if (this.courseLevel == null){
            return studentGroups;
        }
        return courseLevel.getStudentGroups(this, partitionNumber);
    }





    public Boolean isCoupled() {
        return coupled;
    }

    public void updateCoupling() {
        this.coupled = ! this.couplingNumbers.isEmpty();
    }

    public void resetCoupling(){
        this.couplingNumbers.removeAll(couplingNumbers);
        this.coupled = false;
    }

    public Integer getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(Integer taskNumber) {
        this.taskNumber = taskNumber;
    }

    public Integer getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Integer multiplicity) {
        this.multiplicity = multiplicity;
    }

    public Set<Lesson> getLessonsOfTaskList() {
        return lessonsOfTaskList;
    }

    public List<Timeslot> getTimeslotsOfTaskList() {return lessonsOfTaskList.stream()
            .map(Lesson::getTimeslot)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());}

    public void setTimeslotsOfTaskList(List<Timeslot> timeslots) {
        int i = 0;
        int length = timeslots.size();
        for (Lesson lesson: lessonsOfTaskList){
            if (i < length){
                lesson.setTimeslot(timeslots.get(i));
                i++;
            } else{
                lesson.setTimeslot(null);
            }
        }
    }



    public void addLessonsToTaskList(Lesson lesson) {
        if (!lessonsOfTaskList.contains(lesson)) {
            lessonsOfTaskList.add(lesson);
            multiplicity++;
        }
    }


    public void deleteLessonsOfTaskList(Lesson lesson) {
        if (lessonsOfTaskList.contains(lesson)) {
            lessonsOfTaskList.remove(lesson);
            multiplicity--;
        }
    }



    public void setLessonsOfTaskList(Set<Lesson> lessonList) {
        this.lessonsOfTaskList = lessonList;
    }

    //SEPARATION CONDITIONS





/*    public Boolean exceedMaxLessonsOnSameDay() {
        List<Integer> numberOfLessons = new ArrayList<>();
        for (Integer j: this.countLessons0nSameDay()){
            if (j > 1) {
                numberOfLessons.add(j);
            }
        }
        int exceedingTimes = 0;
        if (this.multiplicity < 3 && (! this.coupled)) {
            exceedingTimes = numberOfLessons.size();
        } else { //BEWARE: THERE IS STILL A PENALTY WHEN A TASK OF 4 LESSONS IS COUPLED AND is divided on 2 days (2/2)
            if (this.multiplicity < 6) {
            exceedingTimes = numberOfLessons.size() - 1; // One pair is allowed (the penalty for more than 2 lessons follows later)
            }
        }
        for (Integer i: numberOfLessons) {
            if (i > 2) {
                exceedingTimes ++;
            }
        }

    return exceedingTimes > 0;
    }*/
    // DEFAULT SETTING MAXTEACHING DAYS


/*    public int getMinimumPositionOfTimeslots(){
        //BOUND 500/5 LESSONS A dAY
        int min = 500;
        for (Lesson lesson: lessonsOfTaskList){
            Timeslot timeslot = lesson.getTimeslot();
            if (timeslot != null && timeslot.getPosition()< min){
                min = timeslot.getPosition();
            }
        }
        return min;
    }

    public int getMaximumPositionOfTimeslots(){
        int max = -1;
        for (Lesson lesson: lessonsOfTaskList){
            Timeslot timeslot = lesson.getTimeslot();
            if (timeslot != null && timeslot.getPosition()> max){
                max = timeslot.getPosition();
            }
        }
        return max;
    }

    public static int numberOfTimslotOverlaps(LessonTask leta1, LessonTask leta2){
        Map<Integer, Integer> storage = new HashMap<>();
        Integer countOverlaps = 0;
        for (Lesson lesson: leta1.getLessonsOfTaskList()){
            Timeslot timeslot = lesson.getTimeslot();
            if (timeslot != null && ! storage.containsKey(timeslot.getPosition())){
                storage.put(timeslot.getPosition(), 1);
            }}
        for (Lesson lesson: leta2.getLessonsOfTaskList()){
            Timeslot timeslot = lesson.getTimeslot();
            if (timeslot != null && storage.containsKey(timeslot.getPosition())){
                countOverlaps ++;
            }
        }
        return countOverlaps;

    }

    public static int numberOfSticking(LessonTask leta1, LessonTask leta2){

        int numbOfStick = 0;
        Set<Lesson> lessonColl1 = leta1.getLessonsOfTaskList();
        Set<Integer> timeslotColl2 = leta2.getLessonsOfTaskList()
                .stream()
                .map(lesson->{
                    Timeslot timeslot = lesson.getTimeslot();
                    if (timeslot == null) {
                        return null;
                    }
                    return timeslot.getPosition();
                })
                .collect(Collectors.toSet());
        for (Lesson lesson: lessonColl1){
            Timeslot timeslot = lesson.getTimeslot();
            if (timeslot != null) {
                if (timeslotColl2.contains(timeslot.getPosition()+1)){
                    numbOfStick ++;
                }
                if (timeslotColl2.contains(timeslot.getPosition()-1)){
                    numbOfStick ++;
                }
            }
        }
        return numbOfStick;
    }*/

    public int internalTimslotOverlaps(){
        Map<Integer, Integer> storage = new HashMap<>();
        Integer countOverlaps = 0;
        for (Lesson lesson: this.getLessonsOfTaskList()) {
            Timeslot timeslot = lesson.getTimeslot();
            if (timeslot != null) {
                if (storage.containsKey(timeslot.getPosition())) {
                    countOverlaps++;
                } else {
                    storage.put(timeslot.getPosition(), 1);
                }
            }
        }
        return 2 * countOverlaps;
    }

    @Override
    public String toString() {
        return  "task("+ taskNumber +")";
    }
}

