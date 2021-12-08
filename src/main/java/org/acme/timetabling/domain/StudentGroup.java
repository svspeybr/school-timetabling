package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

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
            fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<LessonTask> lessonTasks = new HashSet<>();

    private Integer numberOfStudents;

    private Integer year;

    public StudentGroup() {}

    public StudentGroup(String groupName) {
        this.groupName = groupName;
    }

    public StudentGroup(String name, Integer numberOfStudents) {
        this.groupName =name;
        this.numberOfStudents =numberOfStudents;
    }

    public void addLessonTask(LessonTask lessonTask) {
        lessonTasks.add(lessonTask);
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
    public int compareTo(StudentGroup O) {
        return COMPARATOR.compare(this, O);
    }
}
