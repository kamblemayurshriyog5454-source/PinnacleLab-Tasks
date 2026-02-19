const storeKey = "lms.books";

const el = {
  addForm: document.getElementById("addBookForm"),
  title: document.getElementById("title"),
  author: document.getElementById("author"),
  isbn: document.getElementById("isbn"),
  search: document.getElementById("searchInput"),
  tbody: document.getElementById("booksTbody"),
  invCount: document.getElementById("inventoryCount"),
  availCount: document.getElementById("availableCount"),
  clearBtn: document.getElementById("clearDataBtn"),
};

let books = [];

function uid() {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

function load() {
  try {
    const raw = localStorage.getItem(storeKey);
    books = raw ? JSON.parse(raw) : [];
  } catch {
    books = [];
  }
}

function save() {
  localStorage.setItem(storeKey, JSON.stringify(books));
}

function render() {
  const q = el.search.value.trim().toLowerCase();
  const filtered = q
    ? books.filter(b =>
        b.title.toLowerCase().includes(q) ||
        b.author.toLowerCase().includes(q) ||
        String(b.isbn).toLowerCase().includes(q)
      )
    : books;

  el.tbody.innerHTML = "";
  for (const b of filtered) {
    const tr = document.createElement("tr");
    const statusClass = b.status === "Available" ? "available" : "borrowed";
    tr.innerHTML = [
      `<td>${escapeHtml(b.title)}</td>`,
      `<td>${escapeHtml(b.author)}</td>`,
      `<td>${escapeHtml(b.isbn)}</td>`,
      `<td><span class="status ${statusClass}">${b.status}</span></td>`,
      `<td>${escapeHtml(b.borrower || "")}</td>`,
      `<td>
        <div class="row-actions">
          <button class="small secondary" data-action="borrow" data-id="${b.id}" ${b.status !== "Available" ? "disabled" : ""}>Borrow</button>
          <button class="small" data-action="return" data-id="${b.id}" ${b.status !== "Borrowed" ? "disabled" : ""}>Return</button>
          <button class="small danger" data-action="delete" data-id="${b.id}">Delete</button>
        </div>
      </td>`
    ].join("");
    el.tbody.appendChild(tr);
  }

  el.invCount.textContent = String(books.length);
  el.availCount.textContent = String(books.filter(b => b.status === "Available").length);
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function addBook(e) {
  e.preventDefault();
  const title = el.title.value.trim();
  const author = el.author.value.trim();
  const isbn = el.isbn.value.trim();
  if (!title || !author || !isbn) return;

  books.push({
    id: uid(),
    title,
    author,
    isbn,
    status: "Available",
    borrower: ""
  });
  save();
  el.addForm.reset();
  render();
}

function handleAction(e) {
  const btn = e.target.closest("button[data-action]");
  if (!btn) return;
  const id = btn.dataset.id;
  const action = btn.dataset.action;
  const i = books.findIndex(b => b.id === id);
  if (i < 0) return;

  if (action === "borrow") {
    const borrower = prompt("Enter borrower name");
    if (!borrower) return;
    if (books[i].status !== "Available") return;
    books[i].status = "Borrowed";
    books[i].borrower = borrower.trim();
  } else if (action === "return") {
    if (books[i].status !== "Borrowed") return;
    books[i].status = "Available";
    books[i].borrower = "";
  } else if (action === "delete") {
    if (!confirm("Delete this book?")) return;
    books.splice(i, 1);
  }
  save();
  render();
}

function clearData() {
  if (!confirm("Reset all data?")) return;
  books = [];
  save();
  render();
}

function init() {
  load();
  render();
  el.addForm.addEventListener("submit", addBook);
  el.tbody.addEventListener("click", handleAction);
  el.search.addEventListener("input", render);
  el.clearBtn.addEventListener("click", clearData);
}

document.addEventListener("DOMContentLoaded", init);
