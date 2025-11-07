# 🌊 Pado (파도) - 스터디 올인원 서비스

**Pado**는 스터디의 탐색부터 일정 관리, 자료 공유, 실시간 채팅, 그리고 AI 기반 퀴즈까지 스터디의 A to Z를 관리하는 올인원 플랫폼입니다.

단순한 스터디 관리를 넘어, **랭킹**과 **AI 퀴즈 생성**을 통해 멤버들의 학습 동기를 부여하고, **스마트 일정 조율**과 **진척도 관리** 기능으로 스터디 운영의 효율성을 극대화합니다.

## 💡 주요 기능 (Core Features)

"파도"는 스터디 운영에 필요한 모든 기능을 유기적으로 연동하여 제공합니다.

---

### 1. 스터디 관리 및 탐색

- **스터디 탐색 및 추천:** 키워드 검색, 관심사(Category), 지역(Region) 기반의 필터링을 제공합니다. 로그인 사용자의 경우, 관심사와 지역을 기반으로 한 **맞춤형 추천 점수** 순으로 스터디를 정렬합니다.
- **스터디 생성 및 관리:** 스터디 개설 및 정보 수정이 가능합니다.
- **멤버 관리 시스템:**
   - **신청/승인:** 스터디 참여 신청 및 리더의 승인/거절 기능을 제공합니다.
   - **권한 관리:** 악성 멤버 강제 탈퇴 및 리더 권한 위임이 가능합니다.


---

### 2. AI 기반 학습 및 랭킹

- **AI 퀴즈 자동 생성:** **Google Gemini AI (gemini-2.5-flash)** 와 연동하여, 사용자가 업로드한 학습 자료(PDF, TXT 등)의 본문을 분석해 객관식/주관식 퀴즈를 자동으로 생성합니다.
- **퀴즈 시스템:** 퀴즈 풀이, 답안 임시 저장, 자동 채점 및 결과 조회 기능을 제공합니다.
- **실시간 랭킹:** 퀴즈 점수를 `rank_point`로 환산하여 스터디 내 멤버들의 랭킹을 실시간으로 집계합니다.
  
---

### 3. 일정 조율 및 출석

- **스마트 일정 조율:** 리더가 조율 기간(날짜)과 가용 시간(시/분)을 설정하여 조율을 생성하면, 30분 단위의 `ScheduleTuneSlot`이 자동으로 생성됩니다.
   - **Bitmask 기반 참여:** 멤버들은 생성된 슬롯에 대해 참여 가능 여부를 투표하며, 각 멤버의 응답은 **Bitmask**로 계산되어 `ScheduleTuneSlot`의 `occupancyBits`에 저장됩니다.
   - **일정 확정:** 리더는 멤버들의 응답 비트(OR 연산)를 참고하여 최종 일정을 확정합니다.
- **출석 관리:** 확정된 일정을 기반으로 리더가 멤버들의 출석 상태(참석/결석)를 관리합니다.

---

### 4. 실시간 소통 및 알림

- **실시간 채팅:** **WebSocket (STOMP)** 기반의 스터디 전용 실시간 채팅방을 제공합니다.
- **채팅 편의 기능:**
    - **메시지 삭제** 및 **이모지 리액션** (좋아요/싫어요) 기능을 지원합니다.
    - 채팅방 접속 여부를 **Redis**로 관리하여 카카오톡처럼 **안읽은 메시지 수**를 실시간으로 제공합니다.
- **통합 알림:** 새 공지사항, 새 일정, 새 퀴즈 생성 시, 채팅방에 **시스템 알림 메시지**를 자동으로 전송합니다.

---

### 5. 학습 관리 및 진척도
- **자료 아카이빙:** **AWS S3**와 Presigned-URL 을 연동하여 학습 자료(공지/학습자료/과제)를 안전하게 업로드하고 관리합니다.
- **스터디 로드맵:** 리더가 스터디의 커리큘럼(Chapter)을 생성하고 완료 상태를 관리합니다.
- **대시보드:** 최근 공지, 다가오는 일정, 최근 퀴즈를 요약 제공합니다.
- **진척도 현황판:** 멤버별 **출석 횟수** , **퀴즈 풀이 횟수**, **회고 작성 횟수**를 집계하여 스터디 현황판을 제공합니다.
- **스터디 회고:** 멤버들은 지난 일정을 선택하여 스터디 회고(Satisfaction, Understanding, Participation 점수 등)를 작성할 수 있습니다.

