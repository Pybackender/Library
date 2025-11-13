# استفاده از تصویر پایه OpenJDK
FROM eclipse-temurin:21-jre

# تنظیم دایرکتوری کاری
WORKDIR /app

# کپی کردن فایل‌های پروژه به تصویر Docker
COPY target/bookmarket-0.0.1-SNAPSHOT.jar app.jar

# تعریف متغیرهای محیطی
ENV DB_URL=jdbc:postgresql://db:5432/book_db
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=hasan82.13

# اجرای برنامه
ENTRYPOINT ["java", "-jar", "app.jar"]