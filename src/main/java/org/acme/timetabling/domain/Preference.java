package org.acme.timetabling.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.*;

@Entity
@XStreamAlias("Preference")
public class Preference extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long preferenceId;

    @ManyToOne(targetEntity = Teacher.class)
    private Teacher teacher;
    @ManyToOne
    private Timeslot timeslot;

    public Preference() {
    }

    public Preference(Teacher teacher, Timeslot timeslot) {
        this.teacher = teacher;
        this.timeslot = timeslot;
        teacher.addPreference(this);
    }

    public Long getPreferenceId() {
        return preferenceId;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }


    @Override
    public String toString() {
        return "pref("+ teacher.getAcronym() + "-"+ timeslot.toString() +")";
    }
}
