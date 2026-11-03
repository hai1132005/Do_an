// =====================================================================
// KTX Portal — trang BAN QUẢN LÝ (ban-quan-ly.html). Cần common.js và
// quan-ly-toa-nha.js nạp trước (Ban quản lý còn dùng được các mục vận hành tòa nhà).
// =====================================================================

const NAV_BQL = [
    { id: "bql-tong-quan", label: "Tổng quan hệ thống", title: "Tổng quan toàn hệ thống", render: renderBqlTongQuan },
    { id: "bql-thong-ke", label: "Thống kê tòa nhà", title: "Thống kê theo tòa nhà", render: renderBqlThongKe },
    { id: "bql-tao-toa-nha", label: "Tạo tòa nhà", title: "Tạo tòa nhà mới", render: renderBqlTaoToaNha },
    { id: "bql-tao-nhan-vien", label: "Tạo nhân viên", title: "Tạo tài khoản quản lý tòa nhà", render: renderBqlTaoNhanVien },
    { id: "bql-nhan-vien", label: "Danh sách nhân viên", title: "Nhân viên quản lý tòa nhà", render: renderBqlNhanVien },
    { id: "bql-xuat-excel", label: "Xuất Excel công nợ", title: "Xuất báo cáo công nợ", render: renderBqlXuatExcel },
];

async function renderBqlTongQuan(el) {
  const tq = await api("/api/ban-quan-ly/tong-quan");
  el.innerHTML = `<p class="panel-desc" style="margin-bottom:16px">Số liệu tổng hợp của toàn bộ ký túc xá.</p>` + veTongQuan(tq);
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

