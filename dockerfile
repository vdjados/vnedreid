FROM golang:1.24.3-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./

RUN go mod download

COPY . .

RUN go build -o backendapp ./backend/main.go

RUN go build -o mlapp ./ml/main.go

FROM alpine AS backend

WORKDIR /app

COPY --from=builder /app/backendapp .

ENTRYPOINT ["./backendapp"]

FROM alpine AS ml

WORKDIR /app

COPY --from=builder /app/mlapp .

ENTRYPOINT ["./mlapp"]