package org.acme.timetabling.bootstrap;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

import org.acme.timetabling.domain.*;
import org.acme.timetabling.parser.ExtractUntisText;
import org.acme.timetabling.parser.XmlDomParser;

import io.quarkus.runtime.StartupEvent;
import org.acme.timetabling.persistence.XmlFileIO;
import org.acme.timetabling.rest.FileServer;
import org.acme.timetabling.rest.TimeTableResource;
import org.acme.timetabling.rest.TimeslotResource;

@ApplicationScoped
public class DataGenerator {

    @Transactional
    public void fetchOrGenerateData(@Observes StartupEvent startupEvent) {

        final FileServer fs = new FileServer();

        List<String> loadedFiles = fs.loadFiles();

        if (loadedFiles.isEmpty()){
            generateData(fs);
        } else {
            loadData(loadedFiles, fs);
        }
    }

    private void loadData( List<String> loadedFiles, FileServer fs){
        final XmlFileIO xmlFileIO = new XmlFileIO();
        String pathname = fs.getPathNameFiles();
        //load first file
        File tableFile = new File(pathname + "/" + loadedFiles.get(0) + ".xml");
        TimeTable timeTable = xmlFileIO.readDataFromFile(tableFile);
        TimeTableResource.persistTable(timeTable);
    }


