package org.acme.timetabling.domain.solver;

import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;

public class IgnoreMoveFilter  implements SelectionFilter<TimeTable, ChangeMove> {

        @Override
        public boolean accept(ScoreDirector<TimeTable> scoreDirector, ChangeMove changeMove) {
            for (Object o: changeMove.getPlanningEntities()){
                if (o instanceof Integer){
                    return false;
                }
            }
            return true;
        }
}
