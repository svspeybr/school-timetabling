package org.acme.timetabling.parser;

import org.acme.timetabling.domain.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class ExtractUntisText {

        public static List<Preference> fetchPreferences(String path, List<Teacher> teacherList, List<Timeslot> timeslots) {
            List<Preference> preferences = new ArrayList<>();
            Map<DayOfWeek, List<Timeslot>> mapDayToTimeslots = new HashMap<>(7);
            for (DayOfWeek dayOfWeek: DayOfWeek.values()){
                mapDayToTimeslots.put(dayOfWeek, new ArrayList<>(10));
            }
            //timeslots are supposed to be ordered!!!
            for (Timeslot tsl: timeslots){
                mapDayToTimeslots.get(tsl.getDayOfWeek()).add(tsl);
            }

            try {
                File myObj = new File(path);
                Scanner myReader = new Scanner(myObj);
                Teacher teacher = new Teacher("?1?0*#{}");
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    String[] dataParts = data.split(",", 5);
                    String teacherAcronym = ignoreAccentsAroundString(dataParts[1]);
                    DayOfWeek day = DayOfWeek.of(Integer.parseInt(dataParts[2]));
                    int timeslotIndex = Integer.parseInt(dataParts[3]);

/*                    System.out.println(teacherAcronym);
                    System.out.println(day);
                    System.out.println(timeslotIndex);
                    System.out.println(mapDayToTimeslots.get(day).size());*/


                    if ( ! teacher.getAcronym().equals(teacherAcronym)){
                        List<Teacher> found = teacherList.stream().filter(teach-> teach.getAcronym().equals(teacherAcronym)).collect(Collectors.toList());

                        if (!found.isEmpty()){
                            teacher = found.get(0);
                            Timeslot timeslot = mapDayToTimeslots.get(day).get(timeslotIndex - 1);
                            Preference preference = new Preference(teacher, timeslot);
                            preferences.add(preference);
                        }
                    } else {
                        Timeslot timeslot = mapDayToTimeslots.get(day).get(timeslotIndex -1);
                        Preference preference = new Preference(teacher, timeslot);
                        preferences.add(preference);
                    }
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            return preferences;
        }

    public static List<Teacher> fetchTeachers(String path) {
        List<Teacher> teachers = new ArrayList<>();

        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] dataParts = data.split(",", 18); // 18 -> Determined by Untis configuration
                String teacherAcronym = ignoreAccentsAroundString(dataParts[0]);
                String teacherName = ignoreAccentsAroundString(dataParts[1]);
                String taskHoursString= dataParts[14];
                if (!Objects.equals(taskHoursString, "")){
                    if (taskHoursString.charAt(1) == '.'){
                        taskHoursString = taskHoursString.substring(0,1);
                    } else {
                        taskHoursString = taskHoursString.substring(0,2);
                    }

                } else {
                    taskHoursString = "0";
                }
                int taskHours = Integer.parseInt(taskHoursString);

                String totalString= dataParts[15];
                if (!Objects.equals(totalString, "")){
                    if (totalString.charAt(1) == '.'){
                        totalString = totalString.substring(0,1);
                    } else {
                        totalString = totalString.substring(0,2);
                    }

                } else {
                    totalString = "0";
                }
                int total= Integer.parseInt(totalString);

                Teacher teacher = new Teacher(teacherAcronym, teacherName);
/*                teacher.setTaskHours(taskHours);*/
                teachers.add(teacher);

            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return teachers;
    }

    public static List<StudentGroup> fetchStudentGroups(String path, List<Teacher> teachers) {

        Map<String, Teacher> teacherMap = new HashMap<>(teachers.size());
        for (Teacher teacher: teachers){
            teacherMap.put(teacher.getAcronym(), teacher);
        }
        List<StudentGroup> studentGroups = new ArrayList<>();

        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] dataParts = data.split(",", 35); // 18 -> Determined by Untis configuration
                String teacherAcronym = ignoreAccentsAroundString(dataParts[dataParts.length-4]);
                String studentGroupName = removeBlanks(ignoreAccentsAroundString(dataParts[0]));
                StudentGroup studentGroup = new StudentGroup(studentGroupName);
                studentGroup.setYear(Integer.parseInt(studentGroupName.substring(0,1)));
                Teacher teacher = teacherMap.get(teacherAcronym);
                if (teacher != null) {
                    studentGroup.addClassTeacher(teacher);
                }
                studentGroups.add(studentGroup);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return studentGroups;
    }

    public static void updateStudentGroupsNumbers(List<StudentGroup> studentGroups, Map<String, Integer> studentNumbers){
            for (StudentGroup studentGroup: studentGroups){
                Integer val = studentNumbers.get(studentGroup.getGroupName());
                if (val !=  null){
                    studentGroup.setNumberOfStudents(val);
                }
            }
    }

    private static String ignoreAccentsAroundString(String word){
            if (word.length() >0) {
                return word.substring(1, word.length() - 1);
            }
            return "";
    }

    private static String removeBlanks(String word){
            String newWord = "";
            for (char ch: word.toCharArray()){
                if (ch != ' '){
                    newWord += ch;
                }
            }
        return newWord;
    }

    public static void main(String[] args){
        ExtractUntisText.fetchStudentGroups("/home/svs/IdeaProjects/school-timetabling/data/extern/KlassenSPC_101221.TXT", new ArrayList<>());
    }

}
