package main

import (
	"net/http"
)

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Write([]byte("Test"))
	})

	http.ListenAndServe(":8080", http.DefaultServeMux)
}
