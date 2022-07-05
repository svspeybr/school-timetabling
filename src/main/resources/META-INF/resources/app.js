var autoRefreshIntervalId = null;

const dayTextWidth= "20px"
const badgeHourNumberSize = "100px";
const widthColumn = "220px";
const heightRow = "130px";
const cardWidth = "210px";
const cardHeight = "120px";


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
        unassignedLessons.append($(`<th scope="row"> </th>`))
        /*Preferences*/
        const teachersForNewLesson = $("#lesson_teachers");
        teachersForNewLesson.children().remove();
        const studentGroupsForNewLesson = $("#lesson_studentGroups");
        studentGroupsForNewLesson.children().remove();


        /*
        ---------------------------------------
        HEADING TABLE
        ---------------------------------------
        */
        const theadByRoom = $(`<thead style= "text-align:center; vertical-align: middle; margin: 0px;">`).appendTo(timeTableByRoom);

        const headerRowByRoom = $("<tr>").appendTo(theadByRoom);

        //FULLSCREEN - BUTTON
        headerRowByRoom.append($("<th/>")
                                    .append($(`<button type = "button" class="btn btn-link" style = "padding: 0;"/>`)
                                            .append($(`<i class="fas fa-expand fa-lg"/>`))
                                            .click(()=> setToFullScreen("Room")))
                                    .append($(`<div/>`).css("width", dayTextWidth)));

        headerRowByRoom.append($("<th/>").append($(`<div/>`).css("width", badgeHourNumberSize).text("Tijdslot")));
        $.each(timeTable.roomList, (index, room) => {
            headerRowByRoom
                .append($(`<th />`)
                    .append($(`<div/>`)
                        .prop("class", "columnWidthRoom")
                        .css("width", widthColumn)
                        .append($(`<span/>`).text(room.name)
                        .append($(`<button type="button" class="btn btn-light btn-sm p-1" />`)
                            .append($(`<small class="fas fa-trash"/>`)).click(() => deleteRoom(room))))));
        });

        headerRowByRoom
            .append($("<th/>")
                .append($(`<div/>`)
                    .prop("class", "columnWidthRoom")
                    .css("width", widthColumn)
                    .append($(`<span />`).text("Niet toegewezen"))));

        const theadByTeacher = $(`<thead style= "text-align:center; vertical-align: middle; margin: 0px;">`).appendTo(timeTableByTeacher);
        const headerRowByTeacher = $("<tr>").appendTo(theadByTeacher);

        //FULLSCREEN - BUTTON
        headerRowByTeacher.append($("<th/>").append($(`<button type = "button" class="btn btn-link" style = "padding: 0;"/>`)
                                                    .append($(`<i class="fas fa-expand fa-lg"/>`))
                                                    .click(()=> setToFullScreen("Teacher")))
                                            .append($(`<div/>`).css("width", dayTextWidth)));

        headerRowByTeacher.append($("<th/>")
                            .append($(`<div/>`)
                                .css("width", badgeHourNumberSize)
                                .text("Tijdslot")));

        const teacherList = [...new Set(timeTable.lessonTaskList.flatMap(
                                                            lessontask => lessontask.taughtBy.map(
                                                            teacher => teacher.acronym)))].sort();
        $.each(teacherList, (index, teacher) => {
            headerRowByTeacher
                .append($("<th />")
                         .append($(`<div/>`)
                            .prop("class", "columnWidthTeacher")
                            .css("width", widthColumn)
                            .append($(`<span />`).text(teacher))));
        });
        const theadByStudentGroup = $(`<thead style= "text-align:center; vertical-align: middle; margin: 0px;">`).appendTo(timeTableByStudentGroup);
        const headerRowByStudentGroup = $("<tr>").appendTo(theadByStudentGroup);
        //FULLSCREEN -BUTTON
        headerRowByStudentGroup.append($("<th/>")
                                        .append($(`<button type = "button" class="btn btn-link" style = "padding: 0;"/>`)
                                                .append($(`<i class="fas fa-expand fa-lg"/>`))
                                                .click(()=> setToFullScreen("StudentGroup")))
                                        .append($(`<div/>`).css("width", dayTextWidth)));

        headerRowByStudentGroup.append($("<th/>")
                                    .append($(`<div/>`)
                                        .css("width", badgeHourNumberSize)
                                        .text("Tijdslot")));

        const studentGroupList = [...new Set(timeTable.lessonTaskList.flatMap(
                                                                    lessontask => lessontask.studentGroups.map(
                                                                    studentGroup => studentGroup.groupName)))].sort();
        $.each(studentGroupList, (index, studentGroup) => {
            headerRowByStudentGroup
                .append($("<th />")
                         .append($(`<div/>`)
                         .prop("class", "columnWidthStudentGroup")
                         .css("width", widthColumn)
                         .append($(`<span />`).text(studentGroup))));;
        });


        /*
        ---------------------------------------
        GENERATING ROW (SLOTS)
        ---------------------------------------
        */
        const tbodyByRoom = $("<tbody>").appendTo(timeTableByRoom);
        const tbodyByTeacher = $("<tbody>").appendTo(timeTableByTeacher);
        const tbodyByStudentGroup = $("<tbody>").appendTo(timeTableByStudentGroup);

        var dutchDayNames = ["maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag", "zondag"];
        var previousDay = "MONDAY"
        var firstRow = "firstRow fr-0";
        var slotsADay = 0;
        const slots = [];
        var dayIndex = 0;

        $.each(timeTable.timeslotList, (index, timeslot) => {

                    if (previousDay != timeslot.dayOfWeek) {
                        slots.push(slotsADay);
                        slotsADay = 1;

                        previousDay = timeslot.dayOfWeek;
                        dayIndex ++;
                        firstRow = "firstRow fr-" + dayIndex.toString();

                        const rowByRoom = $(`<tr class ="table-success">`).appendTo(tbodyByRoom);
                        rowByRoom
                                .append($(`<th />`));
                        $.each(timeTable.roomList, (index, room) => {
                                rowByRoom.append($("<td/>"));
                        });
                        rowByRoom
                                .append($("<td/>"));

                        const rowByTeacher = $(`<tr class = "table-success">`).appendTo(tbodyByTeacher);
                        rowByTeacher
                                .append($(`<th/>`));
                        $.each(teacherList, (index, teacher) => {
                                                rowByTeacher.append($("<td/>"));
                        });
                        const rowByStudentGroup = $(`<tr class = "table-success">` ).appendTo(tbodyByStudentGroup);
                        rowByStudentGroup
                                .append($(`<th/>`));
                        $.each(studentGroupList, (index, studentGroup) => {
                                                    rowByStudentGroup.append($("<td/>"));
                        });
                    } else {
                        slotsADay ++;
                    }
                //firstROW class needed for determining when a new day starts in the table
                const rowByRoom = $("<tr>").prop("class", firstRow).css("height", heightRow).appendTo(tbodyByRoom);
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
                        .append($("<span/>")
                            .prop("class", "badge badge-secondary pb-2 align-middle")
                            .prop("title", `${moment(timeslot.startTime, "HH:mm:ss").format("HH:mm")}-${moment(timeslot.endTime, "HH:mm:ss").format("HH:mm")}`)
                            .css({"width": "100px"})
                            .append($("<h4/>")
                            .text(slotsADay.toString()+"u"))));

                $.each(timeTable.roomList, (index, room) => {
                    rowByRoom.append($("<td/>")
                                            .prop("id", `ti-${timeslot.timeslotId}-ro-${room.roomId}`)
                                            .prop("class", `droppable`)
                                            );
                });
                rowByRoom
                    .append($("<td/>")
                         .prop("id", `ti-${timeslot.timeslotId}-ro-0`)
                        .prop("class", `droppable`));


                const rowByTeacher = $("<tr>").prop("class", firstRow).css("height", heightRow).appendTo(tbodyByTeacher);
                rowByTeacher
                        .append($(`<th class="align-middle"/>`)
                        .append($("<span/>")
                            .prop("class", "badge badge-secondary pb-2 align-middle")
                            .prop("title", `${moment(timeslot.startTime, "HH:mm:ss").format("HH:mm")}-${moment(timeslot.endTime, "HH:mm:ss").format("HH:mm")}`)
                            .css("width", "100px")
                            .append($("<h4/>")
                            .text(slotsADay.toString()+"u"))));


                    $.each(teacherList, (index, teacher) => {
                        rowByTeacher.append($("<td/>")
                                                .prop("id", `ti-${timeslot.timeslotId}-te-${teacher}`)
                                                .prop("class", `droppable`)
                                                );
                    });

                const rowByStudentGroup = $("<tr>").prop("class", firstRow).css("height", heightRow).appendTo(tbodyByStudentGroup);
                rowByStudentGroup
                        .append($(`<th class="align-middle"/>`)
                        .append($("<span/>")
                            .prop("class", "badge badge-secondary pb-2 align-middle")
                            .prop("title", `${moment(timeslot.startTime, "HH:mm:ss").format("HH:mm")}-${moment(timeslot.endTime, "HH:mm:ss").format("HH:mm")}`)
                            .css("width", "100px")
                            .append($("<h4/>")
                            .text(slotsADay.toString()+"u"))
                            .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                                .append($(`<small class="fas fa-trash"/>`)).click(() => {deleteTimeslot(timeslot);}))
                            .append(lastResortTimeslotButton)));

                $.each(studentGroupList, (index, studentGroup) => {
                    rowByStudentGroup.append($("<td/>")
                        .prop("id", `ti-${timeslot.timeslotId}-st-${studentGroup}`)
                        .prop("class", `droppable`));
                });

                firstRow = "";

        });
        //Save lastday
        slots.push(slotsADay);
        $(".firstRow").each(function(){
            var dIndex= $(this).attr('class').slice(12);
            $(this).prepend($("<td/>").attr('rowspan', slots[dIndex]).prop('class', 'align-middle').html(`<div style = "writing-mode: vertical-rl; font-size: 140%; padding:0"> <big>${dutchDayNames[dIndex]} </big></div>`));
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
            const lessonElementWithoutDelete = $(`<div class="card draggable"/>`).css({"width": cardWidth, "height": cardHeight});

            const cardTextTeacher = $(`<h3 class="card-title ml-2 mb-1 teachers"   style ="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: bold" />`);
            var teacherNames ="";
            var teacherCoverId;
            $.each(teachersOfLessonTask, (index, teacher) => {
                teacherCoverId = teacher.coverId;
                cardTextTeacher
                    .append(`${teacher.acronym} `);
                teacherNames = teacherNames + teacher.acronym + " ";
            });
            cardTextTeacher.attr('title', teacherNames);

            //SetCOVER
            var coverResource = "covers/teacher"+ teacherCoverId +".jpg";
            lessonElementWithoutDelete.append($(`<img class="card-img-top" src="${coverResource}" alt="hallo">`).css({"max-width": cardWidth, "max-height": cardHeight, "opacity": "0.7"}));
            const overlay = $("<div/>").prop("class", "card-img-overlay");

            overlay.append(cardTextTeacher);
            overlay.append($(`<div class="card-body p-2"/>`)
                        .append($(`<h6 class="card-text ml-2 mb-1"/>`).append($(`<em/>`).text(lessonTask.subject))
                        .append($(`<p class="ml-2 mt-1 card-text align-bottom float-right" style= "font-size: 110%; font-weight: bold"/>`).text(lessonTask.taskNumber))))
                        .append($("<hr/>").css({"margin":"0", "border-top":"2px solid #2c2b2b"}));
            const cardTextStudentGroup = $(`<p class="card-text ml-2 mb-1 students" style ="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: bold"/>`).append($(`<em/>`).text("lln. "));
            var studentNames = "";
            $.each(studentGroupsOfLessonTask, (index, studentGroup) => {
                cardTextStudentGroup
                      .append($(`<em/>`).text(`${studentGroup.groupName} `));
                studentNames = studentNames + studentGroup.groupName + " ";
            });
            cardTextStudentGroup.attr('title', studentNames);

            overlay.append(cardTextStudentGroup);

            lessonElementWithoutDelete.append(overlay);

            $.each(lessonsOfLessonTask, (index, lesson) => {
                const lessonElement = lessonElementWithoutDelete.clone();
                lessonElement.find(".card-title").prepend(
                    $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right delete"/>`)
                    .append($(`<small class="fas fa-trash"/>`))
                );
                //PINNING OPTION TO CARD
                if (lesson.pinned == true) {
                    lessonElement.find(".card-title").prepend(
                          $(`<button type="button" class = "ml-2 btn btn-light btn-sm p-1 float-right pin lock"/>`)
                        .append(`<small class = "fas fa-lock" />`)
                    );
                } else {
                     lessonElement.find(".card-title").prepend(
                       $(`<button type="button" class = "ml-2 btn btn-light btn-sm p-1 float-right pin unlock"/>`)
                       .append(`<small class = "fas fa-unlock" />`)
                     );
                }

                //COUPLING OPTION TO CARD

                if (lessonTask.coupled == true) {
                     lessonElement.find(".card-title").prepend(
                          $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right couple link" data-toggle="modal" data-target="#lessonBlockDialog"/>`)
                          .append(`<span class = "fas fa-link" />`)
                    );
                }
                else {
                     lessonElement.find(".card-title").prepend(
                           $(`<button type="button" class="ml-2 btn btn-light btn-sm p-1 float-right couple unlink" data-toggle="modal" data-target="#lessonBlockDialog"/>`)
                           .append(`<small class = "fas fa-unlink" />`)
                     );
                }

                //*************PLACING CARDS**********************
                //**** ID CARDS:
                //** assigned: ' le-{lessonId}-st/ro/te- {st.name/ro.id/te.acronym}'
                //** unassigned: ' le-{lessonId}-un- as '
                var id;
                if (lesson.timeslot == null ) {
                    id = `le-${lesson.lessonId}-un-as`;
                    lessonElement.prop('id', id);
                    var liElement = $(`<td>`).append(lessonElement);
                    liElement.append('</td>');
                    unassignedLessons.append(liElement);
                    setClickers(lessonElement, lesson.lessonId, lessonTask, id);
                } else {
                    var clone;
                    $.each(teachersOfLessonTask, (index, teacher) => {
                        id = `le-${lesson.lessonId}-te-${teacher.acronym}`
                        clone = lessonElement.clone().prop('id', id);
                        setClickers(clone, lesson.lessonId, lessonTask, id);
                        $(`#ti-${lesson.timeslot.timeslotId}-te-${teacher.acronym}`).append(clone);
                    });
                    $.each(studentGroupsOfLessonTask, (index, studentGroup) => {
                        id = `le-${lesson.lessonId}-st-${studentGroup.groupName}`
                        clone = lessonElement.clone().prop('id', id);
                        setClickers(clone, lesson.lessonId, lessonTask, id);
                        $(`#ti-${lesson.timeslot.timeslotId}-st-${studentGroup.groupName}`).append(clone);
                    });

                    if (lesson.room == null) {
                        id = `le-${lesson.lessonId}-ro-0`;
                        lessonElement.prop('id', id);
                        $(`#ti-${lesson.timeslot.timeslotId}-ro-0`).append(lessonElement);
                    } else {
                        id = `le-${lesson.lessonId}-ro-${lesson.room.roomId}`;
                        lessonElement.prop('id', id);
                        $(`#ti-${lesson.timeslot.timeslotId}-ro-${lesson.room.roomId}`).append(lessonElement);
                    }
                    setClickers(lessonElement, lesson.lessonId, lessonTask, id);
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
                           .append($("</option>")));
        });

        $.each(timeTable.teacherList, (index,teacher) => {
            teachersForNewLesson
                           .append($(`<option value= ${teacher.acronym}>`).text(`${teacher.acronym}`)
                           .append($("</option>")));
        });

        /*
        ---------------------------------------
        DRAGGING CARDS SETTINGS
        ---------------------------------------
        */
        setDraggable(1);
        });

}

