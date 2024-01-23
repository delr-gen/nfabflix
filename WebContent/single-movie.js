/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */


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


function addMovie(movieId) {
    $.ajax({
        method: "POST",
        dataType: "json",
        url: `api/shopping-cart?item=${movieId}`,
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


function handleSingleMovie(resultData) {
    let movieTableBody = $("#movies_tbody")
    for (res of resultData) {
        document.getElementById("title").innerHTML = `${res["movie_title"]} (${res["movie_year"]})<button role="button" onclick="addMovie('${getParameterByName("movieId")}')">+</button>`;
        document.getElementById("director").innerHTML = `Directed by ${res["movie_director"]}`;
        document.getElementById("rating").innerHTML = `${res["movie_rating"]} rating`;
        movieTableBody.append(`<tr><th>${res["movie_genres"]}</th><th>${res["movie_stars"]}</th></tr>`);
    }
}

$.ajax({
    method: "GET",
    dataType: "json",
    url: `api/single-movie?movieId=${getParameterByName("movieId")}`,
    success: (resultData) => handleSingleMovie(resultData)
}
);