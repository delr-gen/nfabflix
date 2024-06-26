document.getElementById("searchForm").addEventListener("submit", function(event) {
    event.preventDefault();
    let url = `movies.html?title=${$("#title").val()}&year=${$("#year").val()}&director=${$("#director").val()}&star=${$("#star").val()}`;
    
    document.location.href=url;
});


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
    // sortBy = sortOption;
    // page = 0;
    // $('#prev').prop('disabled', true);
    // $('#next').prop('disabled', false);
    // clearTable();
    // makeQuery();
    document.location.href = `movies.html?sort=${sortOption}`;
}


function changeShow(showOption) {
    sessionStorage.setItem("show", showOption);
    document.location.href=`movies.html?show=${showOption}`;
}


document.getElementById("prev").addEventListener("click", function(event) {
    event.preventDefault();
    if (page != 0) {
        page -= 1;
        document.location.href=`movies.html?page=${page}`;
    }
});


document.getElementById("next").addEventListener("click", function(event) {
    event.preventDefault();
    page += 1;
    document.location.href=`movies.html?page=${page}`;
});


function clearTable() {
    let movieTableBody = document.getElementById("movies_tbody");
    movieTableBody.innerHTML = "";
}


function addMovie(movieId) {
    $.ajax({
        method: "POST",
        dataType: "json",
        url: `api/shopping-cart?item=${movieId}&action=add`,
        success: (resultData) => {
            console.log(resultData);
            window.alert("Added movie to cart");
        },
        error: (resultData) => {
            window.alert("Unable to add movie to cart");
        }
    }
    );
}


function handleMoviesResult(resultData) {
    let movieTableBody = $("#movies_tbody")

    let limit = Number(sessionStorage.getItem("show"));
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
    var page = Number(sessionStorage.getItem("page"));
    url = "api/movies";
}
else if (getParameterByName("page") != null && getParameterByName("page") != "") {
    var page = Number(getParameterByName("page"));
    sessionStorage.setItem("page", page);
    url =  `api/movies?page=${page}`; 
}
else if (getParameterByName("show") != null && getParameterByName("show") != "") {
    var page = 0;
    sessionStorage.setItem("page", page);
    let show = Number(getParameterByName("show"));
    url =  `api/movies?show=${show}`; 
}
else if (getParameterByName("sort") != null && getParameterByName("sort") != "") {
    var page = 0;
    sessionStorage.setItem("page", page);
    let sort = getParameterByName("sort");
    url = `api/movies?sortBy=${sort}`;
}
else if (getParameterByName("genreId") != null && getParameterByName("genreId") != "") {
    var page = 0;
    sessionStorage.setItem("show", 25);
    sessionStorage.setItem("page", page);
    url =  `api/movies?genreId=${getParameterByName("genreId")}&sort=${sortBy}`; 
}
else if (getParameterByName("firstLetter") != null && getParameterByName("firstLetter") != "") {
    var page = 0;
    sessionStorage.setItem("show", 25);
    sessionStorage.setItem("page", page);
    url =  `api/movies?firstLetter=${getParameterByName("firstLetter")}&sort=${sortBy}`; 
}
else {
    var page = 0;
    sessionStorage.setItem("show", 25);
    sessionStorage.setItem("page", page);
    url = `api/movies?title=${getParameterByName("title")}&year=${getParameterByName("year")}&director=${getParameterByName("director")}&star=${getParameterByName("star")}&sort=${sortBy}`;
}

makeQuery();

if (page == 0) {
    $('#prev').prop('disabled', true);
}
else {
    $('#prev').prop('disabled', false);
}