function openFullScreen(id) {
    $("#refreshButton").requestFullscreen();
}

function setDraggable(scale){

    var cw = Math.floor(parseInt(cardWidth.slice(0, cardWidth.length - 2)) * scale);
    var ch = Math.floor(parseInt(cardHeight.slice(0, cardHeight.length - 2)) * scale);

    function startFix(event, ui) {
        $(this).hide();
        ui.position.left = 0;
        ui.position.top = 0;

    }

    function dragFix(event, ui) {
        scaleFactor = scale;
        var changeLeft = ui.position.left - ui.originalPosition.left; // find change in left
        var newLeft = ui.originalPosition.left + changeLeft / scale; // adjust new left by our zoomScale

        var changeTop = ui.position.top - ui.originalPosition.top; // find change in top
        var newTop = ui.originalPosition.top + changeTop / scale; // adjust new top by our zoomScale

        ui.position.left = newLeft;
        ui.position.top = newTop;
    }

   const draggableConfig = {
        helper: "clone",
        refreshPositions: true,
        opacity: 0.5,
        start: startFix,
        drag: dragFix,
        stop: function(event, ui){
            $(this).show();
        }
    }

    $(".draggable").draggable(draggableConfig);
    $(".draggable").on('dragstart', function(event,ui){
        scaleFactor = scale;
    });

    $(".droppable").droppable({
    accept: ".draggable",
    tolerance: 'pointer',
    hoverClass: "ui-state-active",
    drop: function(event, ui){
        var cardDrag = ui.draggable;
        var draggedId = ui.draggable.attr('id');
        var droppedId = $(this).attr('id');
        //var childDropId = $(this).find(".card").attr('id'); //needed for swapping <- disabled
        //CONVERT -> extract First and second ID
        var draggedIdVals = convertor(draggedId, "-");
        var dragLessonId = getFirstTagId(draggedIdVals);
        var dragOtherTag = getOtherCardTag(draggedIdVals);
        var dragOtherId = getOtherCardTagId(draggedIdVals);
        //Drag to empty slot OR TO DO: UNASSIGNED LESSON
        var droppedIdVals = convertor(droppedId, "-");
        var notIdentical = false;
/*                    if (childDropId == null) { // for swapping <-disabled
                        droppedIdVals = convertor(droppedId, "-");
                    } else{
                        droppedIdVals = convertor(childDropId, "-");
                    } */
        //var dropFirstTag = getFirstTag(droppedIdVals);
        var dropFirstId = getFirstTagId(droppedIdVals);
        var dropOtherTag = getOtherCardTag(droppedIdVals);
        var dropOtherId = getOtherCardTagId(droppedIdVals);

        // CASE dragCard is already assigned
        if (dragOtherTag != "un") {
            if (dropOtherTag == "ro" || dragOtherId == dropOtherId){
                $.post("/lessons/changeTiRoTe/dragLessonId/" + dragLessonId + "/" + dropOtherTag + "/" + dropOtherId +"/" + dropFirstId, JSON.stringify({
                    }), function() {
                            changeCardPosition(cardDrag, dragLessonId, dragOtherTag, dropFirstId, dropOtherId, draggableConfig);
                            adjustTableSizeInFS(dropOtherTag);
                    }
                ).fail(function(xhr, ajaxOptions, thrownError) {
                    showError("Updating lesson failed.", xhr);
                });
            }
        } else  {
            const values = canSetCardForList(cardDrag, dropOtherTag, dropOtherId)
            if (values.length > 0) {
                $.post("/lessons/changeTiRoTe/dragLessonId/" + dragLessonId + "/"+dropOtherTag +"/"+ dropOtherId +"/" + dropFirstId, JSON.stringify({
                            }), function() {
                            $.get("/lessons/" + dragLessonId, function(lesson) {
                                var newCard;
                                var newId;
                                var toId;

                                for (let i = 0; i < 3; i++){
                                    var tag = values[i][0];
                                    var ids = values[i][1];
                                    for (let j = 0; j < ids.length; j++){
                                        newId = "le-" + dragLessonId + "-" + tag + "-" + ids[j];
                                        toId = "ti-" + dropFirstId + "-" + tag + "-" + ids[j];
                                        newCard = cardDrag.clone().prop({ id: newId, class: "card draggable"});
                                        newCard.find(".couple").click(() => fetchRelatedLessonBlocksByTask(lesson.taskNumber));
                                        newCard.find(".pin").click(() => changePinLesson(dragLessonId));
                                        newCard.find(".delete").click(() => deleteLesson(dragLessonId));
                                        $("#" + toId).append(newCard);
                                        makeDraggable(newCard, draggableConfig);
                                    }

                                }
                                cardDrag.remove();
                                adjustTableSizeInFS(dropOtherTag);
                            });
                }).fail(function(xhr, ajaxOptions, thrownError) {
                     showError("Updating lesson failed.", xhr);
                });
                }
            }
        }
   });

}

