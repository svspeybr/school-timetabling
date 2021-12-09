package org.acme.timetabling.rest;

import org.acme.timetabling.domain.CourseLevel;
import org.acme.timetabling.domain.Lesson;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/courseLevel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class CourseLevelResource {

    @GET
    public List<CourseLevel> get() {
        return CourseLevel.listAll();
    }

}
