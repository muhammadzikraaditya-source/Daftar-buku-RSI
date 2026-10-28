package id.ac.ub.filkom.contoh.service;

import id.ac.ub.filkom.contoh.entity.Buku;
import id.ac.ub.filkom.contoh.repository.BukuRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/** Service: tempat logika bisnis/aturan aplikasi. */
@Service
public class BukuService {

    private final BukuRepository bukuRepository;

    // Dependency injection lewat constructor
    public BukuService(BukuRepository bukuRepository) {
        this.bukuRepository = bukuRepository;
    }

    public List<Buku> cariBuku(String kata) {
        if (kata == null || kata.isBlank()) {
            return bukuRepository.findAll();
        }
        return bukuRepository.findByJudulContainingIgnoreCase(kata.trim());
    }

    public List<Buku> cariBuku(String kata, String status) {
        boolean tersedia = "tersedia".equals(status);
        boolean habis = "habis".equals(status);
        String kataBersih = kata == null ? "" : kata.trim();

        if (kataBersih.isBlank()) {
            if (tersedia) {
                return bukuRepository.findByStokGreaterThan(0);
            }
            if (habis) {
                return bukuRepository.findByStokEquals(0);
            }
            return bukuRepository.findAll();
        }
        if (tersedia) {
            return bukuRepository.findByJudulContainingIgnoreCaseAndStokGreaterThan(kataBersih, 0);
        }
        if (habis) {
            return bukuRepository.findByJudulContainingIgnoreCaseAndStokEquals(kataBersih, 0);
        }
        return bukuRepository.findByJudulContainingIgnoreCase(kataBersih);
    }

    public List<Buku> urutkanBuku(List<Buku> daftarBuku, String urutan) {
        Comparator<Buku> pembanding = "stok".equals(urutan)
                ? Comparator.comparingInt(Buku::getStok).reversed()
                    .thenComparing(Buku::getJudul, String.CASE_INSENSITIVE_ORDER)
                : Comparator.comparing(Buku::getJudul, String.CASE_INSENSITIVE_ORDER);
        return daftarBuku.stream().sorted(pembanding).toList();
    }

    public int jumlahJudul() {
        return (int) bukuRepository.count();
    }

    public int jumlahBukuTersedia() {
        return bukuRepository.findByStokGreaterThan(0).size();
    }

    public int jumlahBukuHabis() {
        return bukuRepository.findByStokEquals(0).size();
    }

    public int totalStok() {
        return bukuRepository.findAll().stream()
                .mapToInt(Buku::getStok)
                .sum();
    }

    public Buku tambahBuku(String idBuku, String judul, int stok) {
        if (stok < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif");
        }
        return bukuRepository.save(new Buku(idBuku, judul, stok));
    }

    public Buku pinjamBuku(String idBuku) {
        Buku buku = ambilBuku(idBuku);
        if (buku.getStok() == 0) {
            throw new IllegalStateException("Buku sedang habis dipinjam");
        }
        return bukuRepository.save(new Buku(buku.getIdBuku(), buku.getJudul(), buku.getStok() - 1));
    }

    public Buku kembalikanBuku(String idBuku) {
        Buku buku = ambilBuku(idBuku);
        return bukuRepository.save(new Buku(buku.getIdBuku(), buku.getJudul(), buku.getStok() + 1));
    }

    private Buku ambilBuku(String idBuku) {
        return bukuRepository.findById(idBuku)
                .orElseThrow(() -> new NoSuchElementException("Buku dengan ID " + idBuku + " tidak ditemukan"));
    }
}