    private void generateData(FileServer fs) {

        //DefaultSettings
        DefaultSettings defaultSettings = new DefaultSettings(1L);
        DefaultSettings.persist(defaultSettings);

        //SET SCIENCE COURSES
        SubjectCollection scienceCourses = new SubjectCollection("science", 5);
        scienceCourses.addSubject("CH");
        scienceCourses.addSubject("FY");
        scienceCourses.addSubject("BI");
        scienceCourses.addSubject("NW");
        SubjectCollection.persist(scienceCourses);

        Function<String, List> giveList = XmlDomParser.main();
        //TIMESLOTS
        List<Timeslot> timeslots = giveList.apply("ti");
        List<Timeslot> semTimeslots = new ArrayList<>();
        //UPdat number of Timeslots
        for (Timeslot timeslot: timeslots){
            defaultSettings.addSlotCount(timeslot.getDayOfWeek().getValue(), 1);
            if (timeslot.getDayOfWeek().equals(DayOfWeek.MONDAY)
                    && timeslot.getStartTime().equals(LocalTime.of(12,25))
                    && timeslot.getEndTime().equals(LocalTime.of(13, 15))){
                semTimeslots.add(timeslot);
            }
            if (timeslot.getDayOfWeek().equals(DayOfWeek.MONDAY)
                    && timeslot.getStartTime().equals(LocalTime.of(15,10))
                    && timeslot.getEndTime().equals(LocalTime.of(16, 00))){
                semTimeslots.add(timeslot);
            }
        }
        semTimeslots.sort(Timeslot::compareTo);

        Timeslot.persist(timeslots);
        //ORDERING TIMESLOTS
        TimeslotResource.updatePositions();

        //TEACHERS
        List<Teacher> teachers = giveList.apply("te");
        Teacher svs = teachers.stream().filter(teacher -> teacher.getAcronym().equals("SVS")).findFirst().get();
        //ROOMS
        List<Room> rooms = giveList.apply("ro");
        Room.persist(rooms);

        //Preferences needs ORDERED TIMESLOTS
        Preference.persist(ExtractUntisText.fetchPreferences("/home/svs/IdeaProjects/school-timetabling/data/extern/TijdswensenSPC_101221.TXT",
                teachers,
                timeslots));

        //STUDENTGROUPS
        List<StudentGroup> studentGroups = giveList.apply("st");
        for (StudentGroup studentGroup: studentGroups) {
            if (studentGroup.getGroupName().equals("5ECWI") ||
                    studentGroup.getGroupName().equals("5GRWI") ||
                    studentGroup.getGroupName().equals("5LAWI")){
                studentGroup.addClassTeacher(svs); //TODO: check if svs is the only one with 'classTeacher'
            }
        }

        StudentGroup.persist(studentGroups);
        //LESSONTASKS
        List<LessonTask> lessonTaskList = giveList.apply("ta");
        //******************************************LESSSONTASK BIO 5th***************************************************
        List<StudentGroup> bio1h = studentGroups.stream()
                                                .filter(studentGroup -> studentGroup.getGroupName().equals("5ECWI") ||
                                                                studentGroup.getGroupName().equals("5LAWI") ||
                                                                studentGroup.getGroupName().equals("5GRWI"))
                                                .collect(Collectors.toList());
        List<StudentGroup> bio2hwi = studentGroups.stream()
                .filter(studentGroup -> studentGroup.getGroupName().equals("5WEWI"))
                .collect(Collectors.toList());
        List<StudentGroup> bio2h = studentGroups.stream()
                .filter(studentGroup -> studentGroup.getGroupName().equals("5LAWE") ||
                        studentGroup.getGroupName().equals("5MTWE"))
                .collect(Collectors.toList());
        List<List<StudentGroup>> bioSgCollection = new ArrayList<>(3);
        bioSgCollection.add(bio1h);
        bioSgCollection.add(bio2hwi);
        bioSgCollection.add(bio2h);

        List<Teacher> bioTeacherList = teachers.stream()
                        .filter(teacher -> teacher.getAcronym().equals("CLE"))
                        .collect(Collectors.toList());

        List<Integer> multiBio = new ArrayList<>();
        multiBio.add(1);
        multiBio.add(2);
        multiBio.add(2);

        List<Integer> taskNumbsBio = new ArrayList<>();
        taskNumbsBio.add(689);
        taskNumbsBio.add(725);
        taskNumbsBio.add(701);

        List<Lesson> lessons = giveList.apply("le"); //FETCH ALREADY LESSONS

        for (int i = 0; i< 3; i++){
            LessonTask leta = new LessonTask(taskNumbsBio.get(i), "BI", bioSgCollection.get(i), bioTeacherList);
            for (int j = 0; j < multiBio.get(i); j ++){
                Lesson lesson = new Lesson(leta);
                lessons.add(lesson);
                leta.addLessonsToTaskList(lesson);
            }
            lessonTaskList.add(leta);
        }
        LessonTask.persist(lessonTaskList);

        //LESSONS

        List<Lesson> sem5 = lessons.stream().filter(lesson -> lesson.getSubject().equals("S5")).collect(Collectors.toList());
        sem5.forEach(lesson -> {
            lesson.setPinned(true);
            lesson.setTimeslot(semTimeslots.get(0));
            lesson.setRoom(rooms.get(0));
        });

        List<Lesson> sem6 = lessons.stream().filter(lesson -> lesson.getSubject().equals("S6")).collect(Collectors.toList());
        sem6.forEach(lesson -> {
            lesson.setPinned(true);
            lesson.setTimeslot(semTimeslots.get(1));
            lesson.setRoom(rooms.get(0));
        });

        Lesson.persist(lessons);

        //ADD COURSELEVEL FOR HISTORY COURSES
        List<LessonTask> historyTasks5 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("GE") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(5)).collect(Collectors.toList());
        List<LessonTask> historyTasks6 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("GE") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(6)).collect(Collectors.toList());
        CourseLevel histCourseLevel5 = new CourseLevel(new ArrayList<>(historyTasks5));
        CourseLevel histCourseLevel6 = new CourseLevel(new ArrayList<>(historyTasks6));
        CourseLevel.persist(histCourseLevel5);
        CourseLevel.persist(histCourseLevel6);
        //ADD COURSELEVEL FOR RELIGION COURSES
        List<LessonTask> religionTasks5 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("GO") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(5)).collect(Collectors.toList());
        List<LessonTask> religionTasks6 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("GO") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(6)).collect(Collectors.toList());
        CourseLevel relCourseLevel5 = new CourseLevel(new ArrayList<>(religionTasks5));
        CourseLevel relCourseLevel6 = new CourseLevel(new ArrayList<>(religionTasks6));
        CourseLevel.persist(relCourseLevel5);
        CourseLevel.persist(relCourseLevel6);

        //ADD COURSELEVEL FOR ART/culture COURSES
        List<LessonTask> esthTasks5 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("ES") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(5)).collect(Collectors.toList());
        List<LessonTask> esthTasks6 = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("ES") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(6)).collect(Collectors.toList());
        CourseLevel esthCourseLevel5 = new CourseLevel(new ArrayList<>(esthTasks5));
        CourseLevel esthCourseLevel6 = new CourseLevel(new ArrayList<>(esthTasks6));
        CourseLevel.persist(esthCourseLevel5);
        CourseLevel.persist(esthCourseLevel6);


        //TURNZAAL - TODO: BAD (2e graad)
        ThemeCollection tc = new ThemeCollection("TURNZAAL");
        for (Lesson lesson: lessons){
            if (lesson.getSubject().equals("LO")){
                tc.addLesson(lesson.getLessonId());
            }
        }
        tc.addMultiplicityForTimeslot(timeslots.get(0), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(1), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(2), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(3), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(4), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(5), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(6), 2);

        tc.addMultiplicityForTimeslot(timeslots.get(8), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(9), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(10), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(11), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(12), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(13), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(14), 2);

        tc.addMultiplicityForTimeslot(timeslots.get(16), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(17), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(18), 1);
        tc.addMultiplicityForTimeslot(timeslots.get(19), 1);

        tc.addMultiplicityForTimeslot(timeslots.get(21), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(22), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(23), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(24), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(25), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(26), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(27), 2);

        tc.addMultiplicityForTimeslot(timeslots.get(29), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(30), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(31), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(32), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(33), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(34), 2);
        tc.addMultiplicityForTimeslot(timeslots.get(35), 2);

        tc.persist();

        //UpdateDependents after all lessontasks and criteria are made
        int index = 0;
        for (Teacher teacher: teachers){
            index++;
            //Referring to image of card
            teacher.setCoverId(index);
            //update fulltime FROM lessonTasks + update firstOrLastHours and noTeachingDays FROM PREFERENCES
            teacher.updateDependents(defaultSettings);
            if (Objects.equals(teacher.getAcronym(), "?")){
                teacher.setAcronym("unkown" + index);
            }
        }
        Teacher.persist(teachers);

        fs.saveFileInXmlForm("NieuwRooster");
    }
}




