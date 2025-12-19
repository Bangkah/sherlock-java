# sherlock-java

Aplikasi ini digunakan untuk mencari keberadaan sebuah username di ratusan situs sosial media secara otomatis dan cepat.

## Fitur Utama
- Mencari username di ~1000 situs sosial media.
- Hasil pencarian otomatis dicatat ke file di folder `hasil/`.
- Menggunakan model async (CompletableFuture) untuk kecepatan maksimal.
- Hasil pencarian: ditemukan, tidak ditemukan, gagal akses.
- Output ringkas di terminal dan file.

## Struktur Folder
```
sherlock-java/
├── LICENSE
├── README.md
├── pom.xml
├── mvnw, mvnw.cmd
├── hasil/                # Folder hasil pencarian (output file)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/davistiba/
│   │   │       ├── App.java
│   │   │       └── Website.java
│   │   └── resources/
│   │       └── websites.json
│   └── test/
│       └── java/org/davistiba/
│           └── AppTest.java
└── target/
```

## Instalasi & Persiapan
### 1. Persyaratan
- Java JDK 17 atau lebih baru
- Maven (untuk build, install dengan `sudo apt install maven` di Ubuntu/Debian)

### 2. Build Project
Jalankan di terminal dari root folder project:
```bash
mvn clean package
```
Hasil build akan berupa file JAR di folder `target/`.

### 3. Menjalankan Aplikasi
Contoh menjalankan pencarian username:
```bash
java -jar target/sherlock-java.jar <username>
```
Contoh:
```bash
java -jar target/sherlock-java.jar Bangkah
```

### 4. Output
- Hasil pencarian akan tampil di terminal.
- File hasil pencarian otomatis dibuat di folder `hasil/` dengan nama `hasil_pencarian_<username>.txt`.

## Penjelasan Fitur
- **Async Search:** Pencarian dilakukan secara paralel menggunakan thread pool.
- **Status HTTP:** Penentuan hasil berdasarkan status HTTP (200/301/302 = ditemukan, 404 = tidak ditemukan, lainnya = gagal).
- **File Output:** Semua hasil pencarian dicatat ke file, memudahkan dokumentasi dan analisis.
- **Konfigurasi Situs:** Daftar situs diambil dari file `websites.json` (bisa diubah sesuai kebutuhan).

## Dependensi
- [Gson](https://github.com/google/gson) untuk parsing JSON
- [JUnit Jupiter](https://junit.org/junit5/) untuk testing
- [JetBrains Annotations](https://github.com/JetBrains/java-annotations) untuk anotasi kode

## Troubleshooting
- **Maven tidak ditemukan:** Install dengan `sudo apt install maven`.
- **Java version error:** Pastikan menggunakan JDK 17+ (`java -version`).
- **File hasil tidak muncul:** Pastikan folder `hasil/` ada dan aplikasi dijalankan dari root project.
- **websites.json tidak ditemukan:** Pastikan file ada di `src/main/resources/`.

## Contoh Penggunaan
1. Build project:
   ```
   mvn clean package
   ```
2. Jalankan pencarian:
   ```
   java -jar target/sherlock-java.jar davis
   ```
3. Cek hasil di terminal dan file:
   ```
   cat hasil/hasil_pencarian_davis.txt
   ```

