# Daily Plan

## Day 1 - Authentication Flow

### Goal
- Hoan thien luong xac thuc nguoi dung cho backend (register, login, xac thuc JWT co ban).
- Chot duoc cac API auth de frontend co the tich hop som.

### Scope trong ngay
- Tao cau truc module auth (controller, service, dto, entity/repository can thiet).
- Trien khai API dang ky tai khoan (`POST /api/auth/register`).
- Trien khai API dang nhap (`POST /api/auth/login`).
- Tao va tra ve JWT token sau khi login thanh cong.
- Viet middleware/filter de doc JWT va gan user vao SecurityContext.
- Tao endpoint test xac thuc (`GET /api/auth/me`) de verify token.

### Kiem tra dau ra (Definition of Done)
- Dang ky thanh cong voi user moi, email trung thi bi chan.
- Dang nhap dung thong tin thi nhan duoc token hop le.
- Goi endpoint can auth voi token hop le thi thanh cong.
- Goi endpoint can auth khong co token hoac token sai thi bi tu choi.
- Co tai lieu ngan ve request/response mau cho cac API auth.

### Checklist thuc hien
- [ ] Chuan hoa model `users` phuc vu auth (email, password_hash, role, is_active).
- [ ] Tao DTO cho register/login + validate input.
- [ ] Tao AuthService xu ly register/login.
- [ ] Cau hinh Spring Security + JWT utility.
- [ ] Tao JWT filter + Security configuration.
- [ ] Tao AuthController (register/login/me).
- [ ] Test nhanh bang Postman/HTTP client.
- [ ] Cap nhat ghi chu API auth vao tai lieu du an.

### Non-goals (chua lam trong ngay 1)
- Chua tich hop Google OAuth.
- Chua trien khai rate limiting freemium.
- Chua trien khai payment PayOS.
- Chua trien khai upload PDF va AI translate.
