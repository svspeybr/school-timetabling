package org.acme.timetabling.rest;

import org.acme.timetabling.domain.*;
import org.acme.timetabling.rest.repository.LessonTaskRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/lessonTasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class LessonTaskResource {
    @Inject
    LessonTaskRepository lessonTaskRepository = new LessonTaskRepository();

    @GET
    @Path("/{taskNumber}")
    public LessonTask get(@PathParam("taskNumber") Integer taskNumber) {
        LessonTask lessonTask = LessonTask.findById(taskNumber);
        lessonTask.updateCoupling();
        if (lessonTask == null){
            throw new NotFoundException();
        }
        return lessonTask;
    }
    @GET
    @Path("/bySubject/{subject}")
    public List<LessonTask> getLessonTasksBySubject(@PathParam("subject") String subject){
        if (subject.equals("_")){
            return LessonTask.listAll();
        }
        return LessonTask.find("SUBJECT", subject).list();
    }

    @GET
    @Path("/FetchByLessonId/{lessonId}")
    public LessonTask getBlockSizesRelatedTo(@PathParam("lessonId") Long lessonId) {
            LessonTask lessonTask = lessonTaskRepository.getLessonTaskByLessonId(lessonId);
            if (lessonTask == null){
                throw new NotFoundException();
            }
            lessonTask.getCouplingNumbers().sort(Comparator.naturalOrder());
            lessonTask.updateCoupling();
            return lessonTask;
    }

    @GET
    public List<LessonTask> get() {
        return LessonTask.listAll();
    }


    @POST
    @Path("add/{multiplicity}/{taskNumber}/{studentGroups}/{taughtBy}/{subject}")
    public Response copyLessons(@PathParam("multiplicity") Integer multiplicity,
                                @PathParam("taskNumber") Integer taskNumber,
                                @PathParam("studentGroups")String studentGroupString,
                                @PathParam("taughtBy")  String teacherString,
                                @PathParam("subject") String subject) {
        //CONVERT PATHPARAM: studentGroupString & teacherString to separate values
        //The values are separated by 'separationValue'
        List<String> teachers = convertor(teacherString, '-');
        List<String> groupNames = convertor(studentGroupString, '-');

        //DEFAULTSETTINGS:
        DefaultSettings ds = DefaultSettings.findById(1L);

        //No OLD LESSONTASK
        LessonTask oldLessonTask = LessonTask.findById(taskNumber);
        if (oldLessonTask != null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

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

        //NEW LESSONTASK
        LessonTask lessonTask = new LessonTask(taskNumber,
                subject,
                studentGroupList,
                teList);

        //POST LESSONTASK -->ALREADY POSTED: STUDENTGROUPS/TEACHERS
        LessonTask.persist(lessonTask);
        for (int i = 0; i < multiplicity; i++) {
            //GET LESSON
            Lesson newLesson = new Lesson(lessonTask);
            //UPDATE LESSONTASK -->  FOR LESSON AND VICA VERSA!!!!
            lessonTask.addLessonsToTaskList(newLesson);
            //POST UPDATED LESSON
            Lesson.persist(newLesson);
        }

        //UPDATE TEACHERS TaskHOURS
        teList.forEach(teacher -> teacher.updateDependsFromLeTa(ds));

        return Response.status(Response.Status.OK).build();
    }


    @POST
    @Path("/addCouplingOfSize/{size}/ForTask/{taskId}")
    public Response addCoupling(@PathParam("size") int size,
                                @PathParam("taskId") Integer taskId){
        LessonTask task = LessonTask.findById(taskId);
        if (task == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        int numberOfBlocks = task.getCouplingNumbers().size();
        int numberOfLessonsInBlock = 0;
        for (Integer numb: task.getCouplingNumbers()){
            numberOfLessonsInBlock +=numb;
        }
        int numberOfLessonsLeft = task.getMultiplicity()- numberOfLessonsInBlock;
        //maxAllowedDays 5
        if (size < 2 ||
                size > numberOfLessonsLeft ||
                (numberOfBlocks >= 5 -1 && numberOfLessonsLeft > size)){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        task.addCoupling(size);
        return Response.status(Response.Status.OK).build();
    }
    @POST
    @Path("/removeCouplingOfSize/{size}/FromTask/{taskId}")
    public Response removeCoupling(@PathParam("size") int size,
                                   @PathParam("taskId") Long taskId){
        if(! lessonTaskRepository.removeBlockRecord(taskId, size)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK).build();
    }


    /*TO DO -DELETING LESSONS!!!!*/
    @DELETE
    @Path("/{taskNumber}")
    public Response delete (@PathParam("taskNumber") Integer taskNumber){
        DefaultSettings ds = DefaultSettings.findById(1L);
        LessonTask lessonTask = LessonTask.findById(taskNumber);
        if (lessonTask == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        for (Lesson lesson: lessonTask.getLessonsOfTaskList()){
            lesson.delete();
        }
        for (Teacher teacher: lessonTask.getTaughtBy()){
            teacher.removeLessonTask(lessonTask);
            teacher.updateDependsFromLeTa(ds);
        }
        lessonTask.delete();
        return Response.status(Response.Status.OK).build();
    }


    //**************************************************************************************************
    // Convertor for pathparameters -- > TO DO: REMOVE DUPLICATE IN THEMECOLLECTIONRESOURCE
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


