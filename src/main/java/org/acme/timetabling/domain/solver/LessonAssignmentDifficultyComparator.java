package org.acme.timetabling.domain.solver;

import org.acme.timetabling.domain.LessonAssignment;

import java.util.Comparator;

public class LessonAssignmentDifficultyComparator implements Comparator<LessonAssignment> {
    private static final Comparator<LessonAssignment> COMPARATOR = Comparator.comparingInt(lessonAssignment -> lessonAssignment.getStudentGroups().size());

    @Override
    public int compare(LessonAssignment a, LessonAssignment b){
        return COMPARATOR.compare(a,b);
    }
}
