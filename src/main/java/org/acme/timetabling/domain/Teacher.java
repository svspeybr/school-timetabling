package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.engine.internal.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Teacher extends PanacheEntityBase {

    @Id
    private String acronym;
    private String name;

    @ManyToMany(targetEntity = Lesson.class,
            mappedBy = "taughtBy",
            cascade = CascadeType.MERGE)
    @JsonIgnore
    private Set<Lesson> teachesLessons = new HashSet<>();

    @OneToMany(targetEntity = Preference.class,
            cascade = CascadeType.MERGE,
            mappedBy = "teacher")
    @JsonIgnore
    List<Preference> preferenceList;


/*    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "preferences",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "timeslot_id"))
    private List<Timeslot> preferences = new ArrayList<>();*/

    public Teacher() {
    }

    public Teacher(String acronym) {
        this.acronym = acronym;
    }

    public Teacher(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }


    public String getAcronym() {
        return acronym;
    }

    public String getName() {
        return name;
    }

    public List<Preference> getPreferenceList() {
        return preferenceList;
    }

    public void addLessonToTeach(Lesson lesson) {
        teachesLessons.add(lesson);
    }

    public void removeLessonToTeach(Lesson lesson) {teachesLessons.remove(lesson);}
    /*Preferences ADD - DELETE */

/*   public List<Timeslot> addPreference(Timeslot preference) {
        List<Timeslot> newPreferences = new ArrayList<>(this.preferences);
        newPreferences.add(preference);
        return newPreferences;}*/

/*    public void deletePreference(Timeslot preference) {
        this.preferences.remove(preference);}*/

    public void setName(String name) {
        this.name = name;
    }

/*    public void addPreference(Timeslot preference) {
        this.preferences.add(preference);
    }*/

/*
    Searching with hibernate-panache
    */

    public static List<Teacher> findByAcronym(String acronym) {
       return find("acronym", acronym).list();
}
}
