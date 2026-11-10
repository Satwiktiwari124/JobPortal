
const jobsGrid = document.getElementById("jobsGrid");
const jobsMessage = document.getElementById("jobsMessage");
const jobCount = document.getElementById("jobCount");
const searchForm = document.getElementById("searchForm");
const keywordInput = document.getElementById("keyword");
const locationInput = document.getElementById("location");
const clearSearchBtn = document.getElementById("clearSearchBtn");
const currentYear = document.getElementById("currentYear");

let allJobs = [];

const authToken = sessionStorage.getItem("jwtToken");
const authUsername = sessionStorage.getItem("username");
const authRole = sessionStorage.getItem("role");

const guestAuthLinks = document.getElementById("guestAuthLinks");
const userAuthLinks = document.getElementById("userAuthLinks");
const navRecruiterLink = document.getElementById("navRecruiterLink");
const navUsername = document.getElementById("navUsername");

if (authToken) {
    guestAuthLinks?.classList.add("d-none");
    userAuthLinks?.classList.remove("d-none");

    if (navUsername) {
        navUsername.textContent = authUsername || "Candidate";
    }

    if (navRecruiterLink) {
        navRecruiterLink.classList.toggle(
            "d-none",
            String(authRole).replace(/^ROLE_/, "") !== "RECRUITER"
        );
    }
}

// Candidate logout
const candidateLogoutBtn = document.getElementById("logoutBtn");

if (candidateLogoutBtn) {
    candidateLogoutBtn.addEventListener("click", async () => {
        try {
            await fetch("/auth/logout", {
                method: "POST",
                headers: authHeaders()
            });
        } catch (_) {}

        sessionStorage.removeItem("jwtToken");
        sessionStorage.removeItem("username");
        sessionStorage.removeItem("role");

        window.location.replace("/login.html");
    });
}

// Authorization headers
function authHeaders(json = false) {
    const headers = {};

    if (authToken) {
        headers.Authorization = `Bearer ${authToken}`;
    }

    if (json) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

// Parse API response
async function parseApiResponse(response) {
    const raw = await response.text();

    if (!raw) return null;

    try {
        return JSON.parse(raw);
    } catch {
        return { message: raw };
    }
}

// Dashboard message
function showDashboardMessage(message, type = "success") {
    const box = document.getElementById("candidateDashboardMessage");

    if (!box) return;

    box.className = `alert alert-${type}`;
    box.textContent = message;
    box.classList.remove("d-none");
}

// Set current year
if (currentYear) {
    currentYear.textContent = new Date().getFullYear();
}

// Apply for job
async function applyForJob(jobId, button) {
    if (!authToken) {
        window.location.href = "/login.html";
        return;
    }

    button.disabled = true;

    try {
        const response = await fetch(`/applications/jobs/${jobId}`, {
            method: "POST",
            headers: authHeaders()
        });

        const data = await parseApiResponse(response);

        if (!response.ok) {
            throw new Error(
                data?.message || `Unable to apply (${response.status})`
            );
        }

        button.textContent = "Applied";

        showDashboardMessage(
            "Your application has been submitted successfully.",
            "success"
        );

        loadMyApplications();

    } catch (error) {
        showDashboardMessage(error.message, "danger");
        button.disabled = false;
    }
}

// Load candidate profile
async function loadMyProfile() {
    if (!authToken) return;

    try {
        const response = await fetch("/auth/me", {
            headers: authHeaders()
        });

        const data = await parseApiResponse(response);

        if (!response.ok) {
            throw new Error(data?.message || "Unable to load profile");
        }

        const profileUsername = document.getElementById("profileUsername");
        const profileEmail = document.getElementById("profileEmail");

        if (profileUsername) {
            profileUsername.value = data.username || "";
        }

        if (profileEmail) {
            profileEmail.value = data.email || "";
        }

        const nav = document.getElementById("navUsername");

        if (nav) {
            nav.textContent = data.username || authUsername || "Candidate";
        }

    } catch (error) {
        showDashboardMessage(error.message, "danger");
    }
}

// Load candidate applications
async function loadMyApplications() {
    const body = document.getElementById("myApplicationsBody");

    if (!body || !authToken) return;

    try {
        const response = await fetch("/applications/my", {
            headers: authHeaders()
        });

        const data = await parseApiResponse(response);

        if (!response.ok) {
            throw new Error(data?.message || "Unable to load applications");
        }

        body.replaceChildren();

        if (!Array.isArray(data) || data.length === 0) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 5;
            cell.className = "text-secondary";
            cell.textContent = "You have not applied to any jobs yet.";

            row.appendChild(cell);
            body.appendChild(row);

            return;
        }

        data.forEach(application => {
            const row = document.createElement("tr");

            [
                application.jobTitle,
                application.company,
                application.location,
                application.appliedAt
                    ? new Date(application.appliedAt).toLocaleDateString()
                    : "—",
                application.status
            ].forEach((value, index) => {
                const cell = document.createElement("td");

                cell.textContent = value || "—";

                if (index === 4) {
                    const status = String(value || "APPLIED");

                    const badgeColor =
                        status === "SELECTED" ? "success" :
                        status === "REJECTED" ? "danger" :
                        status === "SHORTLISTED" ? "info" :
                        "secondary";

                    const badge = document.createElement("span");
                    badge.className = `badge text-bg-${badgeColor}`;
                    badge.textContent = status;

                    cell.replaceChildren(badge);
                }

                row.appendChild(cell);
            });

            body.appendChild(row);
        });

    } catch (error) {
        body.innerHTML = '<tr><td colspan="5" class="text-danger"></td></tr>';
        body.querySelector("td").textContent = error.message;
    }
}

