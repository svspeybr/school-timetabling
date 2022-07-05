package org.acme.timetabling.rest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import org.acme.timetabling.domain.*;
import org.acme.timetabling.persistence.CopyTableObjectsToPersist;
import org.acme.timetabling.persistence.XmlFileIO;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;

import java.io.File;
import java.nio.charset.CoderResult;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Path("/timeTable")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class TimeTableResource {

    private final FileServer fs = new FileServer(); //only needed for PATH where files are saved - redundend !?
    private final String PATHNAME = fs.getPathNameFiles();

    private static final String FILENAME = "/home/svs/IdeaProjects/school-timetabling/src/main/java/org/acme/timetabling/solver/timetablingSolverConfig.xml";
    private TableConfigurations tableConfigurations = new TableConfigurations();
    private final XmlFileIO xmlFileIO = new XmlFileIO();



/*    @Inject
    LessonRepository lessonRepository;*/

    /* @Inject
    SolverManager<TimeTable, Long> solverManager;*/
    File configSolverFile = new File(FILENAME);
    SolverConfig solverConfig = SolverConfig.createFromXmlFile(configSolverFile);
    SolverManager<TimeTable, Long> solverManager = SolverManager.create(solverConfig, new SolverManagerConfig());

    @Inject
    ScoreManager<TimeTable, HardSoftScore> scoreManager;



    // To try, open http://localhost:8080/timeTable
    @GET
    public TimeTable getTimeTable() {
        // Get the solver status before loading the solution
        Long table_id = tableConfigurations.getTableId();

        SolverStatus solverStatus = solverManager.getSolverStatus(table_id);
        TimeTable timeTable = findById(table_id, null);
        scoreManager.updateScore(timeTable);
        // to avoid the race condition that the solver terminates between them
        timeTable.setSolverStatus(solverStatus);
       return timeTable;
    }

    @POST
    @Path("changeTableTo/{tableName}")
    public void changeTableTo(@PathParam("tableName") String tableName){
        emptyTable();
        File tableFile = new File(PATHNAME + "/" + tableName + ".xml");
        TimeTable timeTable = xmlFileIO.readDataFromFile(tableFile);
        xmlFileIO.saveDataToFile(); //SHOULD NOT BE NECESSARY...
        tableConfigurations = new TableConfigurations();
        persistTable(timeTable);
    }

    @GET
    @Path("/summary")
    public List<String> getSummary() {
        Long table_id = tableConfigurations.getTableId();

        TimeTable timeTable = findById(table_id, null);

        Map<String, ConstraintMatchTotal<HardSoftScore>> map = scoreManager.explainScore(timeTable).getConstraintMatchTotalMap();
        List<String> constraintsValues = new ArrayList<>(map.keySet().size() * 2);

        for (String domEl: map.keySet()){
            constraintsValues.add(domEl);
            constraintsValues.add(map.get(domEl).getScore().toString());
        }

        return constraintsValues;
    }

    @POST
    @Path("/solve")
    public void solve() {
        //ADAPT?
        int numberOfCopies = 5;
        List<CourseLevel> courseLevelList = CourseLevel.listAll();
        courseLevelList.forEach(CourseLevel::updateCourseLevel);
        tableConfigurations.setTableConfigurations(numberOfCopies, courseLevelList);

        for (long tabId = 1L; tabId <= (long) numberOfCopies; tabId++){
/*            for (CourseLevel cl: tableConfigurations.getCourseLevelList()){
                System.out.println(cl.getCurrentPartition());
            }*/
            long finalTabId = tabId;
            solverManager.solveAndListen(tabId,
                    id -> findById(id, courseLevelList),
                    tb -> save(tb, finalTabId));
        }
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        //ADAPT
        int numberOfCopies = 5;

        for (long tabId = 1L; tabId <= (long) numberOfCopies; tabId++){
            solverManager.terminateEarly(tabId);
        }
        //************************************************************************************
        // TOD0  Create errors?? --> if some copies are not yet been started to solve ????
        //************************************************************************************
    }




    public TimeTable findById(Long id, List<CourseLevel> courseLevels) {

        List<CourseLevel> courseLevelList;
        Map<Long, Integer> studentGroupConfiguration;

        if (courseLevels != null) {
            courseLevelList = courseLevels; //YET UPDATED -- IN SOLVE -- 2X times also in timetableResource
            studentGroupConfiguration = this.tableConfigurations.getCourseLevelToPartitionFor(id);
        } else {
            courseLevelList = CourseLevel.listAll();
            studentGroupConfiguration = null;
        }
/*
        if (viaTableConfigurations) {
            this.tableConfigurations.updateCourseLevelsFor(id);
            courseLevelList = new ArrayList<>(this.tableConfigurations.getCourseLevelList());
        } else {
            courseLevelList = CourseLevel.listAll();
            courseLevelList.forEach(CourseLevel::updateCourseLevel);
        }*/
/*        System.out.println("test-out-start");
        for (CourseLevel cl: courseLevelList){
                System.out.println(cl.getCurrentPartition());
            }
        System.out.println("test-out-end");*/

        return new TimeTable(
                studentGroupConfiguration,
                Timeslot.listAll(Sort.by("position")),
                Room.listAll(),
                Lesson.listAll(),
                LessonTask.listAll(),
                courseLevelList, // YET UPDATED
                StudentGroup.listAll(),
                Teacher.listAll(),
                Preference.listAll(),
                SubjectCollection.listAll(),
                ThemeCollection.listAll()
        );
    }

    public static void persistTable(TimeTable timeTable){
        //new DEFAULTSETTINGS
        final CopyTableObjectsToPersist copyManager = new CopyTableObjectsToPersist();
        DefaultSettings ds = new DefaultSettings(1L);
        int length;
        try {
            /*
            * FETCHING (COPIES)
            */

            //Fetch Timeslots + map
            List<Timeslot> timeslotList = copyManager.copyTimeslotsToPersist(timeTable.getTimeslotList(), ds);
            Map<Integer, Timeslot> timeslotMap = new HashMap<>(timeslotList.size());
            for (Timeslot ts: timeslotList){
                timeslotMap.put(ts.getPosition(), ts);
            }

            //Fetch Teachers + map
            List<Teacher> oldTeachersList = timeTable.getTeacherList(); // This is again persistable???
            List<Teacher> teachersList = copyManager.copyTeachersToPersist(oldTeachersList, ds);

            Map<String, Teacher> teacherMap = new HashMap<>(teachersList.size());
            for (Teacher te: teachersList){
                teacherMap.put(te.getAcronym(), te);
            }

            //Fetch Students +map
            List<StudentGroup> sgList = copyManager.copyStudentsToPersist(timeTable.getStudentGroupList(), teacherMap);
            Map<String, StudentGroup> sgMap = new HashMap<>(sgList.size());
            for (StudentGroup sg: sgList){
                sgMap.put(sg.getGroupName(), sg);
            }

            //Fetch Rooms + map
            List<Room> roomList = copyManager.copyRoomsToPersist(timeTable.getRoomList());
            Map<String, Room> roomMap = new HashMap<>(roomList.size());
            for (Room room: roomList){
                roomMap.put(room.getName(), room);
            }

            //Fetch Preferences
            List<Preference> pfList = copyManager.copyPreferences(timeTable.getPreferenceList(), timeslotMap, teacherMap);

            //Fetch SubjectCollection
            List<SubjectCollection> subList = copyManager.copySubjectCollectionToPersist(timeTable.getSubjectCollectionList());

            //Fetch Lessontasks + map
            List<LessonTask> ltList = copyManager.copyLessonTaskToPersist(timeTable.getLessonTaskList(), sgMap, teacherMap);
            Map<Integer, LessonTask> ltMap = new HashMap<>(ltList.size());
            for (LessonTask lt: ltList){
                ltMap.put(lt.getTaskNumber(), lt);
            }

            //Fetch Lessons
            List<Lesson> lessonList = timeTable.getLessonList();
            //LessonList is updated by 'copyLessonToPersist' + return map from old to new key
            List<Long> oldIndices = copyManager.copyLessonToPersist(lessonList,
                    ltMap,
                    timeslotMap,
                    roomMap);

            //ThemeCollection is collected after Lesson is persisted to get id

            //Fetch CourseLevels
            List<CourseLevel> courseLevelList = copyManager.copyCourseLevelToPersist(timeTable.getCourseLevelList(), ltMap);

            /*
            -----------
            PERSIST (+1 fetch)
            -----------
             */

            //Persist DS
            DefaultSettings.persist(ds);

            //Persist Timeslots
            Timeslot.persist(timeslotList);

            //Persist Students
            StudentGroup.persist(sgList);

            //Persist Rooms
            Room.persist(roomList);

            //Persist Teachers
            Teacher.persist(teachersList);

            //Persist Preferences
            Preference.persist(pfList);

            //Persist SubjectCollection
            SubjectCollection.persist(subList);

            //Persist LessonTask
            LessonTask.persist(ltList);

            //Persist Lesson
            Lesson.persist(lessonList);

            // Generate map from oldIndices to new;
            length = lessonList.size();
            Map<Long, Long> fromOldIdToNew = new HashMap<>(length);
            for (int i = 0; i < length; i++){
                fromOldIdToNew.put(oldIndices.get(i), lessonList.get(i).getLessonId());
            }

            // Fetch ThemeCollection
            List<ThemeCollection> tcList = copyManager.copyThemeCollectionToPersist(timeTable.getThemeCollectionList(),
                                                                                    fromOldIdToNew,
                                                                                    timeslotMap);
            //Persist ThemeCollection
            ThemeCollection.persist(tcList);

            //Persist LessonTask
            CourseLevel.persist(courseLevelList);

            //UPDATE teachers
            for (Teacher te: teachersList){
                te.updateDependents(ds);
            }

        } catch (Exception e){
            System.out.println(e);
        }
    }

    @POST
    @Path("resetTableDatabase")
    public void resetTable(){
        emptyTable();
        DefaultSettings ds = new DefaultSettings(1L);
        ds.persist();
        this.fs.saveFileInXmlForm("NieuwRooster");
    }

    protected void emptyTable(){
        deleteEach(Teacher.listAll());
        deleteEach(Preference.listAll());
        deleteEach(CourseLevel.listAll());
        deleteEach(ThemeCollection.listAll());
        deleteEach(SubjectCollection.listAll());
        deleteEach(Lesson.listAll());
        deleteEach(LessonTask.listAll());
        StudentGroup.deleteAll();
        Timeslot.deleteAll();
        Room.deleteAll();
        DefaultSettings.findById(1L).delete();
    }

    private void deleteEach(List<PanacheEntityBase> list){
        int length = list.size();
        for (int i = 0; i < length; i++){
            list.get(i).delete();
        }
    }

    protected void save(TimeTable timeTable, Long tableId) {
        /*lessonRepository.deleteAllGroups();*/
        /*Set<String> valuesForQueryString = new HashSet<>();*/
        System.out.println("BEFORE");
        HardSoftScore score = this.scoreManager.updateScore(timeTable);
        this.tableConfigurations.setScoreKeeper(tableId, score);
        System.out.println(score);
        if (this.tableConfigurations.setBestScoreWithId(score, tableId)) {
            System.out.println("AFTER");

            List<CourseLevel> courseLevelList = timeTable.getCourseLevelList();
            for (LessonAssignment lessonAssignment: timeTable.getLessonAssignmentList()) {
                Lesson attachedLesson = Lesson.findById(lessonAssignment.getLessonId());
                attachedLesson.setTimeslot(lessonAssignment.getTimeslot());
                attachedLesson.setRoom(lessonAssignment.getRoom());
            }

            Map<Long, Integer> studentGroupConfiguration = tableConfigurations.getCourseLevelToPartitionFor(tableId);
            for (CourseLevel courseLevel:courseLevelList) {

                Integer partitionNumber = studentGroupConfiguration.get(courseLevel.getCourseLevelId());
                List<LessonTask> attachedLessonTasks = LessonTask.list("TASKNUMBER in ?1", courseLevel.getLessonTasks());
                courseLevel.updateLessonTasks(attachedLessonTasks, partitionNumber);

            }

        }

    }

    @POST
    @Path("/saveToNewFile/{fileName}/atDir/{dirPath}")
    public void saveToNewFile(@PathParam("fileName") String fileName,
                              @PathParam("dirPath") String dirPath){
        xmlFileIO.setOutputDir(dirPath);
        xmlFileIO.setFileName(fileName);
        xmlFileIO.saveDataToFile();
    }

    @GET
    @Path("/overwriteFile/{fileName}/atDir/{dirPath}")
    public Boolean overwriteFile(){
        if (xmlFileIO.getFileName() == null){
            return false;
        }
        xmlFileIO.saveDataToFile();
        return true;
    }


    //*********************************************
    //ADVANCED
    //**********************************************

    private class TableConfigurations {

        private Integer numberOfCopies;

        private HardSoftScore bestScore;
        private Long preferred_tableId = 1L;

        private Map<Long, StudentGroupPartition> studentGroupConfigurations; // FROM TABLEID TO CORRESPONDING PARTITION
        private Map<Long, HardSoftScore> scoreKeeper;
        private List<CourseLevel> courseLevelList;


        //***************************************
        //ADV - CONSTRUCTOR
        //***************************************
        public TableConfigurations(){

        }

        //CONSTRUCTOR COMPONENT

        public void setTableConfigurations(Integer numberOfCopies, List<CourseLevel> courseLevelList){
            this.numberOfCopies = numberOfCopies; // number of partitions used in scheduling == number of tableIds
            this.studentGroupConfigurations = new HashMap<>(numberOfCopies);
            this.scoreKeeper = new HashMap<>(numberOfCopies);
            this.courseLevelList = new ArrayList<>(courseLevelList);
            setStudentGroupConfigurations(this.courseLevelList);
        }

        public void setScoreKeeper(Long tableId, HardSoftScore score){
            HardSoftScore oldScore = this.scoreKeeper.get(tableId);
            if (oldScore !=  null && greaterThanScore(score, oldScore)){
                this.scoreKeeper.put(tableId, score);
            }
        }

        private void setStudentGroupConfigurations(List<CourseLevel> courseLevelList){
           if (this.studentGroupConfigurations.size() == 0){
               generateStudentGroupConfigurations(courseLevelList);
               System.out.println("NEW studentGROUPCONFIGURATION");
           }
        }

        private void generateStudentGroupConfigurations(List<CourseLevel> courseLevelList){
            
            //necessary???? -> can be ignored the first iteration when updating the courseLevels.
            if (this.numberOfCopies > 0){
                Map<Long, Integer> defaultConfig = new HashMap<>(courseLevelList.size());
                for (CourseLevel cl: courseLevelList){/*
        }*/
                    defaultConfig.put(cl.getCourseLevelId(), 0);
                }
                this.studentGroupConfigurations.put(1L, new StudentGroupPartition(defaultConfig)); // courseLevelSettings as in database
            }
            
            if (this.numberOfCopies > 1) {
                for (int i = 1; i < this.numberOfCopies; i++){
                  this.studentGroupConfigurations.put( (long) i + 1, new StudentGroupPartition(courseLevelList));
                }
            }
        }

        //***********************************************$
        //ADV - UPDATE
        //*********************************************

/*        public void updateCourseLevelsFor(Long table_id){
            System.out.println("up - start");
            for (CourseLevel cl: courseLevelList){
                cl.setCurrentPartition(getCourseLevelToPartitionAtTheMoment(table_id).get(cl.getCourseLevelId()));
                System.out.println(cl.getCurrentPartition());
            }
            System.out.println("up -end");
        }*/

        //************************************************
        //ADV - GETTERS
        //********************************************

        public Map<Long, Integer> getCourseLevelToPartitionFor(Long tableId) {
                return this.studentGroupConfigurations.get(tableId).getCourseLevelToPartition();
        }

        public List<CourseLevel> getCourseLevelList() {
            return courseLevelList;
        }

        public Integer getPreferred_copy(){
            return preferred_tableId.intValue() - 1;
        }

        public Long getTableId() {
            return preferred_tableId;
        }

        //********************************************************
        //ADV - SETTERS
        //******************************************************

        public boolean setBestScoreWithId(HardSoftScore bestScore, Long tableId) {
            if (this.bestScore == null ||
                   greaterThanScore(bestScore, this.bestScore)) {
                this.bestScore = bestScore;
                this.preferred_tableId = tableId;
                return true;
                //this.preferred_copy = copyAtTheMoment;
            }
            return false;
        }

/*        public void setCopyAtTheMoment(Integer copyAtTheMoment) {
            this.copyAtTheMoment = copyAtTheMoment;
        }*/
    }


    //**********************************
    //ADV -HELPER
    //*********************************

    public boolean greaterThanScore(HardSoftScore hs1, HardSoftScore hs2){
        return hs1.getHardScore() > hs2.getHardScore() ||
                (hs1.getHardScore() == hs2.getHardScore() && hs1.getSoftScore() > hs2.getSoftScore());
    }

    private static class StudentGroupPartition {

        private final Map<Long, Integer> courseLevelToPartition;

        public StudentGroupPartition(Map<Long, Integer> courseLevelToPartition){
            this.courseLevelToPartition = courseLevelToPartition;
        };
        
        
        public StudentGroupPartition(List<CourseLevel> courseLevelList){
            this.courseLevelToPartition = generateRandomPartitioning(courseLevelList);
        }

        private HashMap<Long, Integer> generateRandomPartitioning(List<CourseLevel> courseLevelList){
            HashMap<Long, Integer> map = new HashMap<>(courseLevelList.size());
            Random random = new Random();
            for (CourseLevel cl: courseLevelList){
                map.put(cl.getCourseLevelId(), random.nextInt(cl.numberOfPossiblePartitions()));
            }
            return map;
        }

        public Map<Long, Integer> getCourseLevelToPartition() {
            return courseLevelToPartition;
        }
    }

}