package org.acme.timetabling.rest;

import org.acme.timetabling.domain.*;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/lessons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class LessonResource {

    // ADD ONE LESSON TO THE DATABASE
    // ~ 'coppyLessons'
    @POST
    public Response add(Lesson lesson) {
        Lesson.persist(lesson);
        return Response.accepted(lesson).build();
    }

    //RETURN LESSON FROM DATABASE @lessonId
    @GET
    @Path("/{lessonId}")
    // Possible not found execption!!!!
    public Lesson get(@PathParam("lessonId") Long lessonId) {
        Lesson lesson = Lesson.findById(lessonId);
        if (lesson == null) {
            throw new NotFoundException();
        }
        return lesson;
    }

    @GET
    public List<Lesson> get() {
        return Lesson.listAll();
    }

/*
    //POST MULTIPLE LESSONS + CORRESPONDING LESSONTASK
    @POST
    @Path("add/{multiplicity}/{taskNumber}/{studentGroups}/{taughtBy}")
    public Response copyLessons(@PathParam("multiplicity") Integer multiplicity,
                                @PathParam("taskNumber") Integer taskNumber,
                                @PathParam("studentGroups")String studentGroupString,
                                @PathParam("taughtBy")  String teacherString,
                                Lesson lesson) {
        //CONVERT PATHPARAM: studentGroupString & teacherString to separate values
        //The values are separated by 'separationValue'
        List<String> teachers = convertor(teacherString, '-');
        List<String> groupNames = convertor(studentGroupString, '-');

        //GET LESSONTASK
        LessonTask oldLessonTask = LessonTask.findById(taskNumber);
        if (oldLessonTask != null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LessonTask lessonTask = new LessonTask(taskNumber);
        //GET STUDENTGROUPS
        Stream<StudentGroup> studentGroups = StudentGroup.streamAll();
        List<StudentGroup> studentGroupList = studentGroups
                .filter(sg -> groupNames.contains(sg.getGroupName()))
                .collect(Collectors.toList());

        //GET TEACHERS
        Stream<Teacher> teacherNames = Teacher.streamAll();
        List<Teacher> teList = teacherNames
                .filter(te ->  teachers.contains(te.getAcronym()))
                .collect(Collectors.toList());

        //POST LESSONTASK -->ALREADY POSTED: STUDENTGROUPS/TEACHERS
        LessonTask.persist(lessonTask);
        for (int i = 0; i < multiplicity; i++) {
            //GET LESSON
            Lesson newLesson =new Lesson(lesson.getSubject());
            //UPDATE LESSONTASK -->  FOR LESSON AND VICA VERSA!!!!
            newLesson.setLessonTask(lessonTask);
            //UPDATE STUDENTGROUPS
            newLesson.addStudentGroups(studentGroupList);
            //UPDATE TEACHERS
            newLesson.addTeachers(teList);
            //POST UPDATED LESSON
            add(newLesson);

        }
        return Response.status(Response.Status.OK).build();

    }*/

    //LOCKING ONE LESSON -TO DO: MULTIPLE LESSONS!
    @POST
    @Path("changePin/{lesId}")
    public Response changePin(@PathParam("lesId") Long lessonId) {
        Lesson lesson = Lesson.findById(lessonId);
        if (lesson == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        lesson.setPinned(! lesson.isPinned());
        return Response.status(Response.Status.OK).build();

    }

   /* //COUPLING TWO LESSONS -TO DO: MULTIPLE LESSONS???
    @POST
    @Path("changeCoupling/{leId}")
    public Response changeCoupling(@PathParam("leId") Long lessonId) {
        Lesson lesson = Lesson.findById(lessonId);
        if (lesson == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        LessonTask lessonTask = lesson.getLessonTask();

        lesson.changeCoupled();
        //When two lessons are set to coupled --> the user (determined by html) can only decouple
        if (lessonTask.isCoupled() || lessonTask
                .getLessonsOfTaskList()
                .stream()
                .filter(Lesson::isCoupled)
                .count() > 1) {
            lessonTask.changeCoupled();
            //When 2 lessons are coupled, the last coupled its timeslot is only changed by the first
            //By setting the last coupled lesson at the back
*//*            lessonTask.deleteLessonsOfTaskList(lesson);
            lessonTask.addLessonsToTaskList(lesson);*//*

        }
        return Response.status(Response.Status.OK).build();

    }*/
    //SWAP CARDS OR INSERT CARD <--> FULLY DETERMINED BY id's IN 'app.js'
    @POST
    @Path("changeTiRoTe/dragLessonId/{dragLessonId}/{dropOtherTag}/{dropOtherId}/{dropFirstTag}/{dropFirstId}")
    public Response changeTiRoTe(@PathParam("dragLessonId") Long dragLessonId,
                                 @PathParam("dropOtherTag") String dropOtherTag, // needed to change teachers/studentgroup?
                                 @PathParam("dropOtherId") String dropOtherId,// needed to change teachers/studentgroup?
                                 @PathParam("dropFirstTag") String dropFirstTag,
                                 @PathParam("dropFirstId") Long dropFirstId){

        Boolean changeRoom = dropOtherTag.equals("ro");
        Lesson dragLesson = Lesson.findById(dragLessonId);
        //Case we drop card in empty slot. 'dropFirstTag' corresponds with 'timeslotId' in app.js
        if (dropFirstTag.equals("ti")) {
            dragLesson.setTimeslot(Timeslot.findById(dropFirstId));
            if (changeRoom) {
                dragLesson.setRoom(Room.findById(Long.parseLong(dropOtherId)));
            }}
        //Case we drop card on occupied slot. 'dropFirstTag' corresponds with 'lessonId' of other card.
        else {
            Lesson dropLesson = Lesson.findById(dropFirstId);
            swapTimeSlots(dragLesson, dropLesson);
            if (changeRoom) {
                swapRooms(dragLesson, dropLesson);
            }
        }

        return Response.status(Response.Status.OK).build();
    }

    //ADD LESSON TO TIMESLOT BY DRAGGING <-->FULLY DEPENDING ON app.js
    @POST
    @Path("assignTimeSlot/{dropFirstTag}/{dropFirstId}/lessonId/{lessonId}")
    public Response assignTimeslot(@PathParam("dropFirstTag") String dropFirstTag,
                                   @PathParam("dropFirstId") Long dropFirstId,
                                   @PathParam("lessonId") Long lessonId) {
        Lesson lesson = Lesson.findById(lessonId);
        Timeslot timeslot;
        if (dropFirstTag.equals("ti")) {
            timeslot = Timeslot.findById(dropFirstId);}
        else {
            Lesson dropLesson = Lesson.findById(dropFirstId);
            timeslot = dropLesson.getTimeslot();
        }
        lesson.setTimeslot(timeslot);
        return Response.status(Response.Status.OK).build();
    }

    // TO DO - UPDATING TEACHERS/ STUDENTGROUPS
    @DELETE
    @Path("{lessonId}")
    public Response delete(@PathParam("lessonId") Long lessonId) {
        Lesson lesson = Lesson.findById(lessonId);
        if (lesson == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        //Handling lessonTask relation
        LessonTask lessonTask = lesson.getLessonTask();
        lessonTask.deleteLessonsOfTaskList(lesson);
        //+ Coupling
        lessonTask.resetCoupling();


        if (lessonTask.getMultiplicity() < 1) {
            lessonTask.delete();
        }
        //Handling studentGroup relation ...
        //Handling Teacher relation ...
        lesson.delete();
        return Response.status(Response.Status.OK).build();
    }





/*    private Long extractId(String encoded){
        //omit prefix le- inserted in app.js
        return Long.parseLong(encoded.substring(3));
    }*/

    /* private void updateLessonBy(Lesson lesson, String toBeDeleted,  String instruction){
        List<String> toUpdate = convertor(instruction, '-');
        System.out.print(toBeDeleted);
        //The instruction string needs to contain always pairs
        int length = toUpdate.size()/2;
        for(int i=0; i< length; i ++ ){
            String updateVar =  toUpdate.get(2 * i);
            if (updateVar.equals("ti")) {
                lesson.setTimeslot(Timeslot.findById(Long.parseLong(toUpdate.get(2 * i + 1))));
            }
            if (updateVar.equals("te")) {
                    Teacher oldTeacher = Teacher.findById(toBeDeleted);
                    Teacher newTeacher = Teacher.findById(toUpdate.get(2 * i +1));
                    lesson.updateTeacherFromLessonTask(oldTeacher, newTeacher);

            }
            if (updateVar.equals("ro")) {
                lesson.setRoom(Room.findById(Long.parseLong(toUpdate.get(2 * i + 1))));
                }
            if (updateVar.equals("st")) {

            }
            }
    }*/

    public void swapTimeSlots(Lesson lesson1, Lesson lesson2) {
        Timeslot storage = lesson1.getTimeslot();
        lesson1.setTimeslot(lesson2.getTimeslot());
        lesson2.setTimeslot(storage);
    }

    public void swapRooms(Lesson lesson1, Lesson lesson2) {
        Room storage = lesson1.getRoom();
        lesson1.setRoom(lesson2.getRoom());
        lesson2.setRoom(storage);
    }


}