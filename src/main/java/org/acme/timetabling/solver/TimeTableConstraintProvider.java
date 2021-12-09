package org.acme.timetabling.solver;

import org.acme.timetabling.domain.*;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

import static org.optaplanner.core.api.score.stream.Joiners.filtering;

import java.util.HashSet;
import java.util.Set;

//ConstraintFactory.from(...clas)-> gets lists from the planning problem --> planning entities and problem facts

public class TimeTableConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
               roomConflict(constraintFactory),
/*                 lessonTaskInternalConflict(constraintFactory),*/
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                teacherTimeEfficiency(constraintFactory),
               lessonTaskOnSameDayConflict(constraintFactory),
/*                 teacherPreferenceConflict(constraintFactory),*/
                /*couplingConflict(constraintFactory),*/
                lastResortTimeslotConflict(constraintFactory)
        };
    }

    //*****************************************************************
    //HARD CONSTRAINTS
    //**********************************************************************
/*    private Constraint roomConflict(ConstraintFactory constraintFactory) {
            return constraintFactory.from(Lesson.class)
                    .join(Lesson.class, Joiners.equal(Lesson::getTimeslot),
                            Joiners.equal(Lesson::getRoom),
                            Joiners.lessThan(Lesson::getLessonId))
                    .penalize("Room conflict", HardSoftScore.ONE_HARD);
    }*/

/*    private Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .join(LessonTask.class,
                        Joiners.lessThan(LessonTask::getTaskNumber),
                        Joiners.lessThanOrEqual(lessontask -> Collections.min(lessontask.getTaughtBy()),
                                lessontask -> Collections.max(lessontask.getTaughtBy())),
                        Joiners.greaterThanOrEqual(lessontask -> Collections.max(lessontask.getTaughtBy()),
                                lessontask -> Collections.min(lessontask.getTaughtBy())),
                        Joiners.lessThanOrEqual(LessonTask::getMinimumPositionOfTimeslots,
                                LessonTask::getMaximumPositionOfTimeslots),
                        Joiners.greaterThanOrEqual(LessonTask::getMaximumPositionOfTimeslots,
                                LessonTask::getMinimumPositionOfTimeslots))
                .filter(((lessonTask, lessonTask2) -> {
                    HashSet<Teacher> intersection = new HashSet<>(lessonTask2.getTaughtBy());
                    intersection.retainAll(lessonTask.getTaughtBy());
                    return ! intersection.isEmpty();}))
                .penalize("Teacher conflict", HardSoftScore.ONE_HARD, LessonTask::numberOfTimslotOverlaps);
    }*/

/*    private Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .join(Lesson.class,
                Joiners.lessThan(Lesson::getLessonId),
                Joiners.equal(Lesson::getTimeslot))
                .filter(((lesson, lesson2) -> {
                    HashSet<Teacher> intersection = new HashSet<>(lesson2.getTaughtBy());
                    intersection.retainAll(lesson.getTaughtBy());
                    return ! intersection.isEmpty();}))
                .penalize("Teacher conflict", HardSoftScore.ONE_HARD);
    }*/

/*    private Constraint couplingConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonBlockDto.class)
                .join(Lesson.class, Joiners.equal(LessonBlockDto::getLessonId, Lesson::getLessonId))
                .groupBy( (lessonBlockDto, lesson) -> lessonBlockDto.getLessonBlockId(), ConstraintCollectors.toList((lessonBlockDto, lesson)-> lesson.getTimeslot()))
                .penalize("Coupling conflict", HardSoftScore.ONE_HARD, (integer, timeslotList)-> {
                    //problem with rounding?
                    return Math.max(Timeslot.numberOfGapsInBetween(timeslotList), 0);
                });
    }*/


/*    private Constraint lastResortTimeslotConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .filter(Lesson::isOnLastResortTimeslot)
                .penalize("OnLastResortTimeslot conflict", HardSoftScore.ONE_HARD);
    }*/

/*    private Constraint lessonTaskOnSameDayConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .join(Lesson.class, Joiners.equal(lessonTask -> lessonTask,
                        Lesson::getLessonTask))
                .groupBy( (lessonTask, lesson) -> lessonTask, ConstraintCollectors.toList((lessonTask, lesson)-> lesson))
                .penalize("exceedMaxLessonsOnSameDay conflict",
                        HardSoftScore.ONE_HARD,
                        LessonTask::exceedMaxLessonsOnSameDayInt);
    }*/

    private Constraint lessonTaskInternalConflict(ConstraintFactory constraintFactory){
        return constraintFactory.from(LessonTask.class)
                .penalize("lessonTask internal conflict",
                        HardSoftScore.ONE_HARD,
                        LessonTask::internalTimslotOverlaps);
    }


/*    private Constraint studentGroupConflict(ConstraintFactory constraintFactory){
        return constraintFactory.from(Lesson.class)
                .join(Lesson.class,
                        Joiners.lessThan(Lesson::getLessonId),
                        Joiners.equal(Lesson::getTimeslot)
                ).filter(((lesson, lesson2) -> {HashSet<StudentGroup> intersection = new HashSet<>(lesson.getStudentGroups());
                    intersection.retainAll(lesson2.getStudentGroups());
                    return ! intersection.isEmpty();}))
                .penalize("Student group conflict", HardSoftScore.ONE_HARD);
    }*/


/*    private Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .join(LessonTask.class,
                        Joiners.lessThan(LessonTask::getTaskNumber),
                        Joiners.lessThanOrEqual(lessontask -> Collections.min(lessontask.getStudentGroups()),
                        lessontask -> Collections.max(lessontask.getStudentGroups())),
                        Joiners.greaterThanOrEqual(lessontask -> Collections.max(lessontask.getStudentGroups()),
                                lessontask -> Collections.min(lessontask.getStudentGroups())))
*//*                        Joiners.lessThanOrEqual(LessonTask::getMinimumPositionOfTimeslots,
                                LessonTask::getMaximumPositionOfTimeslots),
                        Joiners.greaterThanOrEqual(LessonTask::getMaximumPositionOfTimeslots,
                                LessonTask::getMinimumPositionOfTimeslots))*//*
                .filter(((lessonTask, lessonTask2) -> {
                    HashSet<StudentGroup> intersection = new HashSet<>(lessonTask2.getStudentGroups());
                    intersection.retainAll(lessonTask.getStudentGroups());
                    return ! intersection.isEmpty();}))
                .penalize("Student group conflict", HardSoftScore.ONE_HARD, LessonTask::numberOfTimslotOverlaps);
    }*/

/*
       private Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .join(LessonTask.class,
                        Joiners.lessThan(LessonTask::getTaskNumber),
                        Joiners.lessThanOrEqual(lessontask -> Collections.min(lessontask.getStudentGroups()),
                        lessontask -> Collections.max(lessontask.getStudentGroups())),
                        Joiners.greaterThanOrEqual(lessontask -> Collections.max(lessontask.getStudentGroups()),
                                lessontask -> Collections.min(lessontask.getStudentGroups())))
 */
/*                       Joiners.lessThanOrEqual(LessonTask::getMinimumPositionOfTimeslots,
                                LessonTask::getMaximumPositionOfTimeslots),
                        Joiners.greaterThanOrEqual(LessonTask::getMaximumPositionOfTimeslots,
                                LessonTask::getMinimumPositionOfTimeslots))*//*

                .filter(((lessonTask, lessonTask2) -> {
                    HashSet<StudentGroup> intersection = new HashSet<>(lessonTask2.getStudentGroups());
                    intersection.retainAll(lessonTask.getStudentGroups());
                    return ! intersection.isEmpty();}))
                .join(Lesson.class,
                        Joiners.equal((leta1, leta2)-> leta1.getTaskNumber(), Lesson::getTaskNumber))
                .join(Lesson.class,
                        Joiners.equal((leta1, leta2, les)-> leta2.getTaskNumber(), Lesson::getTaskNumber),
                        Joiners.equal((leta1, leta2, les)->les.getTimeslot(), Lesson::getTimeslot))
                .penalize("Student group conflict", HardSoftScore.ONE_HARD);
    }
*/

