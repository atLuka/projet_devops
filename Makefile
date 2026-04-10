.PHONY: certs build up down restart logs clean reset-db

certs:
	@if [ ! -d certs ] || [ -z "$$(ls -A certs 2>/dev/null)" ]; then \
		chmod +x generate-certs.sh && ./generate-certs.sh; \
	else \
		echo "Certs already exist, skipping (make clean to regenerate)"; \
	fi

build:
	docker-compose build

up: certs
	docker-compose up --build -d

down:
	docker-compose down

restart: down up

logs:
	docker-compose logs -f

log:
	docker-compose logs -f $(S)

clean:
	docker-compose down -v
	rm -rf certs/

reset-db:
	docker-compose down
	docker volume rm -f z_user-db-data z_property-db-data z_messaging-db-data z_minio-data
	docker-compose up --build -d
	@echo "BDD videes, fixtures rechargees automatiquement au demarrage."

ps:
	docker-compose ps
