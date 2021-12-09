package org.acme.timetabling.domain.solver;


import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;

import java.util.*;

public class LessonAssignmentPillarChangeMove extends AbstractMove<TimeTable> {

    private List<LessonAssignment> lessonAssignments;
    private Integer fromPartitionNumber;
    private Integer toPartitionNumber;

    public LessonAssignmentPillarChangeMove(Integer fromPartitionNumber, List<LessonAssignment> lessonAssignments, Integer toPartitionNumber) {
        this.lessonAssignments = lessonAssignments;
        this.fromPartitionNumber = fromPartitionNumber;
        this.toPartitionNumber = toPartitionNumber;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<TimeTable> scoreDirector) {
        return !Objects.equals(fromPartitionNumber, toPartitionNumber);
    }

    @Override
    public LessonAssignmentPillarChangeMove createUndoMove(ScoreDirector<TimeTable> scoreDirector) {
        return new LessonAssignmentPillarChangeMove(toPartitionNumber, lessonAssignments, fromPartitionNumber);
    }



    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<TimeTable> scoreDirector) {
        for (LessonAssignment lessonAssignment: lessonAssignments) {

            //does not matter
/*            if (!lessonAssignment.getPartitionNumber().equals(fromPartitionNumber)) {
                throw new IllegalStateException("The lessonAssignment (" + lessonAssignment + ") should have the same partitionNumber ("
                        + lessonAssignment.getPartitionNumber() + ") as the fromPartitionNumber (" + fromPartitionNumber + ").");
            }*/
            TimeTablingMoveHelper.movePartitionNumber(scoreDirector, lessonAssignment, toPartitionNumber);
        }
    }


    @Override
    public LessonAssignmentPillarChangeMove rebase(ScoreDirector<TimeTable> destinationScoreDirector) {
        return new LessonAssignmentPillarChangeMove(destinationScoreDirector.lookUpWorkingObject(fromPartitionNumber),
                rebaseList(lessonAssignments, destinationScoreDirector),
                destinationScoreDirector.lookUpWorkingObject(toPartitionNumber));
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(lessonAssignments);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Arrays.asList(fromPartitionNumber, toPartitionNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LessonAssignmentPillarChangeMove other = (LessonAssignmentPillarChangeMove) o;
        return Objects.equals(fromPartitionNumber, other.fromPartitionNumber) &&
                Objects.equals(lessonAssignments, other.lessonAssignments) &&
                Objects.equals(toPartitionNumber, other.toPartitionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromPartitionNumber, lessonAssignments, toPartitionNumber);
    }

    @Override
    public String toString() {
        return lessonAssignments + " {? -> " + toPartitionNumber+ "}";
    }
}
