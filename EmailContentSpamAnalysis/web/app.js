// State
let accounts = [];
let currentAccount = "user1@gmail.com";
let currentEmails = [];
let emailAnalysisCache = {};

// DOM Elements
const accountListContainer = document.getElementById("accountListContainer");
const accountCountBadge = document.getElementById("accountCountBadge");
const accountSearch = document.getElementById("accountSearch");
const currentAccountEmail = document.getElementById("currentAccountEmail");
const currentAccountMeta = document.getElementById("currentAccountMeta");
const emailCardsContainer = document.getElementById("emailCardsContainer");
const totalDbStats = document.getElementById("totalDbStats");

// Modal Elements
const emailModal = document.getElementById("emailModal");
const modalCloseBtn = document.getElementById("modalCloseBtn");
const modalSubject = document.getElementById("modalSubject");
const modalVerdictBadge = document.getElementById("modalVerdictBadge");
const modalBodyContainer = document.getElementById("modalBodyContainer");

// Scanner Elements
const scannerSubject = document.getElementById("scannerSubject");
const scannerBody = document.getElementById("scannerBody");
const templatePreset = document.getElementById("templatePreset");
const btnRunScanner = document.getElementById("btnRunScanner");
const scannerResultContainer = document.getElementById("scannerResultContainer");

// Benchmark Elements
const btnCompareUserEmails = document.getElementById("btnCompareUserEmails");
const btnRunBenchmarkAgain = document.getElementById("btnRunBenchmarkAgain");
const benchmarkTableBody = document.getElementById("benchmarkTableBody");
const fastestStringName = document.getElementById("fastestStringName");
const overallFastestName = document.getElementById("overallFastestName");

// Compose Elements
const composeEmailForm = document.getElementById("composeEmailForm");
const composeUser = document.getElementById("composeUser");
const composeSubject = document.getElementById("composeSubject");
const composeBody = document.getElementById("composeBody");
const composeStatusAlert = document.getElementById("composeStatusAlert");

// --- Presets ---
const PRESETS = {
    crypto: {
        subject: "Guaranteed profit in 7 days",
        body: "Invest now and double your money in 7 days. Guaranteed returns with our crypto plan. Join at http://free-gift.click/offer."
    },
    kyc: {
        subject: "Urgent: Your Bank KYC has expired",
        body: "Dear customer, your bank account verification is pending. Verify your password and account details at http://secure-login.click/verify immediately or account will be suspended."
    },
    prize: {
        subject: "Congratulations! Claim your reward now",
        body: "You are the lucky winner of our lottery draw! Claim your free prize of $10,000 and cash bonus now at http://win-cash.click/reward."
    },
    clean: {
        subject: "Design Review Notes & Next Steps",
        body: "Hi team, thank you for attending today's project presentation. The notes from the meeting have been attached. Let me know if you have any questions."
    }
};

// --- Initialization ---
document.addEventListener("DOMContentLoaded", () => {
    setupTabs();
    loadAccounts();

    // Event Listeners
    accountSearch.addEventListener("input", filterAccounts);
    document.getElementById("btnRefresh").addEventListener("click", () => {
        loadAccounts();
        loadUserEmails(currentAccount);
    });

    document.getElementById("btnScanAllUserEmails").addEventListener("click", scanAllUserEmails);
    btnCompareUserEmails.addEventListener("click", () => {
        switchTab("benchmarkTab");
        runBenchmark(currentAccount);
    });
    btnRunBenchmarkAgain.addEventListener("click", () => runBenchmark(currentAccount));

    // Scanner presets
    templatePreset.addEventListener("change", (e) => {
        const val = e.target.value;
        if (PRESETS[val]) {
            scannerSubject.value = PRESETS[val].subject;
            scannerBody.value = PRESETS[val].body;
        }
    });

    btnRunScanner.addEventListener("click", runCustomScanner);

    // Modal
    modalCloseBtn.addEventListener("click", closeModal);
    emailModal.addEventListener("click", (e) => {
        if (e.target === emailModal) closeModal();
    });

    // Compose Form
    composeEmailForm.addEventListener("submit", handleComposeSubmit);
});

