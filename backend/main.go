package main

import (
	"database/sql"
	"io"
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
		resp, err := http.Get("http://ml-service/")
		if err != nil {
			log.Println(err)
			return
		}

		buf, err := io.ReadAll(resp.Body)
		if err != nil {
			log.Println(err)
			return
		}

		log.Println(string(buf))
	})

	http.ListenAndServe(":8080", http.DefaultServeMux)
}
