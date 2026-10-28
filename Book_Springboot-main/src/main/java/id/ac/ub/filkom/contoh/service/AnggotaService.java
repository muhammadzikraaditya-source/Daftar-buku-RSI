package id.ac.ub.filkom.contoh.service;

import java.util.List;

import org.springframework.stereotype.Service;

import id.ac.ub.filkom.contoh.entity.Anggota;
import id.ac.ub.filkom.contoh.repository.AnggotaRepository;

/** Service: logika untuk fitur anggota. */
@Service
public class AnggotaService {

    private final AnggotaRepository anggotaRepository;

    public AnggotaService(AnggotaRepository anggotaRepository) {
        this.anggotaRepository = anggotaRepository;
    }

    public List<Anggota> lihatSemuaAnggota() {
        return anggotaRepository.findAll();
    }

    public Anggota tambahAnggota(String idAnggota, String nama, int batasPinjam) {
        String idBersih = idAnggota == null ? "" : idAnggota.trim();
        String namaBersih = nama == null ? "" : nama.trim();

        if (idBersih.isBlank() || namaBersih.isBlank()) {
            throw new IllegalArgumentException("ID dan nama anggota wajib diisi.");
        }
        if (batasPinjam < 0) {
            throw new IllegalArgumentException("Batas pinjam tidak boleh negatif.");
        }
        if (anggotaRepository.existsById(idBersih)) {
            throw new IllegalArgumentException("ID anggota " + idBersih + " sudah digunakan.");
        }

        return anggotaRepository.save(new Anggota(idBersih, namaBersih, batasPinjam));
    }
}