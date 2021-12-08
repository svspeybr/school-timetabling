var autoRefreshIntervalId = null;

function refreshTimeTable() {
    $.getJSON("/timeTable", function(timeTable) {
        refreshSolvingButtons(timeTable.solverStatus != null && timeTable.solverStatus !== "NOT_SOLVING");
        $("#score").text("Score: " + (timeTable.score == null ? "?" : timeTable.score));

        const timeTableByRoom = $("#timeTableByRoom");
        timeTableByRoom.children().remove();
        const timeTableByTeacher = $("#timeTableByTeacher");
        timeTableByTeacher.children().remove();
        const timeTableByStudentGroup = $("#timeTableByStudentGroup");
        timeTableByStudentGroup.children().remove();
        const unassignedLessons = $("#unassignedLessons");
        unassignedLessons.children().remove();
        /*Preferences*/
        const timeslotPreferences = $("#preference_timeslot");
        timeslotPreferences.children().remove();
        const teachersForNewLesson = $("#lesson_teachers");
        teachersForNewLesson.children().remove();
        const studentGroupsForNewLesson = $("#lesson_studentGroups");
        studentGroupsForNewLesson.children().remove();

        /*
        ---------------------------------------
        HEADING TABLE
        ---------------------------------------
        */
        const theadByRoom = $("<thead>").appendTo(timeTableByRoom);
        const headerRowByRoom = $("<tr>").appendTo(theadByRoom);
        headerRowByRoom.append($("<th>Timeslot</th>"));
        $.each(timeTable.roomList, (index, room) => {
            headerRowByRoom
                .append($("<th/>")
                    .append($("<span/>").text(room.name))
                    .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                        .append($(`<small class="fas fa-trash"/>`)).click(() => deleteRoom(room))));
        });

        headerRowByRoom
            .append($("<th/>")
                .append($("<span/>").text("Unassigned")));

        const theadByTeacher = $("<thead>").appendTo(timeTableByTeacher);
        const headerRowByTeacher = $("<tr>").appendTo(theadByTeacher);
        headerRowByTeacher.append($("<th>Timeslot</th>"));
        const teacherList = [...new Set(timeTable.lessonTaskList.flatMap(
                                                            lessontask => lessontask.taughtBy.map(
                                                            teacher => teacher.acronym)))].sort();
        $.each(teacherList, (index, teacher) => {
            headerRowByTeacher
                .append($("<th/>")
                    .append($("<span/>").text(teacher)));
        });
        const theadByStudentGroup = $("<thead>").appendTo(timeTableByStudentGroup);
        const headerRowByStudentGroup = $("<tr>").appendTo(theadByStudentGroup);
        headerRowByStudentGroup.append($("<th>Timeslot</th>"));
        const studentGroupList = [...new Set(timeTable.lessonTaskList.flatMap(
                                                                    lessontask => lessontask.studentGroups.map(
                                                                    studentGroup => studentGroup.groupName)))].sort();
        $.each(studentGroupList, (index, studentGroup) => {
            headerRowByStudentGroup
                .append($("<th/>")
                    .append($("<span/>").text(studentGroup)));
        });


        /*
        ---------------------------------------
        GENERATING ROW (SLOTS)
        ---------------------------------------
        */
        const tbodyByRoom = $("<tbody>").appendTo(timeTableByRoom);
        const tbodyByTeacher = $("<tbody>").appendTo(timeTableByTeacher);
        const tbodyByStudentGroup = $("<tbody>").appendTo(timeTableByStudentGroup);
        var previousDay = "MONDAY"
        $.each(timeTable.timeslotList, (index, timeslot) => {

                    if (previousDay != timeslot.dayOfWeek) {
                        previousDay = timeslot.dayOfWeek;
                        const rowByRoom = $(`<tr class ="table-success">`).appendTo(tbodyByRoom);
                        rowByRoom
                                .append($("<th />"));
                        $.each(timeTable.roomList, (index, room) => {
                                rowByRoom.append($("<td/>"));
                        });
                        rowByRoom
                                .append($("<td/>"));

                        const rowByTeacher = $(`<tr class = "table-success">`).appendTo(tbodyByTeacher);
                        rowByTeacher
                                .append($("<th />"));
                        $.each(teacherList, (index, teacher) => {
                                                rowByTeacher.append($("<td/>"));
                        });
                        const rowByStudentGroup = $(`<tr class = "table-success">` ).appendTo(tbodyByStudentGroup);
                        rowByStudentGroup
                                .append($("<th />"));
                        $.each(studentGroupList, (index, studentGroup) => {
                                                    rowByStudentGroup.append($("<td/>"));
                        });
                    }

                const rowByRoom = $("<tr>").appendTo(tbodyByRoom);
                const lastResortTimeslotButton  = $(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`);
                if (timeslot.lastResort) {
                    lastResortTimeslotButton
                                .append(`<small class = "fas fa-exclamation" />`).click(() =>changeLastResort(timeslot));
                } else {
                    lastResortTimeslotButton
                                .append(`<small class = "far fa-clock" />`)
                                .click(() =>changeLastResort(timeslot));

                }
                rowByRoom
                    .append($(`<th class="align-middle"/>`)
                        .append($("<span/>").text(`
                        ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
                        ${moment(timeslot.startTime, "HH:mm:ss").format("HH:mm")}
                        -
                        ${moment(timeslot.endTime, "HH:mm:ss").format("HH:mm")}`))
                            .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                                .append($(`<small class="fas fa-trash"/>`)).click(() => deleteTimeslot(timeslot)))
                            .append(lastResortTimeslotButton));

                $.each(timeTable.roomList, (index, room) => {
                    rowByRoom.append($("<td/>")
                                            .prop("id", `ti-${timeslot.timeslotId}-ro-${room.roomId}`)
                                            .prop("class", `droppable`));
                });
                rowByRoom
                    .append($("<td/>")
                         .prop("id", `ti-${timeslot.timeslotId}-ro-0`)
                        .prop("class", `droppable`));


                const rowByTeacher = $("<tr>").appendTo(tbodyByTeacher);
                rowByTeacher
                    .append($(`<th class="align-middle"/>`)
                        .append($("<span/>").text(`
                        ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
                        ${moment(timeslot.startTime, "HH:mm:ss").format("HH:mm")}
                        -
                        ${moment(timeslot.endTime, "HH:mm:ss").format("HH:mm")}
                    `)));
                    $.each(teacherList, (index, teacher) => {
                        rowByTeacher.append($("<td/>")
                                                .prop("id", `ti-${timeslot.timeslotId}-te-${teacher}`)
                                                .prop("class", `droppable`)
                                                );
                    });

                    const rowByStudentGroup = $("<tr>").appendTo(tbodyByStudentGroup);
                    rowByStudentGroup
                               .append($(`<th class="align-middle"/>`)
                               .append($("<span/>").text(`
                                    ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
                                    ${moment(timeslot.startTime, "HH:mm:ss").format("HH:mm")}
                                     -
                                    ${moment(timeslot.endTime, "HH:mm:ss").format("HH:mm")}
                               `)));

                    $.each(studentGroupList, (index, studentGroup) => {
                               rowByStudentGroup.append($("<td/>")
                                                    .prop("id", `ti-${timeslot.timeslotId}-st-${studentGroup}`)
                                                    .prop("class", `droppable`));
                    });


        /*
        ---------------------------------------
        Preferences enlisting of timeslots
        ---------------------------------------
        */
        timeslotPreferences
               .append($(`<option value= ${timeslot.timeslotId}>`).text(`
               ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
               ${moment(timeslot.startTime, "HH:mm:ss").format("HH:mm")}
               -
               ${moment(timeslot.endTime, "HH:mm:ss").format("HH:mm")}`)
               .append($("<option/>")));

        });

        /***
        ---------------------------------------
        GENERATING CARDS
        ---------------------------------------
        ***/

/*         var count =0;
         $.each(timeTable.lessonTaskList, (index, lessonTask) => {
            count ++;
            if (count < 40 ) {
                const studentGroupsOfLessonTask = [...new Set(lessonTask.studentGroups)];
                const teachersOfLessonTask = [...new Set(lessonTask.taughtBy.map(teacher => teacher.acronym))];
                const lessonsOfLessonTask = [...new Set(lessonTask.lessonsOfTaskList)];
*//*                const lessonsLi = [...new Set(lessonTask.lessonsOfTaskList.map(lesson => lesson.subject))];
                alert(lessonLi);*//*
                alert(lessonsOfLessonTask.length);
            }
         });
         alert(count);*/

        $.each(timeTable.lessonTaskList, (index, lessonTask) => {
            var studentGroupsOfLessonTask = [...new Set(lessonTask.studentGroups)];
            var teachersOfLessonTask = [...new Set(lessonTask.taughtBy)];
            var lessonsOfLessonTask = [...new Set(lessonTask.lessonsOfTaskList)];
            const color = pickColor(teachersOfLessonTask[0].acronym);
            const lessonElementWithoutDelete = $(`<div class="card draggable" style="background-color: ${color}; width: 350px"/>`);
            const cardTextTeacher = $(`<h5 class="card-title ml-2 mb-1"/>`);
            $.each(teachersOfLessonTask, (index, teacher) => {
                cardTextTeacher
                    .append(`${teacher.acronym} `);
            });
            lessonElementWithoutDelete
                        .append(cardTextTeacher);
            lessonElementWithoutDelete
                        .append($(`<div class="card-body p-2"/>`)
                        .append($(`<h6 class="card-text ml-2 mb-1"/>`).append($(`<em/>`).text(lessonTask.subject))
                        .append($(`<p class="ml-2 mt-1 card-text text-muted align-bottom float-right"/>`).text(lessonTask.taskNumber))));
            const cardTextStudentGroup = $(`<p class="card-text ml-2 mb-1"/>`).append($(`<em/>`).text("for "));
            $.each(studentGroupsOfLessonTask, (index, studentGroup) => {
                cardTextStudentGroup
                      .append($(`<em/>`).text(`${studentGroup.groupName} `));
            });
            lessonElementWithoutDelete
                            .append(cardTextStudentGroup);
            $.each(lessonsOfLessonTask, (index, lesson) => {
                const lessonElement = lessonElementWithoutDelete.clone();
                lessonElement.find(".card-body").prepend(
                    $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right"/>`)
                    .append($(`<small class="fas fa-trash"/>`)).click(() => deleteLesson(lesson))
                );
                //PINNING OPTION TO CARD
                if (lesson.pinned == true) {
                    lessonElement.find(".card-body").prepend(
                          $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right"/>`)
                        .append(`<small class = "fas fa-lock" />`).click(() =>changePinLesson(lesson))
                    );
                } else {
                     lessonElement.find(".card-body").prepend(
                       $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right"/>`)
                       .append(`<small class = "fas fa-unlock" />`).click(() =>changePinLesson(lesson))
                     );
                }

                //COUPLING OPTION TO CARD

                if (lessonTask.coupled == true) {
                     lessonElement.find(".card-body").prepend(
                          $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right" data-toggle="modal" data-target="#lessonBlockDialog"/>`)
                          .append(`<span class = "fas fa-link" />`).click(() => fetchRelatedLessonBlocks(lessonTask))
                    );
                }
                else {
                     lessonElement.find(".card-body").prepend(
                           $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right" data-toggle="modal" data-target="#lessonBlockDialog"/>`)
                           .append(`<small class = "fas fa-unlink" />`).click(() => fetchRelatedLessonBlocks(lessonTask))
                     );
                }

                //PLACING CARDS

                if (lesson.timeslot == null ) {
                    unassignedLessons.append(lessonElement.prop('id', `le-${lesson.lessonId}-un-as`));
                } else {
                    $.each(teachersOfLessonTask, (index, teacher) => {
                        $(`#ti-${lesson.timeslot.timeslotId}-te-${teacher.acronym}`).append(lessonElementWithoutDelete.clone().prop('id',`le-${lesson.lessonId}-te-${teacher.acronym}`));
                    });
                    $.each(studentGroupsOfLessonTask, (index, studentGroup) => {
                    $(`#ti-${lesson.timeslot.timeslotId}-st-${studentGroup.groupName}`).append(lessonElementWithoutDelete.clone().prop('id',`le-${lesson.lessonId}-st-${studentGroup.groupName}`));
                    });
                    if (lesson.room == null) {
                        $(`#ti-${lesson.timeslot.timeslotId}-ro-0`).append(lessonElement.prop('id', `le-${lesson.lessonId}-ro-0`));
                    } else {
                        $(`#ti-${lesson.timeslot.timeslotId}-ro-${lesson.room.roomId}`).append(lessonElement.prop('id', `le-${lesson.lessonId}-ro-${lesson.room.roomId}`));
                    }
                }
            });
        });
        /*
        ---------------------------------------
        ADDING NEW LESSON OPTIONS
        ---------------------------------------
        */
        $.each(timeTable.studentGroupList, (index,studentGroup) => {
            studentGroupsForNewLesson
                           .append($(`<option value= ${studentGroup.groupName}>`).text(`${studentGroup.groupName}`)
                           .append($("<option/>")));
        });

        $.each(timeTable.teacherList, (index,teacher) => {
            teachersForNewLesson
                           .append($(`<option value= ${teacher.acronym}>`).text(`${teacher.acronym}`)
                           .append($("<option/>")));
        });


            /*
            ---------------------------------------
            DRAGGING CARDS SETTINGS
            ---------------------------------------
            */

                $(".draggable").draggable({
                    helper: "clone",
                    opacity: 0.5,
                    start: function(event,ui){
                        $(this).hide();
                        ui.helper.css("width", "300px");
                        ui.helper.css("height", "120px")},
                    stop: function(event, ui){
                            $(this).show();

                    }
                }
                );
                $(".droppable").droppable({
                hoverClass: "ui-state-active",
                drop: function(event, ui){

                    var draggedId = ui.draggable.attr('id');
                    var droppedId = $(this).attr('id');
                    var childDropId = $(this).find(".card").attr('id');
                    //CONVERT -> extract First and second ID
                    var draggedIdVals = cardIdConvertor(draggedId, "-");
                    var dragLessonId = getFirstTagId(draggedIdVals);
                    var dragOtherId = getOtherCardTagId(draggedIdVals);
                    //Drag to empty slot OR TO DO: UNASSIGNED LESSON
                    var droppedIdVals;
                    var notIdentical = false;
                    if (childDropId == null) {
                        droppedIdVals = cardIdConvertor(droppedId, "-");
                    } else{
                        droppedIdVals = cardIdConvertor(childDropId, "-");
                    }
                    var dropOtherTag = getOtherCardTag(droppedIdVals);
                    var dropOtherId = getOtherCardTagId(droppedIdVals);
                    var dropFirstId = getFirstTagId(droppedIdVals);
                    var dropFirstTag = getFirstTag(droppedIdVals);
                    if (dropOtherTag == "ro" || dragOtherId == dropOtherId){
                        $.post("/lessons/changeTiRoTe/dragLessonId/" + dragLessonId +"/"+dropOtherTag +"/"+ dropOtherId +"/"+ dropFirstTag +"/"+dropFirstId, JSON.stringify({
                                                                      }), function() {
                                                                           refreshTimeTable();
                                                                      }).fail(function(xhr, ajaxOptions, thrownError) {
                                                                          showError("Updating lesson failed.", xhr);
                                                                      });

                    } else {
                    var dragOtherTag =getOtherCardTag(draggedIdVals);
                    if (dragOtherTag == "un") {
                        $.get("/lessons/"+ dragLessonId, function(lesson) {
                        var canChange = false;
                        if (dropOtherTag == "te") {
                            const teachOfLesson = [...new Set(lesson.taughtBy)];
                            $.each(teachOfLesson, (index, teacher) => {
                                if (teacher.acronym == dropOtherId) {
                                    canChange = true;
                                }
                            });
                        }
                        if (dropOtherTag == "st") {
                            const studGroups = [...new Set(lesson.studentGroups)];
                            $.each(studGroups, (index, studentGroup) => {
                                   if (studentGroup.groupName == dropOtherId) {
                                        canChange = true;
                                   }
                            });

                        }

                        if (canChange) {
                        $.post("/lessons/assignTimeSlot/" + dropFirstTag +"/" + dropFirstId + "/lessonId/" + dragLessonId, JSON.stringify({
                                        }), function() {
                                             refreshTimeTable();
                                        }).fail(function(xhr, ajaxOptions, thrownError) {
                                                showError("Updating lesson failed.", xhr);
                                        });
                        }
                        }).fail(function(xhr, ajaxOptions, thrownError) {
                                      showError("Lesson not found.", xhr);
                        });

                    }

                    }

                }});

        });

}


