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
    let cartTableBody = $("#cart_tbody");
    let total = 0;
    if (resultData.length !== 0) {
        for (res of resultData) {
            total += Number(res["quantity"]) * 5;
            let row = `<tr>
                <th>${res["movieTitle"]}</th>
                <th>${res["quantity"]}</th>
                <th>$${Number(res["quantity"]) * 5}.00</th>
                <th><button role="button" onclick="editCart('${res['movieId']}', 'add')">+</button></th>
                <th><button role="button" onclick="editCart('${res['movieId']}', 'subtract')">-</button></th>
                <th><button role="button" onclick="editCart('${res['movieId']}', 'remove')">x</button></th>
            </tr>`;
            cartTableBody.append(row);
        }
    }   
    document.getElementById("total").innerHTML = `Total: $${total}.00`;
    if (resultData.length !== 0) {
        document.getElementById("payButton").innerHTML = '<button role="button" onclick="toCheckout()">Continue to Checkout</button>';
    }
}


function toCheckout() {
    document.location.href="payment.html";
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