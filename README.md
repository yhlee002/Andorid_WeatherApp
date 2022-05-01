날씨어때 <img src="https://user-images.githubusercontent.com/60289743/94329385-8a43f000-fff5-11ea-82b4-55358e946c3f.png" width="45" height="40">
=================
* 개발 언어 : Java 1.8
* 안드로이드 Gradle Plugin 버전 : 4.0.1
* Gradle 버전 : 6.1.1
* 모듈 컴파일 SDK 버전 : API 29(Android  10.0(Q))
* 모듈 빌드 툴 버전 : 30.0.1

* 사용된 Open API : 동네예보 조회 서비스(기상청), 대기오염정보 조회 서비스(한국환경공단), 측정소 정보 조회 서비스(한국환경공단), Geocoding API(Google), Kakao 좌표계 변환 API
* 사용된 외부 라이브러리 : Gson, Volley, Ted Permission
* 주요 기능
  + 사용자의 위치 정보를 받아 해당 동네의 날씨실황정보와 하루동안의 시간대별 예보 조회
  + 원하는 시간의 날씨 정보 알림 설정 기능
  + 다음날 시간대별 예보 조회
  + 7일간의 예보 조회
  + 대기오염정보(미세먼지, 초미세먼지 측정량 및 등급 확인 가능) 조회
