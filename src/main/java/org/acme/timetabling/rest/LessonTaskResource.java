package org.acme.timetabling.rest;

import org.acme.timetabling.domain.Lesson;
import org.acme.timetabling.domain.LessonTask;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/lessonTasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class LessonTaskResource {

    @GET
    @Path("/{taskNumber}")
    public LessonTask get(@PathParam("taskNumber") Integer taskNumber) {
        LessonTask lessonTask = LessonTask.findById(taskNumber);
        if (lessonTask == null){
            throw new NotFoundException();
        }
        return lessonTask;
    }

    @GET
    public List<LessonTask> get() {
        return LessonTask.listAll();
    }

    @POST
    public Response add(LessonTask lessonTask) {
        if (LessonTask.findById(lessonTask.getTaskNumber()) != null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LessonTask.persist(lessonTask);
        return Response.accepted(lessonTask).build();
    }


    /*TO DO -DELETING LESSONS!!!!*/
    @DELETE
    @Path("/{taskNumber}")
    public Response delete (@PathParam("taskNumber") Integer taskNumber){
        LessonTask lessonTask = LessonTask.findById(taskNumber);
        if (lessonTask == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        lessonTask.delete();
        return Response.status(Response.Status.OK).build();
    }
}


