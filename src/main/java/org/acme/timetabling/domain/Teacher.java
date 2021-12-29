package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.apache.commons.math3.fraction.Fraction;
import org.hibernate.engine.internal.Cascade;

import javax.persistence.*;
import javax.ws.rs.NotFoundException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@XStreamAlias("Teacher")
public class Teacher extends PanacheEntityBase implements Comparable<Teacher>{

    private static final Comparator<Teacher> COMPARATOR = Comparator.comparing(Teacher::getAcronym);

    @Id
    private String acronym;

    private String name;


    private int coverId;
    //*********************
    //DEPENDENTS
    //*********************
    //~ PREFERENCES AND TIMESLOTS;
    private int hoursAwayFromFullTime;
    private int numberOfTeachingDays;
    //~ LESSONTASKS
    private int fullTime;
    private int firstOrLastHours;


    @ManyToMany(targetEntity = LessonTask.class,
            mappedBy = "taughtBy",
            fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<LessonTask> lessonTasks = new HashSet<>();

    @OneToMany(targetEntity = Preference.class,
            cascade = CascadeType.MERGE,
            mappedBy = "teacher")
    @JsonIgnore
    private List<Preference> preferenceList = new ArrayList<>();

    @ManyToMany(targetEntity = StudentGroup.class,
            mappedBy = "classTeachers",
            fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<StudentGroup> classTeacherOf = new HashSet<>();

    //******************************
    //CONSTRUCTORS
    //**********************************

    public Teacher() {
    }

    public Teacher(String acronym) {
        this.acronym = acronym;
    }

    public Teacher(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    //************************************************
    //GETTERS
    //**********************************************$$$
    public String getAcronym() {
        return acronym;
    }
    public String getName() {
        return name;
    }

    public int getNumberOfTeachingDays(){
        return numberOfTeachingDays;}

    public int getHoursAwayFromFullTime() {
        return hoursAwayFromFullTime;
    }

    public int getCoverId() {
        return coverId;
    }

    public int getFullTime() {return fullTime;}

    public Integer getFirstOrLastHours() {
        return firstOrLastHours;
    }

    public List<Preference> getPreferenceList() {
        return preferenceList;
    }


    //************************************************************
    //SETTERS + ADJUST
    //***********************************************************

    public void setHoursAwayFromFullTime(int hoursAwayFromFullTime) {
        this.hoursAwayFromFullTime = hoursAwayFromFullTime;
    }

    public void setCoverId(int coverId) {
        this.coverId = coverId;
    }

    public void addPreference(Preference preference) {preferenceList.add(preference);}
    public void removePreference(Preference preference) {preferenceList.remove(preference);}

    public void addLessonTask(LessonTask lessonTask) {
        lessonTasks.add(lessonTask);
    }

    public void removeLessonTask(LessonTask lessonTask) {lessonTasks.remove(lessonTask);}
    /*Preferences ADD - DELETE */

    public void setName(String name) {
        this.name = name;
    }



    public void setFullTime(int fullTime) {
        this.fullTime = fullTime;
    }

    public void setFirstOrLastHours(int firstOrLastHours) {
        this.firstOrLastHours = firstOrLastHours;
    }

    public Integer rightToHalfDays(){
        return (hoursAwayFromFullTime +2) / 3;} // needs to be updated by call: updateRightToHalfDays()

    //can faster?! --> necessary??
   public Integer halfDaysInPreferences(){

        int numberOfHalfDays = 0;
        Map<DayOfWeek, List<Integer>> halfDays = new HashMap<>(7);
        for (DayOfWeek dayOfWeek: DayOfWeek.values()){
            List<Integer > forAndAfterNoon = new ArrayList<>(2);
            forAndAfterNoon.add(1);
            forAndAfterNoon.add(1);
            halfDays.put(dayOfWeek, forAndAfterNoon);
        }

        Set<DayOfWeek> days = new HashSet<>();
        for (Preference preference: this.preferenceList){
            Timeslot timeslot = preference.getTimeslot();
            DayOfWeek dayOfWeek = timeslot.getDayOfWeek();
            days.add(dayOfWeek);
            if( timeslot.inAfternoon()){
                halfDays.get(dayOfWeek).add(1, 0);
            }
            if (timeslot.inForenoon()){
                halfDays.get(dayOfWeek).add(0, 0);
            }
        }
        for (DayOfWeek day: days){
            numberOfHalfDays += halfDays.get(day).get(0) + halfDays.get(day).get(1);
        }

        return numberOfHalfDays;
    }
    //**********************************************************
    // UPDATE DEPENDING
    //********************************************************

    //UPDATE ALL DEPENDENTS:
    public void updateDependents(DefaultSettings ds){
        updateDependsFromLeTa(ds);
        updateDependsFromPrefs(ds);
    }
    //UPDATE FROM LESSONTASKS
    public void updateDependsFromLeTa(DefaultSettings ds){
        List<Integer> hoursPerGrade = new ArrayList<>(Collections.nCopies(ds.getNumberOfGrades(),0));

        for (LessonTask lessonTask: lessonTasks){
            if (lessonTask.isATeachingTask()) {
                int grade = DefaultSettings.determineGradeForYear(lessonTask.getStudentGroups()
                        .stream()
                        .map(StudentGroup::getYear)
                        .max(Integer::compareTo)
                        .orElseThrow(NotFoundException::new));
                hoursPerGrade.set(grade - 1, hoursPerGrade.get(grade - 1) + lessonTask.getMultiplicity());
            }
        }
        Fraction percentage = ds.determineTaskPercentage(hoursPerGrade);
        fullTime = ds.determineFullTime(hoursPerGrade);
        hoursAwayFromFullTime = Math.max((int) Math.ceil((float) (percentage.getDenominator() - percentage.getNumerator() )* fullTime / (float) percentage.getDenominator()), 0);
    }

    //UPDATE FROM PREFERENCES + LESSONTASKS

    public void updateDependsFromPrefs(DefaultSettings ds){

        List<Integer> firstAndLast = new ArrayList<>(Collections.nCopies(2, 0));
        List<Integer> slotsLeftADay = new ArrayList<>(ds.getTimeslotsADay());

        Set <Integer> dayIndexesOfWeek = new HashSet<>();
        for (DayOfWeek dayOfWeek: ds.getFullDays()){
            dayIndexesOfWeek.add(dayOfWeek.getValue());
        }
        for (DayOfWeek dayOfWeek: ds.getHalfDays()){
            dayIndexesOfWeek.add(dayOfWeek.getValue());
        }

        for (Preference preference: this.preferenceList){
            Timeslot timeslot = preference.getTimeslot();
            Integer dayIndex = timeslot.getDayOfWeek().getValue()-1;
            slotsLeftADay.set(dayIndex, slotsLeftADay.get(dayIndex)-1);
            if(ds.isFirstHour(timeslot)){
                firstAndLast.set(0, firstAndLast.get(0)+1);
            } else {
                if (ds.isLastHour(timeslot) && ds.noFullHourBeforeAsLastHour(timeslot)){
                    firstAndLast.set(1, firstAndLast.get(1)+1);
                }}
        }

        numberOfTeachingDays = ds.getWeekLength();
        for (Integer ind: dayIndexesOfWeek){
            if (slotsLeftADay.get(ind-1) < 1){
                this.numberOfTeachingDays -= 1;
            }
        }

        // Change sign to signal that in preferences no full Day is chosen.
        if (ds.getWeekLength() == numberOfTeachingDays){
            numberOfTeachingDays = - numberOfTeachingDays;
        }


        this.firstOrLastHours = ds.determineFirstAndLastHours(firstAndLast);
    }

    public void addStudentGroup(StudentGroup studentGroup) {
        if (studentGroup.getClassTeachers().contains(this)){
            classTeacherOf.add(studentGroup);
        }
    }

    public void removeStudentGroup(StudentGroup studentGroup) {
        if (! studentGroup.getClassTeachers().contains(this)){
            classTeacherOf.remove(studentGroup);
        }
    }

    public Boolean isClassTeacher(){
        return ! classTeacherOf.isEmpty();
    }



    /*
    Searching with hibernate-panache
    */



    public static List<Teacher> findByAcronym(String acronym) {
       return find("acronym", acronym).list();
}
    @Override
    public int compareTo(Teacher O) {
        return COMPARATOR.compare(this, O);
    }

    @Override
    public String toString() {
        return acronym;
    }
}
