setup_postgres:
	helm install my-release oci://registry-1.docker.io/bitnamicharts/postgresql -f custom-values.yaml

conn_postgres:
	kubectl run my-release-postgresql-client \
	--rm \
	--tty \
	-i \
	--restart='Never' \
	--namespace default \
	--image docker.io/bitnami/postgresql:17.5.0-debian-12-r10 \
	--env="PGPASSWORD=pass" \
	--command -- psql --host my-release-postgresql-primary -U user -d tinkoff_db -p 5432

forward_postgres:
	kubectl port-forward svc/my-release-postgresql-primary 5432:5432
