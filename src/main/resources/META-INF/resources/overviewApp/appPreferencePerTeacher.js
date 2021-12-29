var autoRefreshIntervalId = null;


function createHeader(timeTable){
        const daysInDutch = ["Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"];
/*        const dayOfWeeks = [];
        var dayOfWeek = null;
        $.each(timeslotList, (index, timeslot) => {
            if (dayOfWeek != timeslot.dayOfWeek){
                dayOfWeek = timeslot.dayOfWeek;
                dayOfWeeks.push(dayOfWeek);
            }
        });*/

        const preferenceOverview = $("#prefPerTeachOverview");
        preferenceOverview.children().remove();
        const theadPreferenceOverview = $("<thead>").appendTo(preferenceOverview);
        const headerRowPreferenceOverview = $("<tr>").appendTo(theadPreferenceOverview);
        headerRowPreferenceOverview.append($("<th>docent</th>"));
        $.each(timeTable, (index, dayList) =>{
            if (dayList.length > 0) {
                headerRowPreferenceOverview
                                .append($("<th/>")
                                .append($("<span/>").text(daysInDutch[index])));
            }

        });

        $(`<tbody id = "preferenceBody">`).appendTo(preferenceOverview);
}

function fetchTeachers(){

    teachers = $("#teachers");
    teachers.children().remove();
    $.get("/teachers", function(teachersList) {
        $.each(teachersList, (index,teacher) => {
            teachers
                .append($(`<option value= ${teacher.acronym}>`).text(`${teacher.acronym}`)
                .append($("<option/>")));
        });

    }).fail(function(xhr, ajaxOptions, thrownError) {
         showError("Fetching teachers failed.", xhr);
     });
}

function generateTeacherInfo(){
    $("#teacherInfo").children().remove();
    var teacherName = $("#teacher").val();
    $.get("/teachers/" + teacherName, function(teacher) {
         $("#teacherInfo").append($(`<li class="list-group-item"> voltijdse weging: ${teacher.fullTime} </li>`));
         $("#teacherInfo").append($(`<li class="list-group-item"> niet opgenomen uren: ${teacher.hoursAwayFromFullTime} </li>`));
         $("#teacherInfo").append($(`<li class="list-group-item" id= "firstOrLast"> Geen eerste/laatste uren: ${teacher.firstOrLastHours} </li>`));
         $.get("/teachers/rightToHalfDays/" + teacherName, function(halfDays) {
                  $("#teacherInfo").append($(`<li class="list-group-item"> halve dagen recht op: ${halfDays} </li>`));
         }).fail(function(xhr, ajaxOptions, thrownError) {
                                  showError("Fetching teacher " + teacherName + " failed.", xhr);
               });;
    }).fail(function(xhr, ajaxOptions, thrownError) {
                       showError("Fetching teacher " + teacherName + " failed.", xhr);
    });
}

function adjustTeacherInfo(){
    var teacherName = $("#teacher").val();
    $.get("/teachers/" + teacherName, function(teacher) {
         $("#firstOrLast").empty();
         $("#firstOrLast").text(`Geen eerste/laatste uren: ${teacher.firstOrLastHours}`);
    }).fail(function(xhr, ajaxOptions, thrownError) {
                       showError("Fetching teacher " + teacherName + " failed.", xhr);
    });
}

function generatePreferenceTable(timeTable){
    var teacher = $("#teacher").val();
    if (teacher != ""){
        $.get("/preferences/" + teacher, function(preferenceList){
             const teach = teacher;
             const preferenceTimeslots = preferenceList.map(preference => preference.timeslot.timeslotId);
             createHeader(timeTable);
             const tbodyPreferenceOverview = $("#preferenceBody");


             const maxNumbOfHoursADay = Math.max(...timeTable.map(list => list.length));
             const numberOfDays = timeTable.length;

             for (let i = 0; i < maxNumbOfHoursADay; i++) {
                const rowPreferenceOverview = $("<tr>").appendTo(tbodyPreferenceOverview);
                rowPreferenceOverview
                    .append($(`<th class="align-middle"/>`)
                     .append($("<span/>").text(i + 1)));
                for (let j = 0; j < numberOfDays; j++) {
                    if(timeTable[j].length > 0){
                        var notAllowed = "";
                        var id = "-" + String(i) + String(j);
                        if (timeTable[j].length > i ){
                            const timeslot = timeTable[j][i];
                            id = timeslot.timeslotId;
                            if (preferenceTimeslots.includes(timeslot.timeslotId)){
                                var notAllowed = `<small class = "fas fa-ban" />`;

                            }
                        }
                        tab = $(`<td id = ${id} onclick = changeAvailability(${id})> ${notAllowed} </td>`);
                        tab.addClass(teacher);
                        rowPreferenceOverview
                            .append(tab);

                    }
                }
             }

        }).fail(function(xhr, ajaxOptions, thrownError) {
                  showError("Fetching teacher's preferences failed.", xhr);
            });
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

    $.get("/timeslots", function(timeTable){
        createHeader(timeTable);
        fetchTeachers();
        $("#teacher").keypress(function(e) {
            if (e.which == '13') {
                generatePreferenceTable(timeTable);
                generateTeacherInfo();
            }
        });
    }).fail(function(xhr, ajaxOptions, thrownError) {
            showError("Fetching timeslots failed.", xhr);
    });


});