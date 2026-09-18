// =====================================================================
// KTX Portal — frontend JS thuần (không dùng framework), gọi REST API
// bằng fetch(), lưu JWT token trong localStorage của trình duyệt.
// =====================================================================

const API = "";
const state = {
  token: localStorage.getItem("ktx_token") || null,
  hoTen: null, vaiTro: null, userId: null, mssv: null,
  toaNhaId: null, toaNhaTen: null,
  currentSection: null,
};

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

// ---------- Đăng nhập / Đăng ký ----------
const authScreen = document.getElementById("screen-auth");
const appScreen = document.getElementById("screen-app");
const authMessage = document.getElementById("auth-message");

document.querySelectorAll(".auth-tab").forEach(tab => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".auth-tab").forEach(t => t.classList.remove("is-active"));
    tab.classList.add("is-active");
    document.getElementById("form-login").hidden = tab.dataset.tab !== "login";
    document.getElementById("form-register").hidden = tab.dataset.tab !== "register";
    authMessage.hidden = true;
  });
});

function showAuthMessage(msg, isSuccess = false) {
  authMessage.textContent = msg;
  authMessage.hidden = false;
  authMessage.classList.toggle("is-success", isSuccess);
}

document.getElementById("form-login").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const data = await api("/api/auth/dang-nhap", "POST", {
      tenDangNhap: document.getElementById("login-username").value.trim(),
      matKhau: document.getElementById("login-password").value,
    });
    state.token = data.token;
    localStorage.setItem("ktx_token", data.token);
    await loadProfileAndStart();
  } catch (err) {
    showAuthMessage(err.message, false);
  }
});

document.getElementById("form-register").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await api("/api/auth/dang-ky", "POST", {
      hoTen: document.getElementById("reg-hoten").value.trim(),
      mssv: document.getElementById("reg-mssv").value.trim(),
      tenDangNhap: document.getElementById("reg-username").value.trim(),
      matKhau: document.getElementById("reg-password").value,
      email: document.getElementById("reg-email").value.trim(),
      soDienThoai: document.getElementById("reg-phone").value.trim(),
    });
    showAuthMessage("Tạo tài khoản thành công. Hãy đăng nhập.", true);
    document.querySelector('.auth-tab[data-tab="login"]').click();
  } catch (err) {
    showAuthMessage(err.message, false);
  }
});

document.getElementById("btn-logout").addEventListener("click", () => {
  localStorage.removeItem("ktx_token");
  state.token = null;
  appScreen.hidden = true;
  authScreen.hidden = false;
});

async function loadProfileAndStart() {
  const info = await api("/api/nguoi-dung/toi");
  state.hoTen = info.hoTen;
  state.vaiTro = info.vaiTro;
  state.userId = info.id;
  state.mssv = info.mssv;
  state.toaNhaId = info.toaNhaPhuTrachId;
  state.toaNhaTen = info.toaNhaPhuTrachTen;

  document.getElementById("user-name").textContent = state.hoTen;
  document.getElementById("user-role").textContent = nhanVaiTro(state.vaiTro);

  authScreen.hidden = true;
  appScreen.hidden = false;
  buildSidebar();
}

function nhanVaiTro(vt) {
  return { SINH_VIEN: "Sinh viên", QUAN_LY_TOA_NHA: "Quản lý tòa nhà", BAN_QUAN_LY: "Ban quản lý" }[vt] || vt;
}

// ---------- Sidebar & điều hướng ----------
const NAV = {
  SINH_VIEN: [
    { id: "sv-tong-quan", label: "Tổng quan", title: "Tổng quan của tôi", render: renderSvTongQuan },
    { id: "sv-phong-trong", label: "Đăng ký phòng", title: "Phòng còn trống", render: renderSvPhongTrong },
    { id: "sv-dang-ky", label: "Đơn đăng ký", title: "Đơn đăng ký của tôi", render: renderSvDangKy },
    { id: "sv-hop-dong", label: "Hợp đồng", title: "Hợp đồng của tôi", render: renderSvHopDong },
    { id: "sv-hoa-don", label: "Hóa đơn", title: "Hóa đơn phòng tôi", render: renderSvHoaDon },
    { id: "sv-vi-pham", label: "Vi phạm nội quy", title: "Vi phạm nội quy của tôi", render: renderSvViPham },
  ],
  QUAN_LY_TOA_NHA: [
    { id: "ql-tong-quan", label: "Tổng quan", title: "Tổng quan tòa nhà", render: renderQlTongQuan },
    { id: "ql-cho-duyet", label: "Duyệt đăng ký", title: "Đơn đăng ký chờ duyệt", render: renderQlChoDuyet },
    { id: "ql-so-do", label: "Sơ đồ phòng", title: "Sơ đồ phòng / giường", render: renderQlSoDoPhong },
    { id: "ql-tao-phong", label: "Tạo phòng", title: "Tạo phòng mới", render: renderQlTaoPhong },
    { id: "ql-cu-dan", label: "Sinh viên đang ở", title: "Danh sách sinh viên đang ở", render: renderQlCuDan },
    { id: "ql-chi-so", label: "Ghi chỉ số điện nước", title: "Ghi chỉ số điện nước", render: renderQlChiSo },
    { id: "ql-hoa-don", label: "Hóa đơn", title: "Hóa đơn theo tòa nhà", render: renderQlHoaDon },
    { id: "ql-cong-no", label: "Công nợ", title: "Danh sách công nợ", render: renderQlCongNo },
    { id: "ql-vi-pham", label: "Vi phạm nội quy", title: "Quản lý vi phạm nội quy", render: renderQlViPham },
  ],
  BAN_QUAN_LY: [
    { id: "bql-tong-quan", label: "Tổng quan hệ thống", title: "Tổng quan toàn hệ thống", render: renderBqlTongQuan },
    { id: "bql-thong-ke", label: "Thống kê tòa nhà", title: "Thống kê theo tòa nhà", render: renderBqlThongKe },
    { id: "bql-tao-toa-nha", label: "Tạo tòa nhà", title: "Tạo tòa nhà mới", render: renderBqlTaoToaNha },
    { id: "bql-tao-nhan-vien", label: "Tạo nhân viên", title: "Tạo tài khoản quản lý tòa nhà", render: renderBqlTaoNhanVien },
    { id: "bql-nhan-vien", label: "Danh sách nhân viên", title: "Nhân viên quản lý tòa nhà", render: renderBqlNhanVien },
    { id: "bql-xuat-excel", label: "Xuất Excel công nợ", title: "Xuất báo cáo công nợ", render: renderBqlXuatExcel },
  ],
};

