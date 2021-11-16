package org.acme.timetabling.rest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import io.quarkus.panache.common.Sort;
import org.acme.timetabling.domain.*;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;

@Path("/timeTable")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TimeTableResource {

    private static final Long SINGLETON_TIME_TABLE_ID = 1L;

    @Inject
    SolverManager<TimeTable, Long> solverManager;

    @Inject
    ScoreManager<TimeTable, HardSoftScore> scoreManager;

    // To try, open http://localhost:8080/timeTable
    @GET
    public TimeTable getTimeTable() {
        // Get the solver status before loading the solution
        SolverStatus solverStatus =solverManager.getSolverStatus(SINGLETON_TIME_TABLE_ID);

        TimeTable timeTable = findById(SINGLETON_TIME_TABLE_ID);
        scoreManager.updateScore(timeTable);
        // to avoid the race condition that the solver terminates between them
        timeTable.setSolverStatus(solverStatus);
       return timeTable;
    }

    @POST
    @Path("/solve")
    public void solve() {
        solverManager.solveAndListen(SINGLETON_TIME_TABLE_ID,
                this::findById,
                this::save);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_TIME_TABLE_ID);
    }

    @Transactional
    protected TimeTable findById(Long id) {
        return new TimeTable(
                Timeslot.listAll(),
                Room.listAll(),
                Lesson.listAll(),
                LessonTask.listAll(),
                StudentGroup.listAll(),
                Teacher.listAll(),
                //Necessary -> Optaplanner-uses it for constraints
                Preference.listAll());
    }

    @Transactional
    protected void save(TimeTable timeTable) {
        for (Lesson lesson: timeTable.getLessonList()) {
            Lesson attachedLesson = Lesson.findById(lesson.getLessonId());
            attachedLesson.setTimeslot(lesson.getTimeslot());
            attachedLesson.setRoom(lesson.getRoom());
        }
    }
}
