document.getElementById("addStarForm").addEventListener("submit", function(event) {
    event.preventDefault();
    $.ajax({
        method: "POST",
        dataType: "json",
        url: `api/insert-star?name=${$("#star").val()}&birthYear=${$("#birthYear").val()}`
        // ,
        // success: () => {
        //     // print resultId
        //     window.alert(`Added star`);
        // },
        // error: () => {
        //     window.alert("Unable to add star");
        // }

    });
});


document.getElementById("addMovieForm").addEventListener("submit", function(event) {
    event.preventDefault();
    $.ajax({
        method: "POST",
        dataType: "json",
        url: `api/insert-movie?title=${$("#title").val()}&year=${$("#year").val()}&director=${$("#director").val()}&movieStar=${$("#movieStar").val()}&genre=${$("#genre").val()}`
    });
});