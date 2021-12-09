package org.acme.timetabling.domain.solver;

import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.core.api.score.director.ScoreDirector;

public class TimeTablingMoveHelper {

    public static void movePartitionNumber(ScoreDirector<TimeTable> scoreDirector, LessonAssignment lessonAssignment,
                                           Integer toPartitionNumber) {
        scoreDirector.beforeVariableChanged(lessonAssignment, "partitionNumber");
        lessonAssignment.setPartitionNumber(toPartitionNumber);
        scoreDirector.afterVariableChanged(lessonAssignment, "partitionNumber");
    }

    private TimeTablingMoveHelper() {
    }
}
