package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.engine.internal.Cascade;

import javax.persistence.*;
import java.util.*;

@Entity
@XStreamAlias("Teacher")
public class Teacher extends PanacheEntityBase implements Comparable<Teacher>{

    private static final Comparator<Teacher> COMPARATOR = Comparator.comparing(Teacher::getAcronym);

    @Id
    private String acronym;
    private String name;

    @ManyToMany(targetEntity = LessonTask.class,
            mappedBy = "taughtBy",
            fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<LessonTask> lessonTasks = new HashSet<>();

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

    public void addLessonTask(LessonTask lessonTask) {
        lessonTasks.add(lessonTask);
    }

    public void removeLessonTas(LessonTask lessonTask) {lessonTasks.remove(lessonTask);}
    /*Preferences ADD - DELETE */

    public void setName(String name) {
        this.name = name;
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
}
