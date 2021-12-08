package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.netty.util.internal.IntegerHolder;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

import org.acme.timetabling.domain.Timeslot;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@XStreamAlias("LessonTask")
public class LessonTask extends PanacheEntityBase {

    /*FIELDS */
    @Id
    @Column(name = "TASKNUMBER")
    private Integer taskNumber;

    @ManyToMany(targetEntity = StudentGroup.class, fetch = FetchType.EAGER)
    private Set<StudentGroup> studentGroups = new HashSet<>();

    @ManyToMany(targetEntity = Teacher.class, fetch = FetchType.EAGER)
    private Set<Teacher> taughtBy= new HashSet<>();
    /*private String teacher;*/
    private String subject;

    private Integer multiplicity;

    @ManyToOne(targetEntity = CourseLevel.class, fetch = FetchType.EAGER)
    private CourseLevel courseLevel;

    @DeepPlanningClone
    @OneToMany(targetEntity = Lesson.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "TASKNUMBER")
    private Set<Lesson> lessonsOfTaskList;
    //Not necessary
    private Boolean coupled = false;
    //From Large to small
    @ElementCollection(fetch = FetchType.EAGER)
    //Fetchmode Join?
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<Integer> couplingNumbers = new ArrayList<>();




    //Couple the first two elements of lessonsOfTaskList

    public LessonTask(){
    }


    public LessonTask(Integer taskNumber) {
        this.taskNumber =taskNumber;
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

    public Set<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public Set<Teacher> getTaughtBy() {
        return taughtBy;
    }

    public String getSubject() {
        return subject;
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

    //Not necessary
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
    //Not necessary

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

    // DEFAULT SETTING MAXTEACHING DAYS
    private List<List<Integer>> getTimeslotsPerDay(List<LessonAssignment> lesList) {
        List<List<Integer>> numberOfLessons = new ArrayList<>(5);
        for (int i =0; i< 5; i++){
            //Max 8 -> 10 lessonslosts? on same day
            numberOfLessons.add(new ArrayList<>(10));
        }
        for (LessonAssignment lessonAssignment: lesList) {
            Timeslot timeslot = lessonAssignment.getTimeslot();
            if (! (timeslot == null)) {
                int index = timeslot.getDayOfWeek().getValue() - 1;
                numberOfLessons.get(index).add(timeslot.getPosition());
            }
        }

        numberOfLessons.sort(Comparator.comparing(List<Integer>::size).reversed());
        return numberOfLessons;
    }

    private int numberOfGaps(List<Integer> tsIndexList) {
        int length = tsIndexList.size();
        if (length <= 1){
            return 0;
        }
        Collections.sort(tsIndexList);
        return tsIndexList.get(length -1)- tsIndexList.get(0) + 1 - length;
    }

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
    public int exceedMaxLessonsOnSameDayInt(List<LessonAssignment> lesList) {
        Integer exceedNumber = 0;
        // To be taken from teacher/preferences
        int allowedNumberOfDays = 5;
        List<List<Integer>> numberOfLessons = this.getTimeslotsPerDay(lesList);
        int dayIndex = 0;
        int lessonsLeft = this.multiplicity;
        List<Integer> timeslotIndexOnDay;
        for (Integer blockSize : this.couplingNumbers) {
            timeslotIndexOnDay = numberOfLessons.get(dayIndex);
            exceedNumber += 6 * Math.abs(blockSize - timeslotIndexOnDay.size());
            lessonsLeft -= blockSize;
            exceedNumber += numberOfGaps(timeslotIndexOnDay);
            dayIndex += 1;
        }
        // TO DO: CHECK WHEN 5(=allowedNumberOfdays) BLOCKS ARE CHOSEN, NO LESSONS ARE LEFT
        // ONE BLOCK A DAY
        if (lessonsLeft > 0) {
            int daysLeftToDivide = (allowedNumberOfDays - this.couplingNumbers.size());
            int remainderOfDays = lessonsLeft % daysLeftToDivide;
            /*System.out.println("remainderOfDays");
            System.out.println(remainderOfDays);*/
            Integer mean = (int) Math.floor((float) lessonsLeft / daysLeftToDivide);
            /*System.out.println(mean);
            System.out.println("------");*/
            for (int i = dayIndex; i < 5; i++) {
                /*System.out.println(numberOfLessons.get(i));*/
                exceedNumber += Math.abs( numberOfLessons.get(i).size() - mean - sign(remainderOfDays));
                remainderOfDays -= 1;
                /*System.out.println(exceedNumber);
                System.out.println(remainderOfDays);*/
            }
            /*System.out.println("------");*/
        }
        return Math.max(exceedNumber, 0);
    }

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

    private static int sign(int numb){
        if (numb > 0) {
            return 1;
        }
        return 0;
    }
}

