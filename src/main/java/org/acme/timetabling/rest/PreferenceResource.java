package org.acme.timetabling.rest;


import org.acme.timetabling.domain.*;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Path("/preferences")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class PreferenceResource {

    @GET
    public List<Preference> get() {
            return Preference.listAll();
        }

    @GET
    @Path("{teacherAcronym}")
    public List<Preference> getByTeacher(@PathParam("teacherAcronym") String teacherAcronym) {
        return Preference.list("teacher.acronym", teacherAcronym);
    }

    @POST
    public Response add(Preference preference) {
            Preference.persist(preference);
            return Response.accepted(preference).build();
        }


    @POST
    @Path("byTimeslot/{timeslotId}/teacher/{teacherAcronym}")
    public Response add(@PathParam("timeslotId") Long timeslotId,
                           @PathParam("teacherAcronym") String acronym) {
        DefaultSettings ds = DefaultSettings.findById(1L);

        Timeslot timeslot = Timeslot.findById(timeslotId);
        Teacher teacher = Teacher.findById(acronym);
        Preference.persist(new Preference(teacher, timeslot)); // teacher's preferenceList is increased
        teacher.updateDependsFromPrefs(ds); //Determines if first or last hours are filled according to preferencList
        return Response.status(Response.Status.OK).build();
    }


    @DELETE
    @Path("byTimeslot/{timeslotId}/teacher/{teacherAcronym}")
    public Response delete(@PathParam("timeslotId") Long timeslotId,
                           @PathParam("teacherAcronym") String acronym) {
        Preference preference = Preference.find("timeslot.timeslotId = ?1 AND teacher.acronym = ?2", timeslotId, acronym).firstResult();
        if (preference == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        delAfterAdjustTeacherSettings(Collections.singletonList(preference));

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("onlyByTimeslot/{timeslotId}")
    public Response deleteByTimeslot(@PathParam("timeslotId") Long timeslotId) {

        List<Preference> preferences = Preference.list("TIMESLOT_TIMESLOTID", timeslotId);
        delAfterAdjustTeacherSettings(preferences);

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("{preferenceId}")
    public Response delete(@PathParam("preferenceId") Long preferenceId) {
            Preference preference = Preference.findById(preferenceId);
            if (preference == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            delAfterAdjustTeacherSettings(Collections.singletonList(preference));
            return Response.status(Response.Status.OK).build();
    }

    private void delAfterAdjustTeacherSettings(List<Preference> preferences){
        DefaultSettings ds = DefaultSettings.findById(1L);

        for (Preference preference: preferences){
            preference.getTeacher().removePreference(preference);
            preference.getTeacher().updateDependsFromPrefs(ds);
            preference.delete();
        }

    }
}
