
const token = sessionStorage.getItem("jwtToken");
const username = sessionStorage.getItem("username");

const createJobForm = document.getElementById("createJobForm");
const createJobBtn = document.getElementById("createJobBtn");
const dashboardMessage = document.getElementById("dashboardMessage");
const myJobsMessage = document.getElementById("myJobsMessage");
const myJobsGrid = document.getElementById("myJobsGrid");
const totalJobs = document.getElementById("totalJobs");
const currentUsername = document.getElementById("currentUsername");
const refreshJobsBtn = document.getElementById("refreshJobsBtn");
const logoutBtn = document.getElementById("logoutBtn");

let myJobs = [];

// Authentication check
if (!token || !username) {
    window.location.replace("/login.html");
}

// Display current username
currentUsername.textContent = username || "Recruiter";

// Common API headers
function getHeaders(includeJson = false) {
    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (includeJson) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

// Read response safely, including empty responses
async function readResponse(response) {
    const text = await response.text();

    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
}

// Prepare useful error messages
function getErrorMessage(data, status) {
    if (data && data.errors && typeof data.errors === "object") {
        return Object.values(data.errors).join(" | ");
    }

    if (data && data.message) {
        return data.message;
    }

    if (status === 401) {
        return "Session expired. Please log in again.";
    }

    if (status === 403) {
        return "You do not have permission to perform this action.";
    }

    return `Request failed (${status}).`;
}

// Display dashboard success/error messages
function showDashboardMessage(message, type = "success") {
    dashboardMessage.className = `alert alert-${type}`;
    dashboardMessage.textContent = message;
    dashboardMessage.hidden = false;
}

// Handle authentication errors
function handleUnauthorized(status) {
    if (status === 401) {
        sessionStorage.removeItem("jwtToken");
        sessionStorage.removeItem("username");
        window.location.replace("/login.html");
        return true;
    }

    return false;
}

// Load jobs from the Spring Boot API
async function loadMyJobs() {
    myJobsMessage.hidden = false;
    myJobsMessage.textContent = "Loading your job listings...";

    myJobsGrid.replaceChildren();

    try {
        const response = await fetch("/jobs", {
            method: "GET",
            headers: getHeaders()
        });

        const data = await readResponse(response);

        if (handleUnauthorized(response.status)) {
            return;
        }

        if (!response.ok) {
            throw new Error(getErrorMessage(data, response.status));
        }

        if (!Array.isArray(data)) {
            throw new Error("Unexpected response from the server.");
        }

        // The API returns JobResponseDTO objects with recruiterUsername.
        // Filter here for display; backend ownership checks remain essential.
        myJobs = data.filter(job =>
            job.recruiterUsername === username
        );

        renderMyJobs();

    } catch (error) {
        myJobsMessage.hidden = false;
        myJobsMessage.textContent = error.message;
        totalJobs.textContent = "—";
        console.error("Loading jobs failed:", error);
    }
}

// Render only this recruiter's jobs
function renderMyJobs() {
    myJobsGrid.replaceChildren();

    totalJobs.textContent = myJobs.length;

    if (myJobs.length === 0) {
        myJobsMessage.hidden = false;
        myJobsMessage.textContent =
            "You have no job listings yet. Create your first job above.";
        return;
    }

    myJobsMessage.hidden = true;

    myJobs.forEach(job => {
        const column = document.createElement("div");
        column.className = "col-md-6 col-xl-4";

        const card = document.createElement("div");
        card.className = "card job-card h-100";

        const body = document.createElement("div");
        body.className = "card-body";

        const icon = document.createElement("div");
        icon.className = "job-icon mb-3";

        const iconElement = document.createElement("i");
        iconElement.className = "bi bi-briefcase-fill";
        icon.appendChild(iconElement);

        const title = document.createElement("h5");
        title.className = "card-title";
        title.textContent = job.title || "Untitled job";

        const company = document.createElement("p");
        company.className = "job-company";
        company.textContent = job.company || "Company not specified";

        const location = document.createElement("p");
        location.className = "job-location";

        const locationIcon = document.createElement("i");
        locationIcon.className = "bi bi-geo-alt";

        const locationText = document.createElement("span");
        locationText.textContent = job.location || "Location not specified";

        location.append(locationIcon, locationText);

        const jobId = document.createElement("small");
        jobId.className = "text-secondary d-block mb-3";
        jobId.textContent = `Job ID: ${job.id}`;

        const actions = document.createElement("div");
        actions.className = "d-flex gap-2 mt-3";

        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.className = "btn btn-outline-primary btn-sm flex-grow-1";
        editButton.textContent = "Edit";
        editButton.addEventListener("click", () => editJob(job));

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "btn btn-outline-danger btn-sm flex-grow-1";
        deleteButton.textContent = "Delete";
        deleteButton.addEventListener("click", () => deleteJob(job));

        actions.append(editButton, deleteButton);
        body.append(icon, title, company, location, jobId, actions);
        card.appendChild(body);
        column.appendChild(card);
        myJobsGrid.appendChild(column);
    });
}

// Create a new job
createJobForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const title = document.getElementById("jobTitle").value.trim();
    const company = document.getElementById("company").value.trim();
    const location = document.getElementById("jobLocation").value.trim();
    const description = document.getElementById("jobDescription").value.trim();

    if (!title || !company || !location) {
        showDashboardMessage(
            "Please fill in all job details.",
            "warning"
        );
        return;
    }

    createJobBtn.disabled = true;
    createJobBtn.textContent = "Publishing...";

    try {
        const response = await fetch("/jobs", {
            method: "POST",
            headers: getHeaders(true),
            body: JSON.stringify({
                title: title,
                company: company,
                location: location,
                description: description
            })
        });

        const data = await readResponse(response);

        if (handleUnauthorized(response.status)) {
            return;
        }

        if (!response.ok) {
            throw new Error(getErrorMessage(data, response.status));
        }

        createJobForm.reset();

        showDashboardMessage(
            "Job published successfully!",
            "success"
        );

        await loadMyJobs();

    } catch (error) {
        showDashboardMessage(error.message, "danger");
        console.error("Creating job failed:", error);
    } finally {
        createJobBtn.disabled = false;
        createJobBtn.innerHTML =
            '<i class="bi bi-plus-circle me-2"></i>Publish Job';
    }
});

