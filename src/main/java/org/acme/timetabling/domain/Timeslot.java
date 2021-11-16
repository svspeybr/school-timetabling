package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.*;

@Entity
public class Timeslot extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long timeslotId;

    /*@ManyToMany(mappedBy = "preferences",
            fetch = FetchType.EAGER,
            cascade = CascadeType.PERSIST)
    private List<Teacher> preferenceOfTeachers = new ArrayList<>();*/

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    private Boolean lastResort =false;

    // No-arg constructor required for Hibernate
    public Timeslot() {
    }

    public Timeslot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getTimeslotId() {
        return timeslotId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean isLastResort() {
        return this.lastResort;
    }

    public void changeLastResort() {
        this.lastResort = ! this.lastResort;
    }

/*    public List<Timeslot> fetchPrevSlotOnSD() {
        return find("endTime", this.startTime).list();
    }

    public List<Timeslot> fetchNextSlotOnSD() {
        return find("startTime", this.endTime).list();
    }

    @JsonIgnore
    public Timeslot getNextSlot() {
        List<Timeslot> nextTimeSlots = this.fetchNextSlotOnSD();
        if (nextTimeSlots.isEmpty()) {
            nextTimeSlots = this.fetchPrevSlotOnSD();
        }
        return nextTimeSlots.get(0);
    }*/

    public Boolean isConsecutiveTo(Timeslot timeslot) {
        return this.dayOfWeek == timeslot.getDayOfWeek() &&
                (this.startTime.equals(timeslot.getEndTime()) ||
                        this.endTime.equals(timeslot.getStartTime()));
    }
}
