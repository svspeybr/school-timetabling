package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class StudentGroup extends PanacheEntityBase {

    @Id
    private String groupName;
    @ManyToMany(targetEntity = Lesson.class,
            mappedBy = "studentGroups",
            cascade = CascadeType.MERGE)
    @JsonIgnore
    private Set<Lesson> followsLessons = new HashSet<>();

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

    public Set<Lesson> getFollowsLessons() {
        return followsLessons;
    }

    public void addLessonToFollow(Lesson lesson) {
        followsLessons.add(lesson);
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
}
