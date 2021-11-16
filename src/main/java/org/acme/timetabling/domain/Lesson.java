package org.acme.timetabling.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.*;


@PlanningEntity
@Entity
public class Lesson extends PanacheEntityBase {

    /* FIELDS*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long lessonId;

    @ManyToMany(targetEntity = Teacher.class, fetch = FetchType.EAGER)
    private Set<Teacher> taughtBy= new HashSet<>();
    /*private String teacher;*/
    private String subject;
    private boolean pinned = false;
    private boolean coupled = false;

    @ManyToMany(targetEntity = StudentGroup.class, fetch = FetchType.EAGER)
    private Set<StudentGroup> studentGroups = new HashSet<>();

    //Initialized during planning
    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    @ManyToOne
    private Timeslot timeslot;
    @PlanningVariable(valueRangeProviderRefs = "roomRange")
    @ManyToOne
    private Room room;
    @ManyToOne(targetEntity = LessonTask.class)
    private LessonTask lessonTask;

/*    private List<Preference> getPreferences() {
        return taughtBy.stream().flatMap(teacher -> teacher.getPreferenceList().stream()).toList();
    }*/
    /*CONSTRUCTORS*/

    // No-arg constructor required for Hibernate and OptaPlanner
    public Lesson() {
    }

    public Lesson(String subject, Teacher teacher, StudentGroup studentGroup) {
        this.subject = subject;
        addTeacher(teacher);
        addStudentGroup(studentGroup);
    }

    public Lesson(String subject, List<Teacher> teachers, List<StudentGroup> studentGroups) {
        this.subject = subject;
        addTeachers(teachers);
        addStudentGroups(studentGroups);
    }

    public Lesson(String subject) {
        this.subject = subject;
    }

/*    public List<Lesson> generateMultipleLessons(Integer multiplicity,
                                                String subject,
                                                Teacher teacher,
                                                StudentGroup studentGroup) {
        List<Lesson> lessonList = new ArrayList<>();
        for (int i = 0; i < multiplicity; i++) {
            Lesson lesson = new Lesson(subject, teacher, studentGroup);
            lessonList.add(lesson);
        }
        return(lessonList);
    }*/
    /*PINNED */
    @PlanningPin
    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void changeCoupled() {
         coupled = ! coupled;
    }
    /*Coupling*/
    public boolean isCoupled() {
        return coupled;
    }
    public boolean isActivelyCoupled() {
        return this.lessonTask.isCoupled();
    }

    // MORE EFFICIENT?????
    public long nLesSameDaySameTask() {
        return this.lessonTask.getLessonsOfTaskList()
                .stream()
                .filter(lesson -> lesson.getTimeslot().getDayOfWeek()
                        .equals(this.timeslot.getDayOfWeek()))
                .count();
    }
    // CONDITION ACCORDING TO SCHOOL...
    public boolean accToLesSepCriteriaSPC () {
        long nlessons = this.nLesSameDaySameTask();
        return (nlessons < 3 & this.lessonTask.getMultiplicity() > 5) ||
                  nlessons < 2 ;
    }


    /*Placed on last resort */
    public Boolean isOnLastResortTimeslot() {
        if (this.timeslot == null){
            return false;
        }
        return this.timeslot.isLastResort();
    }

    @Override
    public String toString() {
        return subject + "(" + lessonId + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getLessonId() {
        return lessonId;
    }

    public String getSubject() {
        return subject;
    }

    public Set<Teacher> getTaughtBy() {
        return taughtBy;
    }


    public Set<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public String getStudentGroupsNames() {
        StringBuilder studentGroupNames = new StringBuilder();
        for (StudentGroup studentGroup: this.studentGroups) {
            studentGroupNames.append(", "+studentGroup.getGroupName());
        }
        /*if (!studentGroupNames.isEmpty()) {
            studentGroupNames.delete(0,2);
        }*/
        return studentGroupNames.toString();
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public LessonTask getLessonTask() {
        return lessonTask;
    }

    public void setLessonTask(LessonTask lessonTask) {
        if(this.lessonTask != null) {
            this.lessonTask.deleteLessonsOfTaskList(this);
        }
        this.lessonTask = lessonTask;
        lessonTask.addLessonsToTaskList(this);

    }

    public void addStudentGroup(StudentGroup studentGroup) {
        if (!this.studentGroups.contains(studentGroup)) {
            this.studentGroups.add(studentGroup);
            studentGroup.addLessonToFollow(this);
        }
    }

    public void addStudentGroups(Collection<StudentGroup> studentGroups) {
        for (StudentGroup studentGroup: studentGroups) {
            addStudentGroup(studentGroup);
        }
    }

    public void addTeacher(Teacher teacher) {
        if (!this.taughtBy.contains(teacher)) {
            this.taughtBy.add(teacher);
            teacher.addLessonToTeach(this);
        }
    }

    public void addTeachers(Collection<Teacher> teachers) {
        for (Teacher teacher: teachers) {
            addTeacher(teacher);
        }
    }

    public void updateTeacherFromLessonTask(Teacher oldTeacher, Teacher newTeacher){
        //Update lessons linked to task
        //update teachers linked to lessons
        this.lessonTask.getLessonsOfTaskList().forEach(lesson ->
                                                {lesson.removeTeacher(oldTeacher);
                                                 lesson.addTeacher(newTeacher);
                                                oldTeacher.removeLessonToTeach(lesson);
        });

    }

    private void removeTeacher(Teacher teacher) {
        if(this.taughtBy.contains(teacher)){
            this.taughtBy.remove(teacher);
        }
    }



    public Boolean isConsecutiveTo(Lesson lesson) {
        return this.timeslot.isConsecutiveTo(lesson.getTimeslot());
    }


    /*    public boolean hasLargerId (Lesson lesson) {
        return this.lessonId > lesson.getLessonId();
    }*/
    //setTimeSlot --> takes into account coupling --> WRONG METHOD ...
    //The algorithm tries often to separate the coupling --> resulting in increase of NEGATIVE HARD SCORE
   /* public void setTimeslot(Timeslot timeslot) {
        if (this.lessonTask.isCoupled() && this.coupled) {
            List<Lesson> lessonList = this.lessonTask.getLessonsOfTaskList().stream().filter(lesson -> lesson.isCoupled() && lesson.hasLargerId(this) ).toList();
            //MAKING USE OF THE ORDER OF lessonsOfTaskList
            if (! lessonList.isEmpty()) {
            Lesson coupledLesson = lessonList.get(0);
            //UPDATE ONLY SMALLEST TIMESLOT
            this.timeslot = timeslot;
            coupledLesson.setTimeslotUncoupled(timeslot.getNextSlot());
        }
        } else {
            this.timeslot = timeslot;
        }
    }*/
    /*    public void setTimeslotUncoupled(Timeslot timeslot) {

        this.timeslot = timeslot;
    }*/

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }




    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setTaughtBy(Set<Teacher> teachers) {
        this.taughtBy = teachers;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

}