// --- Tab Management ---
function setupTabs() {
    const tabButtons = document.querySelectorAll(".tab-btn");
    tabButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const targetId = btn.getAttribute("data-tab");
            switchTab(targetId);
        });
    });
}

function switchTab(tabId) {
    document.querySelectorAll(".tab-btn").forEach(b => {
        b.classList.toggle("active", b.getAttribute("data-tab") === tabId);
    });
    document.querySelectorAll(".tab-pane").forEach(p => {
        p.classList.toggle("active", p.id === tabId);
    });
}

// --- Load Accounts ---
async function loadAccounts() {
    try {
        const res = await fetch("/api/accounts");
        if (!res.ok) throw new Error("Failed to load accounts");
        accounts = await res.json();
        accountCountBadge.textContent = accounts.length;

        let totalMsgs = accounts.reduce((acc, a) => acc + a.count, 0);
        totalDbStats.textContent = `${totalMsgs.toLocaleString()} Emails`;

        renderAccounts(accounts);

        if (accounts.length > 0) {
            // Select first account
            selectAccount(accounts[0].email);
        }
    } catch (err) {
        accountListContainer.innerHTML = `<div class="empty-state">Unable to load accounts: ${err.message}</div>`;
    }
}

function renderAccounts(list) {
    if (!list || list.length === 0) {
        accountListContainer.innerHTML = `<div class="empty-state">No matching accounts found</div>`;
        return;
    }

    accountListContainer.innerHTML = list.slice(0, 100).map(a => `
        <div class="account-item ${a.email === currentAccount ? 'active' : ''}" data-email="${a.email}">
            <span class="account-email" title="${a.email}">${a.email}</span>
            <span class="account-msg-count">${a.count}</span>
        </div>
    `).join("");

    accountListContainer.querySelectorAll(".account-item").forEach(item => {
        item.addEventListener("click", () => {
            const email = item.getAttribute("data-email");
            selectAccount(email);
        });
    });
}

function filterAccounts() {
    const query = accountSearch.value.trim().toLowerCase();
    const filtered = accounts.filter(a => a.email.toLowerCase().includes(query));
    renderAccounts(filtered);
}

// --- Select Account & Load Emails ---
function selectAccount(email) {
    currentAccount = email;
    currentAccountEmail.textContent = email;
    composeUser.value = email;

    accountListContainer.querySelectorAll(".account-item").forEach(item => {
        item.classList.toggle("active", item.getAttribute("data-email") === email);
    });

    loadUserEmails(email);
}

async function loadUserEmails(email) {
    emailCardsContainer.innerHTML = `<div class="loading-state">Loading messages for ${email}...</div>`;
    try {
        const res = await fetch(`/api/emails?user=${encodeURIComponent(email)}`);
        if (!res.ok) throw new Error("Failed to fetch messages");
        currentEmails = await res.json();

        currentAccountMeta.textContent = `${currentEmails.length} messages found in database`;

        renderEmailCards(currentEmails);
    } catch (err) {
        emailCardsContainer.innerHTML = `<div class="empty-state">Error: ${err.message}</div>`;
    }
}

function renderEmailCards(emails) {
    if (!emails || emails.length === 0) {
        emailCardsContainer.innerHTML = `<div class="empty-state">No messages stored for this user account.</div>`;
        return;
    }

    emailCardsContainer.innerHTML = emails.map(e => `
        <div class="email-card" data-msgid="${e.id}">
            <div class="email-card-header">
                <span class="email-card-id">Message #${e.id}</span>
                <div class="card-badges" id="badges-${e.id}">
                    <span class="badge" style="opacity:0.6;">Analyzing...</span>
                </div>
            </div>
            <h3 class="email-card-subject">${escapeHtml(e.subject)}</h3>
            <p class="email-card-preview">${escapeHtml(e.body)}</p>
        </div>
    `).join("");

    // Hook click to open inspection modal
    emailCardsContainer.querySelectorAll(".email-card").forEach(card => {
        card.addEventListener("click", () => {
            const msgId = parseInt(card.getAttribute("data-msgid"));
            const email = currentEmails.find(em => em.id === msgId);
            if (email) openInspectionModal(email);
        });
    });

    // Auto-scan each card
    emails.forEach(e => autoScanEmailBadge(e));
}

