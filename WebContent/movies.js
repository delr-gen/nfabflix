function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function search() {
    let url = `movies.html?title=${$("#title").val()}&year=${$("#year").val()}&director=${$("#director").val()}&star=${$("#star").val()}`;
    
    document.location.href=url;
}


function makeQuery() {
    $.ajax({
        method: "GET",
        dataType: "json",
        url: url,
        success: (resultData) => handleMoviesResult(resultData)
    }
    );
}


function sort(sortOption) {
    sortBy = sortOption;
    page = 0;
    $('#prev').prop('disabled', true);
    $('#next').prop('disabled', false);
    clearTable();
    makeQuery();
}


function changeShow(showOption) {
    localStorage.setItem("show", showOption);
    document.location.href=`movies.html?show=${showOption}`;
}


function prev() {
    if (page != 0) {
        page -= 1;
        document.location.href=`movies.html?page=${page}`;
    }
}


function next() {
    page += 1;
    document.location.href=`movies.html?page=${page}`;
}


function clearTable() {
    let movieTableBody = document.getElementById("movies_tbody");
    movieTableBody.innerHTML = "";
}


function addMovie(movieId) {
    $.ajax({
        method: "POST",
        dataType: "json",
        url: `api/shopping-cart?item=${movieId}`,
        success: (resultData) => {
            console.log(resultData);
            window.alert("Added Movie");
        }
    }
    );
}


function handleMoviesResult(resultData) {
    let movieTableBody = $("#movies_tbody")

    let limit = Number(localStorage.getItem("show"));
    for (let i = 0; i < resultData.length && i < limit; i ++) {
        let res = resultData[i];
        row = `<tr>
            <th><a href=single-movie.html?movieId=${res['movie_id']}>${res['movie_title']}</a></th>
            <th>${res['movie_year']}</th>
            <th>${res['movie_director']}</th>
            <th>${res['movie_genres']}</th>
            <th>${res['movie_stars']}</th>
            <th>${res['movie_rating']}</th>
            <th><button role="button" onclick="addMovie('${res['movie_id']}')">+</button></th>
        </tr>`;
        movieTableBody.append(row);
    }

    if (resultData.length <= limit) {
        $('#next').prop('disabled', true);
    }
    else {
        $('#next').prop('disabled', false);
    }
}

var url = "";
var sortBy = "rating DESC, title ASC";

if (getParameterByName("session") != null && getParameterByName("session") == "true") {
    var page = Number(localStorage.getItem("page"));
    url = "api/movies";
}
else if (getParameterByName("page") != null && getParameterByName("page") != "") {
    var page = Number(getParameterByName("page"));
    localStorage.setItem("page", page);
    url =  `api/movies?page=${page}`; 
}
else if (getParameterByName("show") != null && getParameterByName("show") != "") {
    var page = 0;
    localStorage.setItem("page", page);
    let show = Number(getParameterByName("show"));
    url =  `api/movies?show=${show}`; 
}
else if (getParameterByName("genreId") != null && getParameterByName("genreId") != "") {
    var page = 0;
    localStorage.setItem("show", 25);
    localStorage.setItem("page", page);
    url =  `api/movies?genreId=${getParameterByName("genreId")}&sort=${sortBy}`; 
}
else {
    var page = 0;
    localStorage.setItem("show", 25);
    localStorage.setItem("page", page);
    url = `api/movies?title=${getParameterByName("title")}&year=${getParameterByName("year")}&director=${getParameterByName("director")}&star=${getParameterByName("star")}&sort=${sortBy}`;
}

makeQuery();

if (page == 0) {
    $('#prev').prop('disabled', true);
}
else {
    $('#prev').prop('disabled', false);
}