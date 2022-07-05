package org.acme.timetabling.rest;

import io.quarkus.panache.common.Sort;
import org.acme.timetabling.domain.*;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


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
    @GET
    public List<List<Timeslot>> get() {
        List<Timeslot> timeslots = Timeslot.listAll(Sort.by("position"));
        List<List<Timeslot>> timetable = new ArrayList<>(7);
        for (int i= 0; i <7; i++ ){
            timetable.add(new ArrayList<>(12));
        }
        for (Timeslot timeslot: timeslots) {
            timetable.get(timeslot.getDayOfWeek().getValue()-1).add(timeslot);
        }
        return timetable;
    }

    @POST
    public Response add(Timeslot timeslot) {
        Timeslot.persist(timeslot);
        updatePositions();
        DefaultSettings ds = DefaultSettings.findById(1L);
        ds.addSlotCount(timeslot.getDayOfWeek().getValue(), 1);
        updateAllTeachersDepFromPref(ds);
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

        //UPDATE DEFAULTSETTINGS
        updatePositions();
        DefaultSettings ds = DefaultSettings.findById(1L);
        ds.addSlotCount(timeslot.getDayOfWeek().getValue(), -1);
        //+ UPDATE TEACHERS DEPENDINGS --> ALREADY DONE AFTER DELETING PREFERENCE


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

    public void updateAllTeachersDepFromPref(DefaultSettings ds){
        List<Teacher> teachers = Teacher.listAll();
        teachers.forEach(teacher -> teacher.updateDependsFromPrefs(ds));
    }

    public static Long numberOfTimeslots(){
        return Timeslot.count();
    }
}

