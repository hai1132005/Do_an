# Trien khai he thong QL_KTX bang Docker Compose + Nginx (SSL)

Giai quyet yeu cau cua GVHD (diem 4): "them buoc trien khai" cho he
thong, ngoai pham vi code nghiep vu.

## Kien truc trien khai

```
Trinh duyet --HTTPS--> Nginx (443) --HTTP noi bo--> Spring Boot app (8081) --JDBC--> MySQL 8
```

- **Nginx**: nhan HTTPS tu ben ngoai, giai ma SSL (SSL termination), roi
  chuyen tiep (reverse proxy) request sang container `app` qua mang noi
  bo cua Docker Compose.
- **app**: build tu `Dockerfile` (multi-stage: build bang Maven, chay
  bang JRE 17 Alpine cho image nho gon), chay voi profile `docker`
  (`application-docker.properties`).
- **mysql**: MySQL 8 chinh thuc, du lieu luu ben ngoai container qua
  Docker volume `ktx_mysql_data` (khong mat du lieu khi container bi xoa
  va tao lai).

## Cac buoc trien khai (moi truong demo/local)

1. Tao chung chi SSL tu ky (hoac thay bang chung chi that neu co domain
   that + Let's Encrypt):
   ```bash
   mkdir -p deploy/ssl
   openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
     -keyout deploy/ssl/privkey.pem -out deploy/ssl/fullchain.pem \
     -subj "/CN=ktx.local"
   ```

2. (Tuy chon) tao file `.env` o thu muc goc de ghi de bien moi truong:
   ```
   MYSQL_ROOT_PASSWORD=mat_khau_manh_hon
   SPRING_MAIL_USERNAME=ten_tai_khoan_gmail@gmail.com
   SPRING_MAIL_PASSWORD=app_password_cua_gmail
   KTX_MAIL_BAT_KICH_HOAT=true
   VNPAY_TMN_CODE=... (lay tu tai khoan sandbox VNPay)
   VNPAY_HASH_SECRET=...
   VNPAY_RETURN_URL=https://<domain-cua-ban>/api/thanh-toan/vnpay-callback
   ```

3. Build va chay toan bo:
   ```bash
   docker compose up -d --build
   ```

4. Kiem tra: `https://localhost` (hoac domain da cau hinh trong
   `deploy/nginx.conf`). Trinh duyet se canh bao chung chi tu ky vi khong
   do CA cong nhan phat hanh - day la binh thuong o moi truong demo.

5. Xem log / dung he thong:
   ```bash
   docker compose logs -f app
   docker compose down          # dung, giu lai du lieu MySQL
   docker compose down -v       # dung VA xoa luon du lieu MySQL
   ```

## Ghi chu bao mat

- Mat khau MySQL that va thong tin SMTP/VNPay KHONG duoc hardcode trong
  `docker-compose.yml` hay `application-docker.properties` - deu doc tu
  bien moi truong (`${...}`), gia tri mac dinh chi phuc vu demo.
- `ktx.mail.bat-kich-hoat=false` theo mac dinh de he thong khong bi loi
  khi chua co SMTP that cau hinh (email chi duoc ghi ra log).
