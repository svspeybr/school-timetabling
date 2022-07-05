package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class ThemeCollection extends PanacheEntityBase {

    //ThemeCollection bundles lessons for which only a restricted number (= timeslots[timeslot]) can have the same timeslot.
    //E.G. Only max. 1(/2) LO lesson(s)can be used for using the swimming.
    // The map 'timeslots' saves the maximal allowed combinations.

    @Id
    @Column(name = "THEME_ID")
    private String theme;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="THEME_LESSONS",
            joinColumns = @JoinColumn( name="THEME_ID"))
    @Column(name = "TL_LESSON_ID", updatable = false, insertable = false)
    private List<Long> lessonIds= new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "THEME_MULTIPLICITIES",
            joinColumns = {@JoinColumn(name = "TM_THEME_ID", referencedColumnName = "THEME_ID")})
    @MapKeyJoinColumn(name = "TM_TIMESLOTID")
    @Column(name = "TM_MULTIPLICITY")
    private Map<Timeslot, Integer> timeslots = new HashMap<>();



    public ThemeCollection(){}

    public ThemeCollection(String theme){
        this.theme = theme;
    }

    public void addMultiplicityForTimeslot(Timeslot timeslot, Integer integer){
        this.timeslots.put(timeslot, integer);
    }
    public void removeTimeslot(Timeslot timeslot){
        this.timeslots.remove(timeslot);
    }

    public Integer getMultiplicity(Timeslot timeslot){
        Integer val = this.timeslots.get(timeslot);
        if (val != null) {
            return val;
        }
        return 0;
    }

    public Map<Timeslot, Integer> getMultiplicityTimeslots(){
        return this.timeslots;
    }

    public String getTheme() {
        return theme;
    }

    public List<Long> getLessonIds() {
        return lessonIds;
    }

    public void addLesson(Long lessonId){
        lessonIds.add(lessonId);
    }

    public void removeLesson(Long lessonId){
        lessonIds.remove(lessonId);
    }
}
