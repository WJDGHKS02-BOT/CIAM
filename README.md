1) 로컬 실행 명령
   - 데이터베이스 터널(로컬) 세팅
   - mvn spring-boot:run -Dspring-boot.run.profiles=local

2) DB접속


java : cf ssh -L 1775:postgres-5e4a2791-0ac8-4552-8bda-11d73d1cacfe.cu2h3ll9ayju.ap-northeast-2.rds.amazonaws.com:1775 dev
DEV : cf8 ssh -L 1625:postgres-8027951d-9d5e-4e9a-9df5-f7c27e527c8b.cu2h3ll9ayju.ap-northeast-2.rds.amazonaws.com:1625 dev
QA : cf8 ssh -L 1644:postgres-aee62015-da2c-4973-96a3-e3ee16b930cb.cu2h3ll9ayju.ap-northeast-2.rds.amazonaws.com:1644 uatToolmate
PRD : cf ssh -L 1657:postgres-78fece32-e8e3-4a01-8555-56020a4d330b.cu2h3ll9ayju.ap-northeast-2.rds.amazonaws.com:1657 production


3) 운영배포
cf push -f manifest.yml
- Domain 변경이 안될때
cf map-route ciam bizaccounts.samsung.com