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
import org.acme.timetabling.rest.repository.LessonRepository;
import org.acme.timetabling.rest.repository.LessonTaskRepository;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Path("/timeTable")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TimeTableResource {

    private static final Long SINGLETON_TIME_TABLE_ID = 1L;
    private static String FILENAME = "/home/svs/IdeaProjects/school-timetabling/src/main/java/org/acme/timetabling/solver/timetablingSolverConfig.xml";

    @Inject
    LessonRepository lessonRepository;

    /*@Inject
    SolverManager<TimeTable, Long> solverManager;*/
    File configSolverFile = new File(FILENAME);
    SolverConfig solverConfig = SolverConfig.createFromXmlFile(configSolverFile);
    SolverManager<TimeTable, Long> solverManager = SolverManager.create(solverConfig, new SolverManagerConfig());

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
                Timeslot.listAll(Sort.by("position")),
                Room.listAll(),
                Lesson.listAll(),
                LessonTask.listAll(),
                StudentGroup.listAll(),
                Teacher.listAll(),
                Preference.listAll());
    }

    @Transactional
    protected void save(TimeTable timeTable) {
        lessonRepository.deleteAllGroups();
        Set<String> values = new HashSet<>();
        for (LessonAssignment lessonAssignment: timeTable.getLessonAssignmentList()) {
            Lesson attachedLesson = Lesson.findById(lessonAssignment.getLessonId());
            values.add("("+ lessonAssignment.getTaskNumber().toString()+ ", '" + lessonAssignment.getStudentGroup().getGroupName()+"')");
            attachedLesson.setTimeslot(lessonAssignment.getTimeslot());
            attachedLesson.setRoom(lessonAssignment.getRoom());
        }
        lessonRepository.addGroups(values);
    }


}