function buildSidebar() {
  const nav = document.getElementById("sidebar-nav");
  nav.innerHTML = "";

  const groups = [];
  if (state.vaiTro === "SINH_VIEN") groups.push(["Sinh viên", NAV.SINH_VIEN]);
  if (state.vaiTro === "QUAN_LY_TOA_NHA") groups.push(["Quản lý tòa nhà — " + (state.toaNhaTen || ""), NAV.QUAN_LY_TOA_NHA]);
  if (state.vaiTro === "BAN_QUAN_LY") {
    groups.push(["Ban quản lý", NAV.BAN_QUAN_LY]);
    groups.push(["Vận hành tòa nhà", NAV.QUAN_LY_TOA_NHA]);
  }

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

  if (firstId) goToSection(firstId);
}

function findNavItem(id) {
  return [...NAV.SINH_VIEN, ...NAV.QUAN_LY_TOA_NHA, ...NAV.BAN_QUAN_LY].find(i => i.id === id);
}

async function goToSection(id) {
  state.currentSection = id;
  document.querySelectorAll(".sidebar-nav button").forEach(b => b.classList.toggle("is-active", b.dataset.section === id));
  const item = findNavItem(id);
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

// ================== TRANG TỔNG QUAN (DASHBOARD) ==================

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

async function renderBqlTongQuan(el) {
  const tq = await api("/api/ban-quan-ly/tong-quan");
  el.innerHTML = `<p class="panel-desc" style="margin-bottom:16px">Số liệu tổng hợp của toàn bộ ký túc xá.</p>` + veTongQuan(tq);
}

async function renderQlTongQuan(el) {
  let url = "/api/quan-ly-toa-nha/tong-quan";
  // Ban quan ly xem muc nay thi chon toa nha cu the
  if (state.vaiTro === "BAN_QUAN_LY") {
    const list = await api("/api/sinh-vien/toa-nha");
    if (!list.length) { el.innerHTML = `<p class="empty-state">Chưa có tòa nhà nào.</p>`; return; }
    el.innerHTML = `<div class="panel"><div class="form-row"><label>Tòa nhà
        <select id="tq-toanha">${list.map(t => `<option value="${t.id}">${t.tenToaNha}</option>`).join("")}</select>
      </label></div></div><div id="tq-result"></div>`;
    const load = async () => {
      const tq = await api(url + "?toaNhaId=" + document.getElementById("tq-toanha").value);
      document.getElementById("tq-result").innerHTML = veTongQuan(tq);
    };
    document.getElementById("tq-toanha").addEventListener("change", load);
    return load();
  }
  const tq = await api(url);
  el.innerHTML = `<p class="panel-desc" style="margin-bottom:16px">Tòa nhà phụ trách: <strong>${state.toaNhaTen || "—"}</strong></p>` + veTongQuan(tq);
}

async function renderSvTongQuan(el) {
  const [hopDongs, viPhams] = await Promise.all([
    api("/api/sinh-vien/hop-dong"),
    api("/api/sinh-vien/vi-pham"),
  ]);
  const hdHienTai = hopDongs.find(h => h.conHieuLuc);

  let hoaDonHtml = "";
  if (hdHienTai) {
    const hoaDons = await api("/api/sinh-vien/hoa-don").catch(() => []);
    const chuaTT = hoaDons.filter(h => h.trangThai !== "DA_THANH_TOAN");
    const tongNo = chuaTT.reduce((s, h) => s + Number(h.tongTien), 0);
    hoaDonHtml = `<div class="stat-grid">
        ${statBox("Hóa đơn chưa thanh toán", chuaTT.length, chuaTT.length > 0)}
        ${statBox("Số tiền còn nợ", tienVND(tongNo), tongNo > 0)}
        ${statBox("Vi phạm nội quy", viPhams.length, viPhams.length > 0)}
      </div>`;
  }

  el.innerHTML = `
    <div class="panel">
      <h2>Chỗ ở hiện tại</h2>
      ${hdHienTai
        ? `<p class="panel-desc">Bạn đang ở <strong>${hdHienTai.giuong.phong.toaNha.tenToaNha} — Phòng ${hdHienTai.giuong.phong.soPhong}</strong>,
           giường <strong>${hdHienTai.giuong.kyHieu}</strong>, từ ngày ${hdHienTai.ngayBatDau}.</p>`
        : `<p class="panel-desc">Bạn chưa có chỗ ở. Hãy vào mục <strong>Đăng ký phòng</strong> để chọn phòng còn trống.</p>`}
    </div>
    ${hoaDonHtml}`;
}

// ================== SINH VIÊN ==================

async function toaNhaSelectHtml(selectedId) {
  const list = await api("/api/sinh-vien/toa-nha");
  return `<select id="select-toa-nha">` +
    list.map(t => `<option value="${t.id}" ${t.id === selectedId ? "selected" : ""}>${t.tenToaNha}</option>`).join("") +
    `</select>`;
}

async function renderSvPhongTrong(el) {
  const list = await api("/api/sinh-vien/toa-nha");
  if (!list.length) {
    el.innerHTML = `<div class="panel"><p class="empty-state">
      Hệ thống chưa có tòa nhà nào.<br>Ban quản lý cần tạo tòa nhà và phòng trước khi sinh viên có thể đăng ký.
    </p></div>`;
    return;
  }

  el.innerHTML = `
    <div class="panel">
      <h2>Chọn tòa nhà</h2>
      <p class="panel-desc">Xem các phòng còn giường trống để đăng ký ở.</p>
      <div class="form-row"><label>Tòa nhà ${await toaNhaSelectHtml(list[0].id)}</label></div>
      <div id="phong-trong-result"></div>
    </div>`;

  const loadPhong = async () => {
    const box = document.getElementById("phong-trong-result");
    const toaNhaId = document.getElementById("select-toa-nha").value;
    box.innerHTML = `<p class="empty-state">Đang tải danh sách phòng…</p>`;

    let phongs;
    try {
      phongs = await api(`/api/sinh-vien/phong-trong/${toaNhaId}`);
    } catch (err) {
      // Truoc day loi bi "nuot" mat khien man hinh trang tron, khong biet chuyen gi xay ra
      box.innerHTML = `<p class="empty-state">Không tải được danh sách phòng: ${err.message}</p>`;
      return;
    }

    if (!phongs.length) {
      box.innerHTML = `<p class="empty-state">
        Tòa nhà này hiện chưa có phòng nào còn trống.<br>
        <span style="font-size:12.5px">Có thể tòa nhà chưa được tạo phòng, hoặc tất cả phòng đã kín giường.
        Bạn hãy chọn tòa nhà khác hoặc liên hệ ban quản lý.</span>
      </p>`;
      return;
    }

    box.innerHTML = `<table class="data-table"><thead><tr>
        <th>Phòng</th><th>Tầng</th><th>Giá phòng/tháng</th><th>Còn trống</th><th></th>
      </tr></thead><tbody>` +
      phongs.map(p => `<tr>
          <td>${p.soPhong}</td><td>${p.tang ?? "—"}</td>
          <td class="num">${tienVND(p.giaPhongThang)}</td>
          <td class="num">${p.soChoTrong}/${p.soGiuongToiDa}</td>
          <td><button class="btn btn-primary" data-dangky="${p.id}">Đăng ký</button></td>
        </tr>`).join("") + `</tbody></table>`;

    box.querySelectorAll("[data-dangky]").forEach(btn => {
      btn.addEventListener("click", async () => {
        try {
          await api(`/api/sinh-vien/dang-ky-phong/${btn.dataset.dangky}`, "POST");
          toast("Đã gửi đăng ký, chờ quản lý tòa nhà duyệt.");
          loadPhong();
        } catch (err) { toast(err.message, true); }
      });
    });
  };

  document.getElementById("select-toa-nha").addEventListener("change", loadPhong);
  loadPhong();
}

async function renderSvDangKy(el) {
  const list = await api("/api/sinh-vien/lich-su-dang-ky");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Bạn chưa có đơn đăng ký nào.</p>`; return; }
  el.innerHTML = `<div class="panel"><table class="data-table"><thead><tr>
      <th>Phòng</th><th>Ngày đăng ký</th><th>Trạng thái</th><th>Ghi chú</th>
    </tr></thead><tbody>` +
    list.map(d => `<tr>
        <td>${d.phong.toaNha.tenToaNha} — P.${d.phong.soPhong}</td>
        <td>${new Date(d.ngayDangKy).toLocaleDateString("vi-VN")}</td>
        <td>${tagDangKy(d.trangThai)}</td>
        <td>${d.ghiChu || "—"}</td>
      </tr>`).join("") + `</tbody></table></div>`;
}

async function renderSvHopDong(el) {
  const list = await api("/api/sinh-vien/hop-dong");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Bạn chưa có hợp đồng ở nào.</p>`; return; }
  el.innerHTML = `<div class="panel"><table class="data-table"><thead><tr>
      <th>Phòng</th><th>Giường</th><th>Ngày bắt đầu</th><th>Trạng thái</th>
    </tr></thead><tbody>` +
    list.map(h => `<tr>
        <td>${h.giuong.phong.toaNha.tenToaNha} — P.${h.giuong.phong.soPhong}</td>
        <td>${h.giuong.kyHieu}</td>
        <td>${h.ngayBatDau}</td>
        <td>${h.conHieuLuc ? '<span class="tag tag-duyet">Đang hiệu lực</span>' : '<span class="tag tag-day">Đã kết thúc</span>'}</td>
      </tr>`).join("") + `</tbody></table></div>`;
}

