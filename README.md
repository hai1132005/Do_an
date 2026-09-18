# QL_KTX — Hệ thống Quản lý Ký túc xá Sinh viên (W01)

Dự án Java Spring Boot + MySQL, chạy trong VS Code, đúng theo mô tả đề bài:
- Sơ đồ phòng/giường, tình trạng còn trống
- Sinh viên đăng ký trực tuyến; Ban quản lý/Quản lý tòa nhà duyệt và xếp phòng
- Ghi chỉ số điện nước, tự tính hóa đơn theo **bậc giá lũy tiến**, theo dõi công nợ
- Thống kê tỷ lệ lấp đầy, doanh thu, danh sách nợ; xuất Excel
- Phân quyền 3 vai trò: `SINH_VIEN`, `QUAN_LY_TOA_NHA`, `BAN_QUAN_LY`

## 1. Công nghệ sử dụng
| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Security, Validation) |
| CSDL | MySQL 8 |
| Build | Maven |
| Xác thực | JWT (JSON Web Token) |
| Xuất báo cáo | Apache POI (Excel) |

## 2. Chuẩn bị môi trường (VS Code)

1. Cài **JDK 17**: `java -version` để kiểm tra.
2. Cài **MySQL Server** (hoặc dùng XAMPP/Docker). Khởi động MySQL.
3. Trong VS Code, cài các extension:
   - **Extension Pack for Java** (Microsoft)
   - **Spring Boot Extension Pack** (VMware)
4. Mở thư mục `ql_ktx` bằng VS Code (`File > Open Folder`).

## 3. Cấu hình kết nối MySQL

Mở file `src/main/resources/application.properties`, sửa lại:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Không cần tạo database thủ công — ứng dụng có `createDatabaseIfNotExist=true` và
`spring.jpa.hibernate.ddl-auto=update` nên **Hibernate sẽ tự tạo database + toàn bộ bảng**
khi chạy lần đầu. File `database.sql` chỉ để bạn tham khảo cấu trúc.

## 4. Chạy dự án

Cách 1 — dùng Maven trong terminal VS Code:
```bash
./mvnw spring-boot:run
```
(Windows: `mvnw.cmd spring-boot:run`)

Cách 2 — bấm nút **Run** phía trên hàm `main()` trong file `QlKtxApplication.java`.

Ứng dụng chạy tại: `http://localhost:8080`

> Lưu ý: nếu chưa có file `mvnw`/`mvnw.cmd`, cài Maven rồi chạy `mvn spring-boot:run`,
> hoặc dùng lệnh Command Palette của VS Code: **Java: Generate Maven Wrapper**.

## 5. Tài khoản mặc định

Khi chạy lần đầu, hệ thống tự tạo tài khoản Ban quản lý:
- **Tên đăng nhập:** `admin`
- **Mật khẩu:** `admin123`

Dùng tài khoản này để đăng nhập, tạo tòa nhà, tạo tài khoản nhân viên quản lý tòa nhà.

## 6. Luồng nghiệp vụ & API chính

### Đăng nhập / Đăng ký (công khai)
- `POST /api/auth/dang-ky` — sinh viên tự đăng ký tài khoản
- `POST /api/auth/dang-nhap` — trả về JWT token, dùng token này trong header
  `Authorization: Bearer <token>` cho các API còn lại.

### Ban quản lý (`BAN_QUAN_LY`)
- `POST /api/ban-quan-ly/tao-toa-nha`
- `POST /api/ban-quan-ly/tao-nhan-vien` — tạo tài khoản Quản lý tòa nhà, gán phụ trách 1 tòa nhà
- `GET  /api/ban-quan-ly/thong-ke/{toaNhaId}` — tỷ lệ lấp đầy, doanh thu, công nợ
- `GET  /api/ban-quan-ly/xuat-excel-cong-no` — tải file Excel danh sách nợ
- `POST /api/ban-quan-ly/khoa-tai-khoan/{id}`

### Quản lý tòa nhà (`QUAN_LY_TOA_NHA`)
- `POST /api/quan-ly-toa-nha/tao-phong` — tạo phòng, tự sinh giường theo sức chứa
- `GET  /api/quan-ly-toa-nha/so-do-phong/{toaNhaId}`
- `GET  /api/quan-ly-toa-nha/dang-ky-cho-duyet/{toaNhaId}`
- `POST /api/quan-ly-toa-nha/xet-duyet/{dangKyId}` — body: `{"dongY": true, "ghiChu": "..."}`.
  Nếu đồng ý, hệ thống **tự động xếp giường trống đầu tiên + tạo hợp đồng**.
- `POST /api/quan-ly-toa-nha/ghi-chi-so` — ghi chỉ số điện/nước tháng,
  hệ thống **tự tính tiền theo bậc giá lũy tiến và tạo hóa đơn ngay lập tức**.
- `POST /api/quan-ly-toa-nha/xac-nhan-thanh-toan/{hoaDonId}`
- `GET  /api/quan-ly-toa-nha/cong-no`

### Sinh viên (`SINH_VIEN`)
- `GET  /api/sinh-vien/toa-nha`
- `GET  /api/sinh-vien/phong-trong/{toaNhaId}` — xem sơ đồ phòng còn trống
- `POST /api/sinh-vien/dang-ky-phong/{phongId}`
- `GET  /api/sinh-vien/lich-su-dang-ky`
- `GET  /api/sinh-vien/hop-dong`
- `GET  /api/sinh-vien/hoa-don`

## 7. Cấu trúc thư mục

```
ql_ktx/
├── pom.xml
├── database.sql                     (tham khảo cấu trúc DB)
├── src/main/java/com/ktx/ql_ktx/
│   ├── entity/          (các bảng: NguoiDung, ToaNha, Phong, Giuong, DangKyPhong,
│   │                      HopDong, ChiSoDienNuoc, BacGiaDien, HoaDon, ViPham...)
│   ├── repository/      (Spring Data JPA)
│   ├── service/         (nghiệp vụ: đăng ký, xét duyệt, tính điện nước, thống kê, xuất excel)
│   ├── controller/      (REST API theo 3 vai trò)
│   ├── security/        (JWT, phân quyền)
│   ├── config/          (SecurityConfig, DataSeeder, xử lý lỗi)
│   └── dto/             (request/response)
└── src/main/resources/application.properties
```

## 8. Kiểm thử nhanh bằng Postman/cURL

```bash
# 1. Đăng nhập admin
curl -X POST http://localhost:8080/api/auth/dang-nhap \
  -H "Content-Type: application/json" \
  -d '{"tenDangNhap":"admin","matKhau":"admin123"}'

# 2. Tạo tòa nhà (dùng token nhận được ở bước 1)
curl -X POST http://localhost:8080/api/ban-quan-ly/tao-toa-nha \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"tenToaNha":"Toa A","diaChi":"123 Nguyen Van Cu","soTang":5}'
```

## 9. Mở rộng thêm (điểm cộng)
- Thanh toán online qua VNPay sandbox: thêm `VnPayService` gọi API VNPay tại bước
  `HoaDonService.xacNhanThanhToan`.
- Thông báo Zalo OA/email tự động: thêm `NotificationService`, gọi sau khi
  `ChiSoService.taoHoaDon()` tạo hóa đơn mới.