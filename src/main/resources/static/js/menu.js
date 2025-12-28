document.addEventListener("DOMContentLoaded", () => {
    const button = document.getElementById("menuButton");
    const dropdown = document.getElementById("userDropdown");

    button.addEventListener("click", (e) => {
        e.stopPropagation();
        dropdown.classList.toggle("show");
    });

    document.addEventListener("click", () => {
        dropdown.classList.remove("show");
    });
})