async function autoScanEmailBadge(email) {
    const cacheKey = `${email.user}_${email.id}`;
    let analysis = emailAnalysisCache[cacheKey];

    if (!analysis) {
        try {
            const res = await fetch("/api/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ subject: email.subject, body: email.body })
            });
            if (res.ok) {
                analysis = await res.json();
                emailAnalysisCache[cacheKey] = analysis;
            }
        } catch (ignored) {}
    }

    const badgeContainer = document.getElementById(`badges-${email.id}`);
    if (badgeContainer && analysis) {
        const isSpam = analysis.isSpam;
        const riskClass = analysis.riskLevel === 'HIGH' ? 'badge-risk-high' :
                         (analysis.riskLevel === 'MEDIUM' ? 'badge-risk-med' : 'badge-risk-low');

        badgeContainer.innerHTML = `
            <span class="${isSpam ? 'badge-spam' : 'badge-clean'}">${analysis.classification}</span>
            <span class="${riskClass}">${analysis.riskLevel} RISK</span>
            ${analysis.category !== 'NOT SPAM' ? `<span class="badge" style="font-size:0.72rem;">${analysis.category}</span>` : ''}
        `;
    }
}

// --- Inspection Modal ---
async function openInspectionModal(email) {
    modalSubject.textContent = `Message #${email.id}: ${email.subject}`;
    modalBodyContainer.innerHTML = `<div class="loading-state">Evaluating all 9 algorithms...</div>`;
    emailModal.classList.add("active");

    const cacheKey = `${email.user}_${email.id}`;
    let analysis = emailAnalysisCache[cacheKey];

    if (!analysis) {
        try {
            const res = await fetch("/api/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ subject: email.subject, body: email.body })
            });
            analysis = await res.json();
            emailAnalysisCache[cacheKey] = analysis;
        } catch (err) {
            modalBodyContainer.innerHTML = `<div class="alert-box">Analysis failed: ${err.message}</div>`;
            return;
        }
    }

    renderAnalysisDetails(modalBodyContainer, analysis, email.body);
    modalVerdictBadge.className = `badge ${analysis.isSpam ? 'badge-spam' : 'badge-clean'}`;
    modalVerdictBadge.textContent = analysis.classification;
}

function closeModal() {
    emailModal.classList.remove("active");
}

