package org.acme.timetabling.rest;

import io.quarkus.panache.common.Sort;
import org.acme.timetabling.domain.Lesson;
import org.acme.timetabling.domain.LessonTask;
import org.acme.timetabling.domain.Timeslot;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Path("/timeslots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class TimeslotResource {

    /*GET Database - Possible Not found error!!!  */
/*    @GET
    @Path("/{timeslotId}")
    public Timeslot get(@PathParam("timeslotId") Long timeslotId) {
        return Timeslot.findById(timeslotId);
    }*/
    /* Add new timeslot to database */

    @POST
    public Response add(Timeslot timeslot) {
        Timeslot.persist(timeslot);
        updatePositions();
        return Response.accepted(timeslot).build();
    }
    @DELETE
    @Path("{timeslotId}")
    public Response delete(@PathParam("timeslotId") Long timeslotId) {
        Timeslot timeslot = Timeslot.findById(timeslotId);
        if (timeslot == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        timeslot.delete();
        updatePositions();
        return Response.status(Response.Status.OK).build();
    }
    @GET
    @Path("/{timeslotId}")
    // Possible not found error!!!!
    public Timeslot get(@PathParam("timeslotId") Long timeslotId) {
        Timeslot timeslot = Timeslot.findById(timeslotId);
        if (timeslot == null) {
            throw new NotFoundException();
        }
        return timeslot;
    }

    @POST
    @Path("changeLastResort/{timeId}")
    public Response changeCoupling(@PathParam("timeId") Long timeslotId) {
        Timeslot timeslot = Timeslot.findById(timeslotId);
        if (timeslot == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        timeslot.changeLastResort();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("updatePositions")
    public static Response updatePositions(){
        List<Timeslot> timeslotList = Timeslot.listAll(Sort.by("dayOfWeek").and("startTime").and("endTime"));
        Integer order =0;
        for (Timeslot timeslot: timeslotList) {
            order++;
            timeslot.setPosition(order);
        }
        return Response.status(Response.Status.OK).build();
    }

    public static Long numberOfTimeslots(){
        return Timeslot.count();
    }
}

