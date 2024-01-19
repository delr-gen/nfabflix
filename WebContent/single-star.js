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

function handleSingleStar(resultData) {
    let movieTableBody = $("#movies_tbody")
    document.getElementById("star_name").innerHTML = resultData["starName"];
    document.getElementById("birthYear").innerHTML = `Born ${resultData["birthYear"]}`;
    movieTableBody.append(`<tr>${resultData["movies"]}</tr>`);
}

$.ajax({
    method: "GET",
    dataType: "json",
    url: `api/single-star?starId=${getParameterByName("starId")}`,
    success: (resultData) => handleSingleStar(resultData)
}
);