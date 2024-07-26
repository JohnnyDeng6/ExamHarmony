function changePassword() {
    const currentPath = window.location.pathname;
    const basePath = currentPath.substring(0, currentPath.lastIndexOf('/'));
    const newUrl = basePath + "/sendPrt";

    fetch(newUrl, {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(result => {
            if (result.trim() === 'success') {
                alert("A link has been sent to your email");
                location.replace(basePath + "/home");
            } else {
                alert("An error occurred: " + result);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("An error occurred: " + error.message);
        });
}

async function logout() {

    if (confirm("Are you sure you want to log out?")) {
        try {
            const response = await fetch("/logout");
            if (!response.ok) {
                throw new Error("Error: Could not log out");
            }
            window.location.reload();
        } catch (err) {
            console.log(err);
        }
    }

}
let logOut = document.querySelector(".log-out-button");
logOut.addEventListener("click", async ()=>{
    if(confirm("Are you sure you want to log out?")){
        try{
            const response = await fetch("/logout");
            if(!response.ok){
                throw new Error("Error: Could not log out");
            }
            window.location.reload();
        } catch (err){
            console.log(err);
        }
    }
});

function goHome() {
    const url = /*[[@{/home}]]*/ '/home';
    window.location.href = url;
    // const currentPath = window.location.pathname;
    // const basePath = currentPath.substring(0, currentPath.lastIndexOf('/'));
    // location.replace(basePath + "/home");
}