async function renderSvHoaDon(el) {
  const list = await api("/api/sinh-vien/hoa-don");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Chưa có hóa đơn nào.</p>`; return; }
  el.innerHTML = `<div class="panel"><table class="data-table"><thead><tr>
      <th>Tháng</th><th>Tiền phòng</th><th>Tiền điện</th><th>Tiền nước</th><th>Tổng</th><th>Hạn TT</th><th>Trạng thái</th><th></th>
    </tr></thead><tbody>` +
    list.map(h => `<tr>
        <td>${h.thang}/${h.nam}</td>
        <td class="num">${tienVND(h.tienPhong)}</td>
        <td class="num">${tienVND(h.tienDien)}</td>
        <td class="num">${tienVND(h.tienNuoc)}</td>
        <td class="num"><strong>${tienVND(h.tongTien)}</strong></td>
        <td>${h.hanThanhToan}</td>
        <td>${tagHoaDon(h.trangThai)}</td>
        <td><button class="btn btn-outline" data-pdf="${h.id}">Tải PDF</button></td>
      </tr>`).join("") + `</tbody></table></div>`;

  el.querySelectorAll("[data-pdf]").forEach(btn => btn.addEventListener("click",
    () => taiFile(`/api/sinh-vien/hoa-don/${btn.dataset.pdf}/pdf`, `hoa_don_${btn.dataset.pdf}.pdf`)));
}

async function renderSvViPham(el) {
  const list = await api("/api/sinh-vien/vi-pham");
  if (!list.length) {
    el.innerHTML = `<div class="panel"><p class="empty-state">Bạn không có vi phạm nội quy nào. Hãy tiếp tục giữ gìn nhé!</p></div>`;
    return;
  }
  el.innerHTML = `<div class="panel">
      <h2>Biên bản vi phạm nội quy</h2>
      <p class="panel-desc">Danh sách các lần vi phạm đã được ban quản lý ghi nhận.</p>
      <table class="data-table"><thead><tr>
        <th>Ngày</th><th>Nội dung vi phạm</th><th>Hình thức xử lý</th><th>Người lập</th>
      </tr></thead><tbody>` +
    list.map(v => `<tr>
        <td>${v.ngayViPham || "—"}</td>
        <td>${v.noiDung}</td>
        <td>${v.hinhThucXuLy || "—"}</td>
        <td>${v.nguoiLap ? v.nguoiLap.hoTen : "—"}</td>
      </tr>`).join("") + `</tbody></table></div>`;
}

// ================== QUẢN LÝ TÒA NHÀ (+ Ban quản lý vận hành) ==================

async function currentToaNhaPicker(container) {
  // QUAN_LY_TOA_NHA: co san toa nha phu trach. BAN_QUAN_LY: chon tu danh sach.
  if (state.vaiTro === "QUAN_LY_TOA_NHA" && state.toaNhaId) {
    return { toaNhaId: state.toaNhaId, html: `<p class="panel-desc">Tòa nhà phụ trách: <strong>${state.toaNhaTen}</strong></p>` };
  }
  const list = await api("/api/sinh-vien/toa-nha");
  if (!list.length) return { toaNhaId: null, html: `<p class="empty-state">Chưa có tòa nhà nào. Hãy tạo tòa nhà trước.</p>` };
  const html = `<div class="form-row"><label>Chọn tòa nhà
      <select id="picker-toa-nha">${list.map(t => `<option value="${t.id}">${t.tenToaNha}</option>`).join("")}</select>
    </label></div>`;
  return { toaNhaId: list[0].id, html, hasPicker: true };
}

async function renderQlChoDuyet(el) {
  const picker = await currentToaNhaPicker(el);
  el.innerHTML = `<div class="panel"><h2>Đơn chờ duyệt</h2><p class="panel-desc">Duyệt sẽ tự động xếp giường trống đầu tiên và tạo hợp đồng.</p>${picker.html}<div id="cho-duyet-list"></div></div>`;

  const load = async () => {
    const toaNhaId = picker.hasPicker ? document.getElementById("picker-toa-nha").value : picker.toaNhaId;
    if (!toaNhaId) return;
    const list = await api(`/api/quan-ly-toa-nha/dang-ky-cho-duyet/${toaNhaId}`);
    const box = document.getElementById("cho-duyet-list");
    if (!list.length) { box.innerHTML = `<p class="empty-state">Không có đơn nào đang chờ duyệt.</p>`; return; }
    box.innerHTML = `<table class="data-table"><thead><tr>
        <th>Sinh viên</th><th>MSSV</th><th>Phòng</th><th>Ngày đăng ký</th><th></th>
      </tr></thead><tbody>` +
      list.map(d => `<tr>
          <td>${d.sinhVien.hoTen}</td><td class="num">${d.sinhVien.mssv}</td>
          <td>P.${d.phong.soPhong}</td>
          <td>${new Date(d.ngayDangKy).toLocaleDateString("vi-VN")}</td>
          <td style="display:flex; gap:8px;">
            <button class="btn btn-primary" data-duyet="${d.id}">Duyệt</button>
            <button class="btn btn-outline" data-tuchoi="${d.id}">Từ chối</button>
          </td>
        </tr>`).join("") + `</tbody></table>`;

    box.querySelectorAll("[data-duyet]").forEach(btn => btn.addEventListener("click", async () => {
      try { await api(`/api/quan-ly-toa-nha/xet-duyet/${btn.dataset.duyet}`, "POST", { dongY: true, ghiChu: "" }); toast("Đã duyệt và xếp giường."); load(); }
      catch (err) { toast(err.message, true); }
    }));
    box.querySelectorAll("[data-tuchoi]").forEach(btn => btn.addEventListener("click", async () => {
      const ghiChu = prompt("Lý do từ chối (không bắt buộc):") || "";
      try { await api(`/api/quan-ly-toa-nha/xet-duyet/${btn.dataset.tuchoi}`, "POST", { dongY: false, ghiChu }); toast("Đã từ chối đơn."); load(); }
      catch (err) { toast(err.message, true); }
    }));
  };

  if (picker.hasPicker) document.getElementById("picker-toa-nha").addEventListener("change", load);
  load();
}

async function renderQlSoDoPhong(el) {
  const picker = await currentToaNhaPicker(el);
  el.innerHTML = `<div class="panel"><h2>Sơ đồ phòng / giường</h2>${picker.html}<div id="so-do-list"></div></div>`;

  const load = async () => {
    const toaNhaId = picker.hasPicker ? document.getElementById("picker-toa-nha").value : picker.toaNhaId;
    if (!toaNhaId) return;
    const list = await api(`/api/quan-ly-toa-nha/so-do-phong/${toaNhaId}`);
    const box = document.getElementById("so-do-list");
    if (!list.length) { box.innerHTML = `<p class="empty-state">Tòa nhà chưa có phòng nào.</p>`; return; }
    box.innerHTML = `<table class="data-table"><thead><tr>
        <th>Phòng</th><th>Tầng</th><th>Giá/tháng</th><th>Giường</th><th>Trạng thái</th>
      </tr></thead><tbody>` +
      list.map(p => `<tr>
          <td>${p.soPhong}</td><td>${p.tang ?? "—"}</td>
          <td class="num">${tienVND(p.giaPhongThang)}</td>
          <td class="num">${p.soGiuongDaO}/${p.soGiuongToiDa}</td>
          <td>${tagPhong(p.trangThai)}</td>
        </tr>`).join("") + `</tbody></table>`;
  };
  if (picker.hasPicker) document.getElementById("picker-toa-nha").addEventListener("change", load);
  load();
}

async function renderQlTaoPhong(el) {
  const list = await api("/api/sinh-vien/toa-nha");
  el.innerHTML = `<div class="panel">
    <h2>Tạo phòng mới</h2>
    <p class="panel-desc">Hệ thống sẽ tự sinh các giường A, B, C... theo sức chứa bạn nhập.</p>
    <form id="form-tao-phong">
      <div class="form-row">
        <label>Tòa nhà<select id="tp-toanha">${list.map(t => `<option value="${t.id}">${t.tenToaNha}</option>`).join("")}</select></label>
        <label>Số phòng<input type="text" id="tp-sophong" required placeholder="VD: 101"></label>
        <label>Tầng<input type="number" id="tp-tang" min="0"></label>
        <label>Sức chứa (số giường)<input type="number" id="tp-succhua" min="1" required></label>
        <label>Giá phòng / tháng (đ)<input type="number" id="tp-gia" min="0" required></label>
      </div>
      <button class="btn btn-primary" type="submit">Tạo phòng</button>
    </form>
  </div>`;

  document.getElementById("form-tao-phong").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("/api/quan-ly-toa-nha/tao-phong", "POST", {
        toaNhaId: Number(document.getElementById("tp-toanha").value),
        soPhong: document.getElementById("tp-sophong").value.trim(),
        tang: Number(document.getElementById("tp-tang").value) || null,
        soGiuongToiDa: Number(document.getElementById("tp-succhua").value),
        giaPhongThang: Number(document.getElementById("tp-gia").value),
      });
      toast("Đã tạo phòng thành công.");
      e.target.reset();
    } catch (err) { toast(err.message, true); }
  });
}

async function renderQlChiSo(el) {
  const picker = await currentToaNhaPicker(el);
  const now = new Date();
  el.innerHTML = `<div class="panel">
      <h2>Ghi chỉ số điện nước</h2>
      <p class="panel-desc">Hệ thống tự lấy chỉ số cũ từ tháng trước, tính tiền theo bậc giá lũy tiến và tạo hóa đơn ngay.</p>
      ${picker.html}
      <div id="chi-so-phong-select"></div>
    </div>`;

  const loadPhongSelect = async () => {
    const toaNhaId = picker.hasPicker ? document.getElementById("picker-toa-nha").value : picker.toaNhaId;
    if (!toaNhaId) return;
    const phongs = await api(`/api/quan-ly-toa-nha/so-do-phong/${toaNhaId}`);
    const box = document.getElementById("chi-so-phong-select");
    box.innerHTML = `<form id="form-chi-so">
        <div class="form-row">
          <label>Phòng<select id="cs-phong">${phongs.map(p => `<option value="${p.id}">P.${p.soPhong}</option>`).join("")}</select></label>
          <label>Tháng<input type="number" id="cs-thang" min="1" max="12" value="${now.getMonth() + 1}" required></label>
          <label>Năm<input type="number" id="cs-nam" value="${now.getFullYear()}" required></label>
          <label>Chỉ số điện mới (kWh)<input type="number" id="cs-dien" min="0" required></label>
          <label>Chỉ số nước mới (m3)<input type="number" id="cs-nuoc" min="0" required></label>
        </div>
        <button class="btn btn-primary" type="submit">Ghi chỉ số &amp; tạo hóa đơn</button>
      </form>`;

    document.getElementById("form-chi-so").addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        const hd = await api("/api/quan-ly-toa-nha/ghi-chi-so", "POST", {
          phongId: Number(document.getElementById("cs-phong").value),
          thang: Number(document.getElementById("cs-thang").value),
          nam: Number(document.getElementById("cs-nam").value),
          chiSoDienMoi: Number(document.getElementById("cs-dien").value),
          chiSoNuocMoi: Number(document.getElementById("cs-nuoc").value),
        });
        toast("Đã ghi chỉ số và tạo hóa đơn.");
      } catch (err) { toast(err.message, true); }
    });
  };
  if (picker.hasPicker) document.getElementById("picker-toa-nha").addEventListener("change", loadPhongSelect);
  loadPhongSelect();
}

async function renderQlCongNo(el) {
  const list = await api("/api/quan-ly-toa-nha/cong-no");
  if (!list.length) { el.innerHTML = `<div class="panel"><p class="empty-state">Không có khoản nợ quá hạn nào.</p></div>`; return; }
  el.innerHTML = `<div class="panel"><h2>Danh sách công nợ quá hạn</h2><table class="data-table"><thead><tr>
      <th>Tòa nhà</th><th>Phòng</th><th>Tháng</th><th>Tổng tiền</th><th>Hạn TT</th><th>Trạng thái</th><th></th>
    </tr></thead><tbody>` +
    list.map(h => `<tr>
        <td>${h.phong.toaNha.tenToaNha}</td><td>P.${h.phong.soPhong}</td>
        <td>${h.thang}/${h.nam}</td>
        <td class="num"><strong>${tienVND(h.tongTien)}</strong></td>
        <td>${h.hanThanhToan}</td><td>${tagHoaDon(h.trangThai)}</td>
        <td><button class="btn btn-outline" data-tt="${h.id}">Xác nhận đã TT</button></td>
      </tr>`).join("") + `</tbody></table></div>`;

  el.querySelectorAll("[data-tt]").forEach(btn => btn.addEventListener("click", async () => {
    try { await api(`/api/quan-ly-toa-nha/xac-nhan-thanh-toan/${btn.dataset.tt}`, "POST"); toast("Đã xác nhận thanh toán."); renderQlCongNo(el); }
    catch (err) { toast(err.message, true); }
  }));
}

async function renderQlCuDan(el) {
  const picker = await currentToaNhaPicker(el);
  el.innerHTML = `<div class="panel">
      <h2>Sinh viên đang ở</h2>
      <p class="panel-desc">Danh sách cư dân theo từng giường. Có thể lập biên bản vi phạm hoặc làm thủ tục trả phòng.</p>
      ${picker.html}<div id="cu-dan-list"></div>
    </div>`;

  const load = async () => {
    const toaNhaId = picker.hasPicker ? document.getElementById("picker-toa-nha").value : picker.toaNhaId;
    if (!toaNhaId) return;
    const list = await api(`/api/quan-ly-toa-nha/cu-dan/${toaNhaId}`);
    const box = document.getElementById("cu-dan-list");
    if (!list.length) { box.innerHTML = `<p class="empty-state">Chưa có sinh viên nào ở trong tòa nhà này.</p>`; return; }
    box.innerHTML = `<table class="data-table"><thead><tr>
        <th>Họ tên</th><th>MSSV</th><th>Phòng</th><th>Giường</th><th>Liên hệ</th><th>Vi phạm</th><th></th>
      </tr></thead><tbody>` +
      list.map(s => `<tr>
          <td>${s.hoTen}</td><td class="num">${s.mssv || "—"}</td>
          <td>P.${s.soPhong}</td><td>${s.kyHieuGiuong}</td>
          <td>${s.soDienThoai || s.email || "—"}</td>
          <td class="num">${s.soLanViPham > 0 ? `<span class="tag tag-quahan">${s.soLanViPham} lần</span>` : "0"}</td>
          <td style="display:flex; gap:6px;">
            <button class="btn btn-outline" data-vipham="${s.sinhVienId}" data-ten="${s.hoTen}">Lập vi phạm</button>
            <button class="btn btn-outline" data-traphong="${s.hopDongId}" data-ten="${s.hoTen}">Trả phòng</button>
          </td>
        </tr>`).join("") + `</tbody></table>`;

    box.querySelectorAll("[data-vipham]").forEach(btn => btn.addEventListener("click", async () => {
      const noiDung = prompt(`Nội dung vi phạm của ${btn.dataset.ten}:`);
      if (!noiDung) return;
      const hinhThucXuLy = prompt("Hình thức xử lý (nhắc nhở / cảnh cáo / phạt...):") || "";
      try {
        await api("/api/quan-ly-toa-nha/vi-pham", "POST",
          { sinhVienId: Number(btn.dataset.vipham), noiDung, hinhThucXuLy });
        toast("Đã lập biên bản vi phạm.");
        load();
      } catch (err) { toast(err.message, true); }
    }));

    box.querySelectorAll("[data-traphong]").forEach(btn => btn.addEventListener("click", async () => {
      if (!confirm(`Xác nhận cho ${btn.dataset.ten} trả phòng? Hợp đồng sẽ kết thúc và giường được giải phóng.`)) return;
      try {
        await api(`/api/quan-ly-toa-nha/tra-phong/${btn.dataset.traphong}`, "POST");
        toast("Đã trả phòng thành công.");
        load();
      } catch (err) { toast(err.message, true); }
    }));
  };
  if (picker.hasPicker) document.getElementById("picker-toa-nha").addEventListener("change", load);
  load();
}

async function renderQlHoaDon(el) {
  const picker = await currentToaNhaPicker(el);
  el.innerHTML = `<div class="panel">
      <h2>Hóa đơn theo tòa nhà</h2>
      <p class="panel-desc">Toàn bộ hóa đơn đã phát hành. Có thể xác nhận thanh toán hoặc tải hóa đơn PDF.</p>
      ${picker.html}<div id="hoa-don-list"></div>
    </div>`;

  const load = async () => {
    const toaNhaId = picker.hasPicker ? document.getElementById("picker-toa-nha").value : picker.toaNhaId;
    if (!toaNhaId) return;
    const list = await api(`/api/quan-ly-toa-nha/hoa-don/toa-nha/${toaNhaId}`);
    const box = document.getElementById("hoa-don-list");
    if (!list.length) { box.innerHTML = `<p class="empty-state">Chưa có hóa đơn nào. Hãy ghi chỉ số điện nước để tạo hóa đơn.</p>`; return; }

    list.sort((a, b) => (b.nam - a.nam) || (b.thang - a.thang));
    box.innerHTML = `<table class="data-table"><thead><tr>
        <th>Phòng</th><th>Tháng</th><th>Tiền phòng</th><th>Điện</th><th>Nước</th><th>Tổng</th><th>Trạng thái</th><th></th>
      </tr></thead><tbody>` +
      list.map(h => `<tr>
          <td>P.${h.phong.soPhong}</td><td>${h.thang}/${h.nam}</td>
          <td class="num">${tienVND(h.tienPhong)}</td>
          <td class="num">${tienVND(h.tienDien)}</td>
          <td class="num">${tienVND(h.tienNuoc)}</td>
          <td class="num"><strong>${tienVND(h.tongTien)}</strong></td>
          <td>${tagHoaDon(h.trangThai)}</td>
          <td style="display:flex; gap:6px;">
            <button class="btn btn-outline" data-pdf="${h.id}" data-phong="${h.phong.soPhong}" data-ky="${h.thang}_${h.nam}">PDF</button>
            ${h.trangThai !== "DA_THANH_TOAN"
              ? `<button class="btn btn-primary" data-tt="${h.id}">Đã thu tiền</button>` : ""}
          </td>
        </tr>`).join("") + `</tbody></table>`;

    box.querySelectorAll("[data-pdf]").forEach(btn => btn.addEventListener("click",
      () => taiFile(`/api/quan-ly-toa-nha/hoa-don/${btn.dataset.pdf}/pdf`,
                    `hoa_don_P${btn.dataset.phong}_${btn.dataset.ky}.pdf`)));

    box.querySelectorAll("[data-tt]").forEach(btn => btn.addEventListener("click", async () => {
      try { await api(`/api/quan-ly-toa-nha/xac-nhan-thanh-toan/${btn.dataset.tt}`, "POST"); toast("Đã xác nhận thanh toán."); load(); }
      catch (err) { toast(err.message, true); }
    }));
  };
  if (picker.hasPicker) document.getElementById("picker-toa-nha").addEventListener("change", load);
  load();
}

async function renderQlViPham(el) {
  const picker = await currentToaNhaPicker(el);
  el.innerHTML = `<div class="panel">
      <h2>Vi phạm nội quy</h2>
      <p class="panel-desc">Các biên bản đã lập cho sinh viên đang ở trong tòa nhà. Lập biên bản mới tại mục "Sinh viên đang ở".</p>
      ${picker.html}<div id="vi-pham-list"></div>
    </div>`;

  const load = async () => {
    const toaNhaId = picker.hasPicker ? document.getElementById("picker-toa-nha").value : picker.toaNhaId;
    if (!toaNhaId) return;
    const list = await api(`/api/quan-ly-toa-nha/vi-pham/${toaNhaId}`);
    const box = document.getElementById("vi-pham-list");
    if (!list.length) { box.innerHTML = `<p class="empty-state">Chưa có biên bản vi phạm nào.</p>`; return; }

    list.sort((a, b) => (b.ngayViPham || "").localeCompare(a.ngayViPham || ""));
    box.innerHTML = `<table class="data-table"><thead><tr>
        <th>Ngày</th><th>Sinh viên</th><th>MSSV</th><th>Nội dung</th><th>Xử lý</th><th></th>
      </tr></thead><tbody>` +
      list.map(v => `<tr>
          <td>${v.ngayViPham || "—"}</td>
          <td>${v.sinhVien.hoTen}</td>
          <td class="num">${v.sinhVien.mssv || "—"}</td>
          <td>${v.noiDung}</td>
          <td>${v.hinhThucXuLy || "—"}</td>
          <td><button class="btn btn-outline" data-xoa="${v.id}">Xóa</button></td>
        </tr>`).join("") + `</tbody></table>`;

    box.querySelectorAll("[data-xoa]").forEach(btn => btn.addEventListener("click", async () => {
      if (!confirm("Xác nhận xóa biên bản vi phạm này?")) return;
      try { await api(`/api/quan-ly-toa-nha/vi-pham/${btn.dataset.xoa}`, "DELETE"); toast("Đã xóa biên bản."); load(); }
      catch (err) { toast(err.message, true); }
    }));
  };
  if (picker.hasPicker) document.getElementById("picker-toa-nha").addEventListener("change", load);
  load();
}

// ================== BAN QUẢN LÝ ==================

async function renderBqlThongKe(el) {
  const list = await api("/api/sinh-vien/toa-nha");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Chưa có tòa nhà nào. Hãy tạo tòa nhà trước.</p>`; return; }
  el.innerHTML = `<div class="panel">
      <div class="form-row"><label>Tòa nhà<select id="tk-toanha">${list.map(t => `<option value="${t.id}">${t.tenToaNha}</option>`).join("")}</select></label></div>
      <div id="tk-result"></div>
    </div>`;

  const load = async () => {
    const toaNhaId = document.getElementById("tk-toanha").value;
    const tk = await api(`/api/ban-quan-ly/thong-ke/${toaNhaId}`);
    document.getElementById("tk-result").innerHTML = `<div class="stat-grid">
        <div class="stat-box"><div class="stat-label">Tổng số phòng</div><div class="stat-value">${tk.tongSoPhong}</div></div>
        <div class="stat-box"><div class="stat-label">Giường đã ở / tổng</div><div class="stat-value">${tk.soGiuongDaO}/${tk.tongSoGiuong}</div></div>
        <div class="stat-box"><div class="stat-label">Tỷ lệ lấp đầy</div><div class="stat-value">${tk.tyLeLapDay}%</div></div>
        <div class="stat-box"><div class="stat-label">Doanh thu tháng này</div><div class="stat-value">${tienVND(tk.doanhThuThangHienTai)}</div></div>
        <div class="stat-box"><div class="stat-label">Tổng công nợ</div><div class="stat-value">${tienVND(tk.tongCongNo)}</div></div>
      </div>`;
  };
  document.getElementById("tk-toanha").addEventListener("change", load);
  load();
}

