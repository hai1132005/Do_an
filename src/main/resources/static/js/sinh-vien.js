// =====================================================================
// KTX Portal — trang SINH VIÊN (sinh-vien.html). Cần common.js nạp trước.
// =====================================================================

const NAV_SV = [
    { id: "sv-tong-quan", label: "Tổng quan", title: "Tổng quan của tôi", render: renderSvTongQuan },
    { id: "sv-phong-trong", label: "Đăng ký phòng", title: "Phòng còn trống", render: renderSvPhongTrong },
    { id: "sv-dang-ky", label: "Đơn đăng ký", title: "Đơn đăng ký của tôi", render: renderSvDangKy },
    { id: "sv-hop-dong", label: "Hợp đồng", title: "Hợp đồng của tôi", render: renderSvHopDong },
    { id: "sv-hoa-don", label: "Hóa đơn", title: "Hóa đơn phòng tôi", render: renderSvHoaDon },
    { id: "sv-hoa-don-ca-nhan", label: "Hóa đơn & Công nợ cá nhân", title: "Hóa đơn cá nhân của tôi", render: renderSvHoaDonCaNhan },
    { id: "sv-vi-pham", label: "Vi phạm nội quy", title: "Vi phạm nội quy của tôi", render: renderSvViPham },
];

async function renderSvTongQuan(el) {
  const [hopDongs, viPhams] = await Promise.all([
    api("/api/sinh-vien/hop-dong"),
    api("/api/sinh-vien/vi-pham"),
  ]);
  const hdHienTai = hopDongs.find(h => h.conHieuLuc);

  let hoaDonHtml = "";
  if (hdHienTai) {
    // Dung hoa don CA NHAN (phan tien rieng cua sinh vien nay), khong phai hoa don chung ca phong,
    // de so lieu giam dung khi sinh vien thanh toan qua VNPay.
    const hoaDons = await api("/api/sinh-vien/hoa-don-ca-nhan").catch(() => []);
    const chuaTT = hoaDons.filter(h => h.trangThai !== "DA_THANH_TOAN");
    const tongNo = chuaTT.reduce((s, h) => s + Number(h.tongTien), 0);
    hoaDonHtml = `<div class="stat-grid">
        ${statBox("Hóa đơn chưa thanh toán", chuaTT.length, chuaTT.length > 0)}
        ${statBox("Số tiền còn nợ", tienVND(tongNo), tongNo > 0)}
        ${statBox("Vi phạm nội quy", viPhams.length, viPhams.length > 0)}
      </div>`;
  }

  // Dung optional chaining (?.) de khong lam vo trang neu du lieu long nhau bi thieu
  const tenToaNha = hdHienTai?.tenToaNha;
  const soPhong = hdHienTai?.soPhong;
  const kyHieu = hdHienTai?.kyHieuGiuong;

  el.innerHTML = `
    <div class="panel">
      <h2>Chỗ ở hiện tại</h2>
      ${hdHienTai
        ? (tenToaNha
            ? `<p class="panel-desc">Bạn đang ở <strong>${tenToaNha} — Phòng ${soPhong}</strong>,
               giường <strong>${kyHieu}</strong>, từ ngày ${ngayHT(hdHienTai.ngayBatDau)}.</p>`
            : `<p class="panel-desc">Bạn đang có hợp đồng ở hiệu lực nhưng hệ thống thiếu dữ liệu chi tiết phòng.
               Vui lòng liên hệ ban quản lý để kiểm tra lại.</p>`)
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

function ngayHT(iso) {
  if (!iso) return "—";
  const [y, m, d] = String(iso).split("-");
  return d ? `${d}/${m}/${y}` : iso;
}

async function renderSvHopDong(el) {
  const list = await api("/api/sinh-vien/hop-dong");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Bạn chưa có hợp đồng ở nào.</p>`; return; }
  const lyDo = { TRA_PHONG: "Trả phòng", CHUYEN_PHONG: "Chuyển phòng" };
  el.innerHTML = `<div class="panel"><table class="data-table"><thead><tr>
      <th>Phòng</th><th>Giường</th><th>Ngày bắt đầu</th><th>Ngày kết thúc</th><th>Trạng thái</th>
    </tr></thead><tbody>` +
    list.map(h => `<tr>
        <td>${h.tenToaNha ?? "—"} — P.${h.soPhong ?? "—"}</td>
        <td>${h.kyHieuGiuong ?? "—"}</td>
        <td>${ngayHT(h.ngayBatDau)}</td>
        <td>${h.conHieuLuc ? (h.ngayKetThuc ? ngayHT(h.ngayKetThuc) + " (dự kiến)" : "—") : ngayHT(h.ngayTraThucTe)}</td>
        <td>${h.conHieuLuc ? '<span class="tag tag-duyet">Đang hiệu lực</span>' : `<span class="tag tag-day">Đã kết thúc${h.lyDoKetThuc ? " — " + (lyDo[h.lyDoKetThuc] || h.lyDoKetThuc) : ""}</span>`}</td>
      </tr>`).join("") + `</tbody></table></div>`;
}

