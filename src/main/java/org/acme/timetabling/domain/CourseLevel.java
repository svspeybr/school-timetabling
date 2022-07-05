package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.*;
import java.util.*;

@Entity
public class CourseLevel extends PanacheEntityBase {

    //PERSISTENT FIELDS
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "COURSELEVELID")
    private Long courseLevelId;

    @OneToMany(targetEntity = LessonTask.class, mappedBy = "courseLevel", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<LessonTask> lessonTaskList;


    //TRANSIENT FIELDS
    @Transient
    private Map<Integer, Integer> indexTable;

    @Transient
    private int maxGroupSize;
    @Transient
    private Set<StudentGroup> studentGroupSet;
    @Transient
    private List<PartitionOfStudentGroups> partitionTable;


    // **************************************************
    // CONSTRUCTORS
    // **************************************************
    public CourseLevel(){
    }

    public CourseLevel(List<LessonTask> lessonTaskList) {
        this.lessonTaskList = lessonTaskList;
        updateLessonTaskSet(lessonTaskList);
        updateCourseLevel();
    }

    // CONSTRUCTOR COMPONENT
    public void updateCourseLevel(){
/*        //CLEAN START --> REMOVE COURSELEVEL FROM LESSONTASK!!
        cleanLessonTaskSet(lessonTaskList);*/
        this.studentGroupSet = extractStudentGroupsFrom(this.lessonTaskList); //link courseLevel to lessonTask
        /*this.anchor = lessonTaskList.stream().flatMap(lessonTask -> lessonTask.getLessonsOfTaskList().stream()).max(Comparator.comparing(Lesson::getLessonId)).get();*/
        this.indexTable = createIndexTable();
        this.maxGroupSize = Math.max(28, Collections.max(this.studentGroupSet, Comparator.comparing(StudentGroup::getNumberOfStudents))
                .getNumberOfStudents());
        this.partitionTable = generateHashPartitionTableFrom();
        this.partitionTable.add(0, generateCurrentPartition());
    }



    //***********************************************************
    //GETTERS
    //***********************************************************

    // GET STUDENTS!!!!!
    public Set<StudentGroup> getStudentGroups(LessonTask lessonTask, Integer currentPartition){
        return this.partitionTable.get(currentPartition)
                .getStudentGroupsAt(this.indexTable.get(lessonTask.getTaskNumber()))
                .getContent();
    }

    public List<LessonTask> getLessonTasks(){
        return this.lessonTaskList;
    }

    public int getMaxGroupSize(){
        return maxGroupSize;
    }

    public Long getCourseLevelId() {
        return courseLevelId;
    }

    public List<PartitionOfStudentGroups> getPartitionTable() {
        return partitionTable;
    }

    public int getPartitionSize(){ //number of related lessonTasks
        return this.lessonTaskList.size();
    }


    public int numberOfPossiblePartitions(){ //number of related lessonTasks
        return this.partitionTable.size();
    }

    public Set<StudentGroup> getStudentGroupSet() {
        return this.studentGroupSet;
    }

    public List<LessonTask> getLessonTaskList(){return this.lessonTaskList;}

    //********************************************
    //SETTERS
    //*******************************************


    //***************************************************************
    // UPDATES
    //***************************************************************

    private void updateLessonTaskSet(List<LessonTask> lessonTasks){
        for (LessonTask lessonTask: lessonTasks){
            lessonTask.setCourseLevel(this);
        }
    }


    public void updateLessonTasks(List<LessonTask> lessonTasks, int i){
        PartitionOfStudentGroups partition = this.partitionTable.get(i);
        for (LessonTask lessonTask: lessonTasks){
            lessonTask.setStudentGroups(partition.getStudentGroupsAt(this.indexTable.get(lessonTask.getTaskNumber())).getContent());
        }
    }

    private void cleanLessonTaskSet(List<LessonTask> lessonTaskList){
        for(LessonTask lessonTask: lessonTaskList){
            lessonTask.setCourseLevel(null);
        }

    }

    //**************************************************
    // ADVANCED - CONSTRUCTOR HELPERS
    //**************************************************


    private Map<Integer, Integer> createIndexTable(){ //FROM TASKNUMBER TO POSITION/INDEX
        Map<Integer, Integer> indexTable = new HashMap<>(this.lessonTaskList.size());
        for (int index = 0; index <this.lessonTaskList.size(); index ++){
            indexTable.put(lessonTaskList.get(index).getTaskNumber(), index);
        }
        return  indexTable;
    }

    private List<PartitionOfStudentGroups> generateHashPartitionTableFrom(){
        List<StudentGroup> sortedStudentGroups = new ArrayList<>(this.studentGroupSet);
        sortedStudentGroups.sort(Comparator.comparing(StudentGroup::getNumberOfStudents).reversed());

        List<PartitionOfStudentGroups> partitionTab = new ArrayList<>();
        //Start with current partition
        PartitionOfStudentGroups partition = new PartitionOfStudentGroups();
        for (int i = 0; i< getPartitionSize(); i++){ //Initialize each task with ONE student-group of the largest size
            StudentGroups studentGroups = new StudentGroups();
            studentGroups.addStudentGroup(sortedStudentGroups.remove(0));
            partition.addStudentGroups(studentGroups);
        }
        partitionTab.add(partition);

    return generatorOfPartition(partitionTab, sortedStudentGroups);
    }

    private PartitionOfStudentGroups generateCurrentPartition(){
        PartitionOfStudentGroups partition = new PartitionOfStudentGroups();
        for (LessonTask lessonTask: this.lessonTaskList){
            StudentGroups studentGroups = new StudentGroups(lessonTask.getStudentGroups());
            partition.addStudentGroups(studentGroups);
        }
        return partition;
    }

    private Set<StudentGroup> extractStudentGroupsFrom(List<LessonTask> lessonTaskList){
        Set<StudentGroup> studentGroupSet = new HashSet<>();
        for (LessonTask lessonTask: lessonTaskList){
            studentGroupSet.addAll(lessonTask.getStudentGroups());
        }
        return studentGroupSet;
    }

    //GENERATE full list of all possible subdivision of students in the same courselevel
    private List<PartitionOfStudentGroups> generatorOfPartition(List<PartitionOfStudentGroups> partitionTable,
                                                                       List<StudentGroup> remainderList){
        if (remainderList.isEmpty()) {
            return partitionTable;
        }
        List<PartitionOfStudentGroups> extPartitionTable = new ArrayList<>();
        StudentGroup nextStudGroup = remainderList.remove(0);
        for (PartitionOfStudentGroups partition: partitionTable){
            for (int index = partition.getSize() - 1;
                 index > -1 &&
                         partition.getStudentGroupsAt(index).getNumberOfStudents()
                                 + nextStudGroup.getNumberOfStudents()
                                 <= getMaxGroupSize();
                    index--)
            {
                    PartitionOfStudentGroups newPartition = new PartitionOfStudentGroups(partition.getCopyOfContent());
                    newPartition.getStudentGroupsAt(index).addStudentGroup(nextStudGroup);
                    extPartitionTable.add(newPartition);
            }
        }
        return generatorOfPartition(extPartitionTable, remainderList);
    }

    private static class PartitionOfStudentGroups{
        List<StudentGroups> content;

        private PartitionOfStudentGroups(){
            this.content = new ArrayList<>();
        }
        private PartitionOfStudentGroups(List<StudentGroups> content){
            this.content = content;
        }

        public void addStudentGroups(StudentGroups studentGroups){
            content.add(studentGroups);
        }
        public int getSize(){
            return content.size();
        }
        public List<StudentGroups> getCopyOfContent(){
            List<StudentGroups> copy = new ArrayList<>();
            for (StudentGroups studentGroups:content){
                copy.add(new StudentGroups(new HashSet<>(studentGroups.getContent())));
            }
            return copy;
        }

        public StudentGroups getStudentGroupsAt(int index){
            return content.get(index);
        }
    }

    private static class StudentGroups{
        Set<StudentGroup> content;

        private StudentGroups(){
            this.content = new HashSet<>();
        }
        private StudentGroups(Set<StudentGroup> content){
            this.content =content;
        }

        private void addStudentGroup(StudentGroup studentGroup){
            content.add(studentGroup);
        }

        public Set<StudentGroup> getContent(){
            return content;
        }

        public int getNumberOfStudents(){
            int numberOfStudents =0;
            for (StudentGroup studentGroup: content){
                numberOfStudents += studentGroup.getNumberOfStudents();
            }
            return numberOfStudents;
        }



    }

}
