function createHeader(timeTable){
        const daysInDutch = ["Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"];

        const multiplicityOverview = $("#theme_timeslotMultiplicity");
        multiplicityOverview.children().remove();
        const theadMultiplicityOverview = $("<thead>").appendTo(multiplicityOverview);
        const headerRowMultiplicityOverview = $("<tr>").appendTo(theadMultiplicityOverview);
        headerRowMultiplicityOverview.append($("<th>Available</th>"));
        $.each(timeTable, (index, dayList) =>{
            if (dayList.length > 0) {
                headerRowMultiplicityOverview
                                .append($("<th/>")
                                .append($("<span/>").text(daysInDutch[index])));
            }

        });

        $(`<tbody id = "multiplicityBody">`).appendTo(multiplicityOverview);
}


function generateMultiplicityTable(timeTable){

    createHeader(timeTable);

    const maxNumbOfHoursADay = Math.max(...timeTable.map(list => list.length));
    const numberOfDays = timeTable.length;
    const tbodyMultiplicityOverview = $("#multiplicityBody");

    for (let i = 0; i < maxNumbOfHoursADay; i++) {
       const rowMultiplicityOverview = $("<tr>").appendTo(tbodyMultiplicityOverview);
       rowMultiplicityOverview
           .append($(`<th class="align-middle"/>`)
            .append($("<span/>").text(i + 1)));
       for (let j = 0; j < numberOfDays; j++) {
           if(timeTable[j].length > 0){
               var input = "";
               var id = "-" + String(i) + String(j);
               if (timeTable[j].length > i ){
                   const timeslot = timeTable[j][i];
                   id = timeslot.timeslotId;
                   var input = `<input style= "width: 10vw" class="inputMultiplicity" type="number" id = ${id} />`;
               }
               tab = $(`<td> ${input} </td>`);
               rowMultiplicityOverview
                   .append(tab);

           }
       }
    }

}

function submitInputForTheme(){

    //***********************
    //Extract
    //**********************
    var valuesNotNull = true;
    var problem = "";

    var themeName = $("#themeName").val();

    //CONTROL
    if (themeName == null || themeName == ""){
        valuesNotNull = false;
        problem += " /Voer een thema in./ "
    }
    var themeLessonTasks = $("#theme_possibleLessonTasks").val();
    var lessonTasksIds = ""
    for (let i= 0; i< themeLessonTasks.length; i++){
        lessonTasksIds += themeLessonTasks[i] + "-";
    }


    //CONTROL
    if (themeLessonTasks == null || themeLessonTasks.length == 0){
            valuesNotNull = false;
            problem += " /Kies minstens één opdracht./ "
    }

    var taskMultiplicity = $("#theme_lessonMultiplicity").val();
    //CONTROL
    if (taskMultiplicity == null || taskMultiplicity < 1){
            valuesNotNull = false;
            problem += "/Zorg dat je minstens één les kiest uit de opdracht(en)./"
    }

    var themeSlots = "";
    var themeSlotsMultiplicities = "";
    var val;
    $(".inputMultiplicity").each(function(){
        val = $(this).val();
        if (val != null && val > 0){
            themeSlots += String($(this).prop('id')) + "-";
            themeSlotsMultiplicities += String(val) + "-";
        }
    });
    //CONTROL
    if (themeSlots.length == 0){
        valuesNotNull = false;
        problem += "/Geef minstens één beschikbaar tijdslot aan./"
    }

    //*********************
    //SUBMIT
    //*********************

    if (valuesNotNull){
        $.post("/themeCollections/add/" + themeName + "/leta/" + lessonTasksIds + "/multiplicity/"+ String(taskMultiplicity) +"/timeslots/" + themeSlots + "/tsmul/" + themeSlotsMultiplicities, function(){
        }
        ).fail(function(xhr, ajaxOptions, thrownError) {
                                    showError("Submitting theme failed.", xhr);
                                });
    } else {
        alert("Indienen mislukt: " + problem);
    }
}

function changeAvailability(id) {
    const tab = $("#"+id);
    const teacher = tab.attr('class');
    if (String(id).charAt(0) != '-'){
        if (tab.children().length > 0) {
            $.delete("/preferences/byTimeslot/" + id + "/teacher/" + teacher, function() {
                tab.children().remove();
                adjustTeacherInfo();
            }).fail(function(xhr, ajaxOptions, thrownError) {
                 showError("Deleting preference (" + teacher + ") failed.", xhr);
            });
        } else {
            $.post("/preferences/byTimeslot/" + id + "/teacher/" + teacher, JSON.stringify({
            }), function() {
                tab.append(`<small class = "fas fa-ban" />`);
                adjustTeacherInfo();
            }).fail(function(xhr, ajaxOptions, thrownError) {
                showError("Getting preference for (" + teacher + ") failed.", xhr);
            });
        }
    }

}


function fetchLessonTasks(subject){
    $.get("/lessonTasks/bySubject/" + subject, function(lessonTasks){
            const theme_possibleLessonTasks = $("#theme_possibleLessonTasks");
            theme_possibleLessonTasks.children().remove();
            $.each(lessonTasks, (index, lessonTask) => {
                theme_possibleLessonTasks.append( $(`<option value= ${lessonTask.taskNumber}/>`).text("opdracht: "+ lessonTask.taskNumber + "-" +lessonTask.subject + "-" +lessonTask.taughtBy[0].acronym ));
            });
        }).fail(function(xhr, ajaxOptions, thrownError) {
                showError("Fetching lessonTasks failed.", xhr);
        });
}

function convertToId(str) {
    // Base64 encoding without padding to avoid XSS
    return btoa(str).replace(/=/g, "");
}

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

    $("#submitThemeButton").click(function() {
         submitInputForTheme();
         generateMultiplicityTable(timeTable);
    });
    //SELECT MULTIPLE LESSONTASKS
    $("#theme_possibleLessonTasks").mousedown(function(e){
        e.preventDefault();

        var select = this;
        var scroll = select .scrollTop;

        e.target.selected = !e.target.selected;

        setTimeout(function(){select.scrollTop = scroll;}, 0);

        $(select ).focus();
    }).mousemove(function(e){e.preventDefault()});

    const subject =  $("#subject");
    subject.keypress(function(e) {
        if (e.which == '13') {
            const val = subject.val();
            if (val == ""){
                fetchLessonTasks("_");
            } else {
               fetchLessonTasks(val);
            }

        }
    });


    $.get("/timeslots", function(timeTable){
        generateMultiplicityTable(timeTable);
    }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Fetching timeslots failed.", xhr);
    });


});