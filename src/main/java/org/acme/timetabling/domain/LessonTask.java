package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class LessonTask extends PanacheEntityBase {

    /*FIELDS */
    @Id
    private Integer taskNumber;

    private Integer multiplicity;
    @OneToMany(targetEntity = Lesson.class,
            cascade = CascadeType.MERGE,
            mappedBy = "lessonTask",
            fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Lesson> lessonsOfTaskList;

    //Couple the first two elements of lessonsOfTaskList
    private Boolean coupled = false;
    public LessonTask(){
    }

    public LessonTask(Integer taskNumber) {
        this.taskNumber =taskNumber;
        this.multiplicity = 0;
        this.lessonsOfTaskList = new ArrayList<>();
    }

    /*GETTERS AND SETTERS */


    public Boolean isCoupled() {
        return coupled;
    }

    public void changeCoupled() {
        this.coupled = ! this.coupled;
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


    public List<Lesson> getLessonsOfTaskList() {
        return lessonsOfTaskList;
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



    public void setLessonsOfTaskList(List<Lesson> lessonList) {
        this.lessonsOfTaskList = lessonList;
    }

    //SEPARATION CONDITIONS

    private List<Integer> countLessons0nSameDay() {
        List<Integer> numberOfLessons = new ArrayList<>(Collections.nCopies(7, 0));
        for (Lesson lesson: this.lessonsOfTaskList) {
            Timeslot timeslot = lesson.getTimeslot();
            if (! (timeslot == null)) {
                int index = timeslot.getDayOfWeek().getValue() - 1;
                numberOfLessons.set(index, numberOfLessons.get(index) + 1);
            }
        }
        return numberOfLessons;
    }

    public Boolean exceedMaxLessonsOnSameDay() {
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
    }

    public int exceedMaxLessonsOnSameDayInt() {
        List<Integer> numberOfLessons = new ArrayList<>(this.countLessons0nSameDay())
                .stream()
                .filter(mul -> mul >= 2)
                .collect(Collectors.toList());
        int exceedingTimes;
        if (this.multiplicity < 3 && (! this.coupled)) {
            exceedingTimes = numberOfLessons.size();
        } else if (this.multiplicity < 6) {//BEWARE: THERE IS STILL A PENALTY WHEN A TASK OF 4 LESSONS IS COUPLED AND is divided on 2 days (2/2)
                exceedingTimes = Math.max(numberOfLessons.size() - 1, 0); // One pair is allowed (the penalty for more than 2 lessons follows later)
        } else {
                exceedingTimes =0;
        }

        exceedingTimes = exceedingTimes + (int) numberOfLessons.stream().filter(mul -> mul >= 3).count();

        return exceedingTimes;
    }
}