function adjustTableSizeInFS(dropOtherTag){
    var themeValues = {"st": "StudentGroup", "te": "Teacher", "ro": "Room"};
    var theme = themeValues[dropOtherTag];
    var oldScale = $("#timeTableBy"+theme).prop("scale");
    if (inFullScreen()){
        var height = window.screen.height;
        var width = window.screen.width;
        var tableHeight = $("#timeTableBy"+theme).get(0).clientHeight * oldScale;
        var tableWidth = $("#timeTableBy"+theme).get(0).clientWidth * oldScale;
        if (tableHeight > height || tableHeight + 4 < height || tableWidth > width || tableWidth + 4 < width){
            rescaleTable(theme)
        }
    }
}

function makeDraggable(newCard, configuration){
    newCard.draggable(configuration);
}

/*function setTextOverFlow(lesElement) {
    lesElement.find(".teachers").mouseover(function() {
        $(this).css("text-overflow","");
        $(this).css("overflow","visible");
    }).mouseout(function() {
        $(this).css("overflow","hidden");
        $(this).css("text-overflow", "ellipsis")
    });
    lesElement.find(".students").mouseover(function() {
            $(this).css("text-overflow","");
            $(this).css("overflow","visible");
        }).mouseout(function() {
            $(this).css("overflow","hidden");
            $(this).css("text-overflow", "ellipsis")
        });
}*/