## ⚙️ 기술 스택 (Tech Stack)

| **구분**          | **기술**                     | **설명**                                                   |
| ----------------- | ------------------------------ | ---------------------------------------------------------- |
| **Backend**       | `Java 21`, `Spring Boot 3.5.5` | 메인 애플리케이션 프레임워크                               |
| **Database**      | `MySQL`, `H2`                  | 프로덕션 RDBMS 및 테스트/개발용 인메모리 DB                |
| **Data Access**   | `Spring Data JPA`, `QueryDSL`  | 데이터베이스 접근 및 동적 쿼리                             |
| **Cache / Token** | `Redis`                        | Refresh Token 저장, Email 인증 코드, 채팅방 접속 상태 관리 |
| **Auth**          | `Spring Security`, `JWT`       | JWT (Access/Refresh) 기반 인증/인가                        |
| **Real-time**     | `WebSocket (STOMP)`            | 실시간 채팅 및 알림                                        |
| **Storage**       | `AWS S3`                       | Presigned-URL을 이용한 파일 스토리지                       |
| **AI**            | `Google Gemini API`            | 학습 자료 기반 AI 퀴즈 생성                                |
| **File Parse**    | `Apache Tika`                  | AI 퀴즈 생성을 위한 PDF/TXT 파일 텍스트 추출               |
| **CI/CD**         | `GitHub Actions`, `Docker`     | 테스트 및 EC2/Docker 기반 자동 배포                        |


## 🐳 CI/CD 및 아키텍처

본 프로젝트는 `GitHub Actions`를 통해 CI/CD 파이프라인을 구축했습니다.

1. **PR (Pull Request):**
- `test`: 유닛 테스트 및 통합 테스트 실행.
- `pr-smoke (local)`: `Dockerfile` 및 `docker-compose.dev.yml`을 사용하여 로컬 환경에서 스모크 테스트 실행.
- `pr-ec2-smoke (prod)`: PR 브랜치를 실제 EC2에 `prod` 프로파일로 임시 포트(18080)에 배포하여 최종 스모크 테스트 실행.

2. **Merge (to `develop`):**
- `build-and-deploy`: `test` 통과 시, Docker 이미지를 빌드하여 Docker Hub에 Push합니다.
- **Blue/Green 배포:** EC2에 접속하여 `Nginx`를 통해 **무중단 배포**를 실행합니다.
   - Nginx는 `8081`과 `8082` 포트를 upstream으로 사용합니다.
   - 현재 `8081` 포트가 사용 중이면 새 버전은 `8082` 포트에 배포됩니다.
   - 새 버전의 Health Check가 성공하면 Nginx 설정(`proxy_pass`)을 `8082`로 변경하고, 기존 `8081` 컨테이너를 종료합니다.

## 🚀 시작하기 (Getting Started)

### 1. 로컬 개발 환경 (H2 + Redis)

로컬 개발 환경에서는 `application-dev.properties`가 활성화됩니다. H2 인메모리 DB를 사용하며, 로컬 Redis(6379) 연결이 필요합니다.
```
# (로컬에 Redis가 설치되어 있지 않은 경우)
docker run -d -p 6379:6379 --name pado-redis redis:7-alpine

# Spring Boot 애플리케이션 실행
./gradlew bootRun
```
### 2. Docker Compose (개발용)

`docker-compose.dev.yml` 파일을 통해 Spring Boot 앱과 Redis를 동시에 실행할 수 있습니다.
```
# Docker 이미지 빌드 및 컨테이너 실행 (dev 프로파일)
docker-compose -f docker-compose.dev.yml up --build
```

## 📚 API 문서

API 명세는 Swagger를 통해 제공됩니다.
아래 주소로 접속하세요. <br/>
https://gogumalatte.site/swagger-ui/index.html
