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
    console.log(show);
    $.ajax({
        method: "GET",
        dataType: "json",
        url: `${url}&sort=${sortBy}&show=${show}&page=${page}`,
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
    show = showOption;
    page = 0;
    $('#prev').prop('disabled', true);
    $('#next').prop('disabled', false);
    clearTable();
    makeQuery();
}


function prev() {
    if (page != 0) {
        page -= 1;
        $('#next').prop('disabled', false);
    }
    if (page == 0) {
        $('#prev').prop('disabled', true);
    }
    clearTable();
    makeQuery();
}


function next() {
    page += 1;
    if (page != 0) {
        $('#prev').prop('disabled', false);
    }
    clearTable();
    makeQuery();
}

function clearTable() {
    let movieTableBody = document.getElementById("movies_tbody");
    movieTableBody.innerHTML = "";
}

function handleMoviesResult(resultData) {
    let movieTableBody = $("#movies_tbody")

    let limit = Number(show);
    for (let i = 0; i < resultData.length && i < limit; i ++) {
        let res = resultData[i];
        row = `<tr>
            <th><a href=single-movie.html?movieId=${res['movie_id']}>${res['movie_title']}</a></th>
            <th>${res['movie_year']}</th>
            <th>${res['movie_director']}</th>
            <th>${res['movie_genres']}</th>
            <th>${res['movie_stars']}</th>
            <th>${res['movie_rating']}</th>
        </tr>`;
        movieTableBody.append(row);
    }

    if (resultData.length <= limit) {
        $('#next').prop('disabled', true);
    }
}

var url = "";
var sortBy = "rating DESC, title ASC";
var show = "25";
var page = 0;
if (getParameterByName("genreId") != null && getParameterByName("genreId") != "") {
    url =  `api/movies?genreId=${getParameterByName("genreId")}`; 
}
else {
    url = `api/movies?title=${getParameterByName("title")}&year=${getParameterByName("year")}&director=${getParameterByName("director")}&star=${getParameterByName("star")}`;
}

makeQuery();