function editCart(movieId, action) {
    $.ajax({
        method: "POST",
        dataType: "json",
        url: `api/shopping-cart?item=${movieId}&action=${action}`,
        success: (resultData) => {
            clearTable();
            getMovies();
        }
    }
    );
}


function clearTable() {
    let cartTableBody = document.getElementById("cart_tbody");
    cartTableBody.innerHTML = "";
}


function handleShoppingCart(resultData) {
    let cartTableBody = $("#cart_tbody")
    for (res of resultData) {
        let row = `<tr>
            <th>${res["movieTitle"]}</th>
            <th>${res["quantity"]}</th>
            <th><button role="button" onclick="editCart('${res['movieId']}', 'add')">+</button></th>
            <th><button role="button" onclick="editCart('${res['movieId']}', 'subtract')">-</button></th>
            <th><button role="button" onclick="editCart('${res['movieId']}', 'remove')">x</button></th>
        </tr>`;
        cartTableBody.append(row);
    }
}


function getMovies() {
    $.ajax({
        method: "GET",
        dataType: "json",
        url: "api/shopping-cart",
        success: (resultData) => handleShoppingCart(resultData)
    }
    );
}

getMovies();