function renderAnalysisDetails(container, a, originalBody) {
    container.innerHTML = `
        <div class="analysis-score-banner ${a.isSpam ? 'spam' : 'clean'}">
            <div>
                <span class="score-title">${a.classification} (Score: ${a.score}/12)</span>
                <p class="score-meta">Category: <strong>${a.category}</strong> | Confidence: <strong>${a.confidence.toFixed(1)}%</strong> | Risk: <strong>${a.riskLevel}</strong></p>
            </div>
            <div style="text-align:right;">
                <span class="badge ${a.isSpam ? 'badge-spam' : 'badge-clean'}">${a.isSpam ? 'THREAT DETECTED' : 'LEGITIMATE EMAIL'}</span>
            </div>
        </div>

        <div class="form-group">
            <label>Raw Email Content</label>
            <div style="background:rgba(255,255,255,0.03); padding:12px; border-radius:6px; font-size:0.88rem; border:1px solid var(--border-color); color:#e5e7eb;">
                ${escapeHtml(originalBody)}
            </div>
        </div>

        <h4 style="margin: 18px 0 10px 0; font-size:0.95rem; font-weight:700;">Multi-Algorithm Breakdown</h4>
        <div class="algorithm-grid">
            <div class="algo-stat-box">
                <div class="algo-stat-name">1. KMP Search (Prefix LPS)</div>
                <div class="algo-stat-value">${a.kmpMatches.length} Matches</div>
                <div class="keyword-chips">${renderChips(a.kmpMatches)}</div>
            </div>
            <div class="algo-stat-box">
                <div class="algo-stat-name">2. Rabin-Karp (Rolling Hash)</div>
                <div class="algo-stat-value">${a.rabinKarpMatches.length} Matches</div>
                <div class="keyword-chips">${renderChips(a.rabinKarpMatches)}</div>
            </div>
            <div class="algo-stat-box">
                <div class="algo-stat-name">3. Z-Algorithm (Z-Box)</div>
                <div class="algo-stat-value">${a.zMatches.length} Matches</div>
                <div class="keyword-chips">${renderChips(a.zMatches)}</div>
            </div>
            <div class="algo-stat-box" style="border-color: rgba(99,102,241,0.4); background:rgba(99,102,241,0.06);">
                <div class="algo-stat-name" style="color:var(--primary);">4. Aho-Corasick (Trie BFS - Single Pass)</div>
                <div class="algo-stat-value">${a.ahoMatches.length} Matches</div>
                <div class="keyword-chips">${renderChips(a.ahoMatches)}</div>
            </div>
            <div class="algo-stat-box">
                <div class="algo-stat-name">5. Bitmask DP</div>
                <div class="algo-stat-value">Optimal Score = ${a.bitmaskScore}</div>
                <span style="font-size:0.75rem; color:var(--text-muted);">2^N subset combinations evaluated</span>
            </div>
            <div class="algo-stat-box">
                <div class="algo-stat-name">6. Edmonds-Karp Network Flow</div>
                <div class="algo-stat-value">Max Flow = ${a.networkFlow}</div>
                <span style="font-size:0.75rem; color:var(--text-muted);">Source &rarr; Categories &rarr; Sink</span>
            </div>
            <div class="algo-stat-box">
                <div class="algo-stat-name">7. Greedy Set Cover</div>
                <div class="algo-stat-value">${a.selectedRules.length} Rules Selected</div>
                <span style="font-size:0.75rem; color:var(--text-secondary);">${a.selectedRules.join(", ") || "No rules triggered"}</span>
            </div>
            <div class="algo-stat-box">
                <div class="algo-stat-name">8. Randomized Hash (Collision Protection)</div>
                <div class="algo-stat-value" style="font-size:0.85rem;">${a.randomizedHash}</div>
                <span style="font-size:0.75rem; color:var(--text-muted);">Random polynomial base</span>
            </div>
        </div>

        ${a.urls.length > 0 ? `
            <div class="url-warning-box">
                <div class="url-warning-title">Detected URLs (${a.urls.length})</div>
                <div style="font-family:var(--font-mono); font-size:0.82rem; color:#fcd34d;">
                    ${a.urls.map(u => `<div>&bull; ${escapeHtml(u)} ${a.suspiciousUrls.includes(u) ? '<span class="badge-risk-high" style="margin-left:6px;">SUSPICIOUS LINK</span>' : ''}</div>`).join("")}
                </div>
            </div>
        ` : ''}
    `;
}

function renderChips(list) {
    if (!list || list.length === 0) return `<span style="font-size:0.75rem; color:var(--text-muted);">None detected</span>`;
    return list.slice(0, 5).map(k => `<span class="chip">${escapeHtml(k)}</span>`).join("");
}

