# SakuKita Design System

## Identitas Visual & Arah Desain
SakuKita adalah aplikasi manajemen keuangan pribadi (*personal finance*) dengan nuansa modern, bersih, profesional, dan tepercaya. 

### Prinsip Utama
1. **Kejelasan Finansial**: Angka, status saldo, dan ringkasan mutasi kas harus terbaca cepat dan jelas.
2. **Konsistensi Lintas Perangkat**:
   - **Desktop (>= 992px)**: Menggunakan Sidebar Kapsul di sisi kiri, layout grid lapang, tombol aksi kontekstual.
   - **Mobile (< 992px)**: Sidebar desktop disembunyikan sepenuhnya. Navigasi beralih ke Bottom Navigation Bar mengambang dengan 5 menu utama (Masuk, Keluar, Beranda di tengah, Kategori, Laporan).
3. **Tanpa Dekorasi Berlebih**: Hindari gradasi mencolok yang tidak perlu, bayangan tebal, atau ornamen visual acak.

---

## Token Warna & Tema

```css
:root {
    --warna-utama: #0F3D32;
    --warna-gelap: #0B2820;
    --warna-latar-gelap: #071F19;
    --warna-aksen: #25A18E;
    --warna-mint: #34D399;
    --warna-wadah-hijau: #E6F4EF;
    --warna-sorot: #0A2B23;
    
    --warna-pemasukan: #0F766E;
    --warna-pemasukan-lembut: #E6F4EF;
    --warna-pengeluaran: #E11D48;
    --warna-pengeluaran-lembut: #FEECEE;
    --warna-peringatan: #D97706;
    
    --warna-latar: #F8FAF9;
    --warna-kartu: #FFFFFF;
    --warna-garis: #E6EFEA;
    --warna-garis-input: #D9E2DC;
    
    --warna-teks-judul: #0F172A;
    --warna-teks-bodi: #334155;
    --warna-teks-redup: #64748B;
}
```

---

## Tipografi
- **Font Utama**: `Plus Jakarta Sans` (400, 500, 600, 700)
- **Font Aksen Judul**: `Playfair Display`
- **Angka Finansial**: `tabular-nums` untuk perataan nominal pada tabel dan ringkasan kas

---

## Header Salam Dashboard (Desktop vs Mobile)
- **Desktop (>= 992px)**:
  - Berada di atas kartu grafik pada area salam (`.area-salam-dashboard`).
  - Judul sapaan: `"Halo, <Nama Pengguna>"`.
  - **Warna Nama Pengguna (`.nama-user-salam`)**: Hijau tua identitas (`--warna-utama` / `#0F3D32`), tebal (*fw-bold*), kontras tinggi terhadap latar belakang terang canvas.
  - Subteks: Deskripsi ringkas abu-abu redup (`--warna-teks-redup` / `#64748B`).
  - Tombol Logout di sisi kanan atas.

- **Mobile (< 992px)**:
  - Terintegrasi di bagian atas kartu saldo utama (`.card-saldo-utama`) yang berlatar gradasi hijau gelap pekat.
  - Sapaan: `"Selamat datang,"` kecil, diikuti `"Halo, <Nama Pengguna>"`.
  - **Warna Nama Pengguna (`.nama-user-salam`)**: Putih bersih (`#FFFFFF` / `!important`), terbaca tajam dan jelas di atas kartu saldo hijau gelap (*high contrast*).
  - Status Akun Aktif di sisi kanan.

---

## Urutan Komponen Dashboard Mobile
1. **Card Saldo Utama**: Total Saldo (dengan sapaan nama pengguna warna putih di bagian atasnya), pil pemasukan & pengeluaran, serta tombol pintasan cepat (Tambah Masuk, Tambah Keluar, Kategori, Template, Laporan).
2. **Card Grafik Arus Kas**: Grafik visual 6 bulan yang proporsional dan ringkas (tinggi 100px pada mobile).
3. **Tabel Riwayat Transaksi**: Daftar mutasi kas terbaru dengan informasi jelas dan rapi.
4. **Bottom Navigation Bar**: Fixed melayang di bagian bawah dengan 5 item menu, posisi Beranda di tengah.

---

## Kartu Ringkasan Laporan Keuangan (Desktop & Mobile)
- **Side-by-Side Grid**: Kartu Total Pemasukan dan Total Pengeluaran diposisikan berdampingan (`col-6`) baik di mobile maupun desktop (`row g-2 g-md-3`), menggantikan layout bertumpuk vertikal sebelumnya.
- **Tipografi Adaptif**:
  - Ukuran nominal menggunakan `clamp(1rem, 3.8vw, 1.45rem)` tabular nums agar pas dan tidak terpotong pada layar ponsel ramping.
  - Ikon tren pemasukan (hijau) dan pengeluaran (merah) berdimensi `28x28px` pada mobile.

---

## Tampilan Kategori Keuangan Mobile
- **List View Native (`.kategori-row`)**: Menggantikan tabel lebar pada layar ponsel (< 768px) dengan daftar kartu/item yang ramping tanpa scroll horizontal.
- **Aksi Cepat Langsung**: Tombol Edit (pensil) dan Hapus (tempat sampah) tersedia langsung di sisi kanan setiap item dengan touch target memadai (`32x32px`), tidak memerlukan menu titik tiga bertingkat.
- **Pencarian Real-Time**: Kolom pencarian otomatis memfilter data secara instan baik pada tabel desktop maupun daftar mobile.