// Update candidate profile
const profileForm = document.getElementById("profileForm");

if (profileForm) {
    profileForm.addEventListener("submit", async event => {
        event.preventDefault();

        const button = document.getElementById("saveProfileBtn");

        if (button) {
            button.disabled = true;
        }

        try {
            const response = await fetch("/auth/me", {
                method: "PUT",
                headers: authHeaders(true),
                body: JSON.stringify({
                    email: document.getElementById("profileEmail").value.trim()
                })
            });

            const data = await parseApiResponse(response);

            if (!response.ok) {
                throw new Error(data?.message || "Profile update failed");
            }

            document.getElementById("profileEmail").value = data.email;

            showDashboardMessage(
                "Profile updated successfully.",
                "success"
            );

        } catch (error) {
            showDashboardMessage(error.message, "danger");

        } finally {
            if (button) {
                button.disabled = false;
            }
        }
    });
}

// Resume upload
const resumeForm = document.getElementById("resumeForm");

if (resumeForm) {
    resumeForm.addEventListener("submit", async event => {
        event.preventDefault();

        const file = document.getElementById("resumeFile").files[0];

        if (!file) {
            return showDashboardMessage(
                "Please select a PDF resume.",
                "warning"
            );
        }

        const formData = new FormData();
        formData.append("file", file);

        const button = document.getElementById("uploadResumeBtn");

        button.disabled = true;

        try {
            const response = await fetch("/resumes/upload", {
                method: "POST",
                headers: authHeaders(),
                body: formData
            });

            const data = await parseApiResponse(response);

            if (!response.ok) {
                throw new Error(data?.message || "Resume upload failed");
            }

            showDashboardMessage(
                "Resume uploaded successfully: " + data.fileName
            );

            resumeForm.reset();

        } catch (error) {
            showDashboardMessage(error.message, "danger");

        } finally {
            button.disabled = false;
        }
    });
}

// Download resume
const downloadResumeBtn = document.getElementById("downloadResumeBtn");

if (downloadResumeBtn) {
    downloadResumeBtn.addEventListener("click", async () => {
        try {
            const response = await fetch("/resumes/my/download", {
                headers: authHeaders()
            });

            if (!response.ok) {
                const data = await parseApiResponse(response);

                throw new Error(
                    data?.message ||
                    "Resume download failed. Upload a resume first."
                );
            }

            const blob = await response.blob();
            const url = URL.createObjectURL(blob);

            const link = document.createElement("a");
            link.href = url;
            link.download = "my-resume.pdf";

            link.click();

            URL.revokeObjectURL(url);

        } catch (error) {
            showDashboardMessage(error.message, "danger");
        }
    });
}

// Refresh profile and applications
const refreshApplicationsBtn = document.getElementById("refreshApplicationsBtn");

if (refreshApplicationsBtn) {
    refreshApplicationsBtn.addEventListener("click", () => {
        loadMyProfile();
        loadMyApplications();
    });
}