function setClickers(lesElement, lessonId, lessonTask, tabId) {
    lesElement.find(".delete").click(() => deleteLesson(lessonId));
    lesElement.find(".couple").click(() => fetchRelatedLessonBlocks(lessonTask));
    lesElement.find(".pin").click(() => changePinLesson(lessonId));
}


function convertToId(str) {
    // Base64 encoding without padding to avoid XSS
    return btoa(str).replace(/=/g, "");
}

//******************************************SOLVING***********************************************//
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
            lesBlockOverview
                .append($(`<option value= ${lesBlockSize}>`).text("Block" + (index+1).toString()+ " - #"+lesBlockSize.toString())
                .append($("<option/>")));
        });
    }
    }).fail(function(xhr, ajaxOptions, thrownError) {
                   showError("Extracting lessonBlocks for task (" + taskId + ") failed.", xhr);
    });
}

//******************************************CHANGE -COUPLING***********************************************//

function coupleToLessonBlock() {
    var size= $("#sizeForNewLessonBlock").val();
    var taskId= $("#lessonIdForLessonBlock").val();
    $.post("/lessonTasks/addCouplingOfSize/"+ size +"/ForTask/"+ taskId, JSON.stringify({
    }),
    function() {
        $.get("/lessonTasks/" + taskId, function(lessonTask) {

            if (lessonTask.coupled == true) {
                var lessonsOfLessonTask = [...new Set(lessonTask.lessonsOfTaskList)];
                $.each(lessonsOfLessonTask, (index, lesson) => {
                    var cardCopies = $("[id*= le-" + lesson.lessonId + "]");
                    var displayedAsUnlinked = cardCopies.find(".unlink").length > 0;

                    if (displayedAsUnlinked) {
                        cardCopies.each(function() {

                        $(this).find(".couple")
                            .before($(`<button type="button" class = "ml-2 btn btn-light btn-sm p-1 float-right couple link" data-toggle="modal" data-target="#lessonBlockDialog"/>`)
                            .append(`<small class = "fas fa-link" />`)
                            .click(()=>fetchRelatedLessonBlocks(lessonTask)));
                        $(this).find(".unlink")
                            .remove();
                        });
                    }
                });
            }
            fetchRelatedLessonBlocks(lessonTask);
        });

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
        $.get("/lessonTasks/" + taskId, function(lessonTask) {
            if (lessonTask.coupled == false) {
                var lessonsOfLessonTask = [...new Set(lessonTask.lessonsOfTaskList)];
                $.each(lessonsOfLessonTask, (index, lesson) => {
                    var cardCopies = $("[id*= le-" + lesson.lessonId + "]");
                    var displayedAslinked = cardCopies.find(".link").length > 0;

                    if (displayedAslinked) {
                        cardCopies.each(function() {

                        $(this).find(".couple")
                            .before($(`<button type="button" class = "ml-2 btn btn-light btn-sm p-1 float-right couple unlink" data-toggle="modal" data-target="#lessonBlockDialog"/>`)
                            .append(`<small class = "fas fa-unlink" />`)
                            .click(()=> fetchRelatedLessonBlocks(lessonTask)));
                        $(this).find(".link")
                            .remove();
                        });
                    }
                });
            }
            fetchRelatedLessonBlocks(lessonTask);
        });
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Verwijderen van lesblok is mislukt.", xhr);
    });
    }
}