// Edit an existing job
async function editJob(job) {
    const title = prompt("Enter job title:", job.title || "");
    if (title === null) return;

    const company = prompt("Enter company name:", job.company || "");
    if (company === null) return;

    const location = prompt("Enter job location:", job.location || "");
    if (location === null) return;
    const description = prompt("Enter job description:", job.description || "");
    if (description === null) return;

    if (!title.trim() || !company.trim() || !location.trim()) {
        showDashboardMessage(
            "Title, company and location are required.",
            "warning"
        );
        return;
    }

    try {
        const response = await fetch(`/jobs/${encodeURIComponent(job.id)}`, {
            method: "PUT",
            headers: getHeaders(true),
            body: JSON.stringify({
                title: title.trim(),
                company: company.trim(),
                location: location.trim(),
                description: description.trim()
            })
        });

        const data = await readResponse(response);

        if (handleUnauthorized(response.status)) {
            return;
        }

        if (!response.ok) {
            throw new Error(getErrorMessage(data, response.status));
        }

        showDashboardMessage("Job updated successfully!", "success");
        await loadMyJobs();

    } catch (error) {
        showDashboardMessage(error.message, "danger");
        console.error("Updating job failed:", error);
    }
}

// Delete an existing job
async function deleteJob(job) {
    const confirmed = confirm(
        `Are you sure you want to delete "${job.title}"?`
    );

    if (!confirmed) return;

    try {
        const response = await fetch(`/jobs/${encodeURIComponent(job.id)}`, {
            method: "DELETE",
            headers: getHeaders()
        });

        const data = await readResponse(response);

        if (handleUnauthorized(response.status)) {
            return;
        }

        if (!response.ok) {
            throw new Error(getErrorMessage(data, response.status));
        }

        showDashboardMessage("Job deleted successfully!", "success");
        await loadMyJobs();

    } catch (error) {
        showDashboardMessage(error.message, "danger");
        console.error("Deleting job failed:", error);
    }
}

// Refresh the dashboard
refreshJobsBtn.addEventListener("click", loadMyJobs);

// Logout
logoutBtn.addEventListener("click", async function () {
    try {
        await fetch("/auth/logout", { method: "POST", headers: getHeaders() });
    } catch (_) {
        // Clear the client session even if the logout request cannot reach the server.
    }
    sessionStorage.removeItem("jwtToken");
    sessionStorage.removeItem("username");
    sessionStorage.removeItem("role");
    window.location.replace("/login.html");
});

