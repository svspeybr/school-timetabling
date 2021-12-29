package org.acme.timetabling.rest;

import org.acme.timetabling.domain.*;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/themeCollections")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class ThemeCollectionResource {

    @POST
    @Path("add/{themeId}/leta/{lessonTasks}/multiplicity/{lessonMultiplicity}/timeslots/{timeslots}/tsmul/{timeslotMultiplicities}")
    public Response addNewTheme(@PathParam("themeId") String themeId,
                                @PathParam("lessonTasks") String tasks,
                                @PathParam("lessonMultiplicity") Integer lessonMultiplicity,
                                @PathParam("timeslots")  String timeslots,
                                @PathParam("timeslotMultiplicities") String timeslotMultiplicities) {
        System.out.println("HEY");

        //THEME NAME
        if (ThemeCollection.findById(themeId) != null){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        //THEME LESSONTASKS + LESSONMULTIPLICITY
        Set<Integer> taskIds = convertor(tasks, '-').stream().map(Integer::parseInt).collect(Collectors.toSet());
        System.out.println(taskIds);
        Stream<LessonTask> lessonTaskStream = LessonTask.streamAll();
        List<LessonTask> lessonTasks = lessonTaskStream
                .filter(task -> taskIds.contains(task.getTaskNumber()))
                .collect(Collectors.toList());

        int minMultiplicity = lessonTasks.stream().map(LessonTask::getMultiplicity).min(Integer::compareTo).get();
        //Each lessontask needs to have  a number of 'lessonMultiplicity' lessons for the theme.
        //REJECT theme if more lessons are demanded than the lessonTasks can provide.
        if (minMultiplicity < lessonMultiplicity){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        //THEME TIMESLOTS
        Set<Integer> tsIds = convertor(timeslots, '-').stream().map(Integer::parseInt).collect(Collectors.toSet());
        Stream<Timeslot> timeslotStream = Timeslot.streamAll();
        List<Timeslot> timeslotList = timeslotStream
                .filter(ts -> tsIds.contains( Integer.parseInt(ts.getTimeslotId().toString())))
                .collect(Collectors.toList());


        //Timeslot Multiplicities
        List<Integer> multiplicities = convertor(timeslotMultiplicities, '-').stream().map(Integer::parseInt).collect(Collectors.toList());


        //CREATE NEW THEME
        ThemeCollection theme = new ThemeCollection(themeId);
        for (LessonTask lessonTask:lessonTasks){
            List<Lesson> lessons = new ArrayList<>(lessonTask.getLessonsOfTaskList());
            for (int i =0; i < lessonMultiplicity; i++){
                theme.addLesson(lessons.get(i).getLessonId());
            }
        }
        for (int k= 0; k < timeslotList.size(); k++){
            theme.addMultiplicityForTimeslot(timeslotList.get(k), multiplicities.get(k));
        }
        theme.persist();

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/delete/{themeId}")
    public Response delete(@PathParam("themeId") String themeId) {
        ThemeCollection.delete("THEME_ID", themeId);
        return Response.status(Response.Status.OK).build();
    }

    //**************************************************************************************************
    // Convertor for pathparameters --> TO DO: REMOVE DUPLICATE IN LESSONTASKRESOURCE
    //**************************************************************************************************

    //Extract all teacherNames/studentGroupnames send by pathparameter in One sting: encoded
    // Each name is separated by a separationValue in app.js --> '-'
    private List<String> convertor(String encoded, char separationValue) {
        String newName ="";
        List<String> setOfNames = new ArrayList<>();
        char[] charsOfNames = encoded.toCharArray();
        for (char ch: charsOfNames){
            if (ch != separationValue) {
                newName += Character.toString(ch);
            }
            else{
                setOfNames.add(newName);
                newName ="";
            }
        }
        if (newName.length() != 0) {
            setOfNames.add(newName);
        }
        return setOfNames;
    }
}