/*    private Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.lessThan(Lesson::getLessonId)
                ).join(LessonTask.class,
                        Joiners.equal((lesson1, Lesson2) -> lesson1.getTaskNumber(), LessonTask::getTaskNumber)
                ).join(LessonTask.class,
                        Joiners.equal((lesson1, lesson2, task) -> lesson2.getTaskNumber(), LessonTask::getTaskNumber),
                        filtering((les1, les2, lessonTask, lessonTask2) -> {
                    HashSet<StudentGroup> intersection = new HashSet<>(lessonTask2.getStudentGroups());
                    intersection.retainAll(lessonTask.getStudentGroups());
                    return ! intersection.isEmpty();}))
                .penalize("Student group conflict", HardSoftScore.ONE_HARD);
    }*/

/*    private Constraint teacherPreferenceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .join(Preference.class, filtering((lessonTask, preference) ->
                        lessonTask.getTaughtBy().contains(preference.getTeacher())))
                .join(Lesson.class, filtering ((lessonTask, preference, lesson)->
                        lesson.getLessonTask().equals(lessonTask) &&
                        lesson.getTimeslot().equals(preference.getTimeslot())))
                .penalize("Teacher preference conflict", HardSoftScore.ONE_HARD);
    }*/

    private Constraint lastResortTimeslotConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonAssignment.class)
                .filter(LessonAssignment::isOnLastResortTimeslot)
                .penalize("OnLastResortTimeslot conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint lessonTaskOnSameDayConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .join(LessonAssignment.class, Joiners.equal(lessonTask -> lessonTask,
                        LessonAssignment::getLessonTask))
                .groupBy( (lessonTask, lessonAssignment) -> lessonTask, ConstraintCollectors.toList((lessonTask, lessonAssignment)-> lessonAssignment))
                .penalize("exceedMaxLessonsOnSameDay conflict",
                        HardSoftScore.ONE_HARD,
                        LessonTask::exceedMaxLessonsOnSameDayInt);
    }

    private Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonAssignment.class)
                .join(LessonAssignment.class, Joiners.equal(LessonAssignment::getTimeslot),
                        Joiners.equal(LessonAssignment::getRoom),
                        Joiners.lessThan(LessonAssignment::getLessonId))
                .penalize("Room conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonAssignment.class)
                .join(LessonAssignment.class,
                        Joiners.lessThan(LessonAssignment::getLessonId),
                        Joiners.equal(LessonAssignment::getTimeslot))
                .filter(((lessonAssignment, lessonAssignment2) -> {
                    HashSet<Teacher> intersection = new HashSet<>(lessonAssignment2.getTaughtBy());
                    intersection.retainAll(lessonAssignment.getTaughtBy());
                    return ! intersection.isEmpty();}))
                .penalize("Teacher conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint studentGroupConflict(ConstraintFactory constraintFactory){
        return constraintFactory.from(LessonAssignment.class)
                .join(LessonAssignment.class,
                        Joiners.equal(LessonAssignment::getTimeslot),
                        Joiners.lessThan(LessonAssignment::getLessonId)
                ).filter((lessonAssignment, lessonAssignment2) -> {
                    HashSet<StudentGroup> intersection = new HashSet<>(lessonAssignment2.getStudentGroups());
                    intersection.retainAll(lessonAssignment.getStudentGroups());
                    return ! intersection.isEmpty();})
                .penalize("Student group conflict", HardSoftScore.ONE_HARD);
    }

    private Constraint teacherPreferenceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonTask.class)
                .join(Preference.class, filtering((lessonTask, preference) ->
                        lessonTask.getTaughtBy().contains(preference.getTeacher())))
                .join(LessonAssignment.class, filtering ((lessonTask, preference, lessonAssignment)->
                        lessonAssignment.getLessonTask().equals(lessonTask) &&
                                lessonAssignment.getTimeslot().equals(preference.getTimeslot())))
                .penalize("Teacher preference conflict", HardSoftScore.ONE_HARD);
    }

    //*****************************************************************
    //SOFT CONSTRAINTS
    //**********************************************************************

    private Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonAssignment.class)
                .join(LessonAssignment.class,
                        Joiners.lessThan(LessonAssignment::getTaskNumber))
                .filter((lesa1, lesa2) -> {
                    Set<Teacher> intersection = new HashSet<Teacher>(lesa1.getTaughtBy());
                    intersection.retainAll(lesa2.getTaughtBy());
                    return ! intersection.isEmpty() && lesa1.getTimeslot().isConsecutiveTo(lesa2.getTimeslot());
                })
                .reward("Teacher time efficiency", HardSoftScore.ONE_SOFT);
    }

}
