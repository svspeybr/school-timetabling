package org.acme.timetabling.bootstrap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

import org.acme.timetabling.domain.*;
import org.acme.timetabling.parser.XmlDomParser;

import io.quarkus.runtime.StartupEvent;
import org.acme.timetabling.persistence.XmlDataGenerator;
import org.acme.timetabling.rest.TimeslotResource;

@ApplicationScoped
public class DataGenerator {


    @Transactional
    public void generateData(@Observes StartupEvent startupEvent) {



        Function<String, List> giveList = XmlDomParser.main();
        //TIMESLOTS
        Timeslot.persist( giveList.apply("ti"));
        //ORDERING TIMESLOTS
        TimeslotResource.updatePositions();

        //TEACHERS
        Teacher.persist(giveList.apply("te"));
        //ROOMS
        Room.persist(giveList.apply("ro"));

        //STUDENTGROUPS
        StudentGroup.persist(giveList.apply("st"));
        //LESSONTASKS
        List<LessonTask> lessonTaskList = giveList.apply("ta");

        //ADD COURSE LEVEL FOR HISTORY COURSES
        List<LessonTask> historyTasks5 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("GE") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(5)).collect(Collectors.toList());
        List<LessonTask> historyTasks6 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("GE") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(6)).collect(Collectors.toList());
        CourseLevel histCourseLevel5 = new CourseLevel(new ArrayList<>(historyTasks5));
        CourseLevel histCourseLevel6 = new CourseLevel(new ArrayList<>(historyTasks6));
        CourseLevel.persist(histCourseLevel5);
        CourseLevel.persist(histCourseLevel6);
        LessonTask lessonTaskWGS = lessonTaskList.stream().filter(leta-> leta.getTaskNumber() == 970).findFirst().get();
        //ADD COUPLING
        lessonTaskWGS.addCoupling(2);
        lessonTaskWGS.addCoupling(3);
        LessonTask.persist(lessonTaskList);
        //LESSONS
        Lesson.persist( giveList.apply("le"));


        //Configure Timetable for benchmark
        /*XmlDataGenerator.main();*/

        //DefaultSettings
        DefaultSettings defaultSettings = new DefaultSettings();
        //SET SCIENCE COURSES
        defaultSettings.addScienceCourse("CH");
        defaultSettings.addScienceCourse("FY");

        //Configure solver

/*
        List<Timeslot> timeslotList = new ArrayList<>();
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        Timeslot.persist(timeslotList);

        List<Room> roomList = new ArrayList<>(3);
        roomList.add(new Room("Room A"));
        roomList.add(new Room("Room B"));
        roomList.add(new Room("Room C"));

        Room.persist(roomList);

        List<StudentGroup> studentGroupList = new ArrayList<>();
        StudentGroup group_5LAWI = new StudentGroup("5LAWI");
        StudentGroup group_6LAWE = new StudentGroup("6LAWE");
        studentGroupList.add(group_5LAWI);
        studentGroupList.add(group_6LAWE);

        StudentGroup.persist(studentGroupList);

        List<Teacher> teacherList = new ArrayList<>();
        Teacher turing = new Teacher( "AVP");
        Teacher curie = new Teacher("EDS");
        Teacher darwin =new Teacher("MSC");
        Teacher jones =new Teacher("VWI");
        Teacher cruz =new Teacher("EVP");
        teacherList.add(turing);
        teacherList.add(curie);
        teacherList.add(darwin);
        teacherList.add(jones);
        teacherList.add(cruz);

        Teacher.persist(teacherList);

        List<Lesson> lessonList = new ArrayList<>();

        lessonList.add(new Lesson("Math", turing, group_5LAWI));
        lessonList.add(new Lesson("Math", turing, group_5LAWI));
        lessonList.add(new Lesson("Physics", curie, group_5LAWI));
        lessonList.add(new Lesson("Chemistry", curie, group_5LAWI));
        lessonList.add(new Lesson("Biology", darwin, group_5LAWI));
        lessonList.add(new Lesson("History", jones,group_5LAWI));
        lessonList.add(new Lesson("English", jones, group_5LAWI));
        lessonList.add(new Lesson("English", jones, group_5LAWI));
        lessonList.add(new Lesson("Spanish", cruz, group_5LAWI));
        lessonList.add(new Lesson("Spanish", cruz, group_5LAWI));


        lessonList.add(new Lesson("Math", turing, group_6LAWE));
        lessonList.add(new Lesson("Math", turing, group_6LAWE));
        lessonList.add(new Lesson("Math", turing, group_6LAWE));
        lessonList.add(new Lesson("Physics", curie, group_6LAWE));
        lessonList.add(new Lesson("Chemistry", curie, group_6LAWE));
        lessonList.add(new Lesson("French", curie, group_6LAWE));
        lessonList.add(new Lesson("Geography", darwin, group_6LAWE));
        lessonList.add(new Lesson("History", jones, group_6LAWE));
        lessonList.add(new Lesson("English", cruz, group_6LAWE));
        lessonList.add(new Lesson("Spanish", cruz, group_6LAWE));


        List<Lesson> alreadyTasked =new ArrayList<>();
        LessonTask iteratorLessonTask = new LessonTask();
        Integer TaskNumber = 1000;
        for (Lesson lesson: lessonList){
            Boolean contained = false;
            for (Lesson taskedLesson:alreadyTasked){
                if (lesson.getSubject().equals(taskedLesson.getSubject()) &&
                    lesson.getTaughtBy().equals(taskedLesson.getTaughtBy()) &&
                    lesson.getStudentGroups().equals(taskedLesson.getStudentGroups())){
                    contained =true;
                }
            }
            if (!contained) {
                TaskNumber ++;
                iteratorLessonTask = new LessonTask(TaskNumber);
                alreadyTasked.add(lesson);
                LessonTask.persist(iteratorLessonTask);
            }
            lesson.setLessonTask(iteratorLessonTask);
            Lesson.persist(lesson);
        }
*/



