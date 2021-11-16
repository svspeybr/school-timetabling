package org.acme.timetabling.solver;

import org.acme.timetabling.domain.*;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import static org.optaplanner.core.api.score.stream.Joiners.filtering;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class TimeTableConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                roomConflict(constraintFactory),
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                teacherTimeEfficiency(constraintFactory),
                lessonTaskOnSameDayConflict(constraintFactory),
                teacherPreferenceConflict(constraintFactory),
                couplingConflict(constraintFactory),
                lastResortTimeslotConflict(constraintFactory)
        };
    }

    //*****************************************************************
    //HARD CONSTRAINTS
    //**********************************************************************
    private Constraint roomConflict(ConstraintFactory constraintFactory) {
            return constraintFactory.from(Lesson.class)
                    .join(Lesson.class, Joiners.equal(Lesson::getTimeslot),
                            Joiners.equal(Lesson::getRoom),
                            Joiners.lessThan(Lesson::getLessonId))
                    .penalize("Room conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .join(Lesson.class, Joiners.equal(Lesson::getTimeslot),
                        Joiners.lessThan(Lesson::getLessonId))
                .filter((lesson1, lesson2) -> {
                    Set<Teacher> intersection = new HashSet<Teacher>(lesson1.getTaughtBy());
                    intersection.retainAll(lesson2.getTaughtBy());
                    return ! intersection.isEmpty();
                })
                .penalize("Teacher conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint couplingConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .filter(Lesson::isActivelyCoupled)
                .filter(Lesson::isCoupled)
                .join(Lesson.class, Joiners.equal(Lesson::getLessonTask),
                        Joiners.equal(Lesson::isCoupled),
                        Joiners.lessThan(Lesson::getLessonId))
                .filter((lesson1, lesson2) -> ! lesson1.isConsecutiveTo(lesson2))
                .penalize("Separation conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint lastResortTimeslotConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .filter(Lesson::isOnLastResortTimeslot)
                .penalize("OnLastResortTimeslot conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint lessonTaskOnSameDayConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .filter(leta -> leta.exceedMaxLessonsOnSameDay())
                .penalize("exceedMaxLessonsOnSameDay conflict",
                        HardSoftScore.ONE_HARD);
    }




    private Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .join(Lesson.class, Joiners.equal(Lesson::getTimeslot),
                        Joiners.lessThan(Lesson::getLessonId))
                .filter((lesson1, lesson2) -> {
                    Set<StudentGroup> intersection = new HashSet<StudentGroup>(lesson1.getStudentGroups());
                    intersection.retainAll(lesson2.getStudentGroups());
                return ! intersection.isEmpty();})
                .penalize("Student group conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint teacherPreferenceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .join(Preference.class, filtering((lesson, preference) ->
                        lesson.getTaughtBy().contains(preference.getTeacher()) &&
                        lesson.getTimeslot().equals(preference.getTimeslot())))
                .penalize("Teacher preference conflict", HardSoftScore.ONE_HARD);
    }


    //*****************************************************************
    //SOFT CONSTRAINTS
    //**********************************************************************

    private Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    Set<Teacher> intersection = new HashSet<Teacher>(lesson1.getTaughtBy());
                    intersection.retainAll(lesson2.getTaughtBy());
                    return ! intersection.isEmpty();
                })
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(
                            lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .reward("Teacher time efficiency", HardSoftScore.ONE_SOFT);
    }


}
