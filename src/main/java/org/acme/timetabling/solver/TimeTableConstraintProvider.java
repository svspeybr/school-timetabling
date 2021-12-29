package org.acme.timetabling.solver;

import org.acme.timetabling.domain.*;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

import static org.optaplanner.core.api.score.stream.Joiners.filtering;

import java.util.*;

//ConstraintFactory.from(...clas)-> gets lists from the planning problem --> planning entities and problem facts

public class TimeTableConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        //TO DO: PROVIDE SETTING CLASS WITH SUBCLASSES:
        // 1) DEFAULT SETTINGS
        // 2) SEPARATION SETTINGS TO PASS TO CONSTRAINT
        //REMARK: THE SETTING ARE FETCHED DURING GENERATING TIMETABLE AND
        // + DEFAULT SETTING ONLY CHANGE WHEN CALLED FROM (TO DO) DEFAULT SETTING RESOURCE
        // + SEPARATION SETTING ONLY WHEN 'PLANNING FACTS (timeslots/days/teacher(preferences))' ARE ADDED.
        //TO DO: PASS THOSE SETTINGS TO CONSTRAINT GENERATING FUNCTIONS:
        return new Constraint[]{
               roomConflict(constraintFactory),
                missesHalfDays(constraintFactory, 2 * 5 - 1, 5 ),
/*                 lessonTaskInternalConflict(constraintFactory),*/
                themeConflict(constraintFactory),
                classTeacherConflict(constraintFactory),
                scienceCourseConflict(constraintFactory),
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                teacherTimeEfficiency(constraintFactory),
               lessonTaskOnSameDayConflict(constraintFactory),
                 teacherPreferenceConflict(constraintFactory),
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

    private Constraint themeConflict(ConstraintFactory constraintFactory){
        return constraintFactory.from(LessonAssignment.class)
                .join(ThemeCollection.class, filtering((lesa, theme)-> theme.getLessonIds().contains(lesa.getLessonId())))
                .groupBy((lesa,theme)-> lesa.getTimeslot(), (lesa, theme)->theme, ConstraintCollectors.countDistinct((lesa, theme)->lesa))
                .penalize("themeConflict", HardSoftScore.ONE_HARD, (ts, theme, count) -> positive(theme.copy(count) - theme.getMultiplicity(ts)));
    }


    private Constraint classTeacherConflict(ConstraintFactory constraintFactory){
        return constraintFactory.from(StudentGroup.class)
                .join(Teacher.class, filtering((st,te ) -> st.getClassTeachers().contains(te)))
                .join(LessonAssignment.class, filtering((st, te, lesa)-> lesa.getDayOfWeek().equals("FRIDAY")
                                                                            && lesa.getTaughtBy().contains(te)
                                                                            && lesa.getStudentGroups().contains(st)
                                                                            ))
                .groupBy(ConstraintCollectors.countDistinct((studentGroup, teacher,lessonAssignment ) -> studentGroup))
                .join(constraintFactory.from(StudentGroup.class).groupBy(ConstraintCollectors.count()))
                .penalize("classTeacher on Friday",
                        HardSoftScore.ONE_HARD, ((integer, integer2) -> integer2 - integer));
    }

    private Constraint scienceCourseConflict(ConstraintFactory constraintFactory){
        return constraintFactory.from(LessonAssignment.class)
                .join(SubjectCollection.class, filtering((lesa, subj)-> subj.getSubjects().contains(lesa.getSubject())))
                .groupBy((lesa, subj) -> subj,(lesa, subj)-> lesa.getTimeslot(), ConstraintCollectors.toList((lesa, subj)-> lesa))
                .penalize("scienceCourseConflict", HardSoftScore.ONE_HARD, (subj, ts, list)-> {
                if (list.size() > subj.getMaxAssignmentsOnSameSlot()){
                    return 10;
                }
                    return 0;
                });

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
                        (lessonTask, lessonAssignments) -> {
                            Integer exceedNumber = 0;
                            // To be taken from teacher/preferences
                            int allowedNumberOfDays = extractAllowedNumberOfDays(lessonTask);
                            List<List<Integer>> numberOfLessons = getTimeslotsPerDay(lessonAssignments);
                            int dayIndex = 0;
                            int lessonsLeft = lessonTask.getMultiplicity();
                            List<Integer> timeslotIndexOnDay;
                            for (Integer blockSize : lessonTask.getCouplingNumbers()) {
                                timeslotIndexOnDay = numberOfLessons.get(dayIndex);
                                exceedNumber += 6 * Math.abs(blockSize - timeslotIndexOnDay.size());
                                lessonsLeft -= blockSize;
                                exceedNumber += numberOfGaps(timeslotIndexOnDay);
                                dayIndex += 1;
                            }
                            // TO DO: CHECK WHEN 5 (= allowedNumberOfdays) BLOCKS ARE CHOSEN, NO LESSONS ARE LEFT
                            // ONE BLOCK A DAY
                            if (lessonsLeft > 0) {
                                int daysLeftToDivide = (allowedNumberOfDays - lessonTask.getCouplingNumbers().size());
                                int remainderOfDays = lessonsLeft % daysLeftToDivide;

                                Integer mean = (int) Math.floor((float) lessonsLeft / daysLeftToDivide);

                                for (int i = dayIndex; i < 5; i++) {

                                    exceedNumber += Math.abs( numberOfLessons.get(i).size() - mean - sign(remainderOfDays));
                                    remainderOfDays -= 1;
                                }
                            }
                            return Math.max(exceedNumber, 0);
                        });
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


    private Constraint missesHalfDays(ConstraintFactory constraintFactory, int maxNumbHalfDays, int maxDays) {
        return constraintFactory.from(LessonAssignment.class)
                .join(Teacher.class, filtering((lesa, teach)-> lesa.getTaughtBy().contains(teach)))
                .groupBy((lesa, teach) -> teach, ConstraintCollectors.toSet((lesa, teach)-> lesa.getHalfDay()))
                .penalize("Missing half Days", HardSoftScore.ONE_HARD, (teach, setHalfDays) -> {
                    int penalizeScore = 0;
                    Set<String> halfDaysTotalOccupied = new HashSet<>(maxNumbHalfDays);
                    Set<String> daysOccupied = new HashSet<>(7);
                    for (String halfDay: setHalfDays){
                        if (halfDay.length() == 2){ // half a day without number index --> both halfdays are occupied
                            halfDaysTotalOccupied.add(halfDay + "0");
                            halfDaysTotalOccupied.add(halfDay + "1");
                        } else {
                            halfDaysTotalOccupied.add(halfDay);
                        }
                        daysOccupied.add(halfDay.substring(0, 2));
                    }
                    // IF TOO FEW HALFDAYS
                    if (maxNumbHalfDays - halfDaysTotalOccupied.size() < (teach.rightToHalfDays() - teach.getFirstOrLastHours() )){
                       penalizeScore += 10;
                    }

                    //IF TEACHER HAS RIGHT TO >=4 HALFDAYS --> RIGHT TO FULL DAY
                    if (maxDays - daysOccupied.size() <= 0 && teach.rightToHalfDays() > 3){
                        penalizeScore += 10;
                    }
                    return penalizeScore;
                });
    }

    private Constraint teacherPreferenceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.from(LessonAssignment.class)
                .join(Preference.class,
                        Joiners.equal(LessonAssignment::getTimeslot, Preference::getTimeslot),
                        filtering((lessonAssignment, preference)->lessonAssignment.getTaughtBy().contains(preference.getTeacher())))
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


    //**********************************************
    // HELP FUNCTIONS
    //**********************************************

    //SEPARATION CONDITIONS

    // DEFAULT SETTING MAXTEACHING DAYS
    private List<List<Integer>> getTimeslotsPerDay(List<LessonAssignment> lesList) {
        List<List<Integer>> numberOfLessons = new ArrayList<>(5);
        for (int i =0; i< 5; i++){
            //Max 8 -> 10 lessonslosts? on same day
            numberOfLessons.add(new ArrayList<>(10));
        }
        for (LessonAssignment lessonAssignment: lesList) {
            Timeslot timeslot = lessonAssignment.getTimeslot();
            if (! (timeslot == null)) {
                int index = timeslot.getDayOfWeek().getValue() - 1;
                numberOfLessons.get(index).add(timeslot.getPosition());
            }
        }

        numberOfLessons.sort(Comparator.comparing(List<Integer>::size).reversed());
        return numberOfLessons;
    }

    private int numberOfGaps(List<Integer> tsIndexList) {
        int length = tsIndexList.size();
        if (length <= 1){
            return 0;
        }
        Collections.sort(tsIndexList);
        return tsIndexList.get(length -1)- tsIndexList.get(0) + 1 - length;
    }

    private static int sign(int numb){
        if (numb > 0) {
            return 1;
        }
        return 0;
    }

    private static int positive(int numb){
        if (numb > 0) {
            return numb;
        }
        return 0;
    }

    private int extractAllowedNumberOfDays(LessonTask lessonTask){
        Set<Teacher> teachers = lessonTask.getTaughtBy();
        int allowedNumberOfDays = 7;
        int temp;
        for (Teacher teacher: teachers){
            temp = teacher.getNumberOfTeachingDays();
            if (temp < 0){
                temp = -temp;
                if (teacher.rightToHalfDays() > 3){
                    temp -= 1;
                }
            }
            if (temp < allowedNumberOfDays){
                allowedNumberOfDays = temp;
            }
        }

        return allowedNumberOfDays;
    }

}