async function renderBqlTaoToaNha(el) {
  el.innerHTML = `<div class="panel">
      <h2>Tạo tòa nhà mới</h2>
      <form id="form-toa-nha">
        <div class="form-row">
          <label>Tên tòa nhà<input type="text" id="tn-ten" required placeholder="VD: Toà A"></label>
          <label>Địa chỉ<input type="text" id="tn-diachi"></label>
          <label>Số tầng<input type="number" id="tn-sotang" min="1"></label>
        </div>
        <button class="btn btn-primary" type="submit">Tạo tòa nhà</button>
      </form>
    </div>`;
  document.getElementById("form-toa-nha").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("/api/ban-quan-ly/tao-toa-nha", "POST", {
        tenToaNha: document.getElementById("tn-ten").value.trim(),
        diaChi: document.getElementById("tn-diachi").value.trim(),
        soTang: Number(document.getElementById("tn-sotang").value) || null,
      });
      toast("Đã tạo tòa nhà.");
      e.target.reset();
    } catch (err) { toast(err.message, true); }
  });
}

async function renderBqlTaoNhanVien(el) {
  const list = await api("/api/sinh-vien/toa-nha");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Hãy tạo tòa nhà trước khi thêm nhân viên.</p>`; return; }
  el.innerHTML = `<div class="panel">
      <h2>Tạo tài khoản Quản lý tòa nhà</h2>
      <form id="form-nhan-vien">
        <div class="form-row">
          <label>Họ tên<input type="text" id="nv-hoten" required></label>
          <label>Tên đăng nhập<input type="text" id="nv-username" required></label>
          <label>Mật khẩu<input type="password" id="nv-password" required></label>
          <label>Email<input type="email" id="nv-email"></label>
          <label>Số điện thoại<input type="tel" id="nv-phone"></label>
          <label>Tòa nhà phụ trách<select id="nv-toanha">${list.map(t => `<option value="${t.id}">${t.tenToaNha}</option>`).join("")}</select></label>
        </div>
        <button class="btn btn-primary" type="submit">Tạo tài khoản</button>
      </form>
    </div>`;
  document.getElementById("form-nhan-vien").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("/api/ban-quan-ly/tao-nhan-vien", "POST", {
        hoTen: document.getElementById("nv-hoten").value.trim(),
        tenDangNhap: document.getElementById("nv-username").value.trim(),
        matKhau: document.getElementById("nv-password").value,
        email: document.getElementById("nv-email").value.trim(),
        soDienThoai: document.getElementById("nv-phone").value.trim(),
        toaNhaPhuTrachId: Number(document.getElementById("nv-toanha").value),
      });
      toast("Đã tạo tài khoản nhân viên.");
      e.target.reset();
    } catch (err) { toast(err.message, true); }
  });
}

async function renderBqlNhanVien(el) {
  const list = await api("/api/ban-quan-ly/danh-sach-nhan-vien");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Chưa có nhân viên quản lý tòa nhà nào.</p>`; return; }
  el.innerHTML = `<div class="panel"><table class="data-table"><thead><tr>
      <th>Họ tên</th><th>Tên đăng nhập</th><th>Tòa nhà phụ trách</th><th>Trạng thái</th><th></th>
    </tr></thead><tbody>` +
    list.map(n => `<tr>
        <td>${n.hoTen}</td><td class="num">${n.tenDangNhap}</td>
        <td>${n.toaNhaPhuTrach ? n.toaNhaPhuTrach.tenToaNha : "—"}</td>
        <td>${n.active ? '<span class="tag tag-duyet">Hoạt động</span>' : '<span class="tag tag-quahan">Đã khóa</span>'}</td>
        <td><button class="btn btn-outline" data-khoa="${n.id}">${n.active ? "Khóa" : "Mở khóa"}</button></td>
      </tr>`).join("") + `</tbody></table></div>`;

  el.querySelectorAll("[data-khoa]").forEach(btn => btn.addEventListener("click", async () => {
    try { await api(`/api/ban-quan-ly/khoa-tai-khoan/${btn.dataset.khoa}`, "POST"); renderBqlNhanVien(el); }
    catch (err) { toast(err.message, true); }
  }));
}

async function renderBqlXuatExcel(el) {
  el.innerHTML = `<div class="panel">
      <h2>Xuất báo cáo công nợ</h2>
      <p class="panel-desc">Tải file Excel (.xlsx) danh sách toàn bộ hóa đơn đang nợ quá hạn.</p>
      <button class="btn btn-primary" id="btn-xuat-excel">Tải file Excel</button>
    </div>`;
  document.getElementById("btn-xuat-excel").addEventListener("click",
    () => taiFile("/api/ban-quan-ly/xuat-excel-cong-no", "cong_no.xlsx"));
}

// ---------- Khởi động ----------
(async function init() {
  if (state.token) {
    try { await loadProfileAndStart(); return; } catch (e) { localStorage.removeItem("ktx_token"); }
  }
  authScreen.hidden = false;
  appScreen.hidden = true;
})();