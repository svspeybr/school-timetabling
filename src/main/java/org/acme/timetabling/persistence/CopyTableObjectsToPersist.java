package org.acme.timetabling.persistence;

import io.quarkus.panache.common.Sort;
import org.acme.timetabling.domain.*;
import org.acme.timetabling.rest.TimeslotResource;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.prefs.Preferences;

public class CopyTableObjectsToPersist {
    public final List<Room> copyRoomsToPersist(List<Room> roomList){
        int length = roomList.size();
        for (int i =0; i < length; i++){
            Room room = new Room(roomList.get(i).getName());
            roomList.set(i, room);
        }
        return roomList;
    }

    public final List<Timeslot> copyTimeslotsToPersist(List<Timeslot> timeslotList, DefaultSettings ds){
        int length = timeslotList.size();
        List<Timeslot> semTimeslots = new ArrayList<>();
        for (int i =0; i < length; i++){
            Timeslot oldTimeslot = timeslotList.get(i);
            Timeslot timeslot = new Timeslot(oldTimeslot.getDayOfWeek(),
                                            oldTimeslot.getStartTime(),
                                            oldTimeslot.getEndTime());
            if (oldTimeslot.isLastResort()){
                timeslot.changeLastResort();
            }

            ds.addSlotCount(timeslot.getDayOfWeek().getValue(), 1);

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
            timeslotList.set(i, timeslot);
        }
        semTimeslots.sort(Comparator.comparing(Timeslot::getDayOfWeek)
                                    .thenComparing(Timeslot::getStartTime)
                                    .thenComparing(Timeslot::getEndTime));
        Integer order =0;
        for (Timeslot timeslot: timeslotList) {
            order++;
            timeslot.setPosition(order);
        }
        return timeslotList;
    }

    public final List<Preference> copyPreferences(List<Preference> pfList, Map<Integer, Timeslot> timeslotMap, Map<String, Teacher> teacherMap){
        int length = pfList.size();
        for (int i =0; i < length; i++){
            Preference oldpf = pfList.get(i);
            String acronymTe = oldpf.getTeacher().getAcronym();
            Preference pf;
            if (teacherMap.containsKey(acronymTe)){
                pf = new Preference(teacherMap.get(acronymTe),
                        timeslotMap.get(oldpf.getTimeslot().getPosition()));
                pfList.set(i, pf);
            }
        }
        return pfList;
    }

    public final List<Teacher> copyTeachersToPersist(List<Teacher> teacherList, DefaultSettings ds){
        int length = teacherList.size();
        for (int i =0; i < length; i++){
            Teacher oldte = teacherList.get(i);
            String acronym = oldte.getAcronym();
            Teacher te = new Teacher(acronym, oldte.getName());
            te.setCoverId(oldte.getCoverId()); // CARD PICTURE ID
            teacherList.set(i, te);
        }
        return new ArrayList<>(teacherList);
    }

    public final List<StudentGroup> copyStudentsToPersist(List<StudentGroup> sgList, Map<String, Teacher> teacherMap){
        int length = sgList.size();
        for (int i =0; i < length; i++){
            StudentGroup oldsg = sgList.get(i);
            StudentGroup sg = new StudentGroup(oldsg.getGroupName(), oldsg.getNumberOfStudents());

            sg.setYear(oldsg.getYear());
            Set<Teacher> classTeachers = oldsg.getClassTeachers();
            if (! classTeachers.isEmpty()) {
                for (Teacher te : classTeachers) {
                    String acronym = te.getAcronym();
                    if (teacherMap.containsKey(acronym)){
                        sg.addClassTeacher(teacherMap.get(acronym));
                    }  //work with persistable objects instead of the teacher object in oldsg
                }
            }
            sgList.set(i, sg);
        }
        return sgList;
    }

    public final List<SubjectCollection> copySubjectCollectionToPersist(List<SubjectCollection> scList){
        int length = scList.size();
        for (int i =0; i < length; i++){
            SubjectCollection oldsc = scList.get(i);
            SubjectCollection sc = new SubjectCollection(oldsc.getCollectionName(), oldsc.getMaxAssignmentsOnSameSlot());
            for (String subject : oldsc.getSubjects()){
                sc.addSubject(subject);
            }
            scList.set(i, sc);
        }
        return scList;
    }

