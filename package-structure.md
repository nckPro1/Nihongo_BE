# Backend Package Structure Convention

## Muc tieu
- To chuc package ro rang theo **vai tro** va **luong nghiep vu**.
- De mo rong nhanh, de tim file, han che file "beo" va trung lap.

## Quy uoc tong the
- `controller` tach theo doi tuong:
  - `controller/user/<flow>/...`
  - `controller/admin/<flow>/...`
- `dto` tach theo huong du lieu + flow:
  - `dto/request/<flow>/...`
  - `dto/response/<flow>/...`
- `service` tach theo contract va implementation + flow:
  - `service/<flow>/...` (interface)
  - `service/impl/<flow>/...` (class implement)
- Cac flow se dat ten nhat quan: `auth`, `payment`, `translate`, `flashcard`, `quiz`, `blog`, ...

## Mau package cho Auth (Day 1)
- `org.example.nihongobackend.controller.user.auth`
- `org.example.nihongobackend.controller.admin.auth`
- `org.example.nihongobackend.dto.request.auth`
- `org.example.nihongobackend.dto.response.auth`
- `org.example.nihongobackend.service.auth`
- `org.example.nihongobackend.service.impl.auth`

## Mau mo rong cho flow khac
- Translate:
  - `controller/user/translate`
  - `dto/request/translate`
  - `dto/response/translate`
  - `service/translate`
  - `service/impl/translate`
- Payment:
  - `controller/user/payment`
  - `controller/public/payment` (neu can webhook public)
  - `dto/request/payment`
  - `dto/response/payment`
  - `service/payment`
  - `service/impl/payment`

## Nguyen tac dat ten class
- Controller:
  - `UserAuthController`, `AdminAuthController`
- DTO:
  - Request: `RegisterRequest`, `LoginRequest`
  - Response: `LoginResponse`, `UserProfileResponse`
- Service interface:
  - `AuthService`
- Service impl:
  - `AuthServiceImpl`
