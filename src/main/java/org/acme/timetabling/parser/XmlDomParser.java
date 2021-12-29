package org.acme.timetabling.parser;

import org.acme.timetabling.domain.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class XmlDomParser {

    public static Function<String, List> main() {

        String FILENAME = "/home/svs/IdeaProjects/school-timetabling/data/extern/Poging1.xml";

        //Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        List<Timeslot> timeslotList = new ArrayList<>();
        List<Teacher> teacherList;
        List<StudentGroup> studentGroupList;
        List<Room> roomList = new ArrayList<>();
        List<LessonTask>  lessonTaskList = new ArrayList<>();
/*        List<LessonBlock> lessonBlockList = new ArrayList<>();*/
        List<Lesson> lessonList =new ArrayList<>();
        Function<String, List> giveList = null;

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            //parse XML file
            DocumentBuilder db =dbf.newDocumentBuilder();

            Document doc =db.parse(new File(FILENAME));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();


            //CREATE TIMESLOTS
            List<List<LocalTime>> fullDay = new ArrayList<>();
            List<List<LocalTime>> halfDay = new ArrayList<>();
            List<LocalTime> first = new ArrayList<>();
            List<LocalTime> second = new ArrayList<>();
            List<LocalTime> third = new ArrayList<>();
            List<LocalTime> fourth = new ArrayList<>();
            List<LocalTime> fourth2 = new ArrayList<>();
            List<LocalTime> fifth = new ArrayList<>();
            List<LocalTime> fifth2 = new ArrayList<>();
            List<LocalTime> sixth = new ArrayList<>();
            List<LocalTime> seventh = new ArrayList<>();
            List<LocalTime> eighth = new ArrayList<>();
            
            
            
            first.add(LocalTime.of(8,55));
            first.add(LocalTime.of(9,45));
            second.add(LocalTime.of(9,45));
            second.add(LocalTime.of(10,35));
            third.add(LocalTime.of(10,35));
            third.add(LocalTime.of(11,25));
            fourth.add(LocalTime.of(12,25));
            fourth.add(LocalTime.of(13,15));
            fourth2.add(LocalTime.of(11,40));
            fourth2.add(LocalTime.of(12,30));
            fifth.add(LocalTime.of(13,15));
            fifth.add(LocalTime.of(14,05));
            fifth2.add(LocalTime.of(12,30));
            fifth2.add(LocalTime.of(13,20));
            sixth.add(LocalTime.of(14,05));
            sixth.add(LocalTime.of(14,55));
            seventh.add(LocalTime.of(15,10));
            seventh.add(LocalTime.of(16,00));
            eighth.add(LocalTime.of(16,00));
            eighth.add(LocalTime.of(16,50));

            fullDay.add(first);
            fullDay.add(second);
            fullDay.add(third);
            fullDay.add(fourth);
            fullDay.add(fifth);
            fullDay.add(sixth);
            fullDay.add(seventh);
            fullDay.add(eighth);

            halfDay.add(first);
            halfDay.add(second);
            halfDay.add(third);
            halfDay.add(fourth2);
            halfDay.add(fifth2);

            Timeslot timSlot;
            for (DayOfWeek d: DayOfWeek.values()) {
                if(d.equals(DayOfWeek.WEDNESDAY)) {
                    for(List<LocalTime> ts: halfDay){
                        timSlot = new Timeslot(d, ts.get(0), ts.get(1));
                        timeslotList.add(timSlot);
                        if (ts.equals(halfDay.get(halfDay.toArray().length -1))) {
                            timSlot.changeLastResort();
                        }
                    }
                }
                else{
                    if ( (! d.equals(DayOfWeek.SATURDAY)) && (! d.equals(DayOfWeek.SUNDAY))) {
                        for (List<LocalTime> ts : fullDay) {
                            timSlot =new Timeslot(d, ts.get(0), ts.get(1));
                            timeslotList.add(timSlot);
                            if (ts.equals(fullDay.get(fullDay.toArray().length -1))) {
                                timSlot.changeLastResort();
                            }
                        }
                    }
                }
            }
//TESTING ORDERING OF TIMESLOTS DURING FETCHING
/*            Timeslot toy1 = new Timeslot(DayOfWeek.WEDNESDAY,seventh.get(0), seventh.get(1));
            Timeslot toy2 = new Timeslot(DayOfWeek.SATURDAY,seventh.get(0), seventh.get(1));
            Timeslot toy3 = new Timeslot(DayOfWeek.WEDNESDAY,seventh.get(0), eighth.get(1));
            timeslotList.add(toy1);
            timeslotList.add(toy2);
            timeslotList.add(toy3);*/

            //EXTRACT TEACHERNAMES FROM DOC
/*            NodeList teachersList = doc.getElementsByTagName("Teacher");
            List<String> teacherNameList = new ArrayList<>();

            for (int index =0; index < teachersList.getLength(); index ++) {
                Node node = teachersList.item(index);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    Node child = element.getElementsByTagName("Name").item(0);
                    if (child !=  null) {
                        String acronym = child.getTextContent();
                        teacherNameList.add(acronym);
                    }


                }
            }

            //CREATE TEACHERLIST


            for (String name: teacherNameList) {
                teacherList.add(new Teacher(name));
            }*/

            //Extract teachers from DATA/EXTERN/DOCENTEN...TEXT

            teacherList = ExtractUntisText.fetchTeachers("/home/svs/IdeaProjects/school-timetabling/data/extern/DocentenSPC_101221.TXT");

            studentGroupList = ExtractUntisText.fetchStudentGroups("/home/svs/IdeaProjects/school-timetabling/data/extern/KlassenSPC_101221.TXT", teacherList);

            Map<String, Integer> studentNumbers = new HashMap<>(studentGroupList.size());

            //EXTRACT STUDENTGROUPNAMES FROM DOC +
            NodeList studentGroupsList = doc.getElementsByTagName("Group");
            //+ CREATE STUDENTGROUPS FROM FILE

            for (int index =0; index < studentGroupsList.getLength(); index ++) {
                Node node = studentGroupsList.item(index);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Node child = element.getElementsByTagName("Name").item(0);
                    Node childNumb = element.getElementsByTagName("Number_of_Students").item(0);
                    if (child !=  null) {
                        String acronym = child.getTextContent();
                        //FETCH NUMBER OF STUDENTS
                        studentNumbers.put(acronym, Integer.parseInt(childNumb.getTextContent()));
                    }
                }
            }

            ExtractUntisText.updateStudentGroupsNumbers(studentGroupList, studentNumbers);


            //CREATE ROOMS

            roomList.add(new Room("Room B21"));
            roomList.add(new Room("Room B22"));
            roomList.add(new Room("Room B23"));
            roomList.add(new Room("Room C31"));
            roomList.add(new Room("Room C32"));
            roomList.add(new Room("Room C33"));
            roomList.add(new Room("Room C34"));
            roomList.add(new Room("Room D21"));
            roomList.add(new Room("Room D22"));
            roomList.add(new Room("Room D23"));
            roomList.add(new Room("Room D31"));
            roomList.add(new Room("Room D32"));
            roomList.add(new Room("Room A21"));
            roomList.add(new Room("Room A31"));
            roomList.add(new Room("Room B11"));

            //EXTRACT LESSONTASKS FROM DOC +
            NodeList LessonTaskList = doc.getElementsByTagName("Activity");
            //+ CREATE STUDENTGROUPS FROM FILE

            LessonTask lessonTask;
            Lesson lesson;
/*            LessonBlock lessonBlock;*/
            List<String> versionB =new ArrayList<>();
            List<String> taskNumbList = new ArrayList<>();
            for (int index =0; index < LessonTaskList.getLength(); index ++) {
                Node node = LessonTaskList.item(index);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    // TASKNUMBER FOR LESSON
                    Node taskNum = element.getElementsByTagName("Activity_Tag").item(0);
                    if (taskNum != null) {
                        String taskNumString = taskNum.getTextContent();
                        Integer length = taskNumString.length();
                        Boolean ignoreVersionB = true;
                        Boolean ignoreTask = true;
                        if (taskNumString.charAt(length - 1) == 'B') {
                            taskNumString = taskNumString.substring(0, length -1);
                            ignoreVersionB = versionB.contains(taskNumString);
                            if (! ignoreVersionB) {
                                versionB.add(taskNumString);
                            }
                        } else {
                            ignoreTask = taskNumbList.contains(taskNumString);
                            if(! ignoreTask) {
                                taskNumbList.add(taskNumString);
                            }
                        }
                        if ((! ignoreTask) || (! ignoreVersionB)) {
                            Integer taskNumber = Integer.parseInt(taskNumString);

                            //STUDENTGROUPS FOR LESSON
                            NodeList students = element.getElementsByTagName("Students");
                            List<String> studNames = new ArrayList<>();
                            if (students.getLength() != 0) {
                                for (int studIndex = 0; studIndex < students.getLength(); studIndex++) {
                                    Node studGroup = students.item(studIndex);
                                    if (studGroup != null) {
                                        studNames.add(studGroup.getTextContent());
                                    }
                                }
                            }
                            List<StudentGroup> stGroupList = studentGroupList.stream().filter(stgr ->studNames.contains(stgr.getGroupName())).collect(Collectors.toList());
                            // TEACHERS FOR LESSON
                            NodeList teachs = element.getElementsByTagName("Teacher");
                            List<String> teNames = new ArrayList<>();
                            if (teachs.getLength() != 0) {
                                for (int teIndex = 0; teIndex < teachs.getLength(); teIndex++) {
                                    Node te = teachs.item(teIndex);
                                    if (te != null) {
                                        teNames.add(te.getTextContent());
                                    }
                                }
                            }
                            List<Teacher> teList = teacherList.stream().filter(tea ->teNames.contains(tea.getAcronym())).collect(Collectors.toList());

                            // SUBJECT FOR LESSON
                            Node subj = element.getElementsByTagName("Subject").item(0);
                            String subject = "";
                            if (subj != null) {
                                subject = subj.getTextContent();
                            }

                            // Multiplicity FOR LESSON
                            Node mul = element.getElementsByTagName("Total_Duration").item(0);
                            Integer multiplicity= 0;
                            if (mul != null) {
                                multiplicity = Integer.parseInt(mul.getTextContent());
                            }

                            //CREATE LESSONTASK
                            List<LessonTask> copy= lessonTaskList.stream().filter(leta-> leta.getTaskNumber().equals(taskNumber)).collect(Collectors.toList());
                            if (copy.isEmpty()) {
                                lessonTask = new LessonTask(taskNumber,subject, stGroupList, teList);
                                lessonTaskList.add(lessonTask);
                            }
                            else {
                                lessonTask = copy.get(0);
                            }
                            for (int k =0; k< multiplicity; k++){
                                lesson = new Lesson(lessonTask);
                                lessonList.add(lesson);
                                lessonTask.addLessonsToTaskList(lesson);
                            }
                            if (subject.equals("LO")){
                                lessonTask.addCoupling(2);
                            }
                        }

                    }


                }
            }
            giveList = message -> {
                if (message.equals("ti")) {
                    return timeslotList;
                }
                if (message.equals("te")) {
                    return teacherList;
                }
                if (message.equals("ro")) {
                    return roomList;
                }
                if (message.equals("st")) {
                    return studentGroupList;
                }
                if (message.equals("ta")) {
                    return lessonTaskList;
                }
                if (message.equals("le")) {
                    return lessonList;
                }
                return new ArrayList();

            };
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return giveList;
    }
}
