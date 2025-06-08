FROM golang:1.24.3-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./

RUN go mod download

COPY . .

RUN go build -o app main.go

FROM alpine

WORKDIR /app

COPY --from=builder /app/app .

ENTRYPOINT ["./app"]