package org.acme.timetabling.domain.solver;

import org.acme.timetabling.domain.Lesson;
import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;

public class DifferentLessonSwapMoveFilter implements SelectionFilter<TimeTable, SwapMove>{

    @Override
    public boolean accept(ScoreDirector<TimeTable> scoreDirector, SwapMove swapMove) {
        LessonAssignment leftLesson = (LessonAssignment) swapMove.getLeftEntity();
        LessonAssignment rightLesson = (LessonAssignment) swapMove.getRightEntity();
        return ! leftLesson.getTaskNumber().equals(rightLesson.getTaskNumber());
    }


}
