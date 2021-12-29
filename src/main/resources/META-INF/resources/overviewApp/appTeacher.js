var autoRefreshIntervalId = null;

function refreshTeacherOverview() {
    $.getJSON("/teachers", function(teacherList) {

        const teacherOverview = $("#teacherOverview");
        teacherOverview.children().remove();

        const theadTeacherOverview = $("<thead>").appendTo(teacherOverview);
        const headerRowTeacherOverview = $("<tr>").appendTo(theadTeacherOverview);
        headerRowTeacherOverview.append($("<th>sigel</th>"));

        // Show - name
        headerRowTeacherOverview
                .append($("<th/>")
                .append($("<span/>").text("naam")));

        //Display - hoursOfWorking
        headerRowTeacherOverview
                .append($("<th/>")
                .append($("<span/>").text("opdracht")));
        //Showing later additional properties of teachers?
/*        $.each(timeTable.roomList, (index, room) => {
            headerRowByRoom
                .append($("<th/>")
                    .append($("<span/>").text(room.name))
                    .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                        .append($(`<small class="fas fa-trash"/>`)).click(() => deleteRoom(room))));
        });*/
        const tbodyTeacherOverview = $("<tbody>").appendTo(teacherOverview);
        $.each(teacherList, (index, teacher) => {
            const rowTeacherOverview = $("<tr>").appendTo(tbodyTeacherOverview);
            rowTeacherOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${teacher.acronym}
                    `)));

            rowTeacherOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${teacher.name}
                    `)));
            rowTeacherOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${teacher.taskHours}
                    `)));
                      /*  .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                            .append($(`<small class="fas fa-trash"/>`)).click(() => deleteTimeslot(timeslot)))));*/

    });

}).fail(function(xhr, ajaxOptions, thrownError) {
         showError("Start solving failed.", xhr);
     });
}

function refreshTaskOverview() {
    $.get("/lessonTasks", function(taskList) {

        const taskOverview = $("#taskOverview");
        taskOverview.children().remove();

        const theadTaskOverview = $("<thead>").appendTo(taskOverview);
        const headerRowTaskOverview = $("<tr>").appendTo(theadTaskOverview);
        headerRowTaskOverview.append($("<th>Opdrachten</th>"));

        headerRowTaskOverview
                .append($("<th/>")
                .append($("<span/>").text("onderwerp")));
        headerRowTaskOverview
                 .append($("<th/>")
                 .append($("<span/>").text("aantal lessen")));
        headerRowTaskOverview
                 .append($("<th/>")
                 .append($("<span/>").text("leerplan")));
        //Showing later additional properties of teachers?
/*        $.each(timeTable.roomList, (index, room) => {
            headerRowByRoom
                .append($("<th/>")
                    .append($("<span/>").text(room.name))
                    .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                        .append($(`<small class="fas fa-trash"/>`)).click(() => deleteRoom(room))));
        });*/
        const tbodyTaskOverview = $("<tbody>").appendTo(taskOverview);
        $.each(taskList, (index, lessonTask) => {
            const rowTaskOverview = $("<tr>").appendTo(tbodyTaskOverview);
            rowTaskOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${lessonTask.taskNumber}
                    `)));
            rowTaskOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${lessonTask.subject}
                    `)));
            rowTaskOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${lessonTask.multiplicity}
                    `)));
            rowTaskOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${lessonTask.courseLevel}
                    `)));
                      /*  .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                            .append($(`<small class="fas fa-trash"/>`)).click(() => deleteTimeslot(timeslot)))));*/

    });

}).fail(function(xhr, ajaxOptions, thrownError) {
         showError("Start solving failed.", xhr);
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
   refreshTeacherOverview();
   refreshTaskOverview();
});

