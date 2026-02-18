services:
  postgres:
    image: postgres:16-alpine
    container_name: hookah-postgres
    environment:
      POSTGRES_DB: hookah
      POSTGRES_USER: hookah
      POSTGRES_PASSWORD: hookah_dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hookah"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - hookah-network

volumes:
  postgres_data:
    driver: local

networks:
  hookah-network:
    driver: bridge