// --- Live Scanner (Tab 2) ---
async function runCustomScanner() {
    const subject = scannerSubject.value.trim();
    const body = scannerBody.value.trim();

    if (!subject && !body) {
        alert("Please enter subject or body text to analyze.");
        return;
    }

    scannerResultContainer.innerHTML = `<div class="loading-state">Running 9 algorithms on custom input...</div>`;

    try {
        const res = await fetch("/api/analyze", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ subject, body })
        });
        if (!res.ok) throw new Error("Analysis failed");
        const analysis = await res.json();

        renderAnalysisDetails(scannerResultContainer, analysis, body);
    } catch (err) {
        scannerResultContainer.innerHTML = `<div class="empty-state">Error: ${err.message}</div>`;
    }
}

// --- Benchmark Arena (Tab 3) ---
async function runBenchmark(user) {
    benchmarkTableBody.innerHTML = `<tr><td colspan="6" class="loading-td">Running live benchmark for ${user}...</td></tr>`;

    try {
        const res = await fetch(`/api/compare?user=${encodeURIComponent(user)}`);
        if (!res.ok) throw new Error("Benchmark failed");
        const data = await res.json();

        fastestStringName.textContent = data.fastestStringMatcher;
        overallFastestName.textContent = data.fastestAlgorithm;

        const maxTime = Math.max(...data.algorithms.map(a => a.timeMs), 0.001);

        benchmarkTableBody.innerHTML = data.algorithms.map(a => {
            const pct = Math.max(4, (a.timeMs / maxTime) * 100);
            const isWinner = a.name === data.fastestAlgorithm;
            return `
                <tr ${isWinner ? 'style="background:rgba(99,102,241,0.08);"' : ''}>
                    <td class="algo-name-cell">
                        ${a.name}
                        ${isWinner ? '<span class="badge" style="margin-left:6px; font-size:0.7rem;">FASTEST</span>' : ''}
                    </td>
                    <td>${a.category}</td>
                    <td><code style="font-family:var(--font-mono); font-size:0.82rem;">${a.complexity}</code></td>
                    <td class="time-cell">${a.timeMs.toFixed(3)} ms</td>
                    <td>${escapeHtml(a.result)}</td>
                    <td>
                        <div class="latency-bar-container" title="${a.timeMs.toFixed(3)} ms">
                            <div class="latency-bar" style="width: ${pct}%;"></div>
                        </div>
                    </td>
                </tr>
            `;
        }).join("");
    } catch (err) {
        benchmarkTableBody.innerHTML = `<tr><td colspan="6" class="empty-state">Error running benchmark: ${err.message}</td></tr>`;
    }
}

// --- Scan All User Emails ---
async function scanAllUserEmails() {
    if (!currentEmails || currentEmails.length === 0) return;
    const btn = document.getElementById("btnScanAllUserEmails");
    btn.disabled = true;
    btn.textContent = "Scanning...";

    for (const email of currentEmails) {
        await autoScanEmailBadge(email);
    }

    btn.disabled = false;
    btn.innerHTML = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg> Scan All Messages`;
}

// --- Compose Form Submit ---
async function handleComposeSubmit(e) {
    e.preventDefault();
    const user = composeUser.value.trim();
    const subject = composeSubject.value.trim();
    const body = composeBody.value.trim();

    composeStatusAlert.style.display = "block";
    composeStatusAlert.className = "alert-box";
    composeStatusAlert.textContent = "Saving message to database...";

    try {
        const res = await fetch("/api/compose", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ user, subject, body })
        });
        if (!res.ok) throw new Error("Failed to save email");
        const result = await res.json();

        composeStatusAlert.className = "alert-box alert-success";
        composeStatusAlert.innerHTML = `<strong>Success!</strong> Message #${result.messageId} stored in <code>data/emails.txt</code>.`;

        composeSubject.value = "";
        composeBody.value = "";

        // Reload emails and switch to inbox
        await loadAccounts();
        selectAccount(user);
        setTimeout(() => switchTab("inboxTab"), 1200);
    } catch (err) {
        composeStatusAlert.className = "alert-box";
        composeStatusAlert.style.background = "var(--danger-bg)";
        composeStatusAlert.textContent = `Error: ${err.message}`;
    }
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
}
