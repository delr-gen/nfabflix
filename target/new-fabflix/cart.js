

function handleShoppingCart(resultData) {
    let cartTableBody = $("#cart_tbody")
    console.log(resultData);
    for (res of resultData) {
        let row = `<tr>
            <th>${res["movieTitle"]}</th>
            <th>${res["quantity"]}</th>
        </tr>`;
        cartTableBody.append(row);
    }
}

$.ajax({
    method: "GET",
    dataType: "json",
    url: "api/shopping-cart",
    success: (resultData) => handleShoppingCart(resultData)
}
);