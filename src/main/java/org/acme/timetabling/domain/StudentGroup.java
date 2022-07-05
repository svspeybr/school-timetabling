package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.*;

@Entity
@XStreamAlias("StudentGroup")
public class StudentGroup extends PanacheEntityBase implements Comparable<StudentGroup>{

    private static final Comparator<StudentGroup> COMPARATOR = Comparator.comparing(StudentGroup::getGroupName);
    @Id
    private String groupName;

    @ManyToMany(targetEntity = LessonTask.class,
            mappedBy = "studentGroups",
            fetch = FetchType.EAGER,
            cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<LessonTask> lessonTasks = new HashSet<>();

    private Integer numberOfStudents;

    @ManyToMany(targetEntity = Teacher.class,
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Teacher> classTeachers = new HashSet<>();

    //To do: configuring it never to be null
    private Integer year;

    public StudentGroup() {}

    public StudentGroup(String groupName) {
        this.groupName = groupName;
    }

    public StudentGroup(String name, Integer numberOfStudents) {
        this.groupName =name;
        this.numberOfStudents = numberOfStudents;
    }

    public void addLessonTask(LessonTask lessonTask) {
        lessonTasks.add(lessonTask);
    }

    public Boolean hasLessonTask() {
        return ! this.lessonTasks.isEmpty();
    }

    public void setNumberOfStudents(Integer numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public String getGroupName() {
        return groupName;
    }

    public Integer getNumberOfStudents() {
        return numberOfStudents;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Set<Teacher> getClassTeachers() {
        return classTeachers;
    }

    public void addClassTeacher(Teacher teacher){
        classTeachers.add(teacher);
        teacher.addStudentGroup(this);
    }
    public void removeClassTeacher(Teacher teacher){
        classTeachers.remove(teacher);
        teacher.removeStudentGroup(this);
    }

    public static List<StudentGroup> findByGroupName(String groupName) {
        return find("groupName", groupName).list();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentGroup that = (StudentGroup) o;
        return getGroupName().equals(that.getGroupName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupName());
    }

    @Override
    public String toString() {
        return groupName;
    }

    @Override
    public int compareTo(StudentGroup O) {
        return COMPARATOR.compare(this, O);
    }
}
