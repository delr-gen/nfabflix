function emptyCart() {
    $.ajax({
        method: "POST",
        dataType: "json",
        url: "api/shopping-cart?&action=empty",
        success: (resultData) => {
            console.log(resultData);
        }
    }
    );
}


function handlePaymentResult(resultData) {
    console.log(resultData);
    if (resultData === "Unsuccessful") {
        window.alert("Invalid card");
    }
    else if (resultData === "Successful") {
        window.alert("Payment successful")
        emptyCart();
        document.location.href = "index.html";
    }
}


document.getElementById("paymentForm").addEventListener("submit", function(event) {
    event.preventDefault();
    //let formData = new FormData(event.target);

    $.ajax({
        method: "POST",
        dataType: "text",
        //data: Object.fromEntries(formData),
        //processData: false,
        //contentType: false,
        //url: "api/payment",
        url: `api/payment?firstName=${$("#firstName").val()}&lastName=${$("#lastName").val()}&expiration=${$("#expirationDate").val()}&id=${$("#creditCardNumber").val()}`,
        success: (resultData) => {
            handlePaymentResult(resultData)
        }
    }
    ); 
})