// Load jobs from Spring Boot backend
async function loadJobs() {
    showMessage("Loading available jobs...");

    try {
        const response = await fetch("/jobs", {
            headers: {
                "Accept": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(
                "Unable to load jobs. Status: " + response.status
            );
        }

        const jobs = await response.json();

        if (!Array.isArray(jobs)) {
            throw new Error("Unexpected response from the server.");
        }

        allJobs = jobs.filter(job =>
            job !== null && typeof job === "object"
        );

        applySearch();

    } catch (error) {
        console.error("Error loading jobs:", error);

        jobsGrid.replaceChildren();
        jobCount.textContent = "0";

        showMessage(
            "Unable to load jobs. Please check whether the backend is running."
        );
    }
}

// Show loading, error, or empty-result messages
function showMessage(message) {
    jobsMessage.hidden = false;
    jobsMessage.replaceChildren();

    const text = document.createElement("span");
    text.textContent = message;

    jobsMessage.appendChild(text);
}

// Update Clear Search button visibility
function updateClearButton() {
    const hasSearch =
        keywordInput.value.trim() !== "" ||
        locationInput.value.trim() !== "";

    clearSearchBtn.hidden = !hasSearch;
}

// Display job cards
function renderJobs(jobs) {
    jobsGrid.replaceChildren();
    jobsMessage.hidden = true;

    jobCount.textContent = String(jobs.length);

    if (jobs.length === 0) {
        const message = allJobs.length === 0
            ? "No jobs have been posted yet. Please check back soon."
            : "No matching jobs found. Try a different keyword or location.";

        showMessage(message);
        updateClearButton();

        return;
    }

    const fragment = document.createDocumentFragment();

    jobs.forEach(job => {
        const column = document.createElement("div");
        column.className = "col-md-6 col-xl-4";

        const card = document.createElement("div");
        card.className = "card job-card h-100";

        const body = document.createElement("div");
        body.className = "card-body";

        const topRow = document.createElement("div");
        topRow.className =
            "d-flex justify-content-between align-items-start mb-3";

        const icon = document.createElement("div");
        icon.className = "job-icon";
        icon.setAttribute("aria-hidden", "true");

        const briefcaseIcon = document.createElement("i");
        briefcaseIcon.className = "bi bi-briefcase-fill";

        icon.appendChild(briefcaseIcon);

        const badge = document.createElement("span");
        badge.className = "badge rounded-pill text-bg-light";
        badge.textContent = "Job #" + (job.id ?? "N/A");

        topRow.append(icon, badge);

        const title = document.createElement("h5");
        title.className = "card-title mt-3";
        title.textContent = job.title || "Untitled position";

        const company = document.createElement("p");
        company.className = "job-company mb-3";
        company.textContent = job.company || "Company not specified";

        const location = document.createElement("p");
        location.className = "job-location mb-0";

        const locationIcon = document.createElement("i");
        locationIcon.className = "bi bi-geo-alt me-2";
        locationIcon.setAttribute("aria-hidden", "true");

        const locationText = document.createElement("span");
        locationText.textContent =
            job.location || "Location not specified";

        location.append(locationIcon, locationText);

        body.append(topRow, title, company, location);

        if (
            authToken &&
            String(authRole).replace(/^ROLE_/, "") === "USER"
        ) {
            const applyButton = document.createElement("button");

            applyButton.type = "button";
            applyButton.className = "btn btn-primary btn-sm mt-3";
            applyButton.textContent = "Apply Now";

            applyButton.addEventListener("click", () => {
                applyForJob(job.id, applyButton);
            });

            body.appendChild(applyButton);
        }

        card.appendChild(body);
        column.appendChild(card);
        fragment.appendChild(column);
    });

    jobsGrid.appendChild(fragment);

    updateClearButton();
}

// Filter jobs by keyword and location
function searchJobs(event) {
    if (event && event.type === "submit") {
        event.preventDefault();
    }

    applySearch();
}

// Apply search filters
function applySearch() {
    const keyword = keywordInput.value.trim().toLowerCase();
    const location = locationInput.value.trim().toLowerCase();

    const filteredJobs = allJobs.filter(job => {
        const title = String(job.title ?? "").toLowerCase();
        const company = String(job.company ?? "").toLowerCase();
        const jobLocation = String(job.location ?? "").toLowerCase();

        const matchesKeyword =
            !keyword ||
            title.includes(keyword) ||
            company.includes(keyword);

        const matchesLocation =
            !location || jobLocation.includes(location);

        return matchesKeyword && matchesLocation;
    });

    renderJobs(filteredJobs);
}

// Clear filters
function clearSearch() {
    keywordInput.value = "";
    locationInput.value = "";

    applySearch();
    keywordInput.focus();
}

// Form submission
searchForm.addEventListener("submit", searchJobs);

// Automatic filtering while typing
keywordInput.addEventListener("input", searchJobs);
locationInput.addEventListener("input", searchJobs);

// Clear search button
clearSearchBtn.addEventListener("click", clearSearch);

// Load jobs and candidate dashboard data
loadJobs();
loadMyProfile();
loadMyApplications();
