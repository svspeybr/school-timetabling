package org.acme.timetabling.domain.solver;

import org.acme.timetabling.domain.CourseLevel;
import org.acme.timetabling.domain.LessonAssignment;
import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.core.impl.heuristic.move.CompositeMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LessonAssignmentPillarChangeMoveFactory implements MoveListFactory<TimeTable> {


    // TO DO PINNING!!!!!!!

        public List<Move<TimeTable>> createMoveList(TimeTable timeTable) {
            List<Move<TimeTable>> moveList = new ArrayList<>();
            List<CourseLevel> courseLevelList = timeTable.getCourseLevelList();
            Map<CourseLevel, List<LessonAssignment>> courseLevelToAssignmentMap = new HashMap<>(courseLevelList.size());
            for (CourseLevel courseLevel: courseLevelList){
                courseLevelToAssignmentMap.put(courseLevel, new ArrayList<>());
            }
            for (LessonAssignment lessonAssignment: timeTable.getLessonAssignmentList()){
                CourseLevel courseLevel = lessonAssignment.getLessonTask().getCourseLevel();
                if(courseLevel != null){
                    courseLevelToAssignmentMap.get(courseLevel).add(lessonAssignment);
                }
            }
            for (CourseLevel courseLevel: courseLevelList){
                List<LessonAssignment> pillarOfAssignments = courseLevelToAssignmentMap.get(courseLevel);
                List<LessonAssignmentPillarChangeMove> possibleMovesForPillar = new ArrayList<>();
                int fromPartitionNumber = pillarOfAssignments.get(0).getPartitionNumber();
                int partitionTableSize = courseLevel.getPartitionSize();
                for (int i = 0; i < partitionTableSize; i++){
                    possibleMovesForPillar.add(new LessonAssignmentPillarChangeMove(
                            fromPartitionNumber, //from
                            pillarOfAssignments,
                            i)); //to
                }
                moveList.add(CompositeMove.buildMove(possibleMovesForPillar));
            }
            return moveList;
        }



}
