package org.acme.timetabling.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.apache.commons.math3.fraction.Fraction;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public final class DefaultSettings extends PanacheEntityBase {

    @Id
    private Long defaultSetId;

    private Integer weekLength = 5;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<DayOfWeek> fullDays = Arrays.stream(DayOfWeek.values()).filter(dayOfWeek -> (dayOfWeek.getValue() <6 && dayOfWeek.getValue() != 3)).collect(Collectors.toList());

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<DayOfWeek> halfDays = new ArrayList<>(Collections.singleton(DayOfWeek.WEDNESDAY));

    private Integer numberOfGrades = 3;

    private Integer NumberOfTeachingDays = 5;
    //Needed for constraintfactory -> counting lesoverlaps per lessontask --> Placed here or
    private Integer NumberOfTimeslots;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Integer> timeslotsADay = new ArrayList<>(Collections.nCopies(7,0));


    public DefaultSettings(){
    }

    public DefaultSettings(Long id){
        this.defaultSetId = id;
    }


    public List<Integer> getTimeslotsADay() {
        return timeslotsADay;
    }

    public Integer getWeekLength() {
        return weekLength;
    }

    public void addSlotCount(int dayIndex, int value){
        int count = timeslotsADay.get(dayIndex - 1) + value;
        if (count < 0) {
            throw new IllegalArgumentException("Something went wrong at counting slots on dayIndex" + dayIndex);
        }
        this.timeslotsADay.set(dayIndex -1, count);
    }

    public static int determineGradeForYear(int year) {
        return (year + 1) /2;
    }

    public List<DayOfWeek> getFullDays() {
        return fullDays;
    }

    public List<DayOfWeek> getHalfDays() {
        return halfDays;
    }

    public int getNumberOfGrades(){
        return numberOfGrades;
    }

    public Fraction determineTaskPercentage(List<Integer> hoursPerGrade){
        Fraction percentage = new Fraction(0);
        for (int grade = 0; grade < numberOfGrades; grade ++){
            percentage = percentage.add(new Fraction(hoursPerGrade.get(grade), gradeWeight(grade + 1)));
            }
        return percentage;
    }

    public int determineFirstAndLastHours(List<Integer> firstAndLast){
        int firstOrLastHours = 0;
        if (firstAndLast.get(0).equals(weekLength)){ //first hours
            firstOrLastHours ++;
        }
        if (firstAndLast.get(1).equals(weekLength -1 )) { //Wednesday
            firstOrLastHours ++;
        }
        return firstOrLastHours;
    }

    private int gradeWeight(int grade){
        if (grade == 1){
            return 22;
        }
        if (grade == 2){
            return 21;
        }
        if (grade == 3){
            return 20;
        }
        throw new IllegalArgumentException();
    }

    public int determineFullTime(List<Integer> hoursPerGrade){
        int max = 0;
        int maxAtGrade = 1;
        for (int grade =0; grade< numberOfGrades; grade ++){
            int hours = hoursPerGrade.get(grade);
            if(hours > max){
                max = hours;
                maxAtGrade = grade + 1;
            }
        }
        return gradeWeight(maxAtGrade);
    }

    public Boolean isFirstHour( Timeslot timeslot) {
        return timeslot.getStartTime().compareTo(LocalTime.of(9, 20)) < 0;
    }

    public Boolean isLastHour( Timeslot timeslot) {
/*        if (timeslot.getDayOfWeek().equals(DayOfWeek.WEDNESDAY)){
            return timeslot.getStartTime().compareTo(LocalTime.of(11, 25)) >= 0;
        }*/
        return timeslot.getStartTime().compareTo(LocalTime.of(14, 55)) >= 0;
    }

    public Boolean noFullHourBeforeAsLastHour(Timeslot timeslot) {
/*        if (timeslot.getDayOfWeek().equals(DayOfWeek.WEDNESDAY)){
            return timeslot.getStartTime().compareTo(LocalTime.of(11, 25)) >= 0;
        }*/
        return timeslot.getStartTime().minusMinutes(50).compareTo(LocalTime.of(14, 55)) < 0;
    }

    public Integer getNumberOfTeachingDays() {
        return NumberOfTeachingDays;
    }

    public void setNumberOfTeachingDays(Integer numberOfTeachingDays) {
        NumberOfTeachingDays = numberOfTeachingDays;
    }
}
