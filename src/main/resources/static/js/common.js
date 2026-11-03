// =====================================================================
// KTX Portal — phần DÙNG CHUNG cho cả 3 trang (Sinh viên / Quản lý tòa nhà
// / Ban quản lý): trạng thái đăng nhập, gọi API, tiện ích, khung giao diện.
//
// Token đăng nhập được lưu trong sessionStorage (riêng từng TAB trình
// duyệt) thay vì localStorage (dùng chung mọi tab), nên có thể mở nhiều
// tab và đăng nhập các tài khoản khác nhau cùng lúc mà không đè lên nhau.
// =====================================================================

const API = "";
try { localStorage.removeItem("ktx_token"); } catch (e) { /* bo phien dung chung cu */ }

function docToken() {
  try { return sessionStorage.getItem("ktx_token"); } catch (e) { return null; }
}
const state = {
  token: docToken(),
  hoTen: null, vaiTro: null, userId: null, mssv: null,
  toaNhaId: null, toaNhaTen: null,
  currentSection: null,
};

const TRANG_THEO_VAI_TRO = {
  SINH_VIEN: "/sinh-vien.html",
  QUAN_LY_TOA_NHA: "/quan-ly-toa-nha.html",
  BAN_QUAN_LY: "/ban-quan-ly.html",
};

function luuToken(token) {
  state.token = token;
  try { sessionStorage.setItem("ktx_token", token); } catch (e) { /* bo qua */ }
}

function dangXuat() {
  state.token = null;
  try { sessionStorage.removeItem("ktx_token"); } catch (e) { /* bo qua */ }
  location.replace("/");
}

// ---------- Helper gọi API ----------
async function api(path, method = "GET", body) {
  const opts = { method, headers: {} };
  if (state.token) opts.headers["Authorization"] = "Bearer " + state.token;
  if (body !== undefined) {
    opts.headers["Content-Type"] = "application/json";
    opts.body = JSON.stringify(body);
  }
  const res = await fetch(API + path, opts);
  if (res.status === 204) return null;
  // Token het han / khong hop le: xoa phien cua TAB nay va ve trang dang nhap
  if (res.status === 401 && state.token) { dangXuat(); throw new Error("Phiên đăng nhập đã hết hạn."); }

  const isJson = (res.headers.get("content-type") || "").includes("application/json");
  const data = isJson ? await res.json() : await res.text();

  if (!res.ok) {
    const msg = (data && data.loi) ? data.loi : (typeof data === "string" ? data : "Có lỗi xảy ra");
    throw new Error(msg);
  }
  return data;
}

function toast(msg, isError = false) {
  const t = document.createElement("div");
  t.className = "toast" + (isError ? " is-error" : "");
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 3200);
}

function tienVND(n) {
  return Number(n || 0).toLocaleString("vi-VN") + " đ";
}

/** Tai file (PDF/Excel) kem token xac thuc, roi luu ve may */
async function taiFile(url, tenFile) {
  try {
    const res = await fetch(url, { headers: { Authorization: "Bearer " + state.token } });
    if (!res.ok) throw new Error("Không tải được file.");
    const blob = await res.blob();
    const link = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = link; a.download = tenFile; a.click();
    URL.revokeObjectURL(link);
  } catch (err) { toast(err.message, true); }
}


function nhanVaiTro(vt) {
  return { SINH_VIEN: "Sinh viên", QUAN_LY_TOA_NHA: "Quản lý tòa nhà", BAN_QUAN_LY: "Ban quản lý" }[vt] || vt;
}

// ---------- Khung giao diện: sidebar & điều hướng ----------
let NAV_HIEN_TAI = [];

/**
 * Khởi động 1 trang vai trò: kiểm tra đăng nhập + đúng vai trò, rồi dựng sidebar.
 * vaiTroChoPhep: các vai trò được vào trang này.
 * layNhomMenu: hàm trả về [[tên nhóm, [mục menu]], ...] (gọi SAU khi đã nạp hồ sơ người dùng).
 */
async function khoiDongTrang(vaiTroChoPhep, layNhomMenu) {
  if (!state.token) { location.replace("/"); return; }
  let info;
  try { info = await api("/api/nguoi-dung/toi"); }
  catch (e) { dangXuat(); return; }

  if (!vaiTroChoPhep.includes(info.vaiTro)) {
    location.replace(TRANG_THEO_VAI_TRO[info.vaiTro] || "/");
    return;
  }
  state.hoTen = info.hoTen;
  state.vaiTro = info.vaiTro;
  state.userId = info.id;
  state.mssv = info.mssv;
  state.toaNhaId = info.toaNhaPhuTrachId;
  state.toaNhaTen = info.toaNhaPhuTrachTen;

  document.getElementById("user-name").textContent = state.hoTen;
  document.getElementById("user-role").textContent = nhanVaiTro(state.vaiTro);
  document.getElementById("btn-logout").addEventListener("click", dangXuat);
  document.getElementById("screen-app").hidden = false;
  buildSidebar(layNhomMenu());
}