//******************************************CHANGE - PIN***********************************************//

function changePinLesson(lessonId) {
    $.post("/lessons/changePin/" + lessonId, JSON.stringify({
        "lessonId": lessonId
    }), function() {
        /*refreshTimeTable();*/
        const cardCopies = $("[id*= le-" + lessonId + "]");
        const unpinned = cardCopies.find(".unlock").length > 0;
        if (unpinned) {
            cardCopies.each(function() {

                $(this).find(".pin")
                       .before($(`<button type="button" class = "ml-2 btn btn-light btn-sm p-1 float-right pin lock"/>`)
                       .append(`<small class = "fas fa-lock" />`)
                       .click(() => changePinLesson(lessonId)));
                $(this).find(".unlock")
                       .remove();
            });
        } else {
            cardCopies.each(function() {

                $(this).find(".pin")
                       .before($(`<button type="button" class = "ml-2 btn btn-light btn-sm p-1 float-right pin unlock"/>`)
                       .append(`<small class = "fas fa-unlock" />`)
                       .click(()=>changePinLesson(lessonId)));
                $(this).find(".lock")
                       .remove();
            });
        }
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Locking lesson (" + lesson.subject + ") failed.", xhr);
    });
}

//******************************************CHANGE - LAST RESORT***********************************************//

