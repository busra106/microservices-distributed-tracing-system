# Spring Boot Microservices: Distributed Tracing + ELK + Zipkin (Docker Compose)

3 ayrı Spring Boot mikroservis içerir. **Dağıtık izleme** (Spring Cloud **Sleuth** + **Zipkin**) ve merkezi **loglama** (Logstash → Elasticsearch → Kibana) entegre edilmiştir.

## Servisler & Endpoint'ler

| Servis           | Port | Endpoint         | Açıklama                                               |
|------------------|------|------------------|--------------------------------------------------------|
| user-service     | 8081 | `GET /user`      | "User OK" döner                                        |
| user-service     | 8081 | `GET /user/fail` | RuntimeException fırlatır (hata trace senaryosu)       |
| order-service    | 8082 | `GET /order`     | user-service + payment-service'i çağırır, birleşik yanıt döner |
| order-service    | 8082 | `GET /order/fail`| payment/fail'i çağırır → hata trace zinciri oluşturur  |
| payment-service  | 8083 | `GET /payment`   | "Payment OK" döner                                     |
| payment-service  | 8083 | `GET /payment/fail` | RuntimeException fırlatır (hata trace senaryosu)    |

## Ön Gereksinimler

- Docker Desktop (Windows'ta çalışıyor olmalı, **Linux containers** modunda)
- `docker version` komutu terminalden çalışmalı

## Çalıştırma

Repo kökünden:

```bash
docker compose up --build
```

Tüm servisler sağlıklı olana kadar `depends_on` + `healthcheck` zincirleri sıralı başlatmayı garantiler.

Açılacak arayüzler:

- **Zipkin UI**: `http://localhost:9411`
- **Kibana**: `http://localhost:5601`

## Trace Akışı (Normal Senaryo)

```
GET http://localhost:8082/order
  └─ order-service   → traceId üretir (ör. abc123)
       ├─ user-service  /user    → aynı traceId ile devam eder
       └─ payment-service /payment → aynı traceId ile devam eder
```

Yanıt: `Order OK | User OK | Payment OK`

Zipkin'de 3 span'lı tek bir trace görünür; her span servis adı, süre ve log bilgisi içerir.

## Hata Trace Senaryosu

```
GET http://localhost:8082/order/fail
  └─ order-service
       └─ payment-service /payment/fail → RuntimeException (HTTP 500)
            order-service exception'ı yakalar, ERROR loglar ve tekrar fırlatır
```

Zipkin'de bu trace **kırmızı (error)** olarak işaretlenir. Hata hem payment-service span'ında hem order-service span'ında görünür. Aynı traceId ile Kibana'da ERROR logları filtrelenebilir.

Doğrudan hata üretmek için:

```bash
# Sadece user-service hatası (bağımsız span)
curl http://localhost:8081/user/fail

# Sadece payment-service hatası (bağımsız span)
curl http://localhost:8083/payment/fail

# Tam hata zinciri: order → payment (2 span birden hata)
curl http://localhost:8082/order/fail
```

## Loglama Nasıl Çalışır

- Her servis şunları yazar:
  - **Console**: insan okunabilir format (`traceId` / `spanId` dahil)
  - **Logstash TCP**: JSON formatında `logstash:5000` adresine (`logstash-logback-encoder`)
- Logstash → Elasticsearch'e `spring-logs-YYYY.MM.dd` index'inde gönderir
- Kibana'da `spring-logs-*` data view oluşturup şu alanlarla arama yapılabilir:
  - `service` — hangi mikroservis
  - `traceId` — Zipkin trace ID'siyle çapraz sorgulama
  - `level` — hata loglarını `ERROR` ile filtrelemek için

## Healthcheck Zinciri

Servisler belirli bir sırayla ayağa kalkar; her biri bağımlılığının sağlıklı olduğunu onayladıktan sonra başlar:

```
elasticsearch (sağlıklı)
  ├─ kibana
  └─ logstash (sağlıklı)
       ├─ zipkin (sağlıklı) ──┐
       ├─ user-service ───────┤
       ├─ payment-service ────┤
       └─ order-service ◄─────┘ (hepsini bekler)
```
