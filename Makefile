up:
	docker-compose up

down:
	docker-compose down

downv:
	docker-compose down -v

conn:
	docker exec -it vnedreid-postgres-1 psql -U user -d tinkoff_db