// Initial load
if (token && username) {
    loadMyJobs();
}


// ==================== APPLICANT MANAGEMENT ====================
const applicantsTableBody = document.getElementById("applicantsTableBody");
const applicantsMessage = document.getElementById("applicantsMessage");
const refreshApplicantsBtn = document.getElementById("refreshApplicantsBtn");

async function loadApplicants() {
    if (!applicantsTableBody) return;
    applicantsMessage.textContent = "Loading applicants...";
    applicantsTableBody.replaceChildren();
    try {
        const response = await fetch("/applications/recruiter", { headers: getHeaders() });
        const data = await readResponse(response);
        if (handleUnauthorized(response.status)) return;
        if (!response.ok) throw new Error(getErrorMessage(data, response.status));
        if (!Array.isArray(data) || data.length === 0) {
            applicantsMessage.textContent = "No applications received yet.";
            return;
        }
        applicantsMessage.textContent = `${data.length} application(s) received.`;
        data.forEach(application => {
            const row = document.createElement("tr");
            const values = [
                application.candidateUsername,
                application.candidateEmail,
                `${application.jobTitle || "Job"} at ${application.company || "—"}`,
                application.appliedAt ? new Date(application.appliedAt).toLocaleDateString() : "—"
            ];
            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value || "—";
                row.appendChild(cell);
            });
            const resumeCell = document.createElement("td");
            const resumeButton = document.createElement("button");
            resumeButton.type = "button";
            resumeButton.className = "btn btn-sm btn-outline-primary";
            resumeButton.textContent = "Download";
            resumeButton.addEventListener("click", async () => {
                try {
                    const resumeResponse = await fetch(`/resumes/applications/${application.applicationId}/download`, {
                        headers: getHeaders()
                    });
                    if (!resumeResponse.ok) {
                        const errorData = await readResponse(resumeResponse);
                        throw new Error(getErrorMessage(errorData, resumeResponse.status));
                    }
                    const blob = await resumeResponse.blob();
                    const url = URL.createObjectURL(blob);
                    const link = document.createElement("a");
                    link.href = url; link.download = `candidate-${application.applicationId}-resume.pdf`;
                    link.click(); URL.revokeObjectURL(url);
                } catch (error) {
                    applicantsMessage.textContent = error.message;
                    applicantsMessage.className = "text-danger py-2";
                }
            });
            resumeCell.appendChild(resumeButton);
            row.appendChild(resumeCell);

            const statusCell = document.createElement("td");
            const statusSelect = document.createElement("select");
            statusSelect.className = "form-select form-select-sm";
            ["APPLIED", "SHORTLISTED", "REJECTED", "SELECTED"].forEach(status => {
                const option = document.createElement("option");
                option.value = status; option.textContent = status;
                option.selected = status === application.status;
                statusSelect.appendChild(option);
            });
            statusCell.appendChild(statusSelect);
            row.appendChild(statusCell);

            const actionCell = document.createElement("td");
            const saveButton = document.createElement("button");
            saveButton.type = "button"; saveButton.className = "btn btn-sm btn-primary";
            saveButton.textContent = "Save";
            saveButton.addEventListener("click", async () => {
                saveButton.disabled = true;
                try {
                    const updateResponse = await fetch(`/applications/${application.applicationId}/status`, {
                        method: "PATCH", headers: getHeaders(true),
                        body: JSON.stringify({ status: statusSelect.value })
                    });
                    const result = await readResponse(updateResponse);
                    if (!updateResponse.ok) throw new Error(getErrorMessage(result, updateResponse.status));
                    applicantsMessage.textContent = "Application status updated successfully.";
                    applicantsMessage.className = "text-success py-2";
                } catch (error) {
                    applicantsMessage.textContent = error.message;
                    applicantsMessage.className = "text-danger py-2";
                } finally { saveButton.disabled = false; }
            });
            actionCell.appendChild(saveButton); row.appendChild(actionCell);
            applicantsTableBody.appendChild(row);
        });
    } catch (error) {
        applicantsMessage.textContent = error.message;
        applicantsMessage.className = "text-danger py-2";
    }
}

if (refreshApplicantsBtn) refreshApplicantsBtn.addEventListener("click", loadApplicants);
loadApplicants();
