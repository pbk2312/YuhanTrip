# YuhanTrip

---

## 프로젝트 소개

### 숙소 예약 사이트

- 사용자는 평점과 후기를 참고하여 원하는 숙소를 쉽게 예약할 수 있습니다. 또한, 18시와 24시에 진행하는 쿠폰 이벤트를 통해 더 저렴하게 예약할 수 있습니다.
- 호스트로 등록된 사용자는 손쉽게 숙소를 등록하고, 예약 확인 및 취소 기능을 통해 숙소 예약을 효율적으로 관리할 수 있습니다.
- 공공데이터를 활용히여 숙소들 정보들을 보고 숙소 예약 및 결제 기능을 제공합니다.



> 포트원 결제 시스템을 연동하여 안전하고 편리한 결제 서비스를 제공합니다.

> Redis를 활용하여 RefreshToken과 이메일 인증번호를 캐시 형태로 저장함으로써 빠르고 효율적인 인증 및 세션 관리를 구현했습니다.


> MVC 패턴 활용을 프로젝트에서 Spring MVC 패턴을 활용하여 각 컴포넌트의 역할을 분리하였고 REST API를 통해 데이터 생성 및 수정 로직을 구현하였습니다.

> AWS EC2 등을 이용하여 서버를 배포해보았고 https 환경에서 사용할 수 있도록 구성하였습니다.






![image](https://github.com/user-attachments/assets/db9f487d-cf54-4662-beca-16666518ce8d)

---
### 주요 기능

#### 로그인 및 회원 가입 기능 

* JWT 토큰을 활용하여 AccessToken과 RefreshToken을 발급하여 쿠키에 저장하는 로직 구현 (쿠키에 저장할 때, HttpOnly, Secure, SameSite 설정을 통해 보안을 강화)
* JWT 토큰을 통해 인증뿐만 아니라, 사용자 역할(관리자, 일반 사용자,숙소 소유자)에 따른 권한 관리도 구현
* 소셜 로그인 연동(카카오 로그인)구현
* RefreshToken은 Redis를 활용하여 관리
* 이메일 인증 기능 구현(인증번호는 Redis에 저장)
* 비밀번호 찾기 및 탈퇴 기능

  
#### 결제 시스템(포트원 연동) 기능

* 예약하고 싶은 날짜,숙박 인원 수 등을 선택하고 결제 -> 결제 성공 시 예약 확정
* 예약 취소(환불 기능) 제공

#### 쿠폰 이벤트 

* 18시와 24시에 10분동안 쿠폰 발급 가능
* 유저들의 쿠폰 정보는 Redis에 저장

#### 지역별 숙소 검색 기능 및 필터링 기능

* 해당 날짜에 예약이 가득차거나 숙박 인원수가 초과되는 객실,숙소등을 필터링
* 평점,가격순 필터링 기능
* 검색 기능

#### 숙소 관리 및 호스트 승급 신청

* 호스트들은 예약 취소 및 숙소 등록 기능 제공
* 호스트로 승급하려면 문서 제출 및 승인 절차 진행

#### 후기 및 평점 기능

* 숙소 숙박 완료 시 후기 및 평점 작성 가능




---

### Data Set

- **출처**: 한국관광공사_국문 관광정보 서비스 API를 활용하여 전국의 숙소 데이터를 수집
- **제한 사항**: 수집한 데이터에는 숙소의 기본 정보만 포함되어 있으며, 객실 정보(가격, 편의 시설 등)에 대한 구체적인 데이터가 부족하여 해당 부분은 더미 데이터를 생성하여 사용

[한국관광공사_국문 관광정보 API](https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101578#/API%20%EB%AA%A9%EB%A1%9D/searchStay1)

---

### 개발 기한

2024.07 ~ 2024.09.20

---

### ERD

![image](https://github.com/user-attachments/assets/eadaf4a9-7709-4c48-b56a-0f16bd43e014)


---
### 사용한 기술 스택 및 라이브러리

<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=Spring%20Boot&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=Spring%20Security&logoColor=white"> <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=Thymeleaf&logoColor=white"> <img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white"> <img src="https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=MariaDB&logoColor=white"> <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=for-the-badge&logo=Amazon%20EC2&logoColor=white"> <img src="https://img.shields.io/badge/Redis-FF4438?style=for-the-badge&logo=Redis&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white">

* LOMBOK
* JWT
* Spring Data JPA
--- 
### 아키텍쳐 

![image](https://github.com/user-attachments/assets/ba1ac1d7-1ef2-4073-89fd-363315a1d17e)

----

### 시연 영상



#### 로그인 및 숙소 조회


카카오 로그인은 카카오 내부적으로 로그인을 한 기록이 있기 때문에 카카오 로그인 없이 미리 넘어갔습니다.


https://github.com/user-attachments/assets/ae8739b4-971d-4a06-8cc6-92698224f13f

#### 예약 하기 및 결제하기 




https://github.com/user-attachments/assets/95df5538-bf67-4809-a305-a8964ff57f6e
----