/*        lessonTaskList.add(
                new LessonTask(1,
                        2,
                        "Math",
                        turing,
                        group_5LAWI));
        lessonTaskList.add(
                new LessonTask(2,
                        1,
                        "Physics",
                        curie,
                        group_5LAWI));
        lessonTaskList.add(
                new LessonTask(3,
                        1,
                        "Chemistry",
                        curie,
                        group_5LAWI));
        lessonTaskList.add(
                new LessonTask(4,
                        1,
                        "Biology",
                        darwin,
                        group_5LAWI));
        lessonTaskList.add(
                new LessonTask(5,
                        1,
                        "History",
                        jones,
                        group_5LAWI));
        lessonTaskList.add(
                new LessonTask(6,
                        2,
                        "English",
                        jones,
                        group_5LAWI));
        lessonTaskList.add(
                new LessonTask(7,
                        2,
                        "Spanish",
                        cruz,
                        group_5LAWI));
        lessonTaskList.add(
                new LessonTask(8,
                        3,
                        "Math",
                        turing,
                        group_6LAWE));
        lessonTaskList.add(
                new LessonTask(9,
                        1,
                        "Physics",
                        curie,
                        group_6LAWE));
        lessonTaskList.add(
                new LessonTask(10,
                        1,
                        "Chemistry",
                        curie,
                        group_6LAWE));
        lessonTaskList.add(
                new LessonTask(11,
                        1,
                        "French",
                        curie,
                        group_6LAWE));
        lessonTaskList.add(
                new LessonTask(12,
                        1,
                        "Geography",
                        darwin,
                        group_6LAWE));
        lessonTaskList.add(
                new LessonTask(13,
                        1,
                        "History",
                        jones,
                        group_6LAWE));

        lessonTaskList.add(
                new LessonTask(14,
                        1,
                        "English",
                        cruz,
                        group_6LAWE));

        lessonTaskList.add(
                new LessonTask(16,
                        1,
                        "Spanish",
                        cruz,
                        group_6LAWE));*/

/*        Hashtable<LessonTask, List<Lesson>> dict = new Hashtable<LessonTask, List<Lesson>>();
        for (LessonTask lessonTask: lessonTaskList){
            List<Lesson> newLessonList = lessonTask.getLessonsOfTaskList();
            dict.put(lessonTask, newLessonList);
            lessonList.addAll(newLessonList);
            Lesson.persist(newLessonList);
            LessonTask.persist(lessonTask);*/
            /* for (Lesson lesson: newLessonList) {
                lesson.setLessonTask(lessonTask);
            }}*/
    }
}


