package org.acme.timetabling.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public final class DefaultSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long defaultSetId;

    Integer NumberOfTeachingDays = 5;
    //Needed for constraintfactory -> counting lesoverlaps per lessontask --> Placed here or
    Integer NumberOfTimeslots;
    // PLACE HERE OR AS BOOLEAN IN LESSONTASK?
    @ElementCollection(fetch = FetchType.EAGER)
    //Fetchmode Join?
    @Fetch(value = FetchMode.SUBSELECT)
    Set<String> scienceCourses = new HashSet<>();


    public DefaultSettings(){
    }

    public void addScienceCourse(String course){
        scienceCourses.add(course);
    }

    public void removeScienceCourse(String course){
        scienceCourses.remove(course);
    }

    public Integer getNumberOfTeachingDays() {
        return NumberOfTeachingDays;
    }

    public void setNumberOfTeachingDays(Integer numberOfTeachingDays) {
        NumberOfTeachingDays = numberOfTeachingDays;
    }
}
