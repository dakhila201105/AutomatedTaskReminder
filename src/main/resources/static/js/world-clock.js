(function () {
    const timeEl = document.getElementById("clock-time");
    const labelEl = document.getElementById("clock-label");
    const tzSelect = document.getElementById("timezone-select");

    if (!timeEl || !labelEl || !tzSelect) return;

    function updateClock() {
        const tz = tzSelect.value;
        const now = new Date();

        const options = {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            hour12: false
        };

        if (tz !== "local") {
            options.timeZone = tz;
        }

        timeEl.textContent = now.toLocaleTimeString([], options);
        labelEl.textContent = tz === "local"
            ? "Local"
            : tz.split("/")[1].replace("_", " ");
    }

    tzSelect.addEventListener("change", updateClock);

    setInterval(updateClock, 1000);
    updateClock();
})();
