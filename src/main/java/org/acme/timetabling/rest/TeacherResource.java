package org.acme.timetabling.rest;

import org.acme.timetabling.domain.DefaultSettings;
import org.acme.timetabling.domain.Teacher;
import org.acme.timetabling.domain.Timeslot;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/teachers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class TeacherResource {

    @GET
    public List<Teacher> get() {
        return Teacher.listAll();
    }

    @GET
    @Path("/{acronym}")
    public Teacher get(@PathParam("acronym") String acronym) {
        List<Teacher> teacherList = Teacher.findByAcronym(acronym);
        if (teacherList.isEmpty()) { //necessary????
            DefaultSettings ds = DefaultSettings.findById(1L);
            Teacher newTeacher = new Teacher(acronym);
            add(newTeacher);
            newTeacher.updateDependsFromLeTa(ds);
            newTeacher.setFirstOrLastHours(0);
            return newTeacher;
        }
        return teacherList.get(0);
    }

    @GET
    @Path("rightToHalfDays/{acronym}")
    public Integer getHalfDays(@PathParam("acronym") String acronym) {
        Teacher teacher = Teacher.findById(acronym);
        return teacher.rightToHalfDays();
    }

    @POST
    public Response add(Teacher teacher) {
        Teacher.persist(teacher);
        return Response.accepted(teacher).build();
    }

/*    @POST
    @Path("/{acronym}")
    public Response update(
            @PathParam("acronym") String acronym,
            Timeslot timeslot) {
        Teacher teacher = Teacher.findById(acronym);
        if (teacher == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        teacher.addPreference(timeslot);
        return Response.status(Response.Status.OK).build();
    }*/

    @DELETE
    @Path("{acronym}")
    public Response delete(@PathParam("acronym") String acronym) {
        Teacher teacher = Teacher.findById(acronym);
        if (teacher == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        teacher.delete();
        return Response.status(Response.Status.OK).build();
    }
}