async function renderSvHoaDon(el) {
  const list = await api("/api/sinh-vien/hoa-don");
  if (!list.length) { el.innerHTML = `<p class="empty-state">Chưa có hóa đơn nào.</p>`; return; }
  el.innerHTML = `<div class="panel"><p class="panel-desc">Đây là hóa đơn CHUNG của cả phòng. Phần tiền riêng của bạn (theo số ngày ở) và việc thanh toán nằm ở mục <strong>Hóa đơn &amp; Công nợ cá nhân</strong>; hóa đơn chung chỉ chuyển "Đã thanh toán" khi mọi người trong phòng đã trả đủ phần của mình.</p><table class="data-table"><thead><tr>
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

/**
 * BO SUNG MOI (diem 3 nhan xet GVHD): hoa don CA NHAN cua rieng sinh vien
 * (chia theo so ngay thuc te da o, khac voi "Hóa đơn" chung ca phong o
 * tab truoc), kem tong cong no va nut thanh toan qua VNPay sandbox.
 */
async function renderSvHoaDonCaNhan(el) {
  const [list, congNo] = await Promise.all([
    api("/api/sinh-vien/hoa-don-ca-nhan"),
    api("/api/sinh-vien/cong-no"),
  ]);

  el.innerHTML = `<div class="panel">
      <h2>Hóa đơn &amp; công nợ cá nhân</h2>
      <p class="panel-desc">Phần tiền phòng/điện/nước được chia riêng theo đúng số ngày bạn thực sự ở trong tháng (không chia đều theo đầu người).</p>
      <div class="stat-box" style="max-width:280px;margin-bottom:16px">
        <div class="stat-label">Tổng công nợ chưa thanh toán</div>
        <div class="stat-value">${tienVND(congNo.tongCongNo)}</div>
      </div>
      ${!list.length ? `<p class="empty-state">Chưa có hóa đơn cá nhân nào.</p>` : `
      <table class="data-table"><thead><tr>
          <th>Tháng</th><th>Số ngày ở</th><th>Tiền phòng</th><th>Tiền điện</th><th>Tiền nước</th><th>Tổng</th><th>Hạn TT</th><th>Trạng thái</th><th></th>
        </tr></thead><tbody>` +
        list.map(h => `<tr>
            <td>${h.hoaDonPhong ? h.hoaDonPhong.thang + "/" + h.hoaDonPhong.nam : "—"}</td>
            <td class="num">${h.soNgayO}</td>
            <td class="num">${tienVND(h.tienPhong)}</td>
            <td class="num">${tienVND(h.tienDien)}</td>
            <td class="num">${tienVND(h.tienNuoc)}</td>
            <td class="num"><strong>${tienVND(h.tongTien)}</strong></td>
            <td>${h.hanThanhToan}</td>
            <td>${tagHoaDon(h.trangThai)}</td>
            <td>${h.trangThai === "DA_THANH_TOAN"
                ? "—"
                : `<button class="btn btn-primary" data-pay="${h.id}">Thanh toán VNPay</button>`}</td>
          </tr>`).join("") + `</tbody></table>`}
    </div>`;

  el.querySelectorAll("[data-pay]").forEach(btn => btn.addEventListener("click", async () => {
    try {
      const res = await api(`/api/sinh-vien/hoa-don-ca-nhan/${btn.dataset.pay}/vnpay-url`);
      window.location.href = res.url;
    } catch (err) { toast(err.message, true); }
  }));
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