    public final List<LessonTask> copyLessonTaskToPersist(List<LessonTask> ltList, Map<String, StudentGroup> sgMap, Map<String, Teacher> teacherMap){
        int length = ltList.size();
        for (int i = 0; i < length; i++){
            LessonTask oldlt = ltList.get(i);
            //UPDATE persistable studentgroups
            List<StudentGroup> sgList = new ArrayList<>(oldlt.getStudentGroups().size());
            for (StudentGroup sg: oldlt.getStudentGroups()){
                sgList.add(sgMap.get(sg.getGroupName()));
            }
            //UPDATE persistable teachers
            List<Teacher> teList = new ArrayList<>(oldlt.getTaughtBy().size());
            for (Teacher te: oldlt.getTaughtBy()){
                teList.add(teacherMap.get(te.getAcronym()));
            }

            LessonTask lt = new LessonTask(oldlt.getTaskNumber(),
                                            oldlt.getSubject(),
                                            sgList,
                                            teList);
            //update couplingNumbers
            for (Integer coupleNumb: oldlt.getCouplingNumbers()){
                lt.addCoupling(coupleNumb);
            }
            //update if Task is a teachingTask
            lt.setTeachingTask(oldlt.isATeachingTask());
            ltList.set(i, lt);
        }
        return ltList;
    }

    public final List<Long> copyLessonToPersist(List<Lesson> lessonList,
                                                  Map<Integer, LessonTask> ltMap,
                                                  Map<Integer, Timeslot> timeslotMap,
                                                  Map<String, Room> roomMap){
        int length = lessonList.size();
        //save oldIndices as List (!!) to link oldLesson IDS to new
        //problem: mapping from old to new does not work immediately since firstly,
        // nnew lesson ids have to be generated AFTER persistence
        List<Long> oldIndices= new ArrayList<>(length);

        for (int i = 0; i < length; i++){
            Lesson oldlesson = lessonList.get(i);
            Lesson lesson = new Lesson(); // New lesson get Id after persistence occured
            oldIndices.add(oldlesson.getLessonId());
            lesson.setLessonTask(ltMap.get(oldlesson.getTaskNumber()));
            Timeslot ts = oldlesson.getTimeslot();
            if (ts != null) {
                lesson.setTimeslot(timeslotMap.get(ts.getPosition()));
            }
            Room room = oldlesson.getRoom();
            if (room != null) {
                lesson.setRoom(roomMap.get(room.getName()));
            }
            lesson.setPinned(oldlesson.isPinned());
            lessonList.set(i, lesson);
        }
        return oldIndices;
    }

    public final List<ThemeCollection> copyThemeCollectionToPersist(List<ThemeCollection> tcList,
                                                     Map<Long, Long> lessonIdOldToNew,
                                                     Map<Integer, Timeslot> timeslotMap){
        int length = tcList.size();
        for (int i = 0; i < length; i++){
            ThemeCollection oldtc = tcList.get(i);
            ThemeCollection tc = new ThemeCollection(oldtc.getTheme());
            Map<Timeslot, Integer> multiTs = oldtc.getMultiplicityTimeslots();
            for (Timeslot ts: multiTs.keySet()){
                tc.addMultiplicityForTimeslot(timeslotMap.get(ts.getPosition()), multiTs.get(ts));
            }

            for (Long oldId: oldtc.getLessonIds()){
                tc.addLesson(lessonIdOldToNew.get(oldId));
            }
            tcList.set(i, tc);
        }
        return tcList;
    }

    public final List<CourseLevel> copyCourseLevelToPersist(List<CourseLevel> clList,
                                                                    Map<Integer, LessonTask> ltMap){
        int length = clList.size();
        for (int i = 0; i < length; i++){
            CourseLevel oldcl = clList.get(i);
            List<LessonTask> oldltList = oldcl.getLessonTasks();
            List<LessonTask> ltList = new ArrayList<>(oldltList.size());
            for (LessonTask oldlt: oldltList){
                ltList.add(ltMap.get(oldlt.getTaskNumber()));
            }
            CourseLevel cl  = new CourseLevel(ltList);
            clList.set(i, cl);
        }
        return clList;
    }

}