function changeLastResort(timeslot) {
    $.post("/timeslots/changeLastResort/" + timeslot.timeslotId, JSON.stringify({
        "timeslotId": timeslot.timeslotId
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Changing avoidance of timeslot (" + timeslot.timeslotId + ") failed.", xhr);
    });
}

//******************************************ADD DATA - LESSON***********************************************//
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

//******************************************REMOVE DATA - LESSON***********************************************//

function deleteLesson(lessonId) {
    $.delete("/lessons/" + lessonId, function() {
    //Remove all cards
        $("[id*= le-" + lessonId + "]").remove();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting lesson (" + lesson.name + ") failed.", xhr);
    });
}

//******************************************ADD DATA -TIMESLOT***********************************************//
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

//******************************************ADD DATA - STUDENTGROUP***********************************************//
function addStudentGroup() {
    /*const putInfo = $("#info_view");*/
    var name = $("#newStudentGroup").val().trim();
    /*putInfo.append($("<option>").text(name).append($("<option/>")));*/
    $.post("/studentGroups/add/" + name, JSON.stringify({
        "studentGroup": name
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Adding studentGroup failed.", xhr);
    });
    $('#studentGroupDialog').modal('toggle');
}

//******************************************REMOVE DATA -TIMESLOT***********************************************//
function deleteTimeslot(timeslot) {
    $.post("/lessons/resetTimeslots/" + timeslot.timeslotId, JSON.stringify({}), function(){
        $.delete("/preferences/onlyByTimeslot/" + timeslot.timeslotId, function(){
         // AFTER TIMESLOTS ARE EMPTIED -LESHOUR Is DELETED?
         refreshTimeTable();
        }).fail(function(xhr, ajaxOptions, thrownError) {
                        showError("Deleting preferences with timeslot (" + timeslot.timeslotId + ") failed.", xhr);
        });
    });
}

//******************************************ADD DATA -ROOM***********************************************//
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

//******************************************REMOVE DATA -ROOM***********************************************//
function deleteRoom(room) {
    $.delete("/rooms/" + room.roomId, function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting room (" + room.name + ") failed.", xhr);
    });
}

//**********************************FETCH SUMMARY - CONSTRAINT VIOLATIONS*****************************************//
function extractConstraintsViolation() {
        $.get("/timeTable/summary/", function(constraintsValues) {
            const tableBody = $("#info_view").find("tbody");
            tableBody.empty();
            var length = constraintsValues.length / 2;
            for (let index = 0; index < length; index++){

                var row = $(`<tr />`);
                row.append($(`<th/>`).text(constraintsValues[2 * index].slice(28)));
                row.append($("<td/>").text(constraintsValues[2 * index + 1]));
                tableBody.append(row);

            }

        }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Extracting constraintviolations failes.", xhr);
        });
}

//*************************** LOAD FILES (TIMETABLES) **********************************************************//

