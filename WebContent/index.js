document.getElementById("searchForm").addEventListener("submit", function(event) {
    event.preventDefault();
    let url = `movies.html?title=${$("#title").val()}&year=${$("#year").val()}&director=${$("#director").val()}&star=${$("#star").val()}`;
    
    document.location.href=url;
});


function handleGenreList(resultData) {
    let genreList = $("#genres");
    for (res of resultData) {
        genreList.append(`<li><a href=movies.html?genreId=${res["genreId"]}>${res["genreName"]}</a></li>`);
    }
}

$.ajax({
    method: "GET",
    dataType: "json",
    url: "api/genres",
    success: (resultData) => handleGenreList(resultData)
}
);