function buildSidebar(groups) {
  const nav = document.getElementById("sidebar-nav");
  nav.innerHTML = "";
  NAV_HIEN_TAI = groups.flatMap(([, items]) => items);

  let firstId = null;
  groups.forEach(([label, items]) => {
    const g = document.createElement("div");
    g.className = "nav-group-label";
    g.textContent = label;
    nav.appendChild(g);
    items.forEach(item => {
      if (!firstId) firstId = item.id;
      const btn = document.createElement("button");
      btn.textContent = item.label;
      btn.dataset.section = item.id;
      btn.addEventListener("click", () => goToSection(item.id));
      nav.appendChild(btn);
    });
  });

  // Cho phep mo thang 1 muc qua #id tren URL (vd: sinh-vien.html#sv-hoa-don-ca-nhan sau khi thanh toan VNPay)
  const tuUrl = decodeURIComponent((location.hash || "").slice(1));
  const batDau = NAV_HIEN_TAI.some(i => i.id === tuUrl) ? tuUrl : firstId;
  if (batDau) goToSection(batDau);
}

async function goToSection(id) {
  state.currentSection = id;
  document.querySelectorAll(".sidebar-nav button").forEach(b => b.classList.toggle("is-active", b.dataset.section === id));
  const item = NAV_HIEN_TAI.find(i => i.id === id);
  document.getElementById("topbar-title").textContent = item.title;
  const content = document.getElementById("app-content");
  content.innerHTML = '<p class="empty-state">Đang tải…</p>';
  try {
    await item.render(content);
  } catch (err) {
    content.innerHTML = `<p class="empty-state">${err.message}</p>`;
  }
}

// ---------- Thẻ trạng thái ----------
function tagPhong(tt) {
  const map = { TRONG: ["tag-trong", "Còn trống"], DAY: ["tag-day", "Đã đầy"], KHOA: ["tag-quahan", "Tạm khóa"] };
  const [cls, label] = map[tt] || ["tag-day", tt];
  return `<span class="tag ${cls}">${label}</span>`;
}
function tagDangKy(tt) {
  const map = { CHO_DUYET: ["tag-cho", "Chờ duyệt"], DA_DUYET: ["tag-duyet", "Đã duyệt"], TU_CHOI: ["tag-tuchoi", "Từ chối"], DA_HUY: ["tag-tuchoi", "Đã hủy"] };
  const [cls, label] = map[tt] || ["tag-day", tt];
  return `<span class="tag ${cls}">${label}</span>`;
}
function tagHoaDon(tt) {
  const map = { CHUA_THANH_TOAN: ["tag-cho", "Chưa thanh toán"], DA_THANH_TOAN: ["tag-duyet", "Đã thanh toán"], QUA_HAN: ["tag-quahan", "Quá hạn"] };
  const [cls, label] = map[tt] || ["tag-day", tt];
  return `<span class="tag ${cls}">${label}</span>`;
}


// ---------- Tổng quan (dùng chung) ----------

function statBox(label, value, highlight = false) {
  return `<div class="stat-box"><div class="stat-label">${label}</div>
    <div class="stat-value" ${highlight ? 'style="color:var(--brick)"' : ""}>${value}</div></div>`;
}

function veTongQuan(tq) {
  return `<div class="stat-grid">
      ${statBox("Tòa nhà", tq.tongSoToaNha)}
      ${statBox("Phòng", tq.tongSoPhong)}
      ${statBox("Giường đã ở / tổng", tq.soGiuongDaO + "/" + tq.tongSoGiuong)}
      ${statBox("Tỷ lệ lấp đầy", tq.tyLeLapDayChung + "%")}
    </div>
    <div class="stat-grid">
      ${statBox("Đơn chờ duyệt", tq.soDonChoDuyet, tq.soDonChoDuyet > 0)}
      ${statBox("Hóa đơn chưa thanh toán", tq.soHoaDonChuaThanhToan, tq.soHoaDonChuaThanhToan > 0)}
      ${statBox("Tổng công nợ", tienVND(tq.tongCongNo), Number(tq.tongCongNo) > 0)}
      ${statBox("Doanh thu tháng này", tienVND(tq.doanhThuThangNay))}
      ${statBox("Vi phạm tháng này", tq.soViPhamThangNay, tq.soViPhamThangNay > 0)}
    </div>`;
}