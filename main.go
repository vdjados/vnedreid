package main

import (
	"database/sql"
	"log"
	"net/http"

	_ "github.com/lib/pq"
)

func main() {
	db, err := sql.Open("postgres", "host=my-release-postgresql-primary user=user password=pass dbname=tinkoff_db port=5432 sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}

	log.Println(db.Ping())

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		log.Println("Test")
	})

	http.ListenAndServe(":8080", http.DefaultServeMux)
}
