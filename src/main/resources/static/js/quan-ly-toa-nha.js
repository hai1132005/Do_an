// =====================================================================
// KTX Portal — chức năng QUẢN LÝ TÒA NHÀ. Dùng cho trang quan-ly-toa-nha.html
// và cũng được nạp ở ban-quan-ly.html (mục "Vận hành tòa nhà" của Ban quản lý).
// Cần common.js nạp trước.
// =====================================================================

const NAV_QL = [
    { id: "ql-tong-quan", label: "Tổng quan", title: "Tổng quan tòa nhà", render: renderQlTongQuan },
    { id: "ql-cho-duyet", label: "Duyệt đăng ký", title: "Đơn đăng ký chờ duyệt", render: renderQlChoDuyet },
    { id: "ql-so-do", label: "Sơ đồ phòng", title: "Sơ đồ phòng / giường", render: renderQlSoDoPhong },
    { id: "ql-tao-phong", label: "Tạo phòng", title: "Tạo phòng mới", render: renderQlTaoPhong },
    { id: "ql-cu-dan", label: "Sinh viên đang ở", title: "Danh sách sinh viên đang ở", render: renderQlCuDan },
    { id: "ql-chi-so", label: "Ghi chỉ số điện nước", title: "Ghi chỉ số điện nước", render: renderQlChiSo },
    { id: "ql-hoa-don", label: "Hóa đơn", title: "Hóa đơn theo tòa nhà", render: renderQlHoaDon },
    { id: "ql-cong-no", label: "Công nợ", title: "Danh sách công nợ", render: renderQlCongNo },
    { id: "ql-vi-pham", label: "Vi phạm nội quy", title: "Quản lý vi phạm nội quy", render: renderQlViPham },
];

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
          <label>Chỉ số điện mới (kWh)<input type="number" id="cs-dien" min="0" step="any" required></label>
          <label>Chỉ số nước mới (m3)<input type="number" id="cs-nuoc" min="0" step="any" required></label>
        </div>
        <p class="panel-desc" id="cs-goi-y"></p>
        <button class="btn btn-primary" type="submit">Ghi chỉ số &amp; tạo hóa đơn</button>
      </form>`;

    const capNhatGoiY = async () => {
      const goiY = document.getElementById("cs-goi-y");
      try {
        const c = await api(`/api/quan-ly-toa-nha/chi-so-cu/${document.getElementById("cs-phong").value}`);
        document.getElementById("cs-dien").min = c.dienCu;
        document.getElementById("cs-nuoc").min = c.nuocCu;
        goiY.innerHTML = `Nhập <strong>số hiện đang hiển thị trên đồng hồ</strong> (chỉ số tích lũy), không phải số tiêu thụ trong tháng. `
          + `Chỉ số cũ${c.kyCuoi ? " (kỳ " + c.kyCuoi + ")" : ""}: điện <strong>${c.dienCu}</strong> kWh, nước <strong>${c.nuocCu}</strong> m³. `
          + `Số tiêu thụ = chỉ số mới − chỉ số cũ.`;
      } catch (err) { goiY.textContent = ""; }
    };
    document.getElementById("cs-phong").addEventListener("change", capNhatGoiY);
    capNhatGoiY();

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
        capNhatGoiY();
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
            <button class="btn btn-outline" data-chuyenphong="${s.sinhVienId}" data-ten="${s.hoTen}">Chuyển phòng</button>
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

    box.querySelectorAll("[data-chuyenphong]").forEach(btn => btn.addEventListener("click", async () => {
      const giuongMoiId = prompt(`Nhập ID giường đích để chuyển ${btn.dataset.ten} sang (xem ở Sơ đồ phòng):`);
      if (!giuongMoiId) return;
      try {
        await api(`/api/quan-ly-toa-nha/chuyen-phong/${btn.dataset.chuyenphong}`, "POST",
          { giuongMoiId: Number(giuongMoiId) });
        toast("Đã chuyển phòng thành công. Hợp đồng cũ được giữ lại làm lịch sử.");
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
    const [list, caNhan] = await Promise.all([
      api(`/api/quan-ly-toa-nha/hoa-don/toa-nha/${toaNhaId}`),
      api(`/api/quan-ly-toa-nha/hoa-don-ca-nhan/toa-nha/${toaNhaId}`).catch(() => []),
    ]);
    const theoHoaDon = {};
    caNhan.forEach(c => (theoHoaDon[c.hoaDonPhongId] ||= []).push(c));
    const box = document.getElementById("hoa-don-list");
    if (!list.length) { box.innerHTML = `<p class="empty-state">Chưa có hóa đơn nào. Hãy ghi chỉ số điện nước để tạo hóa đơn.</p>`; return; }

    list.sort((a, b) => (b.nam - a.nam) || (b.thang - a.thang));
    box.innerHTML = `<table class="data-table"><thead><tr>
        <th>Phòng</th><th>Tháng</th><th>Tiền phòng</th><th>Điện</th><th>Nước</th><th>Tổng</th><th>Thanh toán theo sinh viên</th><th>Trạng thái</th><th></th>
      </tr></thead><tbody>` +
      list.map(h => `<tr>
          <td>P.${h.phong.soPhong}</td><td>${h.thang}/${h.nam}</td>
          <td class="num">${tienVND(h.tienPhong)}</td>
          <td class="num">${tienVND(h.tienDien)}</td>
          <td class="num">${tienVND(h.tienNuoc)}</td>
          <td class="num"><strong>${tienVND(h.tongTien)}</strong></td>
          <td>${(theoHoaDon[h.id] || []).map(c => `<div>${c.hoTen}: ${tienVND(c.tongTien)} — ${c.trangThai === "DA_THANH_TOAN"
              ? `<span class="tag tag-duyet">Đã thanh toán (${c.hinhThuc === "VNPay" ? "VNPay" : "tiền mặt"})</span>`
              : `<span class="tag tag-cho">Chưa thanh toán</span>`}</div>`).join("") || "—"}</td>
          <td>${tagHoaDon(h.trangThai)}</td>
          <td style="display:flex; gap:6px;">
            <button class="btn btn-outline" data-pdf="${h.id}" data-phong="${h.phong.soPhong}" data-ky="${h.thang}_${h.nam}">PDF</button>
            ${h.trangThai !== "DA_THANH_TOAN"
              ? `<button class="btn btn-primary" data-tt="${h.id}">Đã thu tiền</button>` : ""}
            <button class="btn btn-outline" data-xoa="${h.id}" data-ky="${h.thang}/${h.nam}" data-phong="${h.phong.soPhong}">Xóa</button>
          </td>
        </tr>`).join("") + `</tbody></table>`;

    box.querySelectorAll("[data-pdf]").forEach(btn => btn.addEventListener("click",
      () => taiFile(`/api/quan-ly-toa-nha/hoa-don/${btn.dataset.pdf}/pdf`,
                    `hoa_don_P${btn.dataset.phong}_${btn.dataset.ky}.pdf`)));

    box.querySelectorAll("[data-xoa]").forEach(btn => btn.addEventListener("click", async () => {
      const daTra = (theoHoaDon[btn.dataset.xoa] || []).filter(c => c.trangThai === "DA_THANH_TOAN").length;
      const canhBao = daTra ? `\nLưu ý: đã có ${daTra} sinh viên thanh toán, dữ liệu thanh toán đó cũng sẽ bị xóa.` : "";
      if (!confirm(`Xóa hóa đơn P.${btn.dataset.phong} tháng ${btn.dataset.ky}?\nHóa đơn từng sinh viên và chỉ số điện nước của kỳ này cũng bị xóa để có thể ghi lại.${canhBao}`)) return;
      try { await api(`/api/quan-ly-toa-nha/hoa-don/${btn.dataset.xoa}`, "DELETE"); toast("Đã xóa hóa đơn."); load(); }
      catch (err) { toast(err.message, true); }
    }));

    box.querySelectorAll("[data-tt]").forEach(btn => btn.addEventListener("click", async () => {
      const chuaTra = (theoHoaDon[btn.dataset.tt] || []).filter(c => c.trangThai !== "DA_THANH_TOAN");
      if (chuaTra.length && !confirm(`Còn ${chuaTra.length} sinh viên chưa thanh toán: ${chuaTra.map(c => c.hoTen).join(", ")}.\nBấm OK để ghi nhận đã thu tiền mặt phần của họ và đóng hóa đơn?`)) return;
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