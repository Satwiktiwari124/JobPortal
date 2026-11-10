// ==================== LOGIN ====================

const loginForm = document.getElementById("loginForm");

if (loginForm) {
    const messageBox = document.getElementById("loginMessage");
    const loginButton = document.getElementById("loginBtn");

    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value;

        loginButton.disabled = true;
        loginButton.textContent = "Logging in...";
        messageBox.className = "alert d-none";
        messageBox.textContent = "";

        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                throw new Error(
                    response.status === 401
                        ? "Invalid username or password."
                        : (data.message || `Login failed (${response.status}).`)
                );
            }

            if (!data.token) {
                throw new Error("JWT token was not received.");
            }

            // Normalize ROLE_USER / USER and ROLE_RECRUITER / RECRUITER
            const role = String(data.role || "")
                .trim()
                .toUpperCase();

            const normalizedRole = role.replace(/^ROLE_/, "");

            // Do not redirect if the backend role is missing or unexpected.
            if (normalizedRole !== "USER" && normalizedRole !== "RECRUITER") {
                throw new Error(
                    "Role missing or invalid. Please check the account role."
                );
            }

            sessionStorage.setItem("jwtToken", data.token);
            sessionStorage.setItem("username", data.username || username);
            sessionStorage.setItem("role", normalizedRole);

            // Redirect according to the role returned by the backend.
            if (normalizedRole === "RECRUITER") {
                window.location.replace("/recruiter.html");
            } else {
                window.location.replace("/index.html");
            }

        } catch (error) {
            messageBox.className = "alert alert-danger";
            messageBox.textContent =
                error.message || "Something went wrong. Please try again.";
        } finally {
            loginButton.disabled = false;
            loginButton.textContent = "Login";
        }
    });
}


// ==================== REGISTER ====================

const registerForm = document.getElementById("registerForm");

if (registerForm) {
    const roleSelect = document.getElementById("accountRole");
    const recruiterCodeGroup = document.getElementById("recruiterCodeGroup");
    if (roleSelect && recruiterCodeGroup) {
        roleSelect.addEventListener("change", () => {
            recruiterCodeGroup.classList.toggle("d-none", roleSelect.value !== "RECRUITER");
        });
    }

    const messageBox = document.getElementById("registerMessage");
    const registerButton = document.getElementById("registerBtn");

    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        registerButton.disabled = true;
        registerButton.textContent = "Creating Account...";
        messageBox.className = "alert d-none";
        messageBox.textContent = "";

        try {
            const selectedRole = roleSelect ? roleSelect.value : "USER";
            const headers = { "Content-Type": "application/json" };
            let endpoint = "/auth/register";
            if (selectedRole === "RECRUITER") {
                endpoint = "/auth/register/recruiter";
                headers["X-Recruiter-Invite-Code"] =
                    document.getElementById("recruiterCode").value;
            }
            const response = await fetch(endpoint, {
                method: "POST",
                headers,
                body: JSON.stringify({ username, email, password })
            });

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                let errorMessage =
                    data.message ||
                    data.error ||
                    `Registration failed (${response.status}).`;

                if (data.errors && typeof data.errors === "object") {
                    errorMessage = Object.entries(data.errors)
                        .map(([field, message]) => `${field}: ${message}`)
                        .join(" | ");
                }

                throw new Error(errorMessage);
            }

            messageBox.className = "alert alert-success";
            messageBox.textContent =
                "Account created successfully! You can now log in.";

            registerForm.reset();

        } catch (error) {
            messageBox.className = "alert alert-danger";
            messageBox.textContent =
                error.message || "Something went wrong. Please try again.";
        } finally {
            registerButton.disabled = false;
            registerButton.textContent = "Create Account";
        }
    });
}