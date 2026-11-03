// =====================================================================
// Trang ĐĂNG NHẬP / ĐĂNG KÝ (index.html). Đăng nhập xong sẽ chuyển sang
// đúng trang theo vai trò: sinh-vien.html / quan-ly-toa-nha.html / ban-quan-ly.html
// (cần common.js nạp trước).
// =====================================================================

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

async function chuyenDenTrangTheoVaiTro() {
  const info = await api("/api/nguoi-dung/toi");
  location.replace(TRANG_THEO_VAI_TRO[info.vaiTro] || "/");
}

document.getElementById("form-login").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const data = await api("/api/auth/dang-nhap", "POST", {
      tenDangNhap: document.getElementById("login-username").value.trim(),
      matKhau: document.getElementById("login-password").value,
    });
    luuToken(data.token);
    await chuyenDenTrangTheoVaiTro();
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

// Tab nay da dang nhap san (cung tab) -> vao thang trang cua minh
(async function init() {
  if (!state.token) return;
  try { await chuyenDenTrangTheoVaiTro(); }
  catch (e) { try { sessionStorage.removeItem("ktx_token"); } catch (x) { /* bo qua */ } state.token = null; }
})();