function convertToId(str) {
    // Base64 encoding without padding to avoid XSS
    return btoa(str).replace(/=/g, "");
}

function solve() {
    $.post("/timeTable/solve", function() {
        refreshSolvingButtons(true);
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
    });
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshTimeTable, 2000);
        }
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
        if (autoRefreshIntervalId != null) {
            clearInterval(autoRefreshIntervalId);
            autoRefreshIntervalId = null;
        }
    }
}

function stopSolving() {
    $.post("/timeTable/stopSolving", function() {
        refreshSolvingButtons(false);
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function addLesson() {
   var teachersV = $("#lesson_teachers").val();
    var teachers =""
        $.each(teachersV, (index, teacher)=>{
            teachers= teachers + teacher + "-"
        });


    var groups = $("#lesson_studentGroups").val();
    var groupNames=""
            $.each(groups, (index, group)=>{
                groupNames = groupNames + group + "-"
            });
    var subject = $("#lesson_subject").val().trim();
    var taskNumber = parseInt($("#lesson_taskNumber").val(),10);
    var multiplicity = parseInt($("#lesson_multiplicity").val(),10);
    $.post("/lessonTasks/add/" + multiplicity  + "/"+taskNumber +"/"+ groupNames +"/"+ teachers + "/" + subject, JSON.stringify({
    }), function() {
         refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Adding lesson failed.", xhr);
    });
    $('#lessonDialog').modal('toggle');
}


function deleteLesson(lesson) {
    $.delete("/lessons/" + lesson.lessonId, function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting lesson (" + lesson.name + ") failed.", xhr);
    });
}

/*function changeCoupling(lesson) {
    $.post("/lessons/changeCoupling/" + lesson.lessonId, JSON.stringify({
        "lessonId": lesson.lessonId
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Locking lesson (" + lesson.name + ") failed.", xhr);
    });
}*/

function fetchRelatedLessonBlocks(lessonTask){
    const lessontaskIdForLessonBlockDisplay = $("#lessonIdForLessonBlock");
    lessontaskIdForLessonBlockDisplay.children().remove();
    const lesBlockOverview = $("#lesBlockOption");
    lesBlockOverview.children().remove();
    lessontaskIdForLessonBlockDisplay
                           .append($(`<option value= ${lessonTask.taskNumber}>`).text(`${lessonTask.taskNumber}`)
                           .append($("<option/>")));
    var lessonBlockSizeList = lessonTask.couplingNumbers;
    if (lessonBlockSizeList.length != 0) {
        $.each(lessonBlockSizeList, (index, lesBlockSize)=>{
            alert(lesBlockSize);
            lesBlockOverview
                .append($(`<option value= ${lesBlockSize}>`).text("Block" + (index+1).toString()+ " - #"+lesBlockSize.toString())
                .append($("<option/>")));
        });
    }
}

function fetchRelatedLessonBlocksByTask(taskId){
    const lessontaskIdForLessonBlockDisplay = $("#lessonIdForLessonBlock");
    lessontaskIdForLessonBlockDisplay.children().remove();
    const lesBlockOverview = $("#lesBlockOption");
    lesBlockOverview.children().remove();
    $.get("/lessonTasks/"+taskId, function(lessonTask) {
    lessontaskIdForLessonBlockDisplay
                           .append($(`<option value= ${lessonTask.taskNumber}>`).text(`${lessonTask.taskNumber}`)
                           .append($("<option/>")));
    var lessonBlockSizeList = lessonTask.couplingNumbers;
    if (lessonBlockSizeList.length != 0) {
        $.each(lessonBlockSizeList, (index, lesBlockSize)=>{
            alert(lesBlockSize);
            lesBlockOverview
                .append($(`<option value= ${lesBlockSize}>`).text("Block" + (index+1).toString()+ " - #"+lesBlockSize.toString())
                .append($("<option/>")));
        });
    }
    }).fail(function(xhr, ajaxOptions, thrownError) {
                   showError("Extracting lessonBlocks for task (" + taskId + ") failed.", xhr);
    });
}

/*function fetchRelatedLessonBlocks(lesson){
    const lessonIdForLessonBlockDisplay = $("#lessonIdForLessonBlock");
    lessonIdForLessonBlockDisplay.children().remove();
    const lesBlockOverview = $("#lesBlockOption");
    lesBlockOverview.children().remove();
    lesBlockOverview
              .append($(`<option value= "NewLessonBlock">`).text("New lessonblock")
              .append($("<option/>")));
    $.get("/lessonBlocks/"+ lesson.lessonId, function(lessonBlockList) {
            lessonIdForLessonBlockDisplay
                                   .append($(`<option value= ${lesson.lessonId}>`).text(lesson.subject)
                                   .append($("<option/>")));
            var mul = 0;
            if (lessonBlockList.length != 0) {
                $.each(lessonBlockList, (index, lesBlock)=>{
                    mul++;
                    lesBlockOverview
                        .append($(`<option value= ${lesBlock.lessonBlockId}>`).text("Block" + mul.toString())
                        .append($("<option/>")));
                });
            }
    }).fail(function(xhr, ajaxOptions, thrownError) {
             showError("Extracting lessonBlocks for (" + lesson.subject + ") failed.", xhr);
    });
}*/

function coupleToLessonBlock() {
    var size= $("#sizeForNewLessonBlock").val();
    var taskId= $("#lessonIdForLessonBlock").val();
    alert(taskId)
    $.post("/lessonTasks/addCouplingOfSize/"+ size +"/ForTask/"+ taskId, JSON.stringify({
    }),
    function() {
        refreshTimeTable();
        fetchRelatedLessonBlocksByTask(taskId);
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Adding new lessonblock failed.", xhr);
    });
    }

function uncoupleFromLessonBlock() {

    var taskId = $("#lessonIdForLessonBlock").val();
    var size = $("#lesBlockOption").val();
    if (size != null) {
    $.post("/lessonTasks/removeCouplingOfSize/" + size +"/FromTask/" +taskId, JSON.stringify({
    }),
    function() {
        refreshTimeTable();
        fetchRelatedLessonBlocksByTask(taskId);
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Verwijderen van lesblok is mislukt.", xhr);
    });
    }
    }


/*function coupleToLessonBlock() {
    var lessonBlock = $("#lesBlockOption").val();
    var lesId= parseInt($("#lessonIdForLessonBlock").val(),10);
    if (lessonBlock == "NewLessonBlock"){
        $.post("/lessonBlocks/addNewBlock/"+ lesId, JSON.stringify({
        "lessonId": lesId
        }),
        function() {
            refreshTimeTable();
        }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Adding new lessonblock failed.", xhr);
        });
    } else {
        lessonBlock = parseInt($("#lesBlockOption").val(),10);
        $.post("/lessonBlocks/addLesson/"+ lesId +"/toLessonBlock/"+ lessonBlock , JSON.stringify({
        "lessonId": lesId,
        "lessonBlock": lessonBlock
        }),
        function() {
            refreshTimeTable();
        }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Adding new lessonblock failed.", xhr);
        });
    }
    $('#lessonBlockDialog').modal('toggle');
}*/

/*function uncoupleFromLessonBlock(lesson) {
    var lesId = lesson.lessonId;
     $.post("/lessonBlocks/removeLesson/"+ lesId, JSON.stringify({
     "lessonId": lesId
     }),
      function() {
         refreshTimeTable();
     }).fail(function(xhr, ajaxOptions, thrownError) {
         showError("Adding new lessonblock failed.", xhr);
      });

}*/

function changePinLesson(lesson) {
    $.post("/lessons/changePin/" + lesson.lessonId, JSON.stringify({
        "lessonId": lesson.lessonId
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Locking lesson (" + lesson.subject + ") failed.", xhr);
    });
}

function changeLastResort(timeslot) {
    $.post("/timeslots/changeLastResort/" + timeslot.timeslotId, JSON.stringify({
        "timeslotId": timeslot.timeslotId
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Changing avoidance of timeslot (" + timeslot.timeslotId + ") failed.", xhr);
    });
}

function addTimeslot() {
    $.post("/timeslots", JSON.stringify({
        "dayOfWeek": $("#timeslot_dayOfWeek").val().trim().toUpperCase(),
        "startTime": $("#timeslot_startTime").val().trim(),
        "endTime": $("#timeslot_endTime").val().trim()
    }), function() {
/*        $.post("/timeslots/updatePositions", {}, function() {
        });*/
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Adding timeslot failed.", xhr);
    });

    $('#timeslotDialog').modal('toggle');
}

function addStudentGroup() {
    const putInfo = $("#info_view");
    var name = $("#newStudentGroup").val().trim();
    putInfo.append($("<option>").text(name).append($("<option/>")));
    $.post("/studentGroups/add/" + name, JSON.stringify({
        "studentGroup": name
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Adding studentGroup failed.", xhr);
    });
    $('#studentGroupDialog').modal('toggle');
}

function deleteTimeslot(timeslot) {
    $.delete("/timeslots/" + timeslot.timeslotId, function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting timeslot (" + timeslot.name + ") failed.", xhr);
    });
}

function addRoom() {
    var name = $("#room_name").val().trim();
    $.post("/rooms", JSON.stringify({
        "name": name
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Adding room (" + name + ") failed.", xhr);
    });
    $("#roomDialog").modal('toggle');
}

function deleteRoom(room) {
    $.delete("/rooms/" + room.roomId, function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting room (" + room.name + ") failed.", xhr);
    });
}


function addPreference() {
    var timeslotId = $("#preference_timeslot").val().trim();
    $.get("/timeslots/" + timeslotId, function(timeslot) {
            var acronym = $("#preference_teacher").val().trim();
            $.get("/teachers/" + acronym, function(teacher) {
                     $.post("/preferences", JSON.stringify({
                            "teacher": teacher,
                            "timeslot": timeslot
                     }), function() {
                            refreshTimeTable();
                     });
            });
    }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Adding lesson failed.", xhr);
    });

/*            $.post("/timeslots", JSON.stringify({
                "timeslot": timeslot}), function() {

                });*/
/*            var acronym = $("#preference_teacher").val().trim();
            $.get("/teachers/" + acronym, function(teacher) {
                $.post("/preferences", JSON.stringify({
                    "teacher": teacher,
                    "timeslot": timeslot
                }), function() {
                        refreshTimeTable();
                });
            });*/
/*    $.get("/timeslots/" + timeslotId, function(timeslot) {
        $.put("/teachers/" + acronym, JSON.stringify({
            "acronym": acronym,
            "timeslot": timeslot
        }), function() {
            refreshTimeTable();
        }).fail(function(xhr, ajaxOptions, thrownError) {
                                  showError("Adding lesson (" + subject + ") failed.", xhr);
        });
    }).fail(function(xhr, ajaxOptions, thrownError) {
                  showError("Adding lesson (" + subject + ") failed.", xhr);
              });*/
    $('#preferenceDialog').modal('toggle');
}
/*
function deletePreference(preference) {
    $.delete("/preferences/" + preference.id, function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting preferences for (" + preference.teacher + ") failed.", xhr);
    });
}*/


function showError(title, xhr) {
    const serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
        .append($(`<div class="toast-header bg-danger">
                 <strong class="mr-auto text-dark">Error</strong>
                 <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                   <span aria-hidden="true">&times;</span>
                 </button>
               </div>`))
        .append($(`<div class="toast-body"/>`)
            .append($(`<p/>`).text(title))
            .append($(`<pre/>`)
                .append($(`<code/>`).text(serverErrorMessage))
            )
        );
    $("#notificationPanel").append(notification);
    notification.toast({ delay: 30000 });
    notification.toast('show');
}

$(document).ready(function() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    });
    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function(i, method) {
        jQuery[method] = function(url, data, callback, type) {
            if (jQuery.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return jQuery.ajax({
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });

    //GRID

    //GRID END
    $("#addLessonBlockSubmitButton").click(function() {
           coupleToLessonBlock();
    });

    $("#removeLessonBlockSubmitButton").click(function() {
         uncoupleFromLessonBlock();
    });

    $("#refreshButton").click(function() {
        refreshTimeTable();
    });
    $("#solveButton").click(function() {
        solve();
    });
    $("#stopSolvingButton").click(function() {
        stopSolving();
    });
    $("#addLessonSubmitButton").click(function() {
        addLesson();
    });
    $("#addTimeslotSubmitButton").click(function() {
        addTimeslot();
    });
    $("#addRoomSubmitButton").click(function() {
        addRoom();
    });
    $("#addStudentGroupSubmitButton").click(function() {
        addStudentGroup();
    });

   $("#addPreferenceSubmitButton").click(function() {
        addPreference();
    });

    //Select multiple options in lesson_studentGroups without using ctrl
    $("#lesson_fields").mousedown(function(e){
        e.preventDefault();

        var select = this;
        var scroll = select .scrollTop;

        e.target.selected = !e.target.selected;

        setTimeout(function(){select.scrollTop = scroll;}, 0);

        $(select ).focus();
    }).mousemove(function(e){e.preventDefault()});

   refreshTimeTable();

});


// ****************************************************************************
// TangoColorFactory
// ****************************************************************************

const SEQUENCE_1 = [0x8AE234, 0xFCE94F, 0x729FCF, 0xE9B96E, 0xAD7FA8];
const SEQUENCE_2 = [0x73D216, 0xEDD400, 0x3465A4, 0xC17D11, 0x75507B];

var colorMap = new Map;
var nextColorCount = 0;

function pickColor(object) {
    let color = colorMap[object];
    if (color !== undefined) {
        return color;
    }
    color = nextColor();
    colorMap[object] = color;
    return color;
}

function nextColor() {
    let color;
    let colorIndex = nextColorCount % SEQUENCE_1.length;
    let shadeIndex = Math.floor(nextColorCount / SEQUENCE_1.length);
    if (shadeIndex === 0) {
        color = SEQUENCE_1[colorIndex];
    } else if (shadeIndex === 1) {
        color = SEQUENCE_2[colorIndex];
    } else {
        shadeIndex -= 3;
        let floorColor = SEQUENCE_2[colorIndex];
        let ceilColor = SEQUENCE_1[colorIndex];
        let base = Math.floor((shadeIndex / 2) + 1);
        let divisor = 2;
        while (base >= divisor) {
            divisor *= 2;
        }
        base = (base * 2) - divisor + 1;
        let shadePercentage = base / divisor;
        color = buildPercentageColor(floorColor, ceilColor, shadePercentage);
    }
    nextColorCount++;
    return "#" + color.toString(16);
}

function buildPercentageColor(floorColor, ceilColor, shadePercentage) {
    let red = (floorColor & 0xFF0000) + Math.floor(shadePercentage * ((ceilColor & 0xFF0000) - (floorColor & 0xFF0000))) & 0xFF0000;
    let green = (floorColor & 0x00FF00) + Math.floor(shadePercentage * ((ceilColor & 0x00FF00) - (floorColor & 0x00FF00))) & 0x00FF00;
    let blue = (floorColor & 0x0000FF) + Math.floor(shadePercentage * ((ceilColor & 0x0000FF) - (floorColor & 0x0000FF))) & 0x0000FF;
    return red | green | blue;
}

//*************************************************
//Convertor
//********************************************************

function cardIdConvertor(encoded, separationValue) {
    var newName = "";
    let listOfNames = [];
    for (let symbol of encoded) {
       if (symbol == separationValue){
           listOfNames.push(newName);
           newName = "";
       } else {
            newName += symbol;
        }
    }
    if (newName.length != 0) {
       listOfNames.push(newName);
    }
    return listOfNames;
}
function getFirstTag(listOfNames){
    return listOfNames[0];
}
function getFirstTagId(listOfNames){
    return listOfNames[1];
}
function getOtherCardTag(listOfNames){
    return listOfNames[2];
}
function getOtherCardTagId(listOfNames){
    return listOfNames[3];
}