function loadTimeTables() {
    const loadedTimeTables = $("#fileVersion");
    loadedTimeTables.children().remove();
    $.get("/fm/load", function(fileNames){
                //TO DO: TRANSFER FILE FROM SERVER TO CLIENT: HOW????
                var FV = localStorage.getItem('fv') ;

                loadedTimeTables.attr('onchange', `loadTable(this.value)`);
                for (let index = 0; index < fileNames.length; index++){
                    loadedTimeTables.append($(`<option value = ${fileNames[index]}> ${fileNames[index]} </option>`));
                }

                if (FV == 'undefined' ) {
                    FV = fileNames[0]; //select the first
                    localStorage.setItem('fv', FV)
                }
                loadedTimeTables.val(FV);

        }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Loading files failed.", xhr);
        });
}

function loadTable(tableName){
    $.post("/timeTable/changeTableTo/" + tableName, function(){
            localStorage.setItem('fv', tableName)
            refreshTimeTable()
        }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Changing table failed.", xhr);
        });
}

//*************************** DOWNLOAD OPTION ****************************************************//

function downloadFile() {
    var fileName = $("#downloadFileName").val();
    var uri = "/fm/download/" + fileName + "/xml";
    $.get(uri, function(){
                //TODO
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Saving file failed.", xhr);
    });

}

//******************************SAVE OPTION (ON SERVER)*********************************************//
function saveFile(){
    var fileName = $("#fileVersion").val();
    var uri = "/fm/save/" + fileName + "/xml";
    $.post(uri, JSON.stringify({}), function(){
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Saving file failed.", xhr);
    });
}

//******************************DELETE OPTION (ON SERVER)*********************************************//
function deleteFile(){
    var fileName = $("#fileVersion").val();
    const numberOfFiles = $("#fileVersion option").length
    $.post("/fm/delete/" + fileName + "/xml", JSON.stringify({}), function(){
        $.get("/fm/loadFile", function(fileName){
            const length = fileName.length;
            if (length > 0) {
                localStorage.setItem('fv', fileName[0]);
                loadTable(fileName[0]);
                loadTimeTables();
            } else {
                localStorage.setItem('fv', 'undefined');
                resetTableDataset();
            }
        })
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting file failed.", xhr);
    });
}

//Clean table
function resetTableDataset(){
    $.post("/timeTable/resetTableDatabase", function(){
            refreshTimeTable();
            loadTimeTables();
        }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Resetting table failed.", xhr);
        });

}

//******************************COPY FILE OPTION (ON SERVER)*********************************************//

function copyFile(){
    $.get("/fm/load", function(files){
        const fileName = $("#fileVersion").val();
        const copyFile = renameCopy(fileName, files); //check for no overlap
        $.post("/fm/copy/" + copyFile + "/xml", function(){
            localStorage.setItem('fv', copyFile);
            loadTimeTables()
        })
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Copying file failed.", xhr);
    });
}

function renameCopy(fileName, files){
    var copyFile = fileName + "_v2";
    if (fileName.length > 3 && fileName.lastIndexOf("_v") > -1){
        const index = fileName.lastIndexOf("_v") + 2
        const end = fileName.substring(index);
        if (end.length > 0 && end.match(/[0-9]+/)){
            copyFile = fileName.substring(0, index) + (parseInt(end) + 1);
        }
    }
    if (files.includes(copyFile)){
        copyFile = renameCopy(copyFile, files);
    }
    return copyFile
}

//******DISPLAY INFO *****//

function showVariableInShell(message){
    $.post("/fm/send/" + message, function(){
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Sending message failed.", xhr);
    });
}

//***********************************ERROR HANDLING***********************************************//

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

/*
----------------------------------------------------------------------------------------------------------------
*******************************************DOCUMENT READY******************************************************
---------------------------------------------------------------------------------------------------------------
*/

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

    $("#downloadFileButton").click(function() {
        downloadFile();
    });

    $("#infoScoreButton").click(function() {
        extractConstraintsViolation();
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


   //FILE BUTTONS - SAVE/COPY/DELETE
   $("#saveFile").click(function() {
        saveFile();
   });
   $("#copyFile").click(function() {
        copyFile();
   });
   $("#deleteFile").click(function() {
        deleteFile();
   });



   $("#preferencePerTeacherNav").click(function(){
        var win = window.open("overview/preferencePerTeacher.html", '_blank', 'width = 800, height = 550');
        win.location.reload();
   });

   $("#themeConfigurationNav").click(function(){
           var win = window.open("overview/themeConfiguration.html", '_blank', 'width = 800, height = 550');
           win.location.reload();
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

    addEventListener("fullscreenchange", function(event){
        if(!document.fullscreenElement){
            afterFullScreenEvent();
        }
    }, false);

    //LOADING timeTable versions saved at PATHNAME repository//
    loadTimeTables();
    refreshTimeTable();
});

/*
------------------------------------------------------------------------
***********************TABLE TO FULL SCREEN*****************************
-------------------------------------------------------------------------
*/

function inFullScreen(){
    return ( document.fullscreenElement ||
             document.webkitFullscreenElement ||
             document.mozFullScreenElement ||
             document.msFullscreenElement)
}


function setToFullScreen(theme){
    var scale = 5;
      if ( inFullScreen()) {
            var exit = false;
            if (document.exitFullscreen) {
              afterFullScreenEvent();
              document.exitFullscreen();
            } else if (document.mozCancelFullScreen) {
              afterFullScreenEvent();
              document.mozCancelFullScreen();
            } else if (document.webkitExitFullscreen) {
              afterFullScreenEvent();
              document.webkitExitFullscreen();
            } else if (document.msExitFullscreen) {
              afterFullScreenEvent();
              document.msExitFullscreen();
            }
          } else {

            $("#resp" +theme).css("height", "");
            rescaleTable(theme);
            $("#by"+theme).css("background-color", "rgba(255,255,255)");

            element = $("#by"+theme).get(0);

            if (element.requestFullscreen) {
              element.requestFullscreen();
            } else if (element.mozRequestFullScreen) {
              element.mozRequestFullScreen();
            } else if (element.webkitRequestFullscreen) {
              element.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
            } else if (element.msRequestFullscreen) {
              element.msRequestFullscreen();
            }
          }
}

function rescaleTable(theme){

    var height = window.screen.height;
    var width = window.screen.width;

    var tableHeight = $("#timeTableBy"+theme).get(0).clientHeight;
    var tableWidth = $("#timeTableBy"+theme).get(0).clientWidth;

    var xTableScale = ( width / tableWidth - 0.001).toPrecision(3);
    var yTableScale = ( height / tableHeight - 0.001).toPrecision(3);
    var tableScale = Math.min(xTableScale, yTableScale);


    var table =$("#timeTableBy"+theme);
    table.css("transform-origin", "0 0")
    table.css("transform", `scale(${tableScale})`);
    table.prop('scale', `${tableScale}`);

    setDraggable(tableScale);

}

function afterFullScreenEvent(){

    setDraggable(1);
    $(".draggable").css('width', cardWidth);
    $(".table").removeAttr('style');
    $(".table-responsive").removeAttr('style');
    $(".table-responsive").css("height", "500px");

}


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

function convertor(encoded, separationValue) {
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

function changeCardPosition(cardDrag, dragLessonId, dragOtherTag, dropFirstId, dropOtherId, draggableConfig){

    const values = [];
    var tes = convertor(cardDrag.find(".teachers").last().text(), " ");
    values.push(["te", tes]);

    var sts = convertor(cardDrag.find(".students").children().text(), " ");
    values.push(["st", sts.slice(1)]);

    $.get("/lessons/"+ dragLessonId, function(lesson) {
            values.push(["ro", [lesson.room.roomId]]);
            var parentTabIdVals =  convertor(cardDrag.parent().attr('id'), "-");
            var parentTabFirstId = getFirstTagId(parentTabIdVals);

            for (let i = 0; i < 3; i++){
                var tag = values[i][0];
                var ids = values[i][1];
                var val;
                for (let j = 0; j < ids.length; j++){
                    if (dragOtherTag == "ro" && tag == "ro"){
                        val = getOtherCardTagId(parentTabIdVals);
                    } else {
                        val = ids[j];
                    }

                    //var fromId = "ti-" + parentTabFirstId + "-" + tag + "-" + val;
                    var fromId = "le-" + dragLessonId + "-" + tag + "-" + val;
                    var newId = "le-" + dragLessonId + "-" + tag + "-" + ids[j];
                    var toId = "ti-" + dropFirstId + "-" + tag + "-" + ids[j];
                    var newCard = cardDrag.clone().prop({ id: newId, class: "card draggable"});
                    removeCard(fromId);
                    newCard.find(".couple").click(() => fetchRelatedLessonBlocksByTask(lesson.taskNumber));
                    newCard.find(".pin").click(() => changePinLesson(dragLessonId));
                    newCard.find(".delete").click(() => deleteLesson(dragLessonId));
                    $("#" + toId).append(newCard);
                    makeDraggable(newCard, draggableConfig);
                }
            }
        }).fail(function(xhr, ajaxOptions, thrownError) {
                       showError("Extracting room for lesson (" + dragLessonId + ") failed.", xhr);
        });

}

function removeCard(id){
    $("#" + id).remove();
}
function emptyTab(id) {
    $("#" + id).empty();
}

function canSetCardForList(cardDrag, dropOtherTag, dropOtherId){

    var canChange = false
    var roomId = "0";
    const values = [];

     var tes = convertor(cardDrag.find(".teachers").last().text(), " ");
     var sts = convertor(cardDrag.find(".students").children().text(), " ");

    if (dropOtherTag == "ro") {
        canChange = true;
        roomId = dropOtherId;
    }

    if (dropOtherTag == "te") {
        for (let i =0; i < tes.length; i++){
            if (tes[i] == dropOtherId) {
                canChange = true;
            }
        }
    }

    if (dropOtherTag == "st") {
        for (let i =1; i < sts.length; i++){ //ignore 'FOR' in eg 'FOR' 6LAWE ...
            if (sts[i] == dropOtherId) {
                canChange = true;
            }
        }
    }
    if (canChange){
        values.push(["ro", [roomId]]);
        values.push(["st", sts.slice(1)]);
        values.push(["te", tes])
    }

    return values;
}