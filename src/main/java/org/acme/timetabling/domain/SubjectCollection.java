package org.acme.timetabling.domain;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class SubjectCollection extends PanacheEntityBase {

    @Id
    @Column(name = "SUBJECTCOLLECTION_ID")
    private  String collectionName;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> subjects = new HashSet<>();

    private int maxAssignmentsOnSameSlot;

    public SubjectCollection(){
    }

    public SubjectCollection(String name, int max){
        this.collectionName = name;
        this.maxAssignmentsOnSameSlot = max;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Set<String> getSubjects() {
        return subjects;
    }

    public int getMaxAssignmentsOnSameSlot() {
        return maxAssignmentsOnSameSlot;
    }

    public void addSubject(String subject){
        this.subjects.add(subject);
    }

    public void removeSubject(String subject){
        this.subjects.remove(subject);
    }

    @Override
    public String toString(){
        return collectionName +"(max: " + maxAssignmentsOnSameSlot +" )";
    }
}
