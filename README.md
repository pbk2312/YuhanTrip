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
