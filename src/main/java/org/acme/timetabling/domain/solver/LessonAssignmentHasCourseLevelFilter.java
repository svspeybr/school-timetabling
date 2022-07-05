package org.acme.timetabling.domain.solver;

import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;

public class LessonAssignmentHasCourseLevelFilter implements SelectionFilter<TimeTable, LessonAssignment> {
    @Override
    public boolean accept(ScoreDirector<TimeTable> scoreDirector, LessonAssignment lessonAssignment){
        return  lessonAssignment.getLessonTask().getCourseLevel() != null;
    }
}
