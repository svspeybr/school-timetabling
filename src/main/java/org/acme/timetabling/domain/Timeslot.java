package org.acme.timetabling.domain;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;


import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import javax.persistence.*;

@Entity
@XStreamAlias("Timeslot")
public class Timeslot extends PanacheEntityBase implements Comparable<Timeslot>{

    private static final Comparator<Timeslot> COMPARATOR = Comparator.comparing(Timeslot::getPosition);
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long timeslotId;

    private Integer position = 0;
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

    public Integer getPosition() {
        return position;
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

    public void setPosition(Integer position) {
        this.position = position;
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

    public Boolean inAfternoon(){
        LocalTime midTime = LocalTime.of(12, 30);
        return endTime.compareTo(midTime) > 0;
    }

    public Boolean inForenoon(){
        LocalTime midTime = LocalTime.of(12, 30);
        return startTime.compareTo(midTime) < 0;
    }

    public Boolean isConsecutiveTo(Timeslot timeslot) {
        return this.dayOfWeek == timeslot.getDayOfWeek() &&
                (this.startTime.equals(timeslot.getEndTime()) ||
                        this.endTime.equals(timeslot.getStartTime()));
    }

    public static Integer numberOfGapsInBetween(List<Timeslot> timeslotList) {
        int length = timeslotList.size();
        if (length <= 1) {
            return 0;
        }
        Set<DayOfWeek> onDifferentDays = new HashSet<>(length);
        timeslotList.forEach(timeslot -> onDifferentDays.add(timeslot.getDayOfWeek()));
        Collections.sort(timeslotList);
        return timeslotList.get(length - 1).getPosition() - timeslotList.get(0).getPosition()
                + 1 - length + 8 * (onDifferentDays.size() - 1);

    }
    @Override
    public int compareTo(Timeslot O) {
        return COMPARATOR.compare(this, O);
    }

}
