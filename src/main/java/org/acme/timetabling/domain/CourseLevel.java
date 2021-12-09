package org.acme.timetabling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.acme.timetabling.parser.XmlDomParser;

import javax.persistence.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
public class CourseLevel extends PanacheEntityBase {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "COURSELEVELID")
    private Long CourseLevelId;

    @OneToMany(targetEntity = LessonTask.class, mappedBy = "courseLevel", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<LessonTask> lessonTaskList;

    @Transient
    private int maxGroupSize;
    @Transient
    private Set<StudentGroup> studentGroupSet;
    @Transient
    private List<PartitionOfStudentGroups> partitionTable;

/*    public static void main(String[] args) {
        Function<String, List> giveList = XmlDomParser.main();
        List<LessonTask> lessonTaskList = giveList.apply("ta");
        List<LessonTask> historyTasks = lessonTaskList.stream().filter(lessonTask -> lessonTask.getSubject().equals("GE") &&
                new ArrayList<>(lessonTask.getStudentGroups()).get(0).getYear().equals(5)).collect(Collectors.toList());
        System.out.println(historyTasks.stream().map(lessonTask -> lessonTask.getStudentGroups().stream().map(
                StudentGroup::getGroupName).collect(Collectors.toList())).collect(Collectors.toList()));
        CourseLevel courseLevel = new CourseLevel(1L, new HashSet<>(historyTasks));
        List<PartitionOfStudentGroups> partitionTab = courseLevel.getPartitionTable();
        System.out.println(partitionTab.size());
        for (PartitionOfStudentGroups partition: partitionTab){
            System.out.println("***new partition***");
            for (StudentGroups studentGroups: partition.getCopyOfContent()){
                System.out.println("--studentGroups--");
                studentGroups.getContent().stream().forEach(studentGroup -> System.out.println(studentGroup.getGroupName()));
            }
        }
    }*/

    public CourseLevel(){
    }
//NO SINGLE ADDITION --> HASHTABLE CALCULATED ONLY ONCE FOR EACH CourseLEVEL?!!
/*    public CourseLevel(Long courseLevelId, LessonTask lessonTask, StudentGroup studentGroup) {
        this.CourseLevelId = courseLevelId;
        this.lessonTaskSet = new HashSet<>();
        lessonTaskSet.add(lessonTask);
        this.studentGroupSet = new HashSet<>();
        this.studentGroupSet.add(studentGroup);
        this.partitionTable = generateHashPartitionTableFrom();
    }

        public void addStudentGroup(StudentGroup studentGroup){
        this.studentGroupSet.add(studentGroup);
    }

    public void addLessonTask(LessonTask lessonTask){
        this.lessonTaskSet.add(lessonTask);
    }
    */

    public CourseLevel(List<LessonTask> lessonTaskList) {
        this.lessonTaskList = lessonTaskList;
        updateLessonTaskSet(lessonTaskList);
    }

    public void updateCourseLevel(){
        this.studentGroupSet = extractStudentGroupsFrom(lessonTaskList);
        this.maxGroupSize = Math.max(28, Collections.max(studentGroupSet, Comparator.comparing(StudentGroup::getNumberOfStudents))
                .getNumberOfStudents());
        this.partitionTable = generateHashPartitionTableFrom();
        this.partitionTable.add(0, generateCurrentPartition());
    }

    public Set<StudentGroup> getStudentGroups(Integer partitionNumber, LessonTask lessonTask){
        return this.partitionTable.get(partitionNumber)
                .getStudentGroupsAt(this.lessonTaskList.indexOf(lessonTask))
                .getContent();
    }
    private void updateLessonTaskSet(List<LessonTask> lessonTasks){
        for (LessonTask lessonTask: lessonTasks){
            lessonTask.setCourseLevel(this);
        }
    }

    public int getMaxGroupSize(){
        return maxGroupSize;
    }

    public Long getCourseLevelId() {
        return CourseLevelId;
    }

    public void updateLessonTasks(int i){
        PartitionOfStudentGroups partition = partitionTable.get(i);
        int index =0;
        for (LessonTask lessonTask: lessonTaskList){
            lessonTask.setStudentGroups(partition.getStudentGroupsAt(index).getContent());
            index ++;
        }
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
        return studentGroupSet;
    }
    public List<LessonTask> getLessonTaskList(){return this.lessonTaskList;}

    private List<PartitionOfStudentGroups> generateHashPartitionTableFrom(){
        List<StudentGroup> sortedStudentGroups = new ArrayList<>(studentGroupSet);
        sortedStudentGroups.sort(Comparator.comparing(StudentGroup::getNumberOfStudents).reversed());

        List<PartitionOfStudentGroups> partitionTab = new ArrayList<>();
        //Start with current partition
        PartitionOfStudentGroups partition = new PartitionOfStudentGroups();
        for (int i = 0; i< getPartitionSize(); i++){ //Initialize each task with ONE student-group of the largest size
            StudentGroups studentGroups= new StudentGroups();
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
