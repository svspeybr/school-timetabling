package org.acme.timetabling.rest;

import org.acme.timetabling.domain.StudentGroup;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/studentGroups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class StudentGroupResource {
    @GET
    public List<StudentGroup> get() {
        return StudentGroup.listAll();
    }

    @GET
    @Path("/{name}")
    public StudentGroup get(@PathParam("name") String name) {
        StudentGroup studentGroup = StudentGroup.findById(name);
        if (studentGroup == null) {
            StudentGroup newStudentGroup = new StudentGroup(name);
            add(newStudentGroup);
            return newStudentGroup;
        }
        return studentGroup;
    }

    @POST
    public Response add(StudentGroup studentGroup) {
        StudentGroup.persist(studentGroup);
        return Response.accepted(studentGroup).build();

    }

    @POST
    @Path("/add/{studentGroupName}")
    public Response addByName(@PathParam("studentGroupName") String name) {
        StudentGroup studentGroup = StudentGroup.findById(name);
        if (studentGroup == null) {
            StudentGroup newStudentGroup = new StudentGroup(name);
            StudentGroup.persist(newStudentGroup);
            return Response.accepted(newStudentGroup).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE
    @Path("{name}")
    public Response delete(@PathParam("name") String name) {
        StudentGroup studentGroup = StudentGroup.findById(name);
        if (studentGroup == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        studentGroup.delete();
        return Response.status(Response.Status.OK